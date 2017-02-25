package com.explodingbacon.steambot;

import com.explodingbacon.bcnlib.framework.Log;
import com.explodingbacon.bcnlib.utils.Utils;
import com.explodingbacon.bcnlib.vision.*;
import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.CameraServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VisionThread extends Thread {

    private TargetMode mode = TargetMode.GEAR;
    private boolean canSeeTarget = false;
    private boolean atTarget = false;
    private Long timeOfTargetFind = null;
    private Integer positionWhenDetected = -1;
    private double errorInPixels = 0;
    private Double errorInInches = null;

    private final double TARGET_POS_OFFSET = -30.5; //10
    private double gearPixelError;

    //TODO: catch the vision exception thing that ALWAYS happens

    @Override
    public void run() {
        Log.v("Vision thread started.");
        UsbCamera camera = CameraServer.getInstance().startAutomaticCapture();
        camera.setFPS(5);
        camera.setResolution(320, 240);

        CvSink cvSink = CameraServer.getInstance().getVideo();
        CvSource outputStream = CameraServer.getInstance().putVideo("Vision", 320, 240);

        Image source = new Image();
        Image output;

        CameraSettings.setExposureAuto(1);
        CameraSettings.setExposure(9);

        //noinspection InfiniteLoopStatement
        while (true) {
            long timeOfGet = System.currentTimeMillis();
            int possiblePosition = Robot.drive.strafeEncoder.get();
            cvSink.grabFrame(source.getMat());

            //Log.v("Millis diff: " + (System.currentTimeMillis() - millis));

            output = source.copy();
            source.toHSV();

            //use hue values that match the RGB appearance of the object
            //working h range 40-100
            source.inRange(new HSV(40, 100, 100), new HSV(100, 255, 255));

            List<Contour> allContours = source.getContours();
            List<Contour> correctContours = new ArrayList<>();

            for (Contour c : allContours) {
                if (c.getArea() > 100 && c.getArea() < 2500 && c.getY() > (source.getHeight() / 3)) {
                    correctContours.add(c);
                }
            }

            //System.out.println("Correct Contours: " + correctContours.size());

            if (correctContours.size() > 0) {
                //TODO: make sure this sorting puts the highest area contours at the top of the list
                Collections.sort(correctContours, (o1, o2) -> Utils.round(o1.getArea() - o2.getArea()));

                List<Contour> contoursWithinRange = new ArrayList<>();
                for (Contour c1 : correctContours) {
                    for (Contour c2 : correctContours) {
                        double diff = c1.getMiddleX() - c2.getMiddleX();
                        double avgWidth = (c1.getWidth() + c2.getWidth()) / 2;
                        double pixelsPerInch = avgWidth / 2;
                        diff /= pixelsPerInch;
                        if (Math.abs(diff) > 5 && Math.abs(diff) < 10) {
                            if (!contoursWithinRange.contains(c1) || !contoursWithinRange.contains(c2)) {
                                contoursWithinRange.add(c1);
                                contoursWithinRange.add(c2);
                            }
                        }
                    }
                }
                //Contour target1 = correctContours.get(0);

                double pixelsPerInch;

                if (mode == TargetMode.GEAR) {

                    double aligned = (source.getWidth() / 2) + TARGET_POS_OFFSET;
                    Log.i("ContoursWithinRange: " + contoursWithinRange.size());
                    if (contoursWithinRange.size() == 2) {
                        canSeeTarget = true;
                        Contour target1 = contoursWithinRange.get(0);
                        Contour target2 = contoursWithinRange.get(1);

                        double targetPos = (target1.getMiddleX() + target2.getMiddleX()) / 2;
                        errorInPixels = aligned - targetPos;

                        //Log.d("Error in pixels:" + errorInPixels);

                        double avgWidth = /*target1.getWidth();*/(target1.getWidth() + target2.getWidth()) / 2;
                        pixelsPerInch = avgWidth / 2; //TODO: label/variable that shows that this 2 is how long the target is in inches
                        errorInInches = errorInPixels / pixelsPerInch;
                        positionWhenDetected = possiblePosition;

                        //Log.d("Inches off target: " + errorInInches);
                        //gearPixelError = width * 1.5;
                        gearPixelError = Math.abs(target1.getMiddleX() - target2.getMiddleX()) / 2;
                        gearPixelError *= .5; //.75;
                        if (Math.abs(errorInPixels) <= gearPixelError) {
                            //if (!atTarget) {
                            //}
                            atTarget = true;
                        } else {
                            atTarget = false;
                            //timeOfTargetFind = null;
                        }
                        timeOfTargetFind = timeOfGet;

                        //output.drawLine(Utils.round(aligned + TARGET_POS_OFFSET), Color.WHITE);
                        output.drawLine(Utils.round(targetPos), Color.YELLOW);

                    } else {
                        errorInInches = null;
                        positionWhenDetected = null;
                        canSeeTarget = false;
                    }
                    Color lines;
                    if (atTarget) {
                        lines = Color.ORANGE;
                    } else {
                        lines = Color.PURPLE;
                    }
                    output.drawLine(Utils.round(aligned - gearPixelError), lines);
                    output.drawLine(Utils.round(aligned + gearPixelError), lines);
                    output.drawLine(Utils.round(aligned), lines);
                }
            }

            output.drawContours(correctContours, Color.RED);

            outputStream.putFrame(output.getMat());

        }
    }

    /**
     * Gets the difference between the desired position and the current position of the target.
     *
     * @return The difference between the desired position and the current position of the target.
     */
    public double getError() {
        return errorInPixels;
    }

    /**
     * Gets which target this VisionThread is currently looking for.
     *
     * @return Which target this VisionThread is currently looking for.
     */
    public TargetMode getTargetMode() {
        return mode;
    }

    public Long getTimeOfTargetFind() {
        return timeOfTargetFind;
    }

    public Integer getPositionWhenDetected() {
        return positionWhenDetected;
    }

    public boolean canSeeTarget() {
        return canSeeTarget;
    }

    /**
     * Gets how many inches off-target we are. (side-to-side inches, NOT DISTANCE).
     *
     * @return How many inches off-target we are. Returns null if the target is not in sight.
     */
    public Double getInchesFromTarget() {
        return errorInInches;
    }

    /**
     * Checks if we are at the desired target.
     *
     * @return If we are at the desired target.
     */
    public boolean isAtTarget() {
        return atTarget;
    }

    //TODO: is there any chance of flicker from false to true to false again because threading? Synchronize?

    /**
     * Sets the new desired target.
     *
     * @param m The new desired target.
     */
    public void setTarget(TargetMode m) {
        mode = m;
        atTarget = false;
    }

    /**
     * Waits until we are at the new desired target.
     *
     * @param m The new desired target.
     */
    public void waitForAtTarget(TargetMode m) {
        setTarget(m);
        waitForAtTarget();
    }

    /**
     * Waits until we are at the desired target.
     */
    public void waitForAtTarget() {
        while (!isAtTarget()) {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public enum TargetMode {
        NONE,
        GEAR,
        BOILER
    }
}