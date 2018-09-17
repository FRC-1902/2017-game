package com.explodingbacon.steambot.commands;

import com.explodingbacon.bcnlib.framework.Command;
import com.explodingbacon.bcnlib.framework.Log;
import com.explodingbacon.steambot.Robot;
import com.explodingbacon.steambot.VisionThread;
import com.explodingbacon.steambot.subsystems.DriveSubsystem;

public class BetterAuto extends Command {

    DriveSubsystem drive;
    VisionThread vision;

    public BetterAuto() {
        drive = Robot.drive;
        vision = Robot.visionThread;
    }

    private final double offset = 0;//-2.5;
    private final double frontGearAngle = 0;
    private final double rightGearAngle = 360 - DriveCommand.pegAngle;
    private final double leftGearAngle = DriveCommand.pegAngle;

    private final double blueShootAngle = 360 - 15;
    private final double redShootAngle = 15;

    private final double findingTargetStrafeSpeed = 0.4;
    private final double backUpSpeed = -0.4;

    private final long crossFieldTime = 2000;

    @Override
    public void onInit() {
        boolean didZero = false;

        boolean sideGear = !Robot.auto.getSelected().toString().equalsIgnoreCase("front");
        boolean rightSide = Robot.auto.getSelected().toString().equalsIgnoreCase("right");

        boolean blue = Robot.alliance.getSelected().toString().equalsIgnoreCase("blue");

        boolean shoot = Robot.shootInAuto.getSelected().toString().equalsIgnoreCase("shoot");
        boolean deadReckon = Robot.whichAutoClass.getSelected().toString().equalsIgnoreCase("dead");
        if (deadReckon) Log.a("Doing dead reckon! No vision being used.");

        boolean useTouchplate = Robot.useTouchplate.getSelected().toString().equalsIgnoreCase("true");

        String spamSelection = Robot.spamDrive.getSelected().toString();
        boolean spamDrive = !spamSelection.equalsIgnoreCase("no");
        boolean spamDriveLeft = spamDrive ? spamSelection.contains("left") : false;

        boolean movePosBecauseShoot = false;

        if (shoot) {
            Robot.shooter.rev();
            drive.strafePID.enable();
            drive.strafePID.setTarget(drive.inchesToClicks(12 * (blue ? -1 : 1)));
            boolean timedOut = false;
            long pidStart = System.currentTimeMillis();
            while (!drive.strafePID.isDone()) {
                if (System.currentTimeMillis() - pidStart > 3000) {
                    timedOut = true;
                    Log.e("Auto shoot strafe timed out. Aborting shoot portion of auto.");
                    break;
                }
                drive.fieldCentricDrive(0, 0, 0);
                try {
                    Thread.sleep(5);
                } catch (Exception e) {
                    timedOut = true;
                }
            }
            if (!timedOut) {
                Log.d("Auto strafe PID complete. Setting angle.");
                drive.strafePID.disable();
                drive.set(0, 0, 0);
                double shootAngle = blue ? blueShootAngle : redShootAngle;
                drive.rotatePID.enable();
                drive.rotatePID.setTarget(shootAngle);
                pidStart = System.currentTimeMillis();
                while (!drive.rotatePID.isDone()) {
                    if (System.currentTimeMillis() - pidStart > 3000) {
                        timedOut = true;
                        Log.e("Auto shoot strafe timed out. Aborting shoot portion of auto.");
                        break;
                    }
                    drive.keepHeading(shootAngle);
                    try {
                        Thread.sleep(5);
                    } catch (Exception e) {
                        timedOut = true;
                    }
                }
                drive.set(0, 0, 0);

                if (!timedOut) {
                    Log.d("Auto pid turn complete. Shooting...");
                    long shootStart = System.currentTimeMillis();
                    boolean reachedSpeed = false;


                    while (System.currentTimeMillis() - shootStart <= 3000 && Robot.isEnabled() && Robot.isEnabled()) {
                        //Robot.shooter.rev();
                        if (Robot.shooter.shootPID.isDone()) {
                            reachedSpeed = true;
                            Log.d("SHOOTING NOW");
                        }
                        if (reachedSpeed) {
                            Robot.shooter.justShoot();
                        }
                    }
                    Log.d("done shooting");
                }
            }
            drive.set(0, 0, 0);
            Robot.shooter.stopRev();
            Robot.shooter.stopShoot();

            //TODO: why did this 1.5 second sleep exist?
            /*
            try {
                Thread.sleep(1500);
            } catch (Exception e) {}
            */
            //BLUES BOILER IS ON THE LEFT

            double shift = blue ? -90 : 90;
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
            long angleFixStart = System.currentTimeMillis();
            while (System.currentTimeMillis() - angleFixStart < 1000 && Robot.isAutonomous() && Robot.isEnabled()) {
                drive.keepHeading(0);
            }
            drive.set(0, 0, 0);
        }

        if (sideGear) {
            if (rightSide) {
                drive.gyro.shiftZero(90);
            } else {
                drive.gyro.shiftZero(-90);
            }
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
        if (angle < 0) angle = 360 + angle;

        if (shoot) {
            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start <= 500 && Robot.isAutonomous() && Robot.isEnabled()) {
                drive.keepHeading(angle);
            }
        }

        /*
        if (movePosBecauseShoot) {
            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start <= 2000 && Robot.isEnabled() && Robot.isAutonomous()) {
                drive.xyzAbsoluteAngleDrive(0, -.7, 0);
            }
        }
        */

        while (!(vision.canSeeTarget() || deadReckon) && Robot.isEnabled() && Robot.isAutonomous()) {
            if (sideGear) {
                drive.strafePID.enable();
                drive.strafePID.setTarget(drive.inchesToClicks((5 * 12) * (rightSide ? 1 : -1)));
                while (!drive.strafePID.isDone() && Robot.isAutonomous() && Robot.isEnabled()) {
                    drive.fieldCentricDrive(0, 0, 0);
                    try {
                        Thread.sleep(5);
                    } catch (Exception e) {
                    }
                }
                drive.strafePID.disable();
                drive.rotatePID.enable();
                drive.rotatePID.setTarget(angle);
                while (!drive.rotatePID.isDone() && Robot.isAutonomous() && Robot.isEnabled()) {
                    drive.keepHeading(angle);
                    try {
                        Thread.sleep(5);
                    } catch (Exception e) {
                    }
                }
                drive.rotatePID.disable();
                drive.set(0, 0, 0);
            } else {
                drive.fieldCentricAbsoluteAngleDrive(findingTargetStrafeSpeed, 0, angle); //0.4 worked
            }
        }

        //Log.v("FOUND MATCH");
        drive.fieldCentricAbsoluteAngleDrive(0, 0, angle);
        if (Robot.isEnabled() && Robot.isAutonomous()) {
            long millis = System.currentTimeMillis();
            long timeElapsed;

            boolean touched = false;

            //Log.v("Starting vision attempts");
            while (!touched && Robot.isAutonomous() && Robot.isEnabled()) {
                Robot.gear.setDeployed(false);
                //drive.strafePID.enable();
                //TODO: adjust timeout based off of which auto we're doing
                Log.a("Approaching peg.");
                while (!(touched = Robot.gear.getTouchSensor()) && (timeElapsed = Math.abs(millis - System.currentTimeMillis())) <= 7000 && Robot.isAutonomous() && Robot.isEnabled()) {
                    if (deadReckon) {
                        double speed;
                        final int accelTime = 1425;
                        speed = timeElapsed >= accelTime ? 0.35 : 0.6; //.3 was good most of the time
                        if (timeElapsed < 1000) speed = 0; //Let PID adjust for 1 second before moving

                        if (speed == 0) {
                            drive.keepHeading(0 + offset);
                        } else {
                            drive.fieldCentricAbsoluteAngleDrive(0, speed, 0 + offset);
                        }
                    } else {
                        if (!drive.strafePID.isEnabled()) drive.strafePID.enable();
                        //Log.v("Approaching peg");
                        Integer oldPos = vision.getPositionWhenDetected();
                        //drive.strafePID.log();
                        Double errorInches = vision.getInchesFromTarget();
                        if (oldPos != null && errorInches != null) {
                            double target = oldPos - (drive.inchesToClicks(errorInches));

                            //TODO: IF VISION IS BEING WEIRD REMOVE THE - OFFSET IN HERE
                            double angleError = drive.rotatePID.getCurrentError();
                            //if (angleError < 0) angleError = 360 + angleError;

                            target /= Math.abs(Math.cos(Math.toRadians(angleError))); //formerly angle - drive.gyro.getForPID()
                            //if (!sideGear) target += drive.inchesToClicks(1.5);
                            drive.strafePID.setTarget(target);
                        } else {
                            //Log.w("Got null data for vision");
                        }

                        double speed;
                        int accelTime = sideGear ? 1400 : 1400; //may need to be lower now
                        speed = timeElapsed >= accelTime ? 0.4 : 0.6; //.45 formerly
                        if (timeElapsed < 1000) speed = 0; //Let PID adjust for 1 second before moving

                        if (speed == 0) {
                            //Log.d("Should be moving");
                        }
                        //if (speed == 0) {
                            //drive.keepHeading(angle + offset);
                        //} else {
                            drive.xyzAbsoluteAngleDrive(0, speed, angle + offset);
                        //}
                    }
                }
                /*
                try {
                    Thread.sleep(500); //was 250
                } catch (Exception e) {
                }
                if (!useTouchplate) touched = true;
                if (!Robot.gear.getDeployed() && touched) Robot.gear.setDeployed(true);
                try {
                    Thread.sleep(500); //was 250
                } catch (Exception e) {
                }
                Robot.gear.setDeployed(false);
                */
                drive.strafePID.disable();
                Log.d("Strafe encoder value at the end: " + drive.strafeEncoder.get() + ", final target was " + drive.strafePID.getTarget());

                /*
                if (touched) {
                    millis = System.currentTimeMillis();
                    while (Math.abs(millis - System.currentTimeMillis()) <= 750 && Robot.isAutonomous() && Robot.isEnabled()) {
                        drive.xyzAbsoluteAngleDrive(0, backUpSpeed, angle);
                    }

                    Robot.gear.setDeployed(true);

                    millis = System.currentTimeMillis();
                    while (Math.abs(millis - System.currentTimeMillis()) <= 1000 && Robot.isAutonomous() && Robot.isEnabled()) {
                        drive.xyzAbsoluteAngleDrive(0, -backUpSpeed + .1, angle);
                    }

                    Robot.gear.setDeployed(false);
                }*/

                millis = System.currentTimeMillis();
                int backupTime = sideGear ? 1500 : 1200;
                if (sideGear && spamDrive) backupTime = 2500;
                Log.a("Backing up from peg...");
                long elapsed;
                while ((elapsed = Math.abs(millis - System.currentTimeMillis())) <= backupTime && Robot.isAutonomous() && Robot.isEnabled()) {
                    drive.xyzAbsoluteAngleDrive(0, backUpSpeed, angle);
                    if (elapsed < 250) {
                        if (touched) Robot.gear.setDeployed(true);
                    }
                }
                if (touched) Robot.gear.setDeployed(false);
            }
            if (spamDrive) {
                if (!sideGear) {
                    double target = drive.inchesToClicks((7 * 12) * (spamDriveLeft ? -1 : 1));
                    target += drive.strafeEncoder.get();
                    Log.e("target: " + target);
                    drive.strafePID.setTarget(target);
                    drive.strafePID.enable();
                    drive.strafePID.setTarget(target);
                    while (Math.abs(drive.strafePID.getCurrentError()) > 400 && Robot.isAutonomous() && Robot.isEnabled()) {
                        drive.fieldCentricAbsoluteAngleDrive(0, 0, 0);
                        drive.strafePID.log();
                    }
                    drive.strafePID.disable();
                    Log.d("PID done!");
                }
                Log.a("Driving across the field (180 woot woot)");
                millis = System.currentTimeMillis();
                while (Math.abs(millis - System.currentTimeMillis()) <= crossFieldTime && Robot.isAutonomous() && Robot.isEnabled()) {
                    drive.fieldCentricAbsoluteAngleDrive(0, 1, 0);
                }
            }

            Robot.gear.setDeployed(false);
            drive.strafePID.disable();

            while (Robot.isAutonomous() && Robot.isEnabled()) {
                drive.fieldCentricAbsoluteAngleDrive(0, 0, 0);
            }
            Log.a("Auto complete");
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

/**
 * List<Waypoint> second_path = new ArrayList<>();
 second_path.add(new Waypoint(new Translation2d(215, 18), 120.0));
 second_path.add(new Waypoint(new Translation2d(150, 18), 60.0));
 second_path.add(new Waypoint(new Translation2d(70, 18), 84.0));
 second_path.add(new Waypoint(new Translation2d(56, 18), 84.0));
 second_path.add(new Waypoint(new Translation2d(56, 24), 84.0));
 second_path.add(new Waypoint(new Translation2d(18, 26), 84.0));
 Path p = new Path(second_path);
 */
