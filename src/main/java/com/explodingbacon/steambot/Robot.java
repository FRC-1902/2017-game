/**
      ___    _         ____             __      ____
    /   |  (_)____   / __ \____  _____/ /__   / __ \____  ___
   / /| | / / ___/  / /_/ / __ \/ ___/ //_/  / / / / __ \/ _ \
  / ___ |/ / /     / ____/ /_/ / /  / ,<    / /_/ / / / /  __/
 /_/  |_/_/_/     /_/    \____/_/  /_/|_|   \____/_/ /_/\___/

 *
 * This project was written and developed for the 2017 FIRST Robotics Competition game, "STEAMWORKS". All code used was
 * either written by team 1902 and/or was open-source and available to all teams.
 *
 * Written by:
 *
 * Ryan Shavell
 * Dominic Canora
 * Varun A.
 * Ruth Pearl
 * Adam C.
 */

package com.explodingbacon.steambot;

import com.explodingbacon.bcnlib.framework.Log;
import com.explodingbacon.bcnlib.framework.RobotCore;
import com.explodingbacon.bcnlib.vision.Vision;
import com.explodingbacon.steambot.commands.*;
import com.explodingbacon.steambot.subsystems.*;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends RobotCore {

    private OI oi;
    public static DriveSubsystem drive;
    public static VisionSubsystem vision;
    public static GearSubsystem gear;
    public static LiftSubsystem lift;
    public static ShooterSubsystem shooter;

    public static VisionThread visionThread = new VisionThread();
    public static PositionLogThread positionLog = new PositionLogThread();
    public static SendableChooser auto;

    private boolean rezeroed = false;

    public static final boolean MAIN_ROBOT = false;

    public Robot(IterativeRobot r) {
        super(r);

        oi = new OI();

        Vision.init();

        drive = new DriveSubsystem();
        vision = new VisionSubsystem();
        gear = new GearSubsystem();
        lift = new LiftSubsystem();
        shooter = new ShooterSubsystem();

        if (Vision.isInit()) visionThread.start();

        positionLog.start();

        SmartDashboard.putNumber("Shoot Speed", 88000);

        auto = new SendableChooser();
        auto.initTable(NetworkTable.getTable("BaconTable"));
        auto.addDefault("Front", "front");
        auto.addObject("Left", "left");
        auto.addObject("Right", "right");
        SmartDashboard.putData("Autonomous Picker", auto);

        Log.i("Air Pork " + (MAIN_ROBOT ? "One" : "Too") + " initialized.");
        if (!MAIN_ROBOT) Log.w("ROBOT IN PRACTICE MODE!");
    }

    @Override
    public void enabledInit() {
        super.enabledInit();

        OI.deleteAllTriggers();

        vision.setRingLight(true);

        if (Robot.drive.gyro.isPresent() && !rezeroed) {
            Robot.drive.gyro.rezero();
            rezeroed = true;
        }

        Log.d("BNO sensor present: " + Robot.drive.gyro.isPresent());

        Log.d("Enabled init");
    }

    @Override
    public void enabledPeriodic() {
        super.enabledPeriodic();
    }

    @Override
    public void autonomousInit() {
        super.autonomousInit();

        OI.runCommand(new StreamlineAuto());

        Log.i("Autonomous init!");
    }

    @Override
    public void autonomousPeriodic() {
        super.autonomousPeriodic();
    }

    @Override
    public void teleopInit() {
        super.teleopInit();

        OI.runCommand(new DriveCommand());
        OI.runCommand(new GearCommand());
        OI.runCommand(new LiftCommand());
        OI.runCommand(new ShooterCommand());

        Log.i("Teleop init!");
    }

    @Override
    public void teleopPeriodic() {
        super.teleopPeriodic();
        shooter.shootPID.logVerbose();
    }

    @Override
    public void disabledPeriodic() {
        super.disabledPeriodic();
    }

    @Override
    public void testInit() {
        super.testInit();
        shooter.getIndexer().setPower(0.5);
        try {
            Thread.sleep(500);
        } catch (Exception e) {}
        shooter.getIndexer().setPower(0);
        shooter.getDisturber().setPower(0.5);
        try {
            Thread.sleep(500);
        } catch (Exception e) {}
        shooter.getDisturber().setPower(0);
    }

    @Override
    public void testPeriodic() {
        super.testPeriodic();
    }
}
