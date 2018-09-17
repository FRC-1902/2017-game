package com.explodingbacon.steambot.subsystems;

import com.explodingbacon.bcnlib.controllers.*;

public class XboxLogitch extends XboxController {

    public XboxLogitch(int port) {
        super(port);


        a = new JoystickButton(this, 2);
        b = new JoystickButton(this, 3);
        x = new JoystickButton(this, 1);
        y = new JoystickButton(this, 4);

        select = new JoystickButton(this, 9);
        start = new JoystickButton(this, 10);

        leftBumper = new JoystickButton(this, 5);
        rightBumper = new JoystickButton(this, 6);

        leftJoyButton = new JoystickButton(this, 11);
        rightJoyButton = new JoystickButton(this, 12);

        leftTrigger = new JoystickButton(this, 7);
        rightTrigger = new JoystickButton(this, 8);

        triggers = new ButtonGroup(leftTrigger, rightTrigger);
        bumpers = new ButtonGroup(leftBumper, rightBumper);
    }
}
