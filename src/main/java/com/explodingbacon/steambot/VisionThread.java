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

    public TargetMode mode = TargetMode.NONE;

    @Override
    public void run() {
        Log.v("Vision thread running.");
        UsbCamera camera = CameraServer.getInstance().startAutomaticCapture();
        camera.setFPS(5);
        camera.setResolution(320, 240);

        CvSink cvSink = CameraServer.getInstance().getVideo();
        CvSource outputStream = CameraServer.getInstance().putVideo("Vision", 320, 240);

        Image source = new Image();
        Image output = new Image();

        CameraSettings.setExposureAuto(1);
        CameraSettings.setExposure(9);

        while (true) {
            cvSink.grabFrame(source.getMat());

            output = source.copy();
            source.toHSV();

            //use hue values that match the RGB appearance of the object
            //working h range 40-100
            source.inRange(new HSV(40, 100, 100), new HSV(100, 255, 255));

            List<Contour> allContours = source.getContours();
            List<Contour> correctContours = new ArrayList<>();

            for (Contour c : allContours) {
                if (c.getArea() > 30) {
                    correctContours.add(c);
                }
            }

            if (correctContours.size() > 1) {
                Contour target1 = null, target2 = null;

                //TODO: make sure this sorting puts the highest area contours at the top of the list
                Collections.sort(correctContours, (o1, o2) -> Utils.round(o1.getArea() - o2.getArea()));

                target1 = correctContours.get(0);
                target2 = correctContours.get(1);
                //Contour[] targets = {target1, target2};
                double inchesPerPixel;

                if (mode == TargetMode.GEAR) {
                    //Target is two inches wide, so half of it's width is the inch-to-pixel ratio
                    //TODO: Which target to use? Average the two together? Test what's accurate
                    inchesPerPixel = target1.getWidth() / 2;

                }
            }

            output.drawContours(correctContours, Color.RED);

            outputStream.putFrame(output.getMat());

        }
    }

    public enum TargetMode {
        NONE,
        GEAR,
        BOILER
    }
}