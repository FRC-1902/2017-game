package com.explodingbacon.steambot.subsystems;

import com.explodingbacon.bcnlib.actuators.FakeMotor;
import com.explodingbacon.bcnlib.actuators.Motor;
import com.explodingbacon.bcnlib.actuators.MotorGroup;
import com.explodingbacon.bcnlib.framework.PIDController;
import com.explodingbacon.bcnlib.framework.Subsystem;
import com.explodingbacon.bcnlib.sensors.AbstractEncoder;
import com.explodingbacon.bcnlib.sensors.Encoder;
import com.explodingbacon.bcnlib.sensors.BNOGyro;
import com.explodingbacon.bcnlib.utils.Utils;
import com.explodingbacon.steambot.Map;
import edu.wpi.first.wpilibj.VictorSP;
import java.util.List;

public class DriveSubsystem extends Subsystem {

    private Double GLOBAL_MIN = 0.2;

    private MotorGroup leftMotors, rightMotors, strafeMotors;
    //private Motor frontLeft, backLeft, frontRight, backRight;
    public Encoder /*frontLeftEncoder, backLeftEncoder, frontRightEncoder, backRightEncoder,*/ strafeEncoder;

    public BNOGyro gyro;

    public PIDController/* frontLeftPID, backLeftPID, frontRightPID, backRightPID,*/ strafePID, rotatePID;
    private FakeMotor rotatePidOutput;

    private Thread watchdogThread;
    private Long lastSet = 0L;

    private final Integer MAX_DRIVE = 100; //TODO: This
    private final Integer MAX_STRAFE = 100; //TODO: This
    /*
    private final Double driveKP = 0.005d; //TODO: This. 0.001 is safe but slow
    private final Double driveKI = 0d; //TODO: This
    private final Double driveKD = 0d; //TODO: This
    */

    public DriveSubsystem() {
        leftMotors = new MotorGroup(VictorSP.class, Map.LEFT_DRIVE_1, Map.LEFT_DRIVE_2);
        rightMotors = new MotorGroup(VictorSP.class, Map.RIGHT_DRIVE_1, Map.RIGHT_DRIVE_2);
        strafeMotors = new MotorGroup(VictorSP.class, Map.STRAFE_DRIVE_1, Map.STRAFE_DRIVE_2);
        strafeMotors.setInverts(false, false);

        leftMotors.setReversed(false);
        rightMotors.setReversed(false);

        /*
        frontLeft = leftMotors.getMotors().get(0);
        backLeft = leftMotors.getMotors().get(1);
        frontRight = rightMotors.getMotors().get(0);
        backRight = rightMotors.getMotors().get(1);
        */

        gyro = new BNOGyro(true);

        watchdogThread = new Thread(watchdogRunnable);

        rotatePidOutput = new FakeMotor();

        /*
        frontLeftEncoder = new Encoder(Map.FRONT_LEFT_ENC_A, Map.FRONT_LEFT_ENC_B);
        backLeftEncoder = new Encoder(Map.BACK_LEFT_ENC_A, Map.BACK_LEFT_ENC_B);
        frontRightEncoder = new Encoder(Map.FRONT_RIGHT_ENC_A, Map.FRONT_RIGHT_ENC_B);
        backRightEncoder = new Encoder(Map.BACK_RIGHT_ENC_A, Map.BACK_RIGHT_ENC_B);
        */
        strafeEncoder = new Encoder(Map.STRAFE_ENC_A, Map.STRAFE_ENC_B); //skrrrt

        /*
        frontLeftEncoder.setPIDMode(AbstractEncoder.PIDMode.POSITION);
        backLeftEncoder.setPIDMode(AbstractEncoder.PIDMode.POSITION);
        frontRightEncoder.setPIDMode(AbstractEncoder.PIDMode.POSITION);
        backRightEncoder.setPIDMode(AbstractEncoder.PIDMode.POSITION);
        */
        strafeEncoder.setPIDMode(AbstractEncoder.PIDMode.POSITION);

        /*
        frontLeftEncoder.setReversed(false);
        backLeftEncoder.setReversed(false);
        frontRightEncoder.setReversed(true);
        backRightEncoder.setReversed(true);
        */
        strafeEncoder.setReversed(false);

        /*
        frontLeftPID = new PIDController(frontLeft, frontLeftEncoder, driveKP, driveKI, driveKD);
        backLeftPID = new PIDController(backLeft, backLeftEncoder, driveKP, driveKI, driveKD);
        frontRightPID = new PIDController(frontRight, frontRightEncoder, driveKP, driveKI, driveKD);
        backRightPID = new PIDController(backRight, backRightEncoder, driveKP, driveKI, driveKD);
        */
        //0.00048, 0.000012, 0.004, 0.05, 1);
        //0.00088
        strafePID = new PIDController(strafeMotors, strafeEncoder, 0.001, 0.000012, 0.004, 0.05, 1);
        rotatePID = new PIDController(rotatePidOutput, gyro, 0.015, 0.0008, 0.09, 0.15, 1)
                .setRotational(true); //TODO: Tune

        //frontLeftPID.setInputInverted(true);
        //backLeftPID.setInputInverted(true);
        rotatePID.setInputInverted(true);

        //Good-enough rotatePID values for field-centric stuff: 0.01, 0.0001, 0.02, 0.0, 0.5

        strafePID.setFinishedTolerance(2);

        lastSet = System.currentTimeMillis();
        watchdogThread.start();
    }

