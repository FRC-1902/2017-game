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
import com.explodingbacon.steambot.subsystems.DriveSubsystem;
import com.explodingbacon.steambot.subsystems.GearSubsystem;
import com.explodingbacon.steambot.subsystems.LiftSubsystem;
import com.explodingbacon.steambot.subsystems.ShooterSubsystem;
import com.explodingbacon.steambot.subsystems.VisionSubsystem;
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

    public Robot(IterativeRobot r) {
        super(r);

        oi = new OI();

        Vision.init();

        drive = new DriveSubsystem();
        vision = new VisionSubsystem();
        gear = new GearSubsystem();
        lift = new LiftSubsystem();
        //shooter = new ShooterSubsystem();

        if (Vision.isInit()) visionThread.start();

        positionLog.start();

        auto = new SendableChooser();
        auto.initTable(NetworkTable.getTable("BaconTable"));
        auto.addDefault("Front", "front");
        auto.addObject("Left", "left");
        auto.addObject("Right", "right");
        SmartDashboard.putData("Autonomous Picker", auto);

        Log.i("Air Pork One initialized.");
    }

    @Override
    public void enabledInit() {
        super.enabledInit();

        OI.deleteAllTriggers();

        vision.setRingLight(true);

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

        OI.runCommand(new GearCommand());
        OI.runCommand(new AutoOne());

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
        //OI.runCommand(new ShooterCommand());

        Log.i("Teleop init");
    }

    @Override
    public void teleopPeriodic() {}

    @Override
    public void disabledPeriodic() {}

    @Override
    public void testInit() {
        super.testInit();
    }

    @Override
    public void testPeriodic() {
        super.testPeriodic();
    }
}
