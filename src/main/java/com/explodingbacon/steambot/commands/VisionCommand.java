package com.explodingbacon.steambot.commands;

import com.explodingbacon.bcnlib.framework.Command;
import com.explodingbacon.bcnlib.framework.Log;
import com.explodingbacon.bcnlib.vision.Image;
import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.CameraServer;

//TODO: Make sure Vision does not use too much memory.

public class VisionCommand extends Command {

    private UsbCamera cam = null;
    private CvSink sink = null;
    private CvSource outputStream = null;
    private Image source = null, output = null;

    @Override
    public void onInit() {
        cam = CameraServer.getInstance().startAutomaticCapture();
        Log.v("Camera initialized!");

        cam.setResolution(640, 480);

        sink = CameraServer.getInstance().getVideo();
        outputStream = CameraServer.getInstance().putVideo("Gear Tracking", 640, 480);

        source = new Image();
        output = new Image();
    }

    @Override
    public void onLoop() {
        if (cam != null) {
            //TODO: this was originally in a while() loop, see if the 25ms delay for onLoop() slows down processing too much
            sink.grabFrame(source.getMat());

            //TODO: Do image processing here
            //Imgproc.cvtColor(source.getMat(), output.getMat(), Imgproc.COLOR_BGR2GRAY);

            outputStream.putFrame(output.getMat());
        }
    }

    @Override
    public void onStop() {
        cam.free();
        cam = null;
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
