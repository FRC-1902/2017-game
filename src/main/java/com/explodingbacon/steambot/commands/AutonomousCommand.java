package com.explodingbacon.steambot.commands;

import com.explodingbacon.bcnlib.framework.Command;
import com.explodingbacon.bcnlib.framework.Log;
import com.explodingbacon.steambot.Robot;
import com.explodingbacon.steambot.VisionThread;

public class AutonomousCommand extends Command {

    @Override
    public void onInit() {
        Robot.visionThread.setTarget(VisionThread.TargetMode.GEAR);
        while (!Robot.visionThread.isAtTarget()) {
            Robot.drive.fieldCentricAbsoluteAngleDrive(0.45, 0, 0, true); //0.5 worked
        }
        double currPos = Robot.drive.strafeEncoder.get();
        double pos = Robot.positionLog.getStrafeAt(Robot.visionThread.getTimeOfTargetFind());

        Log.d("Position difference: " + (currPos - pos));

        Log.d("Strafe encoder value on detect: " + pos);
        Log.v("Gear detected!");
        //Robot.drive.strafePID.resetSource();
        Robot.drive.strafePID.enable();
        Robot.drive.strafePID.setTarget(pos);
        long millis = System.currentTimeMillis();
        while (Math.abs(millis - System.currentTimeMillis()) <= 3000) {
            Robot.drive.fieldCentricAbsoluteAngleDrive(0, 0.5, 0, false);
        }
        Robot.drive.strafePID.disable();
        Robot.drive.fieldCentricAbsoluteAngleDrive(0, 0, 0, true);
        Log.d("Strafe encoder value at the end: " + Robot.drive.strafeEncoder.get());

    }

    @Override
    public void onLoop() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
