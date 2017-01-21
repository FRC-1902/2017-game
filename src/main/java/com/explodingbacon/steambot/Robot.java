package com.explodingbacon.steambot;

import com.explodingbacon.bcnlib.framework.Log;
import com.explodingbacon.bcnlib.framework.RobotCore;
import com.explodingbacon.bcnlib.vision.Vision;
import com.explodingbacon.steambot.commands.DriveCommand;
import com.explodingbacon.steambot.subsystems.DriveSubsystem;
import com.explodingbacon.steambot.subsystems.VisionSubsystem;
import edu.wpi.first.wpilibj.IterativeRobot;

public class Robot extends RobotCore {

    private OI oi;
    public static DriveSubsystem drive;
    public static VisionSubsystem vision;
    private VisionThread visionThread = new VisionThread();

    public Robot(IterativeRobot r) {
        super(r);

        oi = new OI();

        Vision.init();

        drive = new DriveSubsystem();
        vision = new VisionSubsystem();

        if (Vision.isInit()) visionThread.start();

        Log.i("Pork Lift II initialized.");
    }

    @Override
    public void enabledInit() {
        super.enabledInit();

        OI.deleteAllTriggers();

        vision.setRingLight(true);

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

        /*
        try {
            Robot.drive.getLeftMotors().testEachWait(0.3, 0.3);
            Thread.sleep(1000);
            Robot.drive.getRightMotors().testEachWait(0.3, 0.3);
            Thread.sleep(1000);
            Robot.drive.getStrafeMotors().testEachWait(0.3, 0.3);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void teleopPeriodic() {

    }
}
