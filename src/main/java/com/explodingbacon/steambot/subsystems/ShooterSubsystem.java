package com.explodingbacon.steambot.subsystems;

import com.ctre.CANTalon;
import com.explodingbacon.bcnlib.actuators.Motor;
import com.explodingbacon.bcnlib.actuators.MotorGroup;
import com.explodingbacon.bcnlib.framework.PIDController;
import com.explodingbacon.bcnlib.framework.Subsystem;
import com.explodingbacon.bcnlib.sensors.AbstractEncoder;
import com.explodingbacon.steambot.Map;
import com.explodingbacon.steambot.OI;
import com.explodingbacon.steambot.Robot;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import java.util.List;

public class ShooterSubsystem extends Subsystem {
    private MotorGroup shooter;
    private Motor disturber, indexer;
    public AbstractEncoder shootEncoder;
    public PIDController shootPID;

   //TODO: tune
    private double indexPow = 0.5;
    private double disturberPow = 1;

    //private final double SHOOTER_RPM = 80000; //TODO: tune

    public ShooterSubsystem() {
        Class disturberClass = Robot.MAIN_ROBOT ? CANTalon.class : VictorSP.class;
        disturber = new Motor(disturberClass, Map.DISTURBER);
        indexer = new Motor(CANTalon.class, Map.INDEXER);

        shooter = new MotorGroup(CANTalon.class, Map.SHOOTER_1, Map.SHOOTER_2);
        if (Robot.MAIN_ROBOT) {
            shooter.setReversed(true);
        }
        //shooter.setInverts(false, false);

        shootEncoder = shooter.getMotors().get(0).getEncoder();
        shootEncoder.setPIDMode(AbstractEncoder.PIDMode.RATE);

        //shootEncoder = new Encoder(Map.SHOOT_ENC_A, Map.SHOOT_ENC_B);
        shootPID = new PIDController(shooter, shootEncoder, 0.00002 / 2, .00000085, .00001, 0.1, 1); //TODO: tune

        shootPID.setFinishedTolerance(400); //600

        shootPID.setExtraCode(() -> {
            if (Robot.shooter.shootPID.isEnabled() && Robot.shooter.shootPID.isDone()) {
                OI.manipulator.rumble(.2f, .2f);
            } else {
                OI.manipulator.rumble(0f, 0f);
            }
        });
    }

    @Override
    public void enabledInit() {
        shootEncoder.reset();
        //shootPID.enable();
    }

    @Override
    public void disabledInit() {shootPID.disable();}

    @Override
    public void stop() {shootPID.disable();}

    @Override
    public List<Motor> getAllMotors() {return null;}

    public void rev() {
        if(!shootPID.isEnabled()) shootPID.enable();
        shootPID.setTarget(SmartDashboard.getNumber("Shoot Speed", 80000));
    }

    public void stopRev() {
        shootPID.disable();
        //shootPID.setTarget(0);
    }

    private long boopStart = 0;
    public void shoot(){
        if (shootPID.isEnabled()) {
            if (shootPID.isDone() && OI.shooterRev.get()) {
                disturber.setPower(disturberPow);
                indexer.setPower(indexPow);
                boopStart = System.currentTimeMillis();
            }
        }
        if (System.currentTimeMillis() - boopStart >= 500 || !shootPID.isEnabled()) {
            disturber.setPower(0);
            indexer.setPower(0);
        }
    }

    public MotorGroup getShooter() {
        return shooter;
    }

    public Motor getDisturber() {
        return disturber;
    }

    public Motor getIndexer() {
        return indexer;
    }

    public void disable() {
        if(shootPID.isEnabled()) shootPID.disable();
        shooter.setPower(0);
        disturber.setPower(0);
        indexer.setPower(0);
    }
}
