package com.explodingbacon.steambot;

import com.explodingbacon.bcnlib.actuators.MotorGroup;
import com.explodingbacon.bcnlib.controllers.XboxController;
import com.explodingbacon.bcnlib.framework.Command;

/**
 * Created by LenovoBacon1 on 2/2/2017.
 */
public class LiftCommand extends Command {
    private LiftSubsystem lift;
    private XboxController controller;

    private double liftPow = 0.5;
    private double limit = 9001; // > 9000

    private boolean liftToggle;

    @Override
    public void onInit() {
        lift = Robot.lift;
        controller = OI.manipulator;
        liftToggle = false;
    }

    @Override
    public void onLoop() {
        if(OI.liftStart.get() && lift.get() < limit && !liftToggle){
            liftToggle = true;
        } else if(OI.liftStop.get() && liftToggle){
            liftToggle = false;
        } else liftToggle = false;

        if(liftToggle){
            lift.set(liftPow);
        } else{
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
