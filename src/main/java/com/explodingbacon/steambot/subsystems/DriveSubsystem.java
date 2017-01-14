package com.explodingbacon.steambot.subsystems;

import com.explodingbacon.bcnlib.actuators.MotorGroup;
import com.explodingbacon.steambot.Map;
import edu.wpi.first.wpilibj.VictorSP;

public class DriveSubsystem {

    private MotorGroup leftMotors, rightMotors, strafeMotors;

    public DriveSubsystem() {
        leftMotors = new MotorGroup(VictorSP.class, Map.LEFT_DRIVE_1, Map.LEFT_DRIVE_2);
        rightMotors = new MotorGroup(VictorSP.class, Map.RIGHT_DRIVE_1, Map.RIGHT_DRIVE_2);
        strafeMotors = new MotorGroup(VictorSP.class, Map.STRAFE_DRIVE_1, Map.STRAFE_DRIVE_2);
        strafeMotors.setInverts(false, false);
    }

    public void set(double leftPow, double rightPow, double strafePow) {
        leftMotors.setPower(leftPow);
        rightMotors.setPower(rightPow);
        strafeMotors.setPower(strafePow);
    }

    public void tankDrive(double leftPow, double rightPow) {
        set(leftPow, rightPow, 0);
    }

    public void xyzDrive(double x, double y, double z) {

    }

    public void xyzAbsoluteAngleDrive(double x, double y, double angle) {

    }

    public void fieldCentricDrive(double x, double y, double z) {

    }

    public void fieldCentricAbsoluteAngleDrive(double x, double y, double angle) {

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
