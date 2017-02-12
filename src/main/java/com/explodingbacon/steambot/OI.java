package com.explodingbacon.steambot;

import com.explodingbacon.bcnlib.controllers.Button;
import com.explodingbacon.bcnlib.controllers.JoystickButton;
import com.explodingbacon.bcnlib.controllers.LogitechController;
import com.explodingbacon.bcnlib.controllers.XboxController;
import com.explodingbacon.bcnlib.framework.AbstractOI;

import javax.xml.ws.handler.LogicalHandler;

public class OI extends AbstractOI {

    private static boolean isInit = false;

    public static LogitechController drive = null;
    public static XboxController manipulator = null;

    public static Button gear, allowPressureGear;
    public static Button liftStart, liftStop;
    public static Button shoot;

    public OI() {
        init();
        start();
    }

    public static void init() {
        isInit = true;

        drive = new LogitechController(0);
        manipulator = new XboxController(1); //port 1

        gear = drive.rightTrigger;

        allowPressureGear = manipulator.rightTrigger;

        liftStart = manipulator.a;
        liftStop = manipulator.y;
        shoot = manipulator.b;
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
