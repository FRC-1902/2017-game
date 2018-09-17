package com.explodingbacon.steambot.commands;

import com.explodingbacon.bcnlib.framework.Command;
import com.explodingbacon.steambot.Robot;

public class BaselineAuto extends Command {

    @Override
    public void onInit() {
        long start = System.currentTimeMillis();
        while (Robot.isAutonomous() && Robot.isEnabled()) {
            if (System.currentTimeMillis() - start <= 2000) {
                if (Robot.MAIN_ROBOT) {
                    Robot.drive.codeFriendlyTankDrive(.9, .9);
                } else {
                    Robot.drive.fieldCentricAbsoluteAngleDrive(0, 0.9, 0);
                }
            } else {
                if (Robot.MAIN_ROBOT) {
                    Robot.drive.codeFriendlyTankDrive(0, 0);
                } else {
                    Robot.drive.fieldCentricAbsoluteAngleDrive(0, 0, 0);
                }
            }
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
        return true;
    }
}
