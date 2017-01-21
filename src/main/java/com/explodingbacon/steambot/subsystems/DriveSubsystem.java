package com.explodingbacon.steambot.subsystems;

import com.explodingbacon.bcnlib.actuators.MotorGroup;
import com.explodingbacon.bcnlib.framework.Log;
import com.explodingbacon.bcnlib.framework.PIDController;
import com.explodingbacon.bcnlib.sensors.ADXSensor;
import com.explodingbacon.bcnlib.utils.Utils;
import com.explodingbacon.steambot.Map;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.VictorSP;

public class DriveSubsystem {

    private MotorGroup leftMotors, rightMotors, strafeMotors;
    private PIDController rotatePID;
    private double rotatePidOutput = 0;
    private ADXSensor adx;

    public DriveSubsystem() {
        leftMotors = new MotorGroup(VictorSP.class, Map.LEFT_DRIVE_1, Map.LEFT_DRIVE_2);
        rightMotors = new MotorGroup(VictorSP.class, Map.RIGHT_DRIVE_1, Map.RIGHT_DRIVE_2);
        strafeMotors = new MotorGroup(VictorSP.class, Map.STRAFE_DRIVE_1, Map.STRAFE_DRIVE_2);
        strafeMotors.setInverts(false, false);

        adx = new ADXSensor(SPI.Port.kOnboardCS1, SPI.Port.kOnboardCS0);

        //TODO: tune
        rotatePID = new PIDController(null, adx, 0, 0, 0);
    }

    /**
     * Set the power of all of the drive train motors
     *
     * @param leftPow Left Motor Power
     * @param rightPow Right Motor Power
     * @param strafePow Horizontal Strafing Power
     */
    public void set(double leftPow, double rightPow, double strafePow) {
        leftMotors.setPower(leftPow);
        rightMotors.setPower(rightPow);
        strafeMotors.setPower(strafePow);
    }

    /**
     * Drive the Drive Train like it doesn't have the ability to strafe
     *
     * @param leftPow Left Motor Power
     * @param rightPow Right Motor Power
     */
    public void tankDrive(double leftPow, double rightPow) {
        set(leftPow, rightPow, 0);
    }

    /**
     * Drive the robot along a certain vector relative to the robot
     * @param x X power, -1 to 1
     * @param y Y power, -1 to 1
     * @param z Turning power, -1 to 1
     */
    public void xyzDrive(double x, double y, double z) {
        setFiltered(y + z, y - z, x);
    }

    /**
     * Drive the robot along a certain vector, facing a given direction, relative to the robot
     * @param x X power, -1 to 1
     * @param y Y power, -1 to 1
     * @param angle Desired angle, 0 to 360 (degrees)
     */
    public void xyzAbsoluteAngleDrive(double x, double y, double angle) {
        rotatePID.setTarget(angle);
        double z = rotatePidOutput;

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
        double angle = Math.toRadians(adx.getAngle());
        double xSet, ySet;

        xSet = y * Math.sin(angle) + x * Math.cos(angle);

        ySet = y * Math.cos(angle) + x * Math.sin(angle);

        //Log.i("" + Math.sin(angle) + ", " + Math.cos(angle));
        setFiltered(ySet + z, -ySet + z, xSet);
    }

    /**
     * Drive the robot along a certain vector, facing a given direction, relative to the field
     *
     * @param x X power, -1 to 1
     * @param y Y power, -1 to 1
     * @param target Desired angle, 0 to 360 (degrees)
     */
    public void fieldCentricAbsoluteAngleDrive(double x, double y, double target) {
        rotatePID.setTarget(target);
        double z = rotatePidOutput;
        double angle = adx.getAngle();
        double xSet, ySet;

        xSet = y * Math.sin(angle) + x * Math.cos(angle);
        ySet = y * Math.cos(angle) + x * Math.sin(angle);

        setFiltered(ySet + z, ySet - z, xSet);
    }

    public void ghettoFieldCentricAbsoluteAngleDrive(double x, double y, double target) {
        double angle = Math.toRadians(adx.getAngle());
        double z = Utils.minMax((target - angle)/(Math.PI), 0.1, 0.8);
        double xSet, ySet;

        xSet = y * Math.sin(angle) + x * Math.cos(angle);
        ySet = y * Math.cos(angle) + x * Math.sin(angle);

        setFiltered(ySet + z, -ySet + z, xSet);
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

    public ADXSensor getADX() {
        return adx;
    }

    private void setFiltered(double leftPower, double rightPower, double strafePower) {
        double max = Utils.maxDouble(leftPower, rightPower, strafePower);

        if (max < 1) {
            set(leftPower, rightPower, strafePower);
            return;
        }

        set(leftPower / max, rightPower / max, strafePower / max);
    }

    private PIDOutput rotateOutput = output -> DriveSubsystem.this.rotatePidOutput = output;
}
