package com.explodingbacon.steambot.commands;

import com.explodingbacon.bcnlib.framework.Command;
import com.explodingbacon.steambot.OI;
import com.explodingbacon.steambot.Robot;
import com.explodingbacon.steambot.subsystems.GearSubsystem;

public class GearCommand extends Command {
    private GearSubsystem gearSubsystem;

    @Override
    public void onInit() {
        gearSubsystem = Robot.gear;
    }

    @Override
    public void onLoop() {
        boolean pressed = gearSubsystem.getTouchSensor();
        //if (Robot.isEnabled()) Log.d("Pressed: " + pressed);
        if (Robot.isTeleop()) {
            if ((pressed && OI.allowPressureGear.get())) {
                //Log.d("ON");
                gearSubsystem.setDeployed(true);
                OI.drive.rumble(0.2f, 0.2f);
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {}
            } else {
                if(OI.gear.get() || OI.manipGear.get()) {
                    OI.drive.rumble(0.2f, 0.2f);
                    gearSubsystem.setDeployed(true);
                } else {
                    OI.drive.rumble(0, 0);
                    gearSubsystem.setDeployed(false);
                }
            }
        } else if (Robot.isAutonomous()) {
            if (pressed) {
                //Log.d("PRESSURE PAD PRESSED");
                gearSubsystem.setDeployed(true);
                /*
                Utils.runInOwnThread(() -> {
                    try {
                        Thread.sleep(1500);
                    } catch (Exception e) {}
                    gearSubsystem.setDeployed(false);
                });
                */
            }
        }
    }

    @Override
    public void onStop() {

    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
