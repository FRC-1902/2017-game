package com.explodingbacon.steambot.commands;

import com.explodingbacon.bcnlib.utils.Utils;
import com.explodingbacon.steambot.OI;

public class CheesyDrive {

    double mQuickStopAccumulator;

    //Tuning
    public static final double kThrottleDeadband = 0.02;
    private static final double kWheelDeadband = 0.02;
    private static final double kTurnSensitivity = 1.0;

    public DriveOrder calculate(double throttle, double wheel) {
        //double throttle = g.getY();
        //double wheel = g.getX2();

        throttle = (float) Math.pow(throttle, 4) * Utils.sign(throttle);
        wheel = (float) Math.pow(wheel, 4) * Utils.sign(wheel);

        boolean isQuickTurn = throttle < 0.1;

        wheel = handleDeadband(wheel, kWheelDeadband);
        throttle = handleDeadband(throttle, kThrottleDeadband);

        double overPower;

        double angularPower;

        if (!OI.drive.isLeftTriggerPressed()) wheel *= .75;

        if (isQuickTurn) {
            if (Math.abs(throttle) < 0.2) {
                float alpha = 0.1f;
                mQuickStopAccumulator = (1f - alpha) * mQuickStopAccumulator + alpha * Utils.cap(wheel, 1.0f) * 2;
            }
            overPower = 1.0f;
            angularPower = wheel;
        } else {
            overPower = 0.0f;
            angularPower = Math.abs(throttle) * wheel * kTurnSensitivity - mQuickStopAccumulator;
            if (mQuickStopAccumulator > 1) {
                mQuickStopAccumulator -= 1;
            } else if (mQuickStopAccumulator < -1) {
                mQuickStopAccumulator += 1;
            } else {
                mQuickStopAccumulator = 0.0f;
            }
        }

        double rightPwm = throttle - angularPower;
        double leftPwm = throttle + angularPower;
        if (leftPwm > 1.0) {
            rightPwm -= overPower * (leftPwm - 1.0);
            leftPwm = 1.0f;
        } else if (rightPwm > 1.0) {
            leftPwm -= overPower * (rightPwm - 1.0);
            rightPwm = 1.0f;
        } else if (leftPwm < -1.0) {
            rightPwm += overPower * (-1.0 - leftPwm);
            leftPwm = -1.0f;
        } else if (rightPwm < -1.0) {
            leftPwm += overPower * (-1.0 - rightPwm);
            rightPwm = -1.0f;
        }
        return new DriveOrder(leftPwm, rightPwm);
    }

    public double handleDeadband(double val, double deadband) {
        return (Math.abs(val) > Math.abs(deadband)) ? val : 0.0f;
    }
}
