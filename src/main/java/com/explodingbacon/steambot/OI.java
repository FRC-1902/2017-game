package com.explodingbacon.steambot;

import com.explodingbacon.bcnlib.controllers.Joystick;
import com.explodingbacon.bcnlib.framework.AbstractOI;

public class OI extends AbstractOI {

    private static boolean isInit = false;

    public static Joystick drive = null;
    public static Joystick turn = null;

    public OI() {
        init();
        start();
    }

    public static void init() {
        isInit = true;

        drive = new Joystick(0);
        turn = new Joystick(1);
    }

    /**
     * Checks if OI has been initialized via OI.init().
     * @return If OI has been initialized via OI.init().
     */
    public static boolean isInit() {
        return isInit;
    }
}
