package com.explodingbacon.steambot;

import com.explodingbacon.bcnlib.utils.CodeThread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PositionLogThread extends CodeThread {

    private HashMap<Long, Integer> strafePositions = new HashMap<>();
    private final Object STRAFE_POSITIONS_USE = new Object();

    @Override
    public void code() {
        strafePositions.put(System.currentTimeMillis(), Robot.drive.strafeEncoder.get());
        List<Long> longsToDelete = new ArrayList<>();
        for (Long l : strafePositions.keySet()) {
            if ((System.currentTimeMillis() - l) >= 2000) {
                //strafePositions.remove(l);
                longsToDelete.add(l);
            }
        }
        synchronized (STRAFE_POSITIONS_USE) {
            for (Long l : longsToDelete) {
                strafePositions.remove(l);
            }
        }
    }

    public int getStrafeAt(Long time) {
        Long closest = null;
        synchronized (STRAFE_POSITIONS_USE) {
            for (Long l : strafePositions.keySet()) {
                if (closest == null || Math.abs(time - l) < Math.abs(time - closest)) {
                    closest = l;
                }
            }
        }
        return strafePositions.get(closest);
    }
}
