package com.explodingbacon.steambot.commands;

import com.explodingbacon.bcnlib.framework.Command;
import com.explodingbacon.steambot.OI;
import com.explodingbacon.steambot.Robot;
import com.explodingbacon.steambot.subsystems.LiftSubsystem;

public class LiftCommand extends Command {
    private LiftSubsystem lift;

    @Override
    public void onInit() {
        lift = Robot.lift;
    }

    @Override
    public void onLoop() {
        if (OI.liftFast.get()) {
            lift.set(1);
        } else if (OI.liftSlow.get()) {
            lift.set(.8);
        } else {
            lift.set(0);
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
