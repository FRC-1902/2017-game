package com.explodingbacon.steambot.commands;

import com.explodingbacon.bcnlib.controllers.XboxController;
import com.explodingbacon.bcnlib.framework.Command;
import com.explodingbacon.bcnlib.sensors.DigitalInput;
import com.explodingbacon.steambot.Map;
import com.explodingbacon.steambot.OI;
import com.explodingbacon.steambot.Robot;
import com.explodingbacon.steambot.subsystems.GearSubsystem;

public class GearCommand extends Command {
    private XboxController controller;
    private GearSubsystem gearSubsystem;
    private DigitalInput limit;

    @Override
    public void onInit() {
        controller = OI.manipulator;
        limit = new DigitalInput(Map.GEAR_LIMIT);

        gearSubsystem = Robot.gear;
    }

    @Override
    public void onLoop() {
        if(limit.get() || OI.gear.get()) gearSubsystem.setDeployed(true);
        else gearSubsystem.setDeployed(false);
    }

    @Override
    public void onStop() {

    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
