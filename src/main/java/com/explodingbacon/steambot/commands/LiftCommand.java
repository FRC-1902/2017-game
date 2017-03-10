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
        lift.set(OI.liftStart.get() ? 1 : 0);
    }

    @Override
    public void onStop() {

    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
