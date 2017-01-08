package com.explodingbacon.steambot;

import com.explodingbacon.bcnlib.framework.AbstractRobot;
import com.explodingbacon.bcnlib.framework.RobotCore;

 public class WPIRobot extends AbstractRobot {

    @Override
    public void robotInit() {
        RobotCore c = new Robot(this);
        setCore(c);
        c.robotInit();
    }
}
