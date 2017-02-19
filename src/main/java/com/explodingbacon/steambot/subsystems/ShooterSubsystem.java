package com.explodingbacon.steambot.subsystems;

import com.ctre.CANTalon;
import com.explodingbacon.bcnlib.actuators.Motor;
import com.explodingbacon.bcnlib.actuators.MotorGroup;
import com.explodingbacon.bcnlib.framework.Log;
import com.explodingbacon.bcnlib.framework.PIDController;
import com.explodingbacon.bcnlib.framework.Subsystem;
import com.explodingbacon.bcnlib.sensors.Encoder;
import com.explodingbacon.steambot.Map;
import com.explodingbacon.steambot.OI;
import edu.wpi.first.wpilibj.VictorSP;

import java.util.List;

public class ShooterSubsystem extends Subsystem {
    private MotorGroup shooter;
    private Motor disturber, indexer;
    private Encoder shootEncoder;
    private PIDController shootPID;

   //TODO: tune
    private double indexPow = 0.5;
    private double disturberPow = 0.5;

    private final double SHOOTER_RPM = 90000; //TODO: tune

    public ShooterSubsystem() {
        disturber = new Motor(VictorSP.class, Map.DISTURBER);
        indexer = new Motor(CANTalon.class, Map.INDEXER);

        shooter = new MotorGroup(CANTalon.class, Map.SHOOTER_1, Map.SHOOTER_2);

        shootEncoder = new Encoder(Map.SHOOT_ENC_A, Map.SHOOT_ENC_B);
        shootPID = new PIDController(shooter, shootEncoder, 0, 0, 0); //TODO: tune
    }

    @Override
    public void enabledInit() {
        shootEncoder.reset();
        shootPID.enable();
    }

    @Override
    public void disabledInit() {shootPID.disable();}

    @Override
    public void stop() {shootPID.disable();}

    @Override
    public List<Motor> getAllMotors() {return null;}

    public void rev() {
        if(!shootPID.isEnabled()) shootPID.enable();
        shootPID.setTarget(SHOOTER_RPM);
    }

    public void stopRev() {
        shootPID.disable();
        shootPID.setTarget(0);
    }

    public void shoot(){
        if(shootPID.isDone()){
            OI.manipulator.rumble(.5f, .5f);
            disturber.setPower(disturberPow);
            indexer.setPower(indexPow);
        } else {
            OI.manipulator.rumble(0, 0);
            Log.d("Shooter error: " + shootPID.getCurrentError());
            disturber.setPower(0);
            indexer.setPower(0);
            //shootPID.disable();
            //shootPID.setTarget(0);
        }
    }

    public void disable() {
        if(shootPID.isEnabled()) shootPID.disable();
        disturber.setPower(0);
        indexer.setPower(0);
    }
}
