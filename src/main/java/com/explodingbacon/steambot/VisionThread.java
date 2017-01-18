package com.explodingbacon.steambot;

import com.explodingbacon.bcnlib.framework.Log;
import com.explodingbacon.bcnlib.utils.Utils;
import com.explodingbacon.bcnlib.vision.*;
import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.CameraServer;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class VisionThread extends Thread {

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

        while(true) {
            cvSink.grabFrame(source.getMat());

            output = source.copy();
            source.toHSV();

            //use hue values that match the RGB appearance of the object

            source.inRange(new HSV(40, 100, 100), new HSV(100, 255, 255));

            List<Contour> allContours = source.getContours();

            /*
            for (Contour c : allContours) {
                if (c.getArea() > 30) {
                    correctContours.add(c);
                }
            }
            */

            output.drawContours(allContours, Color.RED);

            outputStream.putFrame(output.getMat());

        }
    }
}