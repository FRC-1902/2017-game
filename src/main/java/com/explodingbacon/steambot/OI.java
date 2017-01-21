package com.explodingbacon.steambot;

import com.explodingbacon.bcnlib.controllers.Joystick;
import com.explodingbacon.bcnlib.controllers.XboxController;
import com.explodingbacon.bcnlib.framework.AbstractOI;

public class OI extends AbstractOI {

    private static boolean isInit = false;

    public static XboxController drive = null;


    public OI() {
        init();
        start();
    }

    public static void init() {
        isInit = true;

        drive = new XboxController(0);
    }

    /**
     * Checks if OI has been initialized via OI.init().
     * @return If OI has been initialized via OI.init().
     */
    public static boolean isInit() {
        return isInit;
    }
}
