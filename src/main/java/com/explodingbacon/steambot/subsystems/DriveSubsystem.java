package com.explodingbacon.steambot.subsystems;

import com.explodingbacon.bcnlib.actuators.FakeMotor;
import com.explodingbacon.bcnlib.actuators.Motor;
import com.explodingbacon.bcnlib.actuators.MotorGroup;
import com.explodingbacon.bcnlib.framework.PIDController;
import com.explodingbacon.bcnlib.sensors.ADXSensor;
import com.explodingbacon.bcnlib.sensors.AbstractEncoder;
import com.explodingbacon.bcnlib.sensors.Encoder;
import com.explodingbacon.bcnlib.utils.Utils;
import com.explodingbacon.steambot.Map;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.VictorSP;

public class DriveSubsystem {

    private MotorGroup leftMotors, rightMotors, strafeMotors;
    private Motor frontLeft, backLeft, frontRight, backRight;
    private Encoder frontLeftEncoder, backLeftEncoder, frontRightEncoder, backRightEncoder, strafeEncoder;

    private PIDController frontLeftPID, backLeftPID, frontRightPID, backRightPID, strafePID, rotatePID;
    private ADXSensor adx;
    private FakeMotor rotatePidOutput;

    private Thread watchdogThread;
    private Long lastSet = 0L;

    private final Integer MAX_DRIVE = 100; //TODO: This
    private final Integer MAX_STRAFE = 100; //TODO: This
    private final Double driveKP = 0d; //TODO: This
    private final Double driveKI = 0d; //TODO: This
    private final Double driveKD = 0d; //TODO: This

    public DriveSubsystem() {
        leftMotors = new MotorGroup(VictorSP.class, Map.LEFT_DRIVE_1, Map.LEFT_DRIVE_2);
        rightMotors = new MotorGroup(VictorSP.class, Map.RIGHT_DRIVE_1, Map.RIGHT_DRIVE_2);
        strafeMotors = new MotorGroup(VictorSP.class, Map.STRAFE_DRIVE_1, Map.STRAFE_DRIVE_2);
        strafeMotors.setInverts(false, false);

        frontLeft = leftMotors.getMotors().get(0);
        backLeft = leftMotors.getMotors().get(1);
        frontRight = rightMotors.getMotors().get(0);
        backRight = rightMotors.getMotors().get(1);

        watchdogThread = new Thread(watchdogRunnable);

        rotatePidOutput = new FakeMotor();

        adx = new ADXSensor(SPI.Port.kOnboardCS1, SPI.Port.kOnboardCS0);
        frontLeftEncoder = new Encoder(Map.FRONT_LEFT_ENC_A, Map.FRONT_LEFT_ENC_B);
        backLeftEncoder = new Encoder(Map.BACK_LEFT_ENC_A, Map.BACK_LEFT_ENC_B);
        frontRightEncoder = new Encoder(Map.FRONT_RIGHT_ENC_A, Map.FRONT_RIGHT_ENC_B);
        backRightEncoder = new Encoder(Map.BACK_RIGHT_ENC_A, Map.BACK_RIGHT_ENC_B);
        strafeEncoder = new Encoder(Map.STRAFE_ENC_A, Map.STRAFE_ENC_B);

        frontLeftEncoder.setPIDMode(AbstractEncoder.PIDMode.RATE);

        frontLeftPID = new PIDController(frontLeft, frontLeftEncoder, driveKP, driveKI, driveKD);
        backLeftPID = new PIDController(backLeft, backLeftEncoder, driveKP, driveKI, driveKD);
        frontRightPID = new PIDController(frontRight, frontLeftEncoder, driveKP, driveKI, driveKD);
        backRightPID = new PIDController(backRight, backRightEncoder, driveKP, driveKI, driveKD);
        strafePID = new PIDController(strafeMotors, strafeEncoder, 0, 0, 0); //TODO: Tune
        rotatePID = new PIDController(rotatePidOutput, adx, 0, 0, 0); //TODO: Tune

        lastSet = System.currentTimeMillis();
        watchdogThread.start();
    }

