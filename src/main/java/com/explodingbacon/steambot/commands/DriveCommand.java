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

    private double joyX, joyY, joyX2, joyY2, joyZ;
    private MotorGroup left, right, strafe;
    private double angle = 0;
    private XboxController drive;

    public static final double pegAngle = Robot.MAIN_ROBOT ? 56.68 : 45;
    public static final double feedAngle = Robot.MAIN_ROBOT ? 60.56 : 45;

    private boolean leftWasTrue = false, rightWasTrue = false;

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

        joyX2 = drive.getX2();
        joyY2 = -drive.getY2();

        joyX2 *= .75;
        joyY2 *= 1;

        //Log.d("joyZ: "+ joyZ);

        joyX = Math.pow(joyX, 2) * Utils.sign(joyX);
        
        joyY = Math.pow(joyY, 2) * Utils.sign(joyY);

        joyX2 = Math.pow(joyX2, 2) * Utils.sign(joyX2);
        joyY2 = Math.pow(joyY2, 2) * Utils.sign(joyY2);

        joyZ = Math.pow(joyZ, 2) * Utils.sign(joyZ);

        /*
        joyX = Math.pow(drive.getX(), 3);
        joyY = Math.pow(drive.getY(), 3);
        joyZ = Math.pow(-turn.getX(), 3); //drive.getZ();
        */

        joyX = Utils.deadzone(joyX, deadzone);
        joyY = Utils.deadzone(joyY, deadzone);

        joyX2 = Utils.deadzone(joyX2, deadzone);
        joyY2 = Utils.deadzone(joyY2, deadzone);

        joyZ = Utils.deadzone(joyZ, deadzone);

        if(drive.x.get()) angle = 360 - pegAngle;
        if(drive.y.get()) angle = 0;
        if(drive.b.get()) angle = pegAngle;
        if(drive.a.get()) angle = 180;

        boolean left = drive.leftBumper.get();
        boolean right = drive.rightBumper.get();

        if (left && !leftWasTrue) {
            angle -= feedAngle;
        } else if (right && !rightWasTrue) {
            angle += feedAngle;
        }

        leftWasTrue = left;
        rightWasTrue = right;

        if (!Robot.drive.gyro.isPresent()) {
            Log.w("NO GYRO DETECTED, IN ROBOT CENTRIC DRIVE MODE");
            Robot.drive.xyzDrive(joyX, joyY, joyZ);
            //Log.d("JoyZ: " + joyZ);
        } else {
            if (joyX2 == 0 && joyY2 == 0) {
                Robot.drive.fieldCentricAbsoluteAngleDrive(joyX, joyY, angle);
            } else {
                Robot.drive.xyzAbsoluteAngleDrive(joyX2, joyY2, angle);
            }
        }

        Robot.vision.setRingLight(!OI.ringlightOff.get());

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
