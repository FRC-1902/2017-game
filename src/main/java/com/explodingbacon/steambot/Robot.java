package com.explodingbacon.steambot;

import com.explodingbacon.bcnlib.framework.Log;
import com.explodingbacon.bcnlib.framework.RobotCore;
import com.explodingbacon.bcnlib.vision.CameraSettings;
import com.explodingbacon.bcnlib.vision.Vision;
import com.explodingbacon.steambot.commands.DriveCommand;
import com.explodingbacon.steambot.subsystems.DriveSubsystem;
import edu.wpi.first.wpilibj.IterativeRobot;

public class Robot extends RobotCore {

    private OI oi;
    public static DriveSubsystem drive;
    private VisionThread vision = new VisionThread();

    public Robot(IterativeRobot r) {
        super(r);

        oi = new OI();

        Vision.init();

        if (Vision.isInit()) vision.start();

        drive = new DriveSubsystem();

        Log.i("Pork Lift II initialized.");
    }

    @Override
    public void enabledInit() {
        super.enabledInit();

        OI.deleteAllTriggers();

        Log.d("Enabled init");
    }

    @Override
    public void autonomousInit() {
        super.autonomousInit();

        Log.d("Autonomous init");
    }

    @Override
    public void teleopInit() {
        super.teleopInit();

        OI.runCommand(new DriveCommand());

        Log.d("Teleop init");
    }
}
