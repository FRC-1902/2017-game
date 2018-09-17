package com.explodingbacon.steambot.commands;

import com.explodingbacon.bcnlib.framework.Command;
import com.explodingbacon.bcnlib.framework.Log;
import com.explodingbacon.steambot.Robot;
import static com.explodingbacon.steambot.Robot.drive;

public class AP2_Auto extends Command {

    double offset = 0;
    private final double backUpSpeed = -0.65;
    private final long APPROACH_ACCEL = 1425, APPROACH_TIME = 4000, GEAR_BACKUP = 500;
    boolean useTouchplate;

    @Override
    public void onInit() {
        boolean sideGear = !Robot.auto.getSelected().toString().equalsIgnoreCase("front");
        boolean rightSide = Robot.auto.getSelected().toString().equalsIgnoreCase("right");

        //boolean blue = Robot.alliance.getSelected().toString().equalsIgnoreCase("blue");
        //boolean shoot = Robot.shootInAuto.getSelected().toString().equalsIgnoreCase("shoot");

        useTouchplate = Robot.useTouchplate.getSelected().toString().equalsIgnoreCase("true");

        String spamSelection = Robot.spamDrive.getSelected().toString();
        boolean spamDrive = !spamSelection.equalsIgnoreCase("no");
        boolean spamDriveLeft = spamDrive ? spamSelection.contains("left") : false;

        if (sideGear) {
            double target = drive.inchesToStrafeEncoder(6 * 12) * (rightSide ? 1 : -1);
            drive.strafePID.enable();
            drive.strafePID.setTarget(target);
            while (!drive.strafePID.isDone() && auto()) {
                drive.fieldCentricAbsoluteAngleDrive(0, 0, 0);
            }
            drive.strafePID.disable();
            drive.gyro.setZero(rightSide ? 90 : -90);
            if (auto()) {
                scoreOntoPeg(DriveCommand.pegAngle * (rightSide ? 1 : -1));
                if (auto() && spamDrive) {
                    spamDrive();
                }
            }
        } else {
            scoreOntoPeg(0);
            if (auto() && spamDrive) {
                double target = drive.inchesToStrafeEncoder((7 * 12) * (spamDriveLeft ? -1 : 1));
                target += drive.strafeEncoder.get();
                //Log.e("target: " + target);
                drive.strafePID.setTarget(target);
                drive.strafePID.enable();
                drive.strafePID.setTarget(target);
                while (Math.abs(drive.strafePID.getCurrentError()) > 400 && auto()) {
                    drive.fieldCentricAbsoluteAngleDrive(0, 0, 0);
                    drive.strafePID.log();
                }
                drive.strafePID.disable();
                Log.d("SPAM drive strafe done!");
                if (auto()) {
                    spamDrive();
                }
            }
        }

        while (auto()) {
            drive.fieldCentricAbsoluteAngleDrive(0, 0, 0);
        }
    }

    public void scoreOntoPeg(double approachAngle) {
        boolean touched;
        long millis = System.currentTimeMillis();
        long timeElapsed;
        //VisionThread vision = Robot.visionThread;
        while (!(touched = Robot.gear.getTouchSensor()) && (timeElapsed = Math.abs(millis - System.currentTimeMillis())) <= APPROACH_TIME && auto()) {
            //Log.v("Approaching peg " + drive.gyro.getForPID());

            double speed;
            speed = timeElapsed >= APPROACH_ACCEL ? 0.45 : 0.45;
            if (timeElapsed < 1000) speed = 0; //Let PID adjust for 1 second before moving

            if (!drive.rotatePID.isEnabled()) drive.rotatePID.enable();
            if (speed == 0) {
                drive.keepHeading(0 + offset);
            } else {
                //drive.xyzDrive(0, speed, 0);
                if (!drive.strafePID.isEnabled()) {
                    drive.strafePID.enable();
                    drive.strafePID.setTarget(drive.strafeEncoder.get());
                }
                drive.fieldCentricAbsoluteAngleDrive(0, speed, approachAngle + offset);
            }
        }
        drive.xyzDrive(0,0,0);
        drive.strafePID.disable();
        try {
            Thread.sleep(500); //was 500
        } catch (Exception e) {}
        if (!useTouchplate) touched = true;
        if (!Robot.gear.getDeployed() && touched) Robot.gear.setDeployed(true);
        try {
            Thread.sleep(350); //was 500
        } catch (Exception e) {}
        millis = System.currentTimeMillis();
        while (System.currentTimeMillis() - millis <= GEAR_BACKUP && auto()) {
            Log.v("Backing up");
            drive.xyzAbsoluteAngleDrive(0, backUpSpeed, 0);
        }
    }

    public void spamDrive() {
        long start = System.currentTimeMillis();
        while (auto() && System.currentTimeMillis() - start <= 2000) {
            drive.fieldCentricAbsoluteAngleDrive(0, 1, 0);
        }
    }
    /*
    if (spamDrive) {
                if (!sideGear) {
                    double target = drive.inchesToStrafeEncoder((7 * 12) * (spamDriveLeft ? -1 : 1));
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
     */

    public boolean auto() {
        return Robot.isAutonomous() && Robot.isEnabled();
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
