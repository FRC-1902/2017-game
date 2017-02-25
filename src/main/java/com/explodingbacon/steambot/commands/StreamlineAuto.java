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

    private final double offset = -4;
    private final double frontGearAngle = 0;
    private final double rightGearAngle = 36;
    private final double leftGearAngle = 99999; //TODO: find (-36?)

    private final double findingTargetStrafeSpeed = 0.4;
    private final double backUpSpeed = -0.4;

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

        angle += offset;

        if (angle < 0) angle = 360 + angle;

        while (!vision.canSeeTarget() && Robot.isEnabled()) {
            if (sideGear) {
                if (rightSide) {
                    drive.fieldCentricAbsoluteAngleDrive(Math.cos(150) * findingTargetStrafeSpeed, Math.sin(150) * findingTargetStrafeSpeed,
                            rightGearAngle + offset);
                } else {
                    //TODO
                }
            }
            drive.fieldCentricAbsoluteAngleDrive(findingTargetStrafeSpeed, 0, angle); //0.4 worked
        }
        Log.v("Found target");
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
                        //target -= drive.inchesToStrafeEncoder(2);
                        target /= Math.abs(Math.cos(Math.toRadians(offset)));
                        drive.strafePID.setTarget(target);
                    } else {
                        //Log.w("Got null data for vision");
                    }

                    double speed;
                    speed = timeElapsed >= 1400 ? 0.3 : 0.6; //.3 was good most of the time
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
                        drive.keepHeading(angle);
                    } else {
                        if (sideGear) {
                            drive.fieldCentricAbsoluteAngleDrive(Math.cos(angle) * speed, Math.sin(angle) * speed, angle);
                        } else {
                            drive.fieldCentricAbsoluteAngleDrive(0, speed, angle);
                        }
                    }
                    /*
                    if (speed != 0) {
                        drive.getLeftMotors().setPower(-speed);
                        drive.getRightMotors().setPower(speed);
                    }
                    */
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
                //Log.d("Strafe encoder value at the end: " + drive.strafeEncoder.get() + ", final target was " + drive.strafePID.getTarget());
                //Robot.drive.strafePID.disable(); //TODO: see if this is more accurate than the other one
                millis = System.currentTimeMillis();
                long diff;
                while ((diff = Math.abs(millis - System.currentTimeMillis())) <= 1200) {
                    if (diff >= 50 && !Robot.gear.getDeployed() && touched) Robot.gear.setDeployed(true);
                    if (sideGear) {
                        drive.fieldCentricAbsoluteAngleDrive(Math.cos(angle) * backUpSpeed, Math.sin(angle) * backUpSpeed, angle - offset);
                    } else {
                        drive.fieldCentricAbsoluteAngleDrive(0, backUpSpeed, angle - offset);
                    }
                }
            }
            Robot.gear.setDeployed(false);
            drive.strafePID.disable(); //TODO: see if this is more accurate than the other
            drive.fieldCentricAbsoluteAngleDrive(0, 0, angle);
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
