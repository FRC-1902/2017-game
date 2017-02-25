package com.explodingbacon.steambot.commands;

import com.explodingbacon.bcnlib.actuators.MotorGroup;
import com.explodingbacon.bcnlib.controllers.XboxController;
import com.explodingbacon.bcnlib.framework.Command;
import com.explodingbacon.bcnlib.framework.Log;
import com.explodingbacon.bcnlib.utils.Utils;
import com.explodingbacon.steambot.OI;
import com.explodingbacon.steambot.Robot;

public class DriveCommand extends Command {

    private final double deadzone = 0.1;

    private final double angleAdjustRate = 2;

    private double joyX, joyY, joyZ;
    private MotorGroup left, right, strafe;
    private double angle = 0;
    private XboxController drive;

    private boolean leftWasTrue = false, rightWasTrue = false;

    private boolean leftTrigWasTrue = false, rightTrigWasTrue = false;

    @Override
    public void onInit() {
        left = Robot.drive.getLeftMotors();
        right = Robot.drive.getRightMotors();
        strafe = Robot.drive.getStrafeMotors();

        drive = OI.drive;
    }

    @Override
    public void onLoop() {
        if (OI.rezero.get()) {
            Robot.drive.gyro.rezero();
        }

        joyX = drive.getX();
        joyY = -drive.getY();
        joyZ = -drive.getX2();

        joyX = Math.pow(joyX, 2) * Utils.sign(joyX);
        joyY = Math.pow(joyY, 2) * Utils.sign(joyY);
        joyZ = Math.pow(joyZ, 2) * Utils.sign(joyZ);

        /*
        joyX = Math.pow(drive.getX(), 3);
        joyY = Math.pow(drive.getY(), 3);
        joyZ = Math.pow(-turn.getX(), 3); //drive.getZ();
        */

        joyX = Utils.deadzone(joyX, deadzone);
        joyY = Utils.deadzone(joyY, deadzone);
        joyZ = Utils.deadzone(joyZ, deadzone);

        /*
        Double scalar = Utils.maxDouble(joyX + joyZ, joyY + joyZ);
        if (scalar < 1) scalar = 1d;

        left.setPower((joyZ + joyY) / scalar);
        right.setPower((joyZ - joyY) / scalar);
        strafe.setPower(joyX / scalar);
        */

        if(drive.x.get()) angle = 270;
        if(drive.y.get()) angle = 0;
        if(drive.b.get()) angle = 90;
        if(drive.a.get()) angle = 180;

        boolean left = drive.leftBumper.get();
        boolean right = drive.rightBumper.get();

        if (left && !leftWasTrue) {
            angle -= 45;
        } else if (right && !rightWasTrue) {
            angle += 45;
        }

        leftWasTrue = left;
        rightWasTrue = right;

        /*
        boolean turnRight = drive.start.get();
        boolean turnLeft = drive.select.get();

        if (turnLeft) {
            angle -= angleAdjustRate;
        } else if (turnRight) {
            angle += angleAdjustRate;
        } else if(leftTrigWasTrue || rightTrigWasTrue){
                //angle = Robot.drive.gyro.getHeading();
        }

        leftTrigWasTrue = turnLeft;
        rightTrigWasTrue = turnRight;

        if (angle < 0) {
            angle = 360 + angle;
        } else if (angle > 360) {
            angle = angle - 360;
        }
        */

        /*
        if(OI.slowButton.get()) {
            joyX *= 0.5;
            joyY *= 0.5;
            joyZ *= 0.5;
        }
        */

        if (!Robot.drive.gyro.isPresent()) {
            Robot.drive.xyzAbsoluteAngleDrive(joyX, joyY, joyZ);
            Log.d("JoyZ: " + joyZ);
        } else {
            Robot.drive.fieldCentricAbsoluteAngleDrive(joyX, joyY, angle);
        }

        /*
        if (DriverStation.getInstance().getBatteryVoltage() <= 8) {
            OI.manipulator.rumble(0.2f, 0.2f);
        } else {
            OI.manipulator.rumble(0, 0);
        }
        */
    }

    @Override
    public void onStop() {

    }

    @Override
    public boolean isFinished() {
        return !Robot.isEnabled();
    }
}
