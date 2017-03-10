package com.explodingbacon.steambot.commands;

import com.explodingbacon.bcnlib.framework.Command;
import com.explodingbacon.bcnlib.framework.Log;
import com.explodingbacon.steambot.Robot;
import com.explodingbacon.steambot.VisionThread;
import com.explodingbacon.steambot.subsystems.DriveSubsystem;

public class StreamlineAuto extends Command {

    DriveSubsystem drive;
    VisionThread vision;

    public StreamlineAuto() {
        drive = Robot.drive;
        vision = Robot.visionThread;
    }

    private final double offset = -2.5;
    private final double frontGearAngle = 0;
    private final double rightGearAngle = 36;
    private final double leftGearAngle = 360-36; //TODO: find (is it -36?)

    private final double findingTargetStrafeSpeed = 0.4;
    private final double backUpSpeed = -0.35;

    @Override
    public void onInit() {
        boolean sideGear = !Robot.auto.getSelected().toString().equalsIgnoreCase("front");
        boolean rightSide = Robot.auto.getSelected().toString().equalsIgnoreCase("right");

        double angle;
        if (sideGear) {
            if (rightSide) {
                angle = rightGearAngle;
            } else {
                angle = leftGearAngle;
            }
        } else {
            angle = frontGearAngle;
        }

        //angle += offset;

        if (angle < 0) angle = 360 + angle;

        while (!(vision.canSeeTarget() && vision.getError() <= 100) && Robot.isEnabled()) {
            if (sideGear) {
                if (rightSide) {
                    drive.fieldCentricAbsoluteAngleDrive(Math.cos(150) * findingTargetStrafeSpeed, Math.sin(150) * findingTargetStrafeSpeed,
                            angle); //formerly rightGeraAngle + offset;
                } else {
                    drive.fieldCentricAbsoluteAngleDrive((-Math.sin(360-120)) * findingTargetStrafeSpeed, (Math.cos(360/-120)) * findingTargetStrafeSpeed,
                            angle);
                    //Log.d("x: " + Math.cos(0) + ", y: " + Math.sin(0));

                }
            } else {
                drive.fieldCentricAbsoluteAngleDrive(findingTargetStrafeSpeed, 0, angle); //0.4 worked
            }
        }
        Log.v("Found target");
        drive.fieldCentricAbsoluteAngleDrive(0, 0, angle);
        if (Robot.isEnabled() && Robot.isAutonomous()) {
            Log.d("Strafe encoder value at detect: " + vision.getPositionWhenDetected() + "( current " + drive.strafeEncoder.get() + ")");
            //Log.v("Gear detected!");

            long millis = System.currentTimeMillis();
            long timeElapsed;

            boolean touched = false;

            while (!touched && Robot.isAutonomous()) {
                Robot.gear.setDeployed(false);
                drive.strafePID.enable();
                boolean wasAligned = false;
                long timeOfAlign = -1;
                //TODO: adjust timeout based off of which auto we're doing
                while (!(touched = Robot.gear.getTouchSensor()) && (timeElapsed = Math.abs(millis - System.currentTimeMillis())) <= 7000) {
                    Integer oldPos = vision.getPositionWhenDetected();

                    Double errorInches = vision.getInchesFromTarget();
                    if (oldPos != null && errorInches != null) {
                        //oldPos = -oldPos;
                        double target = oldPos - (drive.inchesToStrafeEncoder(errorInches));
                        //Log.d("Target: " + target);
                        if (rightSide) {
                            //target -= drive.inchesToStrafeEncoder(1);
                        }
                        target /= Math.abs(Math.cos(Math.toRadians(drive.rotatePID.getCurrentError()))); //formerly angle - drive.gyro.getForPID()
                        drive.strafePID.setTarget(target);
                    } else {
                        //Log.w("Got null data for vision");
                    }

                    double speed;
                    speed = timeElapsed >= 1400 ? 0.35 : 0.6; //.3 was good most of the time
                    if (timeElapsed < 1000) speed = 0; //Let PID adjust for 1 second before moving
                    /*
                    if (!drive.strafePID.isDone() && !wasAligned) {
                        speed = 0;
                        wasAligned = true;
                        timeOfAlign = System.currentTimeMillis();
                    } else {
                        long timeSinceAlign = System.currentTimeMillis() - timeOfAlign;
                        speed = timeSinceAlign >= 900 ? 0.35 : 0.6; //.3 was good most of the time
                        if (timeSinceAlign <= 500) {
                            speed = 0;
                        }
                    }
                    */

                    if (speed == 0) {
                        drive.keepHeading(angle + offset);
                    } else {
                        if (sideGear) {
                            //Log.d("FORWARD");
                            //drive.fieldCentricAbsoluteAngleDrive(-Math.cos(angle) * speed, -Math.sin(angle) * speed, angle + offset);
                        } else {
                            //drive.fieldCentricAbsoluteAngleDrive(0, speed, angle + offset);
                        }
                        drive.xyzAbsoluteAngleDrive(0, speed, angle + offset);
                    }
                    /*
                    if (speed == 0) {
                        drive.keepHeading(0);
                    } else {
                        drive.fieldCentricAbsoluteAngleDrive(0, speed, 0, false);
                    }
                    */
                }
                try {
                    Thread.sleep(250); //was 500
                } catch (Exception e) {
                }
                if (!Robot.gear.getDeployed() && touched) Robot.gear.setDeployed(true);
                Log.d("Strafe encoder value at the end: " + drive.strafeEncoder.get() + ", final target was " + drive.strafePID.getTarget());
                millis = System.currentTimeMillis();
                long diff;
                int backupTime = sideGear ? 1200 : 1200;
                while ((diff = Math.abs(millis - System.currentTimeMillis())) <= backupTime) {
                    //if (diff >= 5 && !Robot.gear.getDeployed() && touched) Robot.gear.setDeployed(true);
                    if (sideGear) {
                        //drive.fieldCentricAbsoluteAngleDrive(-Math.cos(angle) * backUpSpeed, -Math.sin(angle) * backUpSpeed, angle/* - offset*/);
                    } else {
                        //drive.fieldCentricAbsoluteAngleDrive(0, backUpSpeed, angle - offset);
                    }
                    drive.xyzAbsoluteAngleDrive(0, backUpSpeed, angle);
                }
            }
            if (sideGear) {
                if (rightSide) {
                    drive.gyro.shiftZero(90);
                } else {
                    drive.gyro.shiftZero(-90);
                }
            }
            Robot.gear.setDeployed(false);
            drive.strafePID.disable();
            drive.fieldCentricAbsoluteAngleDrive(0, 0, 0);
            if (sideGear && Robot.baseLine.getSelected().equals("yes")) {
                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() - start < 1000 && Robot.isAutonomous() && Robot.isEnabled()) {
                    drive.fieldCentricAbsoluteAngleDrive(0, .5, 0);
                }
            }
        } else {
            drive.fieldCentricAbsoluteAngleDrive(0, 0, drive.rotatePID.getTarget());
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
        return false;
    }
}
