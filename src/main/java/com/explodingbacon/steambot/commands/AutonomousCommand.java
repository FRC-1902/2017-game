package com.explodingbacon.steambot.commands;

import com.explodingbacon.bcnlib.framework.Command;
import com.explodingbacon.bcnlib.framework.Log;
import com.explodingbacon.steambot.Robot;
import com.explodingbacon.steambot.VisionThread;

public class AutonomousCommand extends Command {

    boolean sideGear = true;

    @Override
    public void onInit() {
        sideGear = !Robot.auto.getSelected().toString().equalsIgnoreCase("front");
        Robot.visionThread.setTarget(VisionThread.TargetMode.GEAR);
        while (!Robot.visionThread.isAtTarget() && Robot.isEnabled()) {
            if (sideGear) {
                Robot.drive.fieldCentricAbsoluteAngleDrive(Math.cos(150) / 2, Math.sin(150) / 2, 30, true);
            } else {
                Robot.drive.fieldCentricAbsoluteAngleDrive(0.4, 0, 0, true); //0.5 worked
            }
        }
        if (Robot.isEnabled() && Robot.isAutonomous()) {
            double currPos = Robot.drive.strafeEncoder.get();
            double pos = Robot.positionLog.getStrafeAt(Robot.visionThread.getTimeOfTargetFind());

            Log.d("Position difference: " + (currPos - pos));

            Log.d("Strafe encoder value on detect: " + pos);
            Log.v("Gear detected!");
            //Robot.drive.strafePID.resetSource();
            Robot.drive.strafePID.enable();
            Robot.drive.strafePID.setTarget(pos);
            long millis = System.currentTimeMillis();
            while (!Robot.gear.getTouchSensor() && Math.abs(millis - System.currentTimeMillis()) <= 8000) {
                double speed = Math.abs(millis - System.currentTimeMillis()) >= 500 ? 0.4 : .65;
                if (sideGear) {
                    //Robot.drive.fieldCentricAbsoluteAngleDrive(Math.cos(45 + 180) / 2, Math.abs(45 + 180) / 2, 45, false);
                    Robot.drive.xyzAbsoluteAngleDrive(0, speed, 30, false);
                    //Robot.drive.fieldCentricAbsoluteAngleDrive(0 ,0, Robot.drive.rotatePID.getTarget(), false);
                } else {
                    Robot.drive.fieldCentricAbsoluteAngleDrive(0, speed, 0, false);
                }
            }
            Robot.drive.strafePID.disable();
            millis = System.currentTimeMillis();
            while (Math.abs(millis - System.currentTimeMillis()) <= 6000) {
                Robot.drive.fieldCentricAbsoluteAngleDrive(0, -0.4 ,0, true);
            }
            Robot.drive.fieldCentricAbsoluteAngleDrive(0, 0, 0, true);
            Log.d("Strafe encoder value at the end: " + Robot.drive.strafeEncoder.get());
        } else {
            Robot.drive.fieldCentricAbsoluteAngleDrive(0, 0, Robot.drive.rotatePID.getTarget(), true);
        }

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