    public void enabledInit() {
        /*
        frontLeftEncoder.reset();
        backLeftEncoder.reset();
        frontRightEncoder.reset();
        backRightEncoder.reset();
        */
        strafeEncoder.reset();
    }

    @Override
    public void disabledInit() {
        /*
        frontLeftPID.disable();
        backLeftPID.disable();
        frontRightPID.disable();
        backRightPID.disable();
        */
        strafePID.disable();
    }

    @Override
    public void stop() {
        set(0, 0, 0.0);
    }

    @Override
    public List<Motor> getAllMotors() {
        return null;
    }

    /**
     * Disable all drive train motors if not set in the last second.
     */
    private Runnable watchdogRunnable = () -> {
        if(true) return; //TODO: Re-enable
        //noinspection InfiniteLoopStatement
        while (true) {
            if (System.currentTimeMillis() - lastSet > 1000) set(0, 0, 0.0);

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
    public void set(double leftPow, double rightPow, Double strafePow) {
        leftMotors.setPower(leftPow);
        rightMotors.setPower(rightPow);
        if (strafePow != null) strafeMotors.setPower(strafePow);
    }

    private void setFiltered(double leftPower, double rightPower, double strafePower, boolean useStrafe) {
        double max;
        if (useStrafe) {
            max = Utils.maxDouble(leftPower, rightPower, strafePower);
        } else {
            max = Utils.maxDouble(leftPower, rightPower);
        }

        if(max < GLOBAL_MIN) {
            stop();
            return;
        }

        if (((useStrafe && strafePower < MAX_STRAFE) || !useStrafe) && max < MAX_DRIVE) {
            set(leftPower, rightPower, strafePower);
            return;
        }

        set(leftPower / max, rightPower / max, (useStrafe ? (strafePower / max) : null));
    }

    /**
     * Drive the Drive Train like it doesn't have the ability to strafe
     *
     * @param leftRpm  Left Motor Power
     * @param rightRpm Right Motor Power
     */
    public void tankDrive(double leftRpm, double rightRpm) {
        rotatePID.disable();
        set(leftRpm, rightRpm, 0.0);
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
        setFiltered(y + z, y - z, x, true);
    }

    /**
     * Drive the robot along a certain vector, facing a given direction, relative to the robot
     *
     * @param x     X power, -1 to 1
     * @param y     Y power, -1 to 1
     * @param angle Desired angle, 0 to 360 (degrees)
     */
    public void xyzAbsoluteAngleDrive(double x, double y, double angle, boolean useStrafe) {
        if (!rotatePID.isEnabled()) rotatePID.enable();
        y = -y;
        rotatePID.setTarget(angle);
        double z = rotatePidOutput.getPower();

        setFiltered(y + z, z - y, x, useStrafe);
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
        double angle = Math.toRadians(gyro.getHeading());
        double xSet, ySet;

        xSet = y * Math.sin(angle) + x * Math.cos(angle);
        ySet = y * Math.cos(angle) - x * Math.sin(angle);

        setFiltered(ySet + z, z - ySet, xSet, true);
    }

    /**
     * Drive the robot along a certain vector, facing a given direction, relative to the field
     *
     * @param x      X power, -1 to 1
     * @param y      Y power, -1 to 1
     * @param target Desired angle, 0 to 360 (degrees). 0 is facing away from driver station.
     */
    public void fieldCentricAbsoluteAngleDrive(double x, double y, double target, boolean useStrafe) {
        y = -y;
        if (!rotatePID.isEnabled()) rotatePID.enable();
        rotatePID.setTarget(target);

        double z = rotatePidOutput.getPower();
        double angle = Math.toRadians(gyro.getHeading());
        double xSet, ySet;

        xSet = y * Math.sin(angle) + x * Math.cos(angle);
        ySet = y * Math.cos(angle) - x * Math.sin(angle);

        //theoretically, ySet should be equal to y/cos, and xSet should be equal to x/sin

        setFiltered(ySet + z, z - ySet, xSet, useStrafe);
    }

    public void keepHeading(double heading) {
        if (!rotatePID.isEnabled()) rotatePID.enable();
        rotatePID.setTarget(heading);

        leftMotors.setPower(rotatePidOutput.getPower());
        rightMotors.setPower(rotatePidOutput.getPower());
    }

    /**
     * Converts inches to drive encoder clicks.
     *
     * @param inches The inches to be converted.
     * @return The encoder clicks equivalent to the inches provided.
     */
    //TODO: This is the math from 2015, double check it's correct. UPDATE: Seems to be correct
    public double inchesToDriveEncoder(double inches) {
        return inches / (Math.PI * 4) * (444 + (4/9));
    }

    /**
     * Converts inches to strafe encoder clicks.
     *
     * @param inches The inches to be converted.
     * @return The encoder clicks equivalent to the inches provided.
     */
    //TODO: This is the math from 2015, double check it's correct
    public double inchesToStrafeEncoder(double inches) {
        return inches / (Math.PI * 4) * 1440;
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

