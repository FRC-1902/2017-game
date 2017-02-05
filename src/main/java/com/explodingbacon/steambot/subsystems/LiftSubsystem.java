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
    private PIDController liftPID;
    private Encoder liftEncoder;

    public LiftSubsystem() {
        liftMotors = new MotorGroup(VictorSP.class, Map.LIFT_DRIVE_1, Map.LIFT_DRIVE_2);
        liftMotors.setInverts(false, true);

        liftEncoder = new Encoder(Map.LIFT_ENC_A, Map.LIFT_ENC_B);
        liftEncoder.setPIDMode(AbstractEncoder.PIDMode.POSITION);

        liftPID = new PIDController(liftMotors, liftEncoder, 0, 0, 0);
    }

    @Override
    public void enabledInit() {
        liftEncoder.reset();
        liftPID.enable();
    }

    @Override
    public void disabledInit() {
        liftPID.disable();
    }

    @Override
    public void stop() {
        liftPID.disable();
    }

    @Override
    public List<Motor> getAllMotors() {
        return null;
    }

    public void set(double power){liftPID.setTarget(power);}

    public double get(){
        return liftEncoder.get();
    }
}
