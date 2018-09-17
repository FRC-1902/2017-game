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
        boolean didZero = false;

        boolean sideGear = !Robot.auto.getSelected().toString().equalsIgnoreCase("front");
        boolean rightSide = Robot.auto.getSelected().toString().equalsIgnoreCase("right");

        boolean blue = Robot.alliance.getSelected().toString().equalsIgnoreCase("blue");
        boolean shoot = true;

        boolean movePosBecauseShoot = false;

        if (shoot) {
            long shootStart = System.currentTimeMillis();
            boolean reachedSpeed = false;
            while (System.currentTimeMillis() - shootStart <= 7000 && Robot.isEnabled() && Robot.isEnabled()) {
                Robot.shooter.rev();
                if (Robot.shooter.shootPID.isDone()) reachedSpeed = true;
                if (reachedSpeed) {
                    Robot.shooter.justShoot();
                }
            }
            Robot.shooter.stopRev();
            Robot.shooter.stopShoot();

            //BLUES BOILER IS ON THE LEFT

            double shift = 90;
            if (sideGear) {
                if (blue) {
                    if (rightSide) {
                        shift = 0;
                        movePosBecauseShoot = true;
                    } else {
                        shift = 180;
                    }
                } else {
                    if (rightSide) {
                        shift = 180;
                    } else {
                        shift = 0;
                        movePosBecauseShoot = true;
                    }
                }
            }
            drive.gyro.shiftZero(shift);
        }

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

        if (movePosBecauseShoot) {
            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start <= 2000 && Robot.isEnabled() && Robot.isAutonomous()) {
                drive.xyzAbsoluteAngleDrive(0, -.7, 0);
            }
        }

        while (!(vision.canSeeTarget() && vision.getError() <= 100) && Robot.isEnabled()) {
            if (sideGear) {
                if (rightSide) {
                    drive.fieldCentricAbsoluteAngleDrive(Math.cos(150) * findingTargetStrafeSpeed, Math.sin(150) * findingTargetStrafeSpeed,
                            angle);
                } else {
                    drive.fieldCentricAbsoluteAngleDrive((-Math.sin(360-120)) * findingTargetStrafeSpeed, (Math.cos(360/-120)) * findingTargetStrafeSpeed,
                            angle);
                    //Log.d("x: " + Math.cos(0) + ", y: " + Math.sin(0));

                }
            } else {
                drive.fieldCentricAbsoluteAngleDrive(findingTargetStrafeSpeed * (!blue && shoot ? -1 : 1), 0, angle); //0.4 worked
            }
        }

        Log.v("FOUND MATCH");
        drive.fieldCentricAbsoluteAngleDrive(0, 0, angle);
        if (Robot.isEnabled() && Robot.isAutonomous()) {
            //Log.d("Strafe encoder value at detect: " + vision.getPositionWhenDetected() + "( current " + drive.strafeEncoder.get() + ")");
            //Log.v("Gear detected!");

            long millis = System.currentTimeMillis();
            long timeElapsed;

            boolean touched = false;

            while (!touched && Robot.isAutonomous()) {
                Log.v("Starting vision attempts");
                Robot.gear.setDeployed(false);
                drive.strafePID.enable();
                //TODO: adjust timeout based off of which auto we're doing
                while (!(touched = Robot.gear.getTouchSensor()) && (timeElapsed = Math.abs(millis - System.currentTimeMillis())) <= 7000) {
                    Log.v("Approaching peg");
                    Integer oldPos = vision.getPositionWhenDetected();

                    Double errorInches = vision.getInchesFromTarget();
                    if (oldPos != null && errorInches != null) {
                        double target = oldPos - (drive.inchesToClicks(errorInches));
                        //Log.d("Target: " + target);
                        target /= Math.abs(Math.cos(Math.toRadians(drive.rotatePID.getCurrentError()))); //formerly angle - drive.gyro.getForPID()
                        drive.strafePID.setTarget(target);
                    } else {
                        //Log.w("Got null data for vision");
                    }

                    double speed;
                    int accelTime = sideGear ? 1400 : 1425;
                    speed = timeElapsed >= accelTime ? 0.35 : 0.6; //.3 was good most of the time
                    if (timeElapsed < 1000) speed = 0; //Let PID adjust for 1 second before moving

                    if (speed == 0) {
                        drive.keepHeading(angle + offset);
                    } else {
                        drive.xyzAbsoluteAngleDrive(0, speed, angle + offset);
                    }
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
                    Log.v("Backing up");
                    drive.xyzAbsoluteAngleDrive(0, backUpSpeed, angle);
                }
            }

            Robot.gear.setDeployed(false);
            drive.strafePID.disable();
            if (sideGear && Robot.baseLine.getSelected().toString().equalsIgnoreCase("yes")) {
                if (sideGear) {
                    if (rightSide) {
                        drive.gyro.shiftZero(90);
                    } else {
                        drive.gyro.shiftZero(-90);
                    }
                    didZero = true;
                }
                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() - start < 1000 && Robot.isAutonomous() && Robot.isEnabled()) {
                    drive.fieldCentricAbsoluteAngleDrive(0, .5, 0);
                }
                //drive.fieldCentricAbsoluteAngleDrive(0, 0, 0);
            }
            while (Robot.isAutonomous() && Robot.isEnabled()) {
                drive.fieldCentricAbsoluteAngleDrive(0, 0, 0);
            }
        } else {
            drive.fieldCentricAbsoluteAngleDrive(0, 0, drive.rotatePID.getTarget());
        }
        if (sideGear && !didZero) {
            if (rightSide) {
                drive.gyro.shiftZero(90);
            } else {
                drive.gyro.shiftZero(-90);
            }
            //didZero = true;
        }
        Log.v("Angle fixed.");
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
