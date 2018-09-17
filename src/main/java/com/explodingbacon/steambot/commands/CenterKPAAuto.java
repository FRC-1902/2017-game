package com.explodingbacon.steambot.commands;

import com.explodingbacon.bcnlib.framework.Command;
import com.explodingbacon.bcnlib.framework.Log;
import com.explodingbacon.steambot.Robot;
import com.explodingbacon.steambot.subsystems.DriveSubsystem;
import com.explodingbacon.steambot.subsystems.ShooterSubsystem;

public class CenterKPAAuto extends Command {
    DriveSubsystem drive;
    ShooterSubsystem shooter;

    public CenterKPAAuto(){
        drive = Robot.drive;
        shooter = Robot.shooter;
    }

    private long maxShootTime = 7000;
    private double forwardSpeed = 0.5;
    private double backUpSpeed = 0.2;

    private long startMillis = System.currentTimeMillis();
    private boolean centerGear, shootReady, turnTimedOut;

    @Override
    public void onInit(){
        Robot.shooter.rev();

        centerGear = Robot.auto.getSelected().toString().equalsIgnoreCase("front");
        shootReady = false;
        turnTimedOut = false;

        while(System.currentTimeMillis() - startMillis <= maxShootTime && Robot.isEnabled()){
            if(shooter.shootPID.isDone()) shootReady = true;
            if(shootReady) shooter.justShoot();
        }
        shooter.stopShoot();
        shooter.stopRev();

        if(centerGear){
            drive.rotatePID.setTarget(drive.gyro.getHeading() - 90);
            while(!drive.rotatePID.isDone() && !turnTimedOut && Robot.isEnabled()){
                safeWait(5, () -> turnTimedOut = true);
            }
            turnTimedOut = false;

            while(!Robot.gear.getTouchSensor() && Robot.isEnabled() && Robot.isAutonomous()){
                drive.tankDrive(forwardSpeed, forwardSpeed);
            }

            Robot.gear.setDeployed(true);

            safeWait(10);

            drive.tankDrive(-backUpSpeed, -backUpSpeed);

            safeWait(500);

            drive.rotatePID.setTarget(drive.gyro.getHeading()-45);
            while(!drive.rotatePID.isDone() && !turnTimedOut && Robot.isEnabled() && Robot.isAutonomous()){
                safeWait(5, () -> turnTimedOut = true);
            }
            turnTimedOut = false;

            drive.tankDrive(forwardSpeed, forwardSpeed);

            safeWait(500);

            drive.rotatePID.setTarget(drive.gyro.getHeading()+45);
            while(!drive.rotatePID.isDone() && !turnTimedOut && Robot.isEnabled() && Robot.isAutonomous()){
                safeWait(5, () -> turnTimedOut = true);
            }
            turnTimedOut = false;
        }
        else {
            //side auto
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

    public void safeWait(long millis, Runnable r){
        try {
            Thread.sleep(millis);
        } catch (Exception e){
            r.run();
        }
    }
    public void safeWait(long millis){
        try {
            Thread.sleep(millis);
        } catch (Exception e){
            Log.d(e.getStackTrace().toString());
        }
    }

}
