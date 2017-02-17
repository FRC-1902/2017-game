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
        synchronized (STRAFE_POSITIONS_USE) {
            strafePositions.put(System.currentTimeMillis(), Robot.drive.strafeEncoder.get());
        }
        List<Long> longsToDelete = new ArrayList<>();
        synchronized (STRAFE_POSITIONS_USE) {
            for (Long l : strafePositions.keySet()) {
                if ((System.currentTimeMillis() - l) >= 2000) {
                    //strafePositions.remove(l);
                    longsToDelete.add(l);
                }
            }
        }
        for (Long l : longsToDelete) {
            synchronized (STRAFE_POSITIONS_USE) {
                strafePositions.remove(l);
            }
        }
        try {
            Thread.sleep(5);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //TODO: test changes to this
    public int getStrafeAt(Long time) {
        Long above = Long.MAX_VALUE, below = Long.MIN_VALUE;

        synchronized (STRAFE_POSITIONS_USE) {
            for (Long l : strafePositions.keySet()) {
                if (l > below && l <= time)
                    below = l;

                if (l < above && l >= time)
                    above = l;
            }
        }

        Long topDiff = above - time;
        Long bottomDiff = time - below;

        return (int) ((strafePositions.get(above) * bottomDiff + strafePositions.get(below) * topDiff)
                / (bottomDiff + topDiff));
    }
}
