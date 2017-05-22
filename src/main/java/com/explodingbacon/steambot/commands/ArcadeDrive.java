package com.explodingbacon.steambot.commands;

import team254.utils.DriveSignal;
import team254.utils.CheesyUtil;

public class ArcadeDrive {

    private DriveSignal mSignal = new DriveSignal(0, 0);

    public DriveSignal arcadeDrive(double throttle, double wheel) {
        mSignal.leftMotor = CheesyUtil.limit(throttle + wheel, 1);
        mSignal.rightMotor = CheesyUtil.limit(throttle - wheel, 1);
        return mSignal;
    }
}