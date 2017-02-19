package com.explodingbacon.steambot.commands;

import com.explodingbacon.bcnlib.framework.Command;
import com.explodingbacon.bcnlib.framework.Log;
import com.explodingbacon.steambot.Robot;
import com.explodingbacon.steambot.VisionThread;

public class AutoOne extends Command {

    boolean sideGear;
    boolean rightSide;

    final double rightGearAngle = 36; //was 33

    @Override
    public void onInit() {
        sideGear = !Robot.auto.getSelected().toString().equalsIgnoreCase("front");
        rightSide = Robot.auto.getSelected().toString().equalsIgnoreCase("right");
        Robot.visionThread.setTarget(VisionThread.TargetMode.GEAR);
        long start = System.currentTimeMillis();
        while (!Robot.visionThread.isAtTarget() && Robot.isEnabled()) {
            long time = Math.abs(start - System.currentTimeMillis());
            if (sideGear) {
                if (rightSide) {
                    if (time <= 500) {
                        Robot.drive.fieldCentricAbsoluteAngleDrive(0, 0, rightGearAngle, true);
                    } else {
                        //Robot.drive.fieldCentricAbsoluteAngleDrive(0.4, 0, rightGearAngle, true);
                        Robot.drive.fieldCentricAbsoluteAngleDrive(Math.cos(150) * .5, Math.sin(150) * .5, rightGearAngle, true);
                    }
                }
            } else {
                Robot.drive.fieldCentricAbsoluteAngleDrive(0.4, 0, 0, true); //0.5 worked
            }
        }
        if (Robot.isEnabled() && Robot.isAutonomous()) {
            //double currPos = Robot.drive.strafeEncoder.get();
            double pos = Robot.positionLog.getStrafeAt(Robot.visionThread.getTimeOfTargetFind());

            if (!sideGear) {
                pos += Robot.drive.inchesToStrafeEncoder(3);
            } else {
                pos -= Robot.drive.inchesToStrafeEncoder(0);
            }

            //Log.d("Position difference: " + (currPos - pos));

            Log.d("Strafe encoder value on detect: " + pos);
            Log.v("Gear detected!");

            //Robot.drive.strafePID.resetSource();
            //Robot.drive.strafePID.enable();
            //Robot.drive.strafePID.setTarget(pos);

            try {
                Thread.sleep(1000);
            } catch (Exception e) {}

            long millis = System.currentTimeMillis();
            long timeElapsed;

            boolean touched = false;
            while (!touched && Robot.isAutonomous()) {
                Robot.gear.setDeployed(false);
                while (!(touched = Robot.gear.getTouchSensor()) && (timeElapsed = Math.abs(millis - System.currentTimeMillis())) <= (sideGear ? 5000 : 4000)) {

                    //TODO: see if latency adjust is needed
                    double error = Robot.visionThread.getError();
                    if (!Robot.visionThread.isAtTarget()) {
                        if (Math.abs(error) > 5) {
                            if (error > 0) {
                                Robot.drive.getStrafeMotors().setPower(0.4);
                            } else {
                                Robot.drive.getStrafeMotors().setPower(-0.4);
                            }
                        }
                    }
                    //Log.d("Strafe error: " + Robot.drive.strafePID.getCurrentError());

                    double speed = timeElapsed >= 400 ? (sideGear ? 0.3 : 0.25) : (sideGear ? 0.6 : 0.5);
                    if (sideGear) {
                        if (rightSide) {
                            Robot.drive.xyzAbsoluteAngleDrive(0, speed, rightGearAngle - 5, false);
                        }
                    } else {
                        Robot.drive.fieldCentricAbsoluteAngleDrive(0, speed, 2, false);
                    }
                }
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                }

                //Robot.drive.strafePID.disable();
                millis = System.currentTimeMillis();
                while (Math.abs(millis - System.currentTimeMillis()) <= 1000) {
                    /*
                    if (millis >= 500) {
                        Robot.gear.setDeployed(false);
                    } else {
                        Robot.gear.setDeployed(true);
                    }
                    */
                    if (sideGear) {
                        if (rightSide) {
                            Robot.drive.xyzAbsoluteAngleDrive(0, -0.5, rightGearAngle, true);
                        }
                    } else {
                        Robot.drive.fieldCentricAbsoluteAngleDrive(0, -0.4, 0, true);
                    }
                }
            }
            Robot.gear.setDeployed(false);
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
