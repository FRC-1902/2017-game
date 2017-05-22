package com.explodingbacon.steambot.positioning;

import com.explodingbacon.bcnlib.utils.CodeThread;
import com.explodingbacon.steambot.Robot;
import com.explodingbacon.steambot.subsystems.DriveSubsystem;
import edu.wpi.first.wpilibj.Timer;
import team254.utils.RigidTransform2d;
import team254.utils.Rotation2d;

/**
 * Periodically estimates the state of the robot using the robot's distance
 * traveled (compares two waypoints), gyroscope orientation, and velocity, among
 * various other factors. Similar to a car's odometer.
 */

public class RobotStateGenerator extends CodeThread {
    static RobotStateGenerator instance_ = new RobotStateGenerator();

    public static RobotStateGenerator getInstance() {
        return instance_;
    }

    RobotStateGenerator() {}

    RobotState robot_state_ = RobotState.getInstance();
    DriveSubsystem drive = Robot.drive;
    double left_encoder_prev_distance_ = 0;
    double right_encoder_prev_distance_ = 0;

    @Override
    public void start() {
        super.start();
        left_encoder_prev_distance_ = drive.getLeftDistanceInches();
        right_encoder_prev_distance_ = drive.getRightDistanceInches();
    }

    @Override
    public void code() {
        double time = Timer.getFPGATimestamp();
        double left_distance = drive.getLeftDistanceInches();
        double right_distance = drive.getRightDistanceInches();
        Rotation2d gyro_angle = Rotation2d.fromDegrees(drive.gyro.getForPID());

        RigidTransform2d odometry = robot_state_.generateOdometryFromSensors(left_distance - left_encoder_prev_distance_,
                right_distance - right_encoder_prev_distance_, gyro_angle);

        RigidTransform2d.Delta velocity = Kinematics.forwardKinematics(drive.getLeftVelocityInchesPerSec(),
                drive.getRightVelocityInchesPerSec());

        robot_state_.addObservations(time, odometry, velocity);
        left_encoder_prev_distance_ = left_distance;
        right_encoder_prev_distance_ = right_distance;
    }
}
