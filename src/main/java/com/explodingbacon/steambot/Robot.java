/**
 * CURRENTLY UNNAMED 2017 ROBOT -- (totally Pork Lift II though)
 *
 * This project was written and developed for the 2017 FIRST Robotics Competition game, "STEAMWorks". All code used was
 * either written by team 1902 and/or is open-source and available to all teams.
 *
 * Written by:
 *
 * Ryan Shavell
 * Dominic Canora
 * Ruth Pearl
 * Adam C.
 * Varun A.
 */

package com.explodingbacon.steambot;

import com.explodingbacon.bcnlib.framework.Log;
import com.explodingbacon.bcnlib.framework.PIDController;
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
    public void enabledPeriodic() {
        super.enabledPeriodic();

        Log.d("FrontLeft: " + Robot.drive.frontLeftEncoder.get()  + ", BackLeft: " + Robot.drive.backLeftEncoder.get() +
        ", FrontRight: " + Robot.drive.frontRightEncoder.get() + ", BackRight: " + Robot.drive.backRightEncoder.get());


        PIDController fL = Robot.drive.frontLeftPID;
        PIDController bL= Robot.drive.backLeftPID;
        PIDController fR = Robot.drive.frontRightPID;
        PIDController bR = Robot.drive.backRightPID;

        //Log.d("Encoder: " + Robot.drive.frontLeftEncoder.get());

        /*
        Log.d("FrontLeft: " + fL.getCurrentError() + "/" + fL.getTarget() + ", BackLeft: " + bL.getCurrentError() + "/" +
        bL.getTarget() + ", FrontRight: " + fR.getCurrentError() + "/" + fR.getTarget() + ", BackRight: " + bR.getCurrentError() +
        "/" + bR.getTarget());
        */

        /*
        Log.d("LeftMotors speed: " + fL.getMotorPower() + ", Error: " + fL.getCurrentError() +
        ", done: " + fL.isDone() + ", enabled: " + fL.isEnabled());
        */
    }

    @Override
    public void autonomousInit() {
        super.autonomousInit();

        Log.d("Autonomous init");

        /*
        double testDistance = Robot.drive.inchesToDriveEncoder(24);
        Log.d("Drive distance: " + testDistance);
        Robot.drive.set(testDistance, testDistance, 0);
        */

        Robot.drive.getLeftMotors().testEachWait(0.5, 0.5);
        Robot.drive.getRightMotors().testEachWait(0.5, 0.5);
        Robot.drive.getStrafeMotors().testEachWait(0.7, 0.5);
    }

    @Override
    public void teleopInit() {
        super.teleopInit();

        //OI.runCommand(new DriveCommand());

        Log.d("Teleop init");
    }
}
