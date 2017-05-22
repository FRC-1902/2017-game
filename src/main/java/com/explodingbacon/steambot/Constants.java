package com.explodingbacon.steambot;

public class Constants {

    //Wheel Stuff
    public static double wheelDiameterInches = 4;
    //The rest of these are wrong
    public static double kTrackLengthInches = 8.265;
    public static double kTrackWidthInches = 23.8;
    public static double kTrackEffectiveDiameter = (kTrackWidthInches * kTrackWidthInches + kTrackLengthInches * kTrackLengthInches) / kTrackWidthInches;
    public static double kTrackScrubFactor = 0.5;

    // Path following constants
    public static double pathFollowLookahead = 24.0; // inches
    public static double pathFollowMaxVel = 120.0; // inches/sec
    public static double pathFollowMaxAccel = 80.0; // inches/sec^2

    //what is this one's purpose?
    public static double kLooperDt = 0.01;

}
