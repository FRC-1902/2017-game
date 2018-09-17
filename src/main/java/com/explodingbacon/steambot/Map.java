package com.explodingbacon.steambot;

public class Map {

    //PWM
    public static int LEFT_DRIVE_1 = Robot.MAIN_ROBOT ? 4 : 3;
    public static int LEFT_DRIVE_2 = Robot.MAIN_ROBOT ? 2 : 6;

    public static int RIGHT_DRIVE_1 = Robot.MAIN_ROBOT ? 3 : 8;
    public static int RIGHT_DRIVE_2 = Robot.MAIN_ROBOT ? 0 : 4;

    public static int STRAFE_DRIVE_1 = Robot.MAIN_ROBOT ? -1 : 2;
    public static int STRAFE_DRIVE_2 = Robot.MAIN_ROBOT ? -1 : 1;

    public static int LIFT_DRIVE_1 = Robot.MAIN_ROBOT ? 8 : 5;
    public static int LIFT_DRIVE_2 = Robot.MAIN_ROBOT ? 5 : 7;

    //Is CAN on the Real Robot, is PWM on practice robot
    public static int DISTURBER = Robot.MAIN_ROBOT ? 3 : 3;

    //CAN
    public static int SHOOTER_1 = 1;
    public static int SHOOTER_2 = 4;
    public static int INDEXER = 2;

    //DIO
    public static int STRAFE_ENC_A = Robot.MAIN_ROBOT ? 0 : 0;
    public static int STRAFE_ENC_B = Robot.MAIN_ROBOT ? 1 : 1;
    public static int GEAR_LIMIT = 4;

    //Solenoid
    public static int GEAR_SOLENOID = Robot.MAIN_ROBOT ? 0 : 0;
    public static int GEAR_SOLENOID_B = 7; //REAL ROBOT ONLY
    public static int RING_LIGHT = Robot.MAIN_ROBOT ? 6 : 4;

}