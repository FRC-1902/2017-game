package com.explodingbacon.steambot;

import com.explodingbacon.bcnlib.framework.Log;
import com.explodingbacon.bcnlib.vision.Camera;
import edu.wpi.cscore.CvSource;
import edu.wpi.first.wpilibj.CameraServer;

public class AltVisionThread extends Thread {

    @Override
    public void run() {
        Log.v("AltVisionThread init!");
        Camera camera = new Camera(0, true);
        CvSource outputStream = CameraServer.getInstance().putVideo("Vision", 640, 480);
        camera.onEachFrame((image) -> {
            image.toHSV();

            outputStream.putFrame(image.getMat());
            image.release();
        });
    }

}
