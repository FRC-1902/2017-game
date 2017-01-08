package com.explodingbacon.steambot;

import com.explodingbacon.bcnlib.controllers.Joystick;
import com.explodingbacon.bcnlib.framework.AbstractOI;

public class OI extends AbstractOI {

    private static boolean isInit = false;

    private static Joystick joy;

    public OI() {
        init();
        start();
    }

    public static void init() {
        isInit = true;

        //TODO: set up joysticks and variables
        joy = new Joystick(0);
    }

    /**
     * Checks if OI has been initialized via OI.init().
     * @return If OI has been initialized via OI.init().
     */
    public static boolean isInit() {
        return isInit;
    }
}
