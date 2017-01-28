/**
 * CURRENTLY UNNAMED 2017 ROBOT -- (totally Pork Lift II though)
 *
 * This project was written and developed for the 2017 FIRST Robotics Competition game, "STEAMWORKS". All code used was
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

        Log.d("BNO sensor present: " + Robot.drive.gyro.isPresent());

        Log.d("Enabled init");
    }

    @Override
    public void enabledPeriodic() {
        super.enabledPeriodic();

        //Log.d("Strafe Encoder: " + Robot.drive.strafeEncoder.get());

        //Log.d("Gyro: " + Robot.drive.gyro.getHeading() + ", cal: " + Robot.drive.gyro.isCalibrated());

        //drive.rotatePID.logVerbose();

        /*
        Log.d("FrontLeft: " + Robot.drive.frontLeftEncoder.get()  + ", BackLeft: " + Robot.drive.backLeftEncoder.get() +
        ", FrontRight: " + Robot.drive.frontRightEncoder.get() + ", BackRight: " + Robot.drive.backRightEncoder.get());
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

        double testDistance = Robot.drive.inchesToStrafeEncoder(24);
        Log.d("Drive distance: " + testDistance);
        Robot.drive.set(0, 0, testDistance);
    }

    @Override
    public void autonomousPeriodic() {
        super.autonomousPeriodic();
        Robot.drive.strafePID.logVerbose();
    }

    @Override
    public void teleopInit() {
        super.teleopInit();

        OI.runCommand(new DriveCommand());

        Log.d("Teleop init");
    }

    @Override
    public void teleopPeriodic() {

    }

    @Override
    public void testInit() {
        super.testInit();
    }

    @Override
    public void testPeriodic() {
        super.testPeriodic();

        int angle = OI.drive.a.get() ? 90 : 270;
        Robot.drive.fieldCentricAbsoluteAngleDrive(0, 0, angle);
    }
}
