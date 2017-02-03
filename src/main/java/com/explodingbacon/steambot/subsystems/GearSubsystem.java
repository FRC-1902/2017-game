package com.explodingbacon.steambot.subsystems;

import com.explodingbacon.bcnlib.actuators.Motor;
import com.explodingbacon.bcnlib.actuators.Solenoid;
import com.explodingbacon.bcnlib.framework.Subsystem;
import com.explodingbacon.steambot.Map;

import java.util.List;

public class GearSubsystem extends Subsystem {
    private Solenoid lSol, rSol;
    //private Thread watchdogThread;

    public GearSubsystem() {
        lSol = new Solenoid(Map.LEFT_SOL);
        rSol = new Solenoid(Map.RIGHT_SOL);
    }


    @Override
    public void enabledInit() {
        lSol.set(false);
        rSol.set(false);
    }

    @Override
    public void disabledInit() {

    }

    @Override
    public void stop() {

    }

    @Override
    public List<Motor> getAllMotors() {
        return null;
    }

    public void setDeployed(boolean val){
        rSol.set(val);
        lSol.set(val);
    }

    public Solenoid getlSol(){return lSol;}
    public Solenoid getrSol(){return rSol;}
}
