package com.explodingbacon.steambot.commands;

import com.explodingbacon.bcnlib.framework.Command;
import com.explodingbacon.bcnlib.framework.Log;
import com.explodingbacon.steambot.Robot;
import com.explodingbacon.steambot.VisionThread;

import static com.explodingbacon.steambot.Robot.drive;
import static com.explodingbacon.steambot.Robot.useTouchplate;


public class DeadreckonAuto extends Command {

    //these are stolen from betterauto
    private final double offset = -2.5;
    private final double backUpSpeed = -0.35;
    boolean useTouchplate = Robot.useTouchplate.getSelected().toString().equalsIgnoreCase("true");

    @Override
    public void onInit() {
        boolean touched;
        long millis = System.currentTimeMillis();
        long timeElapsed;
        //VisionThread vision = Robot.visionThread;
        while (!(touched = Robot.gear.getTouchSensor()) && (timeElapsed = Math.abs(millis - System.currentTimeMillis())) <= 7000 && Robot.isAutonomous() && Robot.isEnabled()) {
            Log.v("Approaching peg");

            double speed;
            int accelTime = 1425;
            speed = timeElapsed >= accelTime ? 0.35 : 0.6; //.3 was good most of the time
            if (timeElapsed < 1000) speed = 0; //Let PID adjust for 1 second before moving

            if (speed == 0) {
                drive.keepHeading(0 + offset);
            } else {
                drive.fieldCentricAbsoluteAngleDrive(0, speed, 0 + offset);
            }
        }
        try {
            Thread.sleep(500); //was 500
        } catch (Exception e) {}
        if (!useTouchplate) touched = true;
        if (!Robot.gear.getDeployed() && touched) Robot.gear.setDeployed(true);
        try {
            Thread.sleep(250); //was 500
        } catch (Exception e) {}
        millis = System.currentTimeMillis();
        long diff;
        int backupTime = 1200;
        while ((diff = Math.abs(millis - System.currentTimeMillis())) <= backupTime && Robot.isAutonomous() && Robot.isEnabled()) {
            Log.v("Backing up");
            drive.xyzAbsoluteAngleDrive(0, backUpSpeed, 0);
        }
        while (Robot.isAutonomous() && Robot.isEnabled()) {
            drive.fieldCentricAbsoluteAngleDrive(0, 0, 0);
        }
    }

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
