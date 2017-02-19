package com.explodingbacon.steambot.subsystems;

import com.explodingbacon.bcnlib.actuators.Motor;
import com.explodingbacon.bcnlib.actuators.Solenoid;
import com.explodingbacon.bcnlib.framework.Subsystem;
import com.explodingbacon.bcnlib.sensors.DigitalInput;
import com.explodingbacon.steambot.Map;
import java.util.List;

public class GearSubsystem extends Subsystem {
    private Solenoid lSol;
    public DigitalInput touch;

    public GearSubsystem() {
        lSol = new Solenoid(Map.LEFT_SOL);
        touch = new DigitalInput(Map.GEAR_LIMIT);
    }


    @Override
    public void enabledInit() {
        setDeployed(false);
    }

    @Override
    public void disabledInit() {}

    @Override
    public void stop() {
        setDeployed(false);
    }

    @Override
    public List<Motor> getAllMotors() {
        return null;
    }

    public boolean getTouchSensor() {
        return touch.get();
    }

    public void setDeployed(boolean val){
        //val = !val;
        lSol.set(val);
    }

    public Solenoid getSol(){return lSol;}
}
