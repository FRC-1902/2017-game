package com.explodingbacon.steambot;

import com.explodingbacon.bcnlib.controllers.Button;
import com.explodingbacon.bcnlib.controllers.JoystickButton;
import com.explodingbacon.bcnlib.controllers.XboxController;
import com.explodingbacon.bcnlib.framework.AbstractOI;

public class OI extends AbstractOI {

    private static boolean isInit = false;

    public static XboxController drive = null;
    public static XboxController manipulator = null;

    public static Button gear;
    public static Button liftStart, liftStop;

    public OI() {
        init();
        start();
    }

    public static void init() {
        isInit = true;

        drive = new XboxController(0);
        manipulator = new XboxController(1);

        gear = manipulator.rightTrigger;
        liftStart = manipulator.a;
        liftStop = manipulator.y;
    }

    /**
     * Checks if OI has been initialized via OI.init().
     *
     * @return If OI has been initialized via OI.init().
     */
    public static boolean isInit() {
        return isInit;
    }
}
