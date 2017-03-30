package com.explodingbacon.steambot.subsystems;

import com.explodingbacon.bcnlib.actuators.FakeMotor;
import com.explodingbacon.bcnlib.actuators.Motor;
import com.explodingbacon.bcnlib.actuators.MotorGroup;
import com.explodingbacon.bcnlib.framework.Log;
import com.explodingbacon.bcnlib.framework.PIDController;
import com.explodingbacon.bcnlib.framework.Subsystem;
import com.explodingbacon.bcnlib.sensors.AbstractEncoder;
import com.explodingbacon.bcnlib.sensors.BNOGyro;
import com.explodingbacon.bcnlib.sensors.Encoder;
import com.explodingbacon.bcnlib.utils.Utils;
import com.explodingbacon.steambot.Map;
import com.explodingbacon.steambot.Robot;
import edu.wpi.first.wpilibj.VictorSP;

import java.util.List;

public class DriveSubsystem extends Subsystem {

    private Double GLOBAL_MIN = 0.2;

    private MotorGroup leftMotors, rightMotors, strafeMotors;
    //private Motor frontLeft, backLeft, frontRight, backRight;
    public Encoder /*frontLeftEncoder, backLeftEncoder, frontRightEncoder, backRightEncoder,*/ strafeEncoder;

    public BNOGyro gyro;

    public PIDController/* frontLeftPID, backLeftPID, frontRightPID, backRightPID,*/ strafePID, rotatePID;
    private FakeMotor rotatePidOutput, strafePidOutput;

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
        if (Robot.MAIN_ROBOT) {
            leftMotors.setReversed(false);
            rightMotors.setReversed(false);
            strafeMotors.setReversed(false);
        } else {
            leftMotors.setReversed(false);
            rightMotors.setReversed(false);
            strafeMotors.setInverts(false, false);
        }

        /*
        frontLeft = leftMotors.getMotors().get(0);
        backLeft = leftMotors.getMotors().get(1);
        frontRight = rightMotors.getMotors().get(0);
        backRight = rightMotors.getMotors().get(1);
        */

        gyro = new BNOGyro(true);

        watchdogThread = new Thread(watchdogRunnable);

        rotatePidOutput = new FakeMotor();
        strafePidOutput = new FakeMotor();

        strafeEncoder = new Encoder(Map.STRAFE_ENC_A, Map.STRAFE_ENC_B);
        strafeEncoder.setPIDMode(AbstractEncoder.PIDMode.POSITION);
        strafeEncoder.setReversed(false);

        //0.00048, 0.000012, 0.004, 0.05, 1);
        //0.00088

        //TODO: figure out if this is right
        /*
        if(Robot.MAIN_ROBOT) {
            strafePID = new PIDController(strafePidOutput, strafeEncoder, 0.008, 0.000012, 0.001, 0.1, 1);
            strafePID.setFinishedTolerance(200); //TODO: tune?
        } else {
        */
        //       strafePID = new PIDController(strafePidOutput, strafeEncoder, 0.0012, 0.000020, 0.0015, 0.1, 1);
        strafePID = new PIDController(strafePidOutput, strafeEncoder, 0.0012, 0.000020, 0.0015, 0.1, 1);
        strafePID.setFinishedTolerance(200);



        //        rotatePID = new PIDController(rotatePidOutput, gyro, 0.018, 0.0008, 0.09, 0.15, 1)
        rotatePID = new PIDController(rotatePidOutput, gyro, 0.01, 0.0008 * 2, 0.09 * 1.5, 0.15, 1)
                .setRotational(true); //TODO: Tune

        rotatePID.setInputInverted(true);

        rotatePID.setFinishedTolerance(1);

        lastSet = System.currentTimeMillis();
        watchdogThread.start();
    }

    public void enabledInit() {
        strafeEncoder.reset();
        if (!gyro.isPresent()) {
            Log.e("GYRO IS NOT PRESENT! IMMEDIATELY DEBUG!");
        }
    }

    @Override
    public void disabledInit() {
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
    public void set(double leftPow, double rightPow, double strafePow) {
        leftMotors.setPower(leftPow);
        rightMotors.setPower(rightPow);
        strafeMotors.setPower(strafePow);
    }

    private void setFiltered(double leftPower, double rightPower, double strafePower) {
        double max = Utils.maxDouble(leftPower, rightPower, strafePower);

        if(max < GLOBAL_MIN) {
            stop();
            return;
        }

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
        y = -y;
        //double oldZ = z;
        //z = x;
        //x = oldZ;

        setFiltered(y + z, z - y, x + strafePidOutput.getPower());
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
        y = -y;
        rotatePID.setTarget(angle);
        double z = rotatePidOutput.getPower();

        setFiltered(y + z, z - y, x + strafePidOutput.getPower());
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

        setFiltered(ySet + z, z - ySet, xSet + strafePidOutput.getPower());
    }

    /**
     * Drive the robot along a certain vector, facing a given direction, relative to the field
     *
     * @param x      X power, -1 to 1
     * @param y      Y power, -1 to 1
     * @param target Desired angle, 0 to 360 (degrees). 0 is facing away from driver station.
     */
    public void fieldCentricAbsoluteAngleDrive(double x, double y, double target) {
        y = -y;
        if (!rotatePID.isEnabled()) rotatePID.enable();
        rotatePID.setTarget(target);

        double z = rotatePidOutput.getPower();
        double angle = Math.toRadians(gyro.getHeading());
        double xSet, ySet;

        xSet = y * Math.sin(angle) + x * Math.cos(angle);
        ySet = y * Math.cos(angle) - x * Math.sin(angle);

        //theoretically, ySet should be equal to y/cos, and xSet should be equal to x/sin

        setFiltered(ySet + z, z - ySet, xSet + strafePidOutput.getPower());
    }

    /**
     *
     * @param direction Driving direction(in degrees)
     * @param magnitude Drive speed(0 to 1)
     * @param heading Direction of heading(in degrees)
     */
    public void fieldCentricDirectionalAbsoluteAngleDrive(double direction, double magnitude, double heading){
        fieldCentricAbsoluteAngleDrive(Math.sin(direction) * magnitude, Math.cos(direction) * magnitude, heading);
    }

    public void keepHeading(double heading) {
        if (!rotatePID.isEnabled()) rotatePID.enable();
        rotatePID.setTarget(heading);

        leftMotors.setPower(rotatePidOutput.getPower());
        rightMotors.setPower(rotatePidOutput.getPower());
    }

    /**
     * Converts inches to strafe encoder clicks.
     *
     * @param inches The inches to be converted.
     * @return The encoder clicks equivalent to the inches provided.
     */
    public double inchesToStrafeEncoder(double inches) {
        return inches / (Math.PI * 4) * 360;
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

