package com.explodingbacon.steambot.subsystems;

import com.explodingbacon.bcnlib.actuators.DoubleSolenoid;
import com.explodingbacon.bcnlib.actuators.Motor;
import com.explodingbacon.bcnlib.actuators.Solenoid;
import com.explodingbacon.bcnlib.actuators.SolenoidInterface;
import com.explodingbacon.bcnlib.framework.Subsystem;
import com.explodingbacon.bcnlib.sensors.DigitalInput;
import com.explodingbacon.steambot.Map;
import com.explodingbacon.steambot.Robot;

import java.util.List;

public class GearSubsystem extends Subsystem {
    private SolenoidInterface lSol;
    public DigitalInput touch;

    public GearSubsystem() {
        if (Robot.MAIN_ROBOT) {
            lSol = new DoubleSolenoid(Map.GEAR_SOLENOID, Map.GEAR_SOLENOID_B);
        } else {
            lSol = new Solenoid(Map.GEAR_SOLENOID);
        }
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
        boolean status = touch.get();
        //if (Robot.MAIN_ROBOT) status = !status;
        return status;
    }

    public void setDeployed(boolean val){
        //val = !val;
        lSol.set(val);
    }

    public boolean getDeployed() {
        return lSol.get();
    }

    public SolenoidInterface getSol(){return lSol;}
}
