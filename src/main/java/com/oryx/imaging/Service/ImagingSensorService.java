package com.oryx.imaging.Service;

import java.io.IOException;
import java.util.ArrayList;

public interface ImagingSensorService {
    void startScanning();

    void stopScanning();

    String getStatus();

    boolean canConnect() throws IOException; 

    boolean start() throws IOException;  

    void stop() throws IOException;  

    boolean isRunning();

    boolean isPano();

    void setPano(boolean pano);

    boolean acquire();

    ArrayList<String> getScannedImagesPaths();

}
