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
        camera.setFPS(30);
        camera.setBrightness(12);
        camera.setExposureManual(1000);
        camera.setResolution(640, 480);


        CvSink cvSink = CameraServer.getInstance().getVideo();
        CvSource outputStream = CameraServer.getInstance().putVideo("Vision", 640, 480);

        Image source = new Image();
        Image output = new Image();

        //TODO: Figure out why contours are not appearing
        //TODO: Figure out NO CONNECTION flashing. Only appears when intercepting frames
        //TODO: Figure out how to properly change exposure
        while(true) {
            cvSink.grabFrame(source.getMat());
            //outputStream.putFrame(source.getMat());

            Imgproc.cvtColor(source.getMat(), output.getMat(), Imgproc.COLOR_BGR2GRAY);
            //output.getContours();
            //output.drawContours(output.getContours(), Color.PURPLE);
            outputStream.putFrame(output.getMat());

        }
    }
}
