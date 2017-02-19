package com.explodingbacon.steambot.subsystems;

import com.explodingbacon.bcnlib.actuators.Motor;
import com.explodingbacon.bcnlib.actuators.MotorGroup;
import com.explodingbacon.bcnlib.framework.PIDController;
import com.explodingbacon.bcnlib.framework.Subsystem;
import com.explodingbacon.bcnlib.sensors.AbstractEncoder;
import com.explodingbacon.bcnlib.sensors.Encoder;
import com.explodingbacon.steambot.Map;
import edu.wpi.first.wpilibj.VictorSP;
import java.util.List;

public class LiftSubsystem extends Subsystem {
    private MotorGroup liftMotors;

    public LiftSubsystem() {
        liftMotors = new MotorGroup(VictorSP.class, Map.LIFT_DRIVE_1, Map.LIFT_DRIVE_2);
        liftMotors.setInverts(true, false);
    }

    @Override
    public void enabledInit() {
    }

    @Override
    public void disabledInit() {
        liftMotors.setPower(0);
    }

    public MotorGroup getLiftMotors() {
        return liftMotors;
    }

    @Override
    public void stop() {
        liftMotors.setPower(0);
    }

    @Override
    public List<Motor> getAllMotors() {
        return null;
    }

    public void set(double power){liftMotors.setPower(power);}
}
