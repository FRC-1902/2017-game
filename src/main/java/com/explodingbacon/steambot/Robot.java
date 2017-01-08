package com.explodingbacon.steambot;

import com.explodingbacon.bcnlib.framework.Log;
import com.explodingbacon.bcnlib.framework.RobotCore;
import com.explodingbacon.bcnlib.vision.Vision;
import com.explodingbacon.steambot.commands.VisionCommand;
import edu.wpi.first.wpilibj.IterativeRobot;

public class Robot extends RobotCore {

    private OI oi;

    public Robot(IterativeRobot r) {
        super(r);

        oi = new OI();

        Vision.init();
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

        if (Vision.isInit()) OI.runCommand(new VisionCommand());

        Log.d("Autonomous init");
    }

    @Override
    public void teleopInit() {
        super.teleopInit();

        if (Vision.isInit()) OI.runCommand(new VisionCommand());

        Log.d("Teleop init");
    }
}
