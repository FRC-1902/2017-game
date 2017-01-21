package com.explodingbacon.steambot.commands;

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
    private final double strafeDrivingDeadzone = 0.2;
    private double joyX, joyY, joyZ;

    @Override
    public void onInit() {}

    @Override
    public void onLoop() {
        XboxController drive = OI.drive;

        joyX = drive.getX();
        joyY = drive.getY();
        joyZ = -drive.getX2();

        joyX = Math.pow(joyX, 2) * Utils.sign(joyX);
        joyY = Math.pow(joyY, 2) * Utils.sign(joyY);
        joyZ = Math.pow(joyZ, 2) * Utils.sign(joyZ);

        joyX = Utils.deadzone(joyX, deadzone);
        joyY = Utils.deadzone(joyY, deadzone);
        joyZ = Utils.deadzone(joyZ, deadzone);

        /*
        Double scalar = Utils.maxDouble(joyX + joyZ, joyY + joyZ);
        if(scalar < 1) scalar = 1d;

        if (joyY > 0 && joyZ < strafeDrivingDeadzone) {
            joyZ = 0;
        }

        Robot.drive.getLeftMotors().setPower((joyZ + joyY) / scalar);
        Robot.drive.getRightMotors().setPower((joyZ - joyY) / scalar);
        Robot.drive.getStrafeMotors() .setPower(joyX / scalar);

        */

        Log.d("GYRO: " + Robot.drive.getADX().getAngle());

        Robot.drive.xyzDrive(joyX, joyZ, joyY);
        //Robot.drive.ghettoFieldCentricAbsoluteAngleDrive(joyX, joyY, joyZ);

        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {

    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
