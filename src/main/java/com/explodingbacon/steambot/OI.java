package com.explodingbacon.steambot;

import com.explodingbacon.bcnlib.controllers.Button;
import com.explodingbacon.bcnlib.controllers.JoystickButton;
import com.explodingbacon.bcnlib.controllers.LogitechController;
import com.explodingbacon.bcnlib.controllers.XboxController;
import com.explodingbacon.bcnlib.framework.AbstractOI;

import javax.xml.ws.handler.LogicalHandler;

public class OI extends AbstractOI {

    private static boolean isInit = false;

    public static XboxController drive = null;
    public static XboxController manipulator = null;

    public static Button slowButton;
    public static Button rezero;
    public static Button gear, manipGear, allowPressureGear;
    public static Button liftStart;
    public static Button shoot;

    public OI() {
        init();
        start();
    }

    public static void init() {
        isInit = true;

        drive = new XboxController(0);
        manipulator = new XboxController(1); //port 1

        rezero = drive.start;

        gear = drive.rightTrigger;
        slowButton = drive.leftTrigger;

        manipGear = manipulator.rightTrigger;

        allowPressureGear = manipulator.leftTrigger;

        liftStart = manipulator.y;
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
