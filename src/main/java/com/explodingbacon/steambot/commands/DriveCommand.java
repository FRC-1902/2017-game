package com.explodingbacon.steambot.commands;

import com.explodingbacon.bcnlib.actuators.MotorGroup;
import com.explodingbacon.bcnlib.controllers.Joystick;
import com.explodingbacon.bcnlib.framework.Command;
import com.explodingbacon.bcnlib.utils.Utils;
import com.explodingbacon.steambot.OI;
import com.explodingbacon.steambot.Robot;

public class DriveCommand extends Command {

    private final double deadzone = 0.1;
    private final double strafeDrivingDeadzone = 0.2;
    private double joyX, joyY, joyZ;

    @Override
    public void onInit() {}

    @Override
    public void onLoop() {
        Joystick drive = OI.drive;
        Joystick turn = OI.turn;

        MotorGroup left = Robot.drive.getLeftMotors();
        MotorGroup right = Robot.drive.getRightMotors();
        MotorGroup strafe = Robot.drive.getStrafeMotors();

        joyX = drive.getX();
        joyY = drive.getY();
        joyZ = -turn.getX();

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

        Double scalar = Utils.maxDouble(joyX + joyZ, joyY + joyZ);
        if(scalar < 1) scalar = 1d;

        if (joyY > 0 && joyZ < strafeDrivingDeadzone) {
            joyZ = 0;
        }

        left.setPower((joyZ + joyY) / scalar);
        right.setPower((joyZ - joyY) / scalar);
        strafe.setPower(joyX / scalar);
    }

    @Override
    public void onStop() {

    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
