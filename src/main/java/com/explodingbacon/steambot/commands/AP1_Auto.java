package com.explodingbacon.steambot.commands;

import com.explodingbacon.bcnlib.framework.Command;
import com.explodingbacon.bcnlib.framework.Log;
import com.explodingbacon.steambot.Robot;

import static com.explodingbacon.steambot.Robot.drive;
import static com.explodingbacon.steambot.Robot.shooter;

public class AP1_Auto extends Command {


    double offset = 0;
    private final double driveSpeed = 0.4;
    private final double backUpSpeed = -0.65;
    private final long APPROACH_ACCEL = 1425, APPROACH_TIME = 5000, GEAR_BACKUP = 500;
    boolean useTouchplate;

    @Override
    public void onInit() {
        long time = System.currentTimeMillis();
        while (auto()) {
            shooter.rev();
            if (System.currentTimeMillis() - time > 3500) {
                shooter.justShoot();
            }
        }
        shooter.stopRev();


        /*
        boolean sideGear = !Robot.auto.getSelected().toString().equalsIgnoreCase("front");
        boolean rightSide = Robot.auto.getSelected().toString().equalsIgnoreCase("right");

        //boolean blue = Robot.alliance.getSelected().toString().equalsIgnoreCase("blue");
        //boolean shoot = Robot.shootInAuto.getSelected().toString().equalsIgnoreCase("shoot");

        useTouchplate = false;//Robot.useTouchplate.getSelected().toString().equalsIgnoreCase("true");

        String spamSelection = Robot.spamDrive.getSelected().toString();
        boolean spamDrive = !spamSelection.equalsIgnoreCase("no");
        boolean spamDriveLeft = spamDrive ? spamSelection.contains("left") : false;

        if (sideGear) {
            if (auto()) drive.codeFriendlyTankDrive(driveSpeed, driveSpeed);
            try {
                Thread.sleep(500);
            } catch (Exception e) {}
            drive.codeFriendlyTankDrive(0, 0);
            drive.rotatePID.enable();
            doForMillis(() -> drive.keepHeading(rightSide ? DriveCommand.pegAngle : -DriveCommand.pegAngle), 1000);
            drive.rotatePID.disable();
            scoreOntoPeg();
            drive.rotatePID.enable();
            doForMillis(() -> drive.keepHeading(rightSide ? -DriveCommand.pegAngle : DriveCommand.pegAngle), 1000);
            drive.rotatePID.disable();
            if (auto()) drive.codeFriendlyTankDrive(driveSpeed, driveSpeed);
            try {
                Thread.sleep(1500);
            } catch (Exception e) {}
            drive.codeFriendlyTankDrive(0, 0);
        } else {
            scoreOntoPeg();
            if (spamDrive) {
                drive.rotatePID.enable();

                doForMillis(() -> drive.keepHeading(spamDriveLeft ? -90 : 90), 2000);
                drive.rotatePID.disable();
                if (auto()) drive.codeFriendlyTankDrive(driveSpeed, driveSpeed);
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {}
                drive.codeFriendlyTankDrive(0,0);

                drive.rotatePID.enable();
                doForMillis(() -> drive.keepHeading(spamDriveLeft ? 90 : -90), 2000);
                drive.rotatePID.disable();
                drive.codeFriendlyTankDrive(driveSpeed, driveSpeed);
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {}
                if (auto()) drive.codeFriendlyTankDrive(0,0);
            }
        }*/
    }

    public void scoreOntoPeg() {
        boolean touched;
        long millis = System.currentTimeMillis();
        long timeElapsed;
        //VisionThread vision = Robot.visionThread;
        while (!(touched = Robot.gear.getTouchSensor()) && (timeElapsed = Math.abs(millis - System.currentTimeMillis())) <= APPROACH_TIME && auto()) {
            Log.v("Approaching peg");

            double speed;
            speed = timeElapsed >= APPROACH_ACCEL ? 0.45 : 0.45;
            if (timeElapsed < 1000) speed = 0; //Let PID adjust for 1 second before moving

            if (speed == 0) {
                drive.keepHeading(0 + offset);
            } else {
                drive.codeFriendlyTankDrive(speed, speed);
            }
        }
        try {
            Thread.sleep(500); //was 500
        } catch (Exception e) {
        }
        if (!useTouchplate) touched = true;
        if (!Robot.gear.getDeployed() && touched) Robot.gear.setDeployed(true);
        try {
            Thread.sleep(250); //was 500
        } catch (Exception e) {
        }
        millis = System.currentTimeMillis();
        long diff;
        while ((diff = Math.abs(millis - System.currentTimeMillis())) <= GEAR_BACKUP && auto()) {
            Log.v("Backing up");
            drive.codeFriendlyTankDrive(backUpSpeed,backUpSpeed);
        }
    }

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
        return false;
    }

    public void doForMillis(Runnable r, double time){
        long start = System.currentTimeMillis();
        while(System.currentTimeMillis() < start + time && auto()){
            r.run();
        }
    }
}
