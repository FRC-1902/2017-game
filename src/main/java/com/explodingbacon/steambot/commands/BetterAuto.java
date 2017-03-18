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

    private final double offset = -2.5;
    private final double frontGearAngle = 0;
    private final double rightGearAngle = DriveCommand.pegAngle;
    private final double leftGearAngle = 360 - DriveCommand.pegAngle; //TODO: find (is it -36?)

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
            /*
            while (System.currentTimeMillis() - shootStart <= 7000 && Robot.isEnabled() && Robot.isEnabled()) {
                Robot.shooter.rev();
                if (Robot.shooter.shootPID.isDone()) reachedSpeed = true;
                if (reachedSpeed) {
                    Robot.shooter.justShoot();
                }
            }
            Robot.shooter.stopRev();
            Robot.shooter.stopShoot();
            */

            try {
                Thread.sleep(1500);
            } catch (Exception e) {}
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

        //angle += offset;

        if (angle < 0) angle = 360 + angle;

        if (movePosBecauseShoot) {
            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start <= 2000 && Robot.isEnabled() && Robot.isAutonomous()) {
                drive.xyzAbsoluteAngleDrive(0, -.7, 0);
            }
        }

        while (!(vision.canSeeTarget() /*&& vision.getError() <= 100*/) && Robot.isEnabled()) {
            if (sideGear) {
                if (rightSide) {
                    drive.fieldCentricDirectionalAbsoluteAngleDrive(angle, findingTargetStrafeSpeed, angle);
                    //drive.fieldCentricAbsoluteAngleDrive(Math.sin(45) * findingTargetStrafeSpeed, Math.cos(45) * findingTargetStrafeSpeed, angle);
                } else {
                    drive.fieldCentricDirectionalAbsoluteAngleDrive(360 - angle, findingTargetStrafeSpeed, angle);
                    //drive.fieldCentricAbsoluteAngleDrive(Math.sin(360 - 45) * findingTargetStrafeSpeed, Math.cos(360 - 45) * findingTargetStrafeSpeed, angle);
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

            Log.v("Starting vision attempts");
            while (!touched && Robot.isAutonomous() && Robot.isEnabled()) {
                Robot.gear.setDeployed(false);
                drive.strafePID.enable();
                //TODO: adjust timeout based off of which auto we're doing
                while (!(touched = Robot.gear.getTouchSensor()) && (timeElapsed = Math.abs(millis - System.currentTimeMillis())) <= 7000 && Robot.isAutonomous() && Robot.isEnabled()) {
                    Log.v("Approaching peg");
                    Integer oldPos = vision.getPositionWhenDetected();

                    Double errorInches = vision.getInchesFromTarget();
                    if (oldPos != null && errorInches != null) {
                        double target = oldPos - (drive.inchesToStrafeEncoder(errorInches));
                        //Log.d("Target: " + target);
                        target /= Math.abs(Math.cos(Math.toRadians(drive.rotatePID.getCurrentError()))); //formerly angle - drive.gyro.getForPID()
                        //if (!sideGear) target += drive.inchesToStrafeEncoder(1.5);
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
                } catch (Exception e) {}
                if (!Robot.gear.getDeployed() && touched) Robot.gear.setDeployed(true);
                try {
                    Thread.sleep(250); //was 500
                } catch (Exception e) {}
                Log.d("Strafe encoder value at the end: " + drive.strafeEncoder.get() + ", final target was " + drive.strafePID.getTarget());
                millis = System.currentTimeMillis();
                long diff;
                int backupTime = sideGear ? 1200 : 1200;
                while ((diff = Math.abs(millis - System.currentTimeMillis())) <= backupTime && Robot.isAutonomous() && Robot.isEnabled()) {
                    Log.v("Backing up");
                    drive.xyzAbsoluteAngleDrive(0, backUpSpeed, angle);
                }
            }

            Robot.gear.setDeployed(false);
            drive.strafePID.disable();
            if (sideGear && Robot.baseLine.getSelected().toString().equalsIgnoreCase("yes")) {
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
