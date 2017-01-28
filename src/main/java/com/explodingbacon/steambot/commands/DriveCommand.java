package com.explodingbacon.steambot.commands;

import com.explodingbacon.bcnlib.actuators.Motor;
import com.explodingbacon.bcnlib.actuators.MotorGroup;
import com.explodingbacon.bcnlib.controllers.Joystick;
import com.explodingbacon.bcnlib.controllers.XboxController;
import com.explodingbacon.bcnlib.framework.Command;
import com.explodingbacon.bcnlib.framework.Log;
import com.explodingbacon.bcnlib.utils.Utils;
import com.explodingbacon.steambot.OI;
import com.explodingbacon.steambot.Robot;

public class DriveCommand extends Command {

    private final double deadzone = 0.1;
    private double joyX, joyY, joyZ;
    private MotorGroup left, right, strafe;
    private int angle = 0;
    private XboxController drive;

    @Override
    public void onInit() {
        left = Robot.drive.getLeftMotors();
        right = Robot.drive.getRightMotors();
        strafe = Robot.drive.getStrafeMotors();
        drive = OI.drive;
    }

    @Override
    public void onLoop() {
        joyX = drive.getX();
        joyY = drive.getY();
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

        Robot.drive.fieldCentricAbsoluteAngleDrive(joyX, joyY, angle);

        if(drive.x.get()) angle = 270;
        if(drive.y.get()) angle = 0;
        if(drive.b.get()) angle = 90;
        if(drive.a.get()) angle = 180;
    }

    @Override
    public void onStop() {

    }

    @Override
    public boolean isFinished() {
        return !Robot.isEnabled();
    }
}
