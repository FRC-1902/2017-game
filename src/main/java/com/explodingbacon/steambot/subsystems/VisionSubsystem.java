package com.explodingbacon.steambot.subsystems;

import com.explodingbacon.bcnlib.actuators.Motor;
import com.explodingbacon.bcnlib.actuators.Solenoid;
import com.explodingbacon.bcnlib.framework.Subsystem;
import com.explodingbacon.steambot.Map;

import java.util.List;

/**
 * Created by LenovoBacon1 on 1/17/2017.
 */
public class VisionSubsystem extends Subsystem {

    private Solenoid ringLight;

    public VisionSubsystem() {
        ringLight = new Solenoid(Map.RINGLIGHT);
    }

    public boolean getRingLight() {
        return ringLight.get();
    }

    public void setRingLight(boolean on) {
        ringLight.set(on);
    }

    @Override
    public void enabledInit() {

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
}
