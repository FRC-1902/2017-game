package com.explodingbacon.steambot.commands;

import com.explodingbacon.bcnlib.framework.Command;
import com.explodingbacon.bcnlib.framework.Log;
import com.explodingbacon.steambot.OI;
import com.explodingbacon.steambot.Robot;
import com.explodingbacon.steambot.subsystems.ShooterSubsystem;

public class ShooterCommand extends Command {
    ShooterSubsystem shooter;

    @Override
    public void onInit() {
        shooter = Robot.shooter;
    }

    @Override
    public void onLoop() {
        if (OI.shooterRev.get()) {
            shooter.rev();
        } else {
            shooter.stopRev();
        }
        shooter.shoot(); //checks for input, don't worry
    }

    @Override
    public void onStop() {

    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
