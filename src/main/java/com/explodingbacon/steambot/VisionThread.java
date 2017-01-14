package com.explodingbacon.steambot;

import com.explodingbacon.bcnlib.framework.Log;
import com.explodingbacon.bcnlib.vision.Image;
import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.CameraServer;
import org.opencv.imgproc.Imgproc;

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

        while(true) {
            cvSink.grabFrame(source.getMat());

            source.toHSV();

            outputStream.putFrame(source.getMat());

        }
    }
}
