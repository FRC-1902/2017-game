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
import com.explodingbacon.steambot.commands.AutonomousCommand;
import com.explodingbacon.steambot.commands.DriveCommand;
import com.explodingbacon.steambot.subsystems.DriveSubsystem;
import com.explodingbacon.steambot.subsystems.VisionSubsystem;
import edu.wpi.first.wpilibj.IterativeRobot;

public class Robot extends RobotCore {

    private OI oi;
    public static DriveSubsystem drive;
    public static VisionSubsystem vision;
    public static VisionThread visionThread = new VisionThread();
    public static PositionLogThread positionLog = new PositionLogThread();

    public Robot(IterativeRobot r) {
        super(r);

        oi = new OI();

        Vision.init();

        drive = new DriveSubsystem();
        vision = new VisionSubsystem();

        if (Vision.isInit()) visionThread.start();

        positionLog.start();

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

        //drive.strafePID.log();

        /*
        Log.d("LeftMotors speed: " + fL.getMotorPower() + ", Error: " + fL.getCurrentError() +
        ", done: " + fL.isDone() + ", enabled: " + fL.isEnabled());
        */
    }

    @Override
    public void autonomousInit() {
        super.autonomousInit();

        OI.runCommand(new AutonomousCommand());

        Log.d("Autonomous init");
    }

    @Override
    public void autonomousPeriodic() {
        super.autonomousPeriodic();
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
        drive.strafePID.disable();
        drive.strafePID.resetSource();
        drive.strafePID.enable();
        drive.strafePID.setTarget(drive.inchesToStrafeEncoder(6));
    }

    @Override
    public void testPeriodic() {
        super.testPeriodic();

        drive.keepHeading(0);
    }
}
