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
import com.explodingbacon.bcnlib.vision.CameraSettings;
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
    public static SendableChooser baseLine;
    public static SendableChooser alliance;
   public static SendableChooser whichAutoClass;
    public static SendableChooser useTouchplate;
    public static SendableChooser shootInAuto;
    public static SendableChooser spamDrive;

    private boolean rezeroed = false;

    public static final boolean MAIN_ROBOT = true;
    public static final boolean VISION_TUNING = false;

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

        baseLine = new SendableChooser();
        baseLine.initTable(NetworkTable.getTable("BaconTable"));
        baseLine.addDefault("Yes", "yes");
        baseLine.addObject("No", "no");
        SmartDashboard.putData("Do Baseline after Auto", baseLine);

        alliance = new SendableChooser();
        alliance.initTable(NetworkTable.getTable("BaconTable"));
        alliance.addDefault("Blue", "blue");
        alliance.addObject("Red", "red");
        SmartDashboard.putData("Which Alliance", alliance);

        whichAutoClass = new SendableChooser();
        whichAutoClass.initTable(NetworkTable.getTable("BaconTable"));
        whichAutoClass.addDefault("Vision", "vision");
        whichAutoClass.addObject("Dead Reckon (Center Only)", "dead");
        whichAutoClass.addObject("Baseline Only", "baseline");
        SmartDashboard.putData("Which Auto File", whichAutoClass);

        useTouchplate = new SendableChooser();
        useTouchplate.addDefault("Use Touchplate", "true");
        useTouchplate.addObject("No Touchplate", "false");
        SmartDashboard.putData("Touchplate Option", useTouchplate);

        shootInAuto = new SendableChooser();
        shootInAuto.addDefault("Shoot Before Gear", "shoot");
        shootInAuto.addObject("No Shoot", "no");
        SmartDashboard.putData("Shoot Options", shootInAuto);

        spamDrive = new SendableChooser();
        spamDrive.addDefault("SPAM Drive - Left", "spam_left");
        spamDrive.addObject("SPAM Drive - Right", "spam_right");
        spamDrive.addObject("NO SPAM DRIVE", "no");
        SmartDashboard.putData("SPAM Drive Chooser", spamDrive);

        //source.inRange(new HSV(40, 100, 50), new HSV(100, 255, 255));
        if (VISION_TUNING) {
            VisionThread v = visionThread;
            SmartDashboard.putNumber("VisionHue_Low", v.hLow);
            SmartDashboard.putNumber("VisionSaturation_Low", v.sLow);
            SmartDashboard.putNumber("VisionValue_Low", v.vLow);

            SmartDashboard.putNumber("VisionHue_High", v.hHigh);
            SmartDashboard.putNumber("VisionSaturation_High", v.sHigh);
            SmartDashboard.putNumber("VisionValue_High", v.vHigh);
        }

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

        Log.i("Autonomous init!");

        CameraSettings.setExposure(VisionThread.VISION_EXPOSURE);

        String r = whichAutoClass.getSelected().toString();
        Log.d("Auto selected: " + r);

        if (r.equalsIgnoreCase("vision") || r.equalsIgnoreCase("dead")) {
            OI.runCommand(new BetterAuto());
            Log.a("Doing standard auto.");
        }/* else if (r.equalsIgnoreCase("dead")) {
            OI.runCommand(new DeadreckonAuto());
            Log.d("Doing dead-wreckoning auto.");
        }*/ else if (r.equalsIgnoreCase("baseline")) {
            OI.runCommand(new BaselineAuto());
            Log.d("Doing baseline auto.");
        }

        //OI.runCommand(new StreamlineAuto());
        //OI.runCommand(new BaselineAuto());
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

        //CameraSettings.setExposure(9);

        Log.i("Teleop init!");
    }

    @Override
    public void teleopPeriodic() {
        super.teleopPeriodic();
    }

    @Override
    public void disabledPeriodic() {
        super.disabledPeriodic();
        //Log.d("gyro: " + drive.gyro.getForPID());

        //Log.d("Encoder: " + drive.strafeEncoder.getForPID());
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