    /**
     * Disable all drive train motors if not set in the last second.
     */
    private Runnable watchdogRunnable = () -> {
        //noinspection InfiniteLoopStatement
        while (true) {
            if (System.currentTimeMillis() - lastSet > 1000) set(0, 0, 0);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Set the power of all of the drive train motors
     *
     * @param leftPow   Left Motor Power
     * @param rightPow  Right Motor Power
     * @param strafePow Horizontal Strafing Power
     */
    private void set(double leftPow, double rightPow, double strafePow) {
        frontLeftPID.setTarget(leftPow);
        backLeftPID.setTarget(leftPow);
        frontRightPID.setTarget(rightPow);
        backRightPID.setTarget(rightPow);
        strafePID.setTarget(strafePow);

    }

    private void setFiltered(double leftPower, double rightPower, double strafePower) {
        double max = Utils.maxDouble(leftPower, rightPower, strafePower);

        if (strafePower < MAX_STRAFE && max < MAX_DRIVE) {
            set(leftPower, rightPower, strafePower);
            return;
        }

        set(leftPower / max, rightPower / max, strafePower / max);
    }

    /**
     * Drive the Drive Train like it doesn't have the ability to strafe
     *
     * @param leftRpm  Left Motor Power
     * @param rightRpm Right Motor Power
     */
    public void tankDrive(double leftRpm, double rightRpm) {
        rotatePID.disable();
        set(leftRpm, rightRpm, 0);
    }

    /**
     * Drive the robot along a certain vector relative to the robot
     *
     * @param x X power, -1 to 1
     * @param y Y power, -1 to 1
     * @param z Turning power, -1 to 1
     */
    public void xyzDrive(double x, double y, double z) {
        rotatePID.disable();
        setFiltered(y + z, y - z, x);
    }

    /**
     * Drive the robot along a certain vector, facing a given direction, relative to the robot
     *
     * @param x     X power, -1 to 1
     * @param y     Y power, -1 to 1
     * @param angle Desired angle, 0 to 360 (degrees)
     */
    public void xyzAbsoluteAngleDrive(double x, double y, double angle) {
        if (!rotatePID.isEnabled()) rotatePID.enable();
        rotatePID.setTarget(angle);
        double z = rotatePidOutput.getPower();

        setFiltered(y + z, y - z, x);
    }

    /**
     * Drive the robot along a certain vector relative to the field
     *
     * @param x X power, -1 to 1
     * @param y Y power, -1 to 1
     * @param z Turning power, -1 to 1
     */
    public void fieldCentricDrive(double x, double y, double z) {
        rotatePID.disable();
        double angle = adx.getAngle();
        double xSet, ySet;

        xSet = y * Math.sin(angle) + x * Math.cos(angle);
        ySet = y * Math.cos(angle) + x * Math.sin(angle);

        setFiltered(ySet + z, ySet - z, xSet);
    }

    /**
     * Drive the robot along a certain vector, facing a given direction, relative to the field
     *
     * @param x      X power, -1 to 1
     * @param y      Y power, -1 to 1
     * @param target Desired angle, 0 to 360 (degrees). 0 is facing away from driver station.
     */
    public void fieldCentricAbsoluteAngleDrive(double x, double y, double target) {
        if (!rotatePID.isEnabled()) rotatePID.enable();
        rotatePID.setTarget(target);

        double z = rotatePidOutput.getPower();
        double angle = adx.getAngle();
        double xSet, ySet;

        xSet = y * Math.sin(angle) + x * Math.cos(angle);
        ySet = y * Math.cos(angle) + x * Math.sin(angle);

        setFiltered(ySet + z, ySet - z, xSet);
    }

    public MotorGroup getLeftMotors() {
        return leftMotors;
    }

    public MotorGroup getRightMotors() {
        return rightMotors;
    }

    public MotorGroup getStrafeMotors() {
        return strafeMotors;
    }
}
