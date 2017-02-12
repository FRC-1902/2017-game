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

    private Image goalSample;
    private TargetMode mode = TargetMode.GEAR;
    private boolean atTarget = false;
    private Long timeOfTargetFind = null;
    private double error = 0;

    private final double TARGET_POS_OFFSET = -33; //-28
    private double gearPixelError;

    @Override
    public void run() {
        //goalSample = Image.fromFile("/home/lvuser/images/goal_sample.png").inRange(new Color(244, 244, 244), new Color(255, 255, 255));

        Log.v("Vision thread running.");
        UsbCamera camera = CameraServer.getInstance().startAutomaticCapture();
        camera.setFPS(5);
        camera.setResolution(320, 240);

        CvSink cvSink = CameraServer.getInstance().getVideo();
        CvSource outputStream = CameraServer.getInstance().putVideo("Vision", 320, 240);

        Image source = new Image();
        Image output;

        //CameraSettings.setExposureAuto(1);
        //CameraSettings.setExposure(9);

        //noinspection InfiniteLoopStatement
        while (true) {
            //TODO: see if grabbing the encoder value pre-picture taking then passing that on is more accurate than the current method
            long timeOfGet = System.currentTimeMillis();
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

                Contour target1 = correctContours.get(0);

                double inchesPerPixel;

                if (mode == TargetMode.GEAR) {
                    /*
                    if (OI.manipulator.rightJoyButton.get()) {
                        Log.d("PIC");
                        source.saveAs("home/lvuser/sample.png");
                    }
                    */
                    double aligned = (source.getWidth() / 2) - TARGET_POS_OFFSET;
                    if (correctContours.size() == 2) {
                        Contour target2 = correctContours.get(1);
                        double targetPos = (target1.getMiddleX() + target2.getMiddleX()) / 2;
                        error = aligned - targetPos;
                        Log.d("ERROR IN PIXELS: " + error);
                        double width = (target1.getWidth() + target2.getWidth()) / 2;
                        //gearPixelError = width * 1.5;
                        gearPixelError = Math.abs(target1.getMiddleX() - target2.getMiddleX()) / 2;
                        if (Math.abs(error) <= gearPixelError) {
                            if (!atTarget) {
                                timeOfTargetFind = timeOfGet;
                            }
                            atTarget = true;
                        } else {
                            atTarget = false;
                            timeOfTargetFind = null;
                        }

                        output.drawLine(Utils.round(aligned + TARGET_POS_OFFSET), Color.WHITE);
                        output.drawLine(Utils.round(targetPos), Color.YELLOW);

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
        return error;
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