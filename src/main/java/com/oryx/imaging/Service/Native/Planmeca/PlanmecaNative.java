package com.oryx.imaging.Service.Native.Planmeca;

import java.io.File;
import java.io.IOException;

import com.oryx.imaging.Config.ImagingSensorWebSocketHandler;
import com.oryx.imaging.Service.ImagingSensorService;
import com.oryx.imaging.Service.Native.ImageSavedCallBack;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ORYX 1
 */
public class PlanmecaNative implements ImagingSensorService, ImageSavedCallBack {

    public static String rootDirectory;
    private String LibraryFileName;
    public PlanmecaThread thread;
    private boolean isRunning;
    private boolean isPano;
    public ArrayList<String> scannedImages;
    public DIDAPI didapi;
    public String driverPath;
    public final static String DEVICE_NAME = "Planmeca Native";
    public String PlanImagesFolder;
    public short devIndex;
    public boolean isIntraOral;
    public final static String states = "Planmeca Sensor States";
    public String ErrorMessage;
    private static ImagingSensorWebSocketHandler imagingSensorWebSocketHandler;

    public PlanmecaNative(ImagingSensorWebSocketHandler _imagingSensorWebSocketHandler) {
        String osName = System.getProperty("os.name").toLowerCase();
        String os;

        if (osName.contains("win")) {
            os = "WINDOWS";
        } else if (osName.contains("mac")) {
            os = "MAC";
        } else {
            throw new UnsupportedOperationException("Unsupported operating system: " + osName);
        }

        switch (os) {
            case "WINDOWS":
                rootDirectory = "C:\\Program Files\\Planmeca\\Didapi\\";
                LibraryFileName = "Didapi_64";
                driverPath = "C:\\Program Files\\Planmeca\\Didapi\\DidapiConfig.exe";
                PlanImagesFolder = "C:\\ProgramData\\Planmeca\\Didapi\\Images\\";
                break;

            case "MAC":
                rootDirectory = "/Library/Preferences/DIDAPI/";
                LibraryFileName = "libDidapi.dylib";
                driverPath = "/Applications/Planmeca/DidapiConfig.app";
                PlanImagesFolder = "/Library/Preferences/DIDAPI/Images/";
                break;
        }

        scannedImages = new ArrayList<>();
        isRunning = false;
        imagingSensorWebSocketHandler = _imagingSensorWebSocketHandler;
    }

    public void setIsIntraOral(boolean isIntraOral) {
        this.isIntraOral = isIntraOral;
    }

    @Override
    public boolean canConnect() throws IOException {
        return true;
    }

    @Override
    public boolean start() throws IOException {
        NativeLibrary.addSearchPath(LibraryFileName, rootDirectory);
        didapi = (DIDAPI) Native.loadLibrary(LibraryFileName, DIDAPI.class);
        thread = new PlanmecaThread(DEVICE_NAME);

        return isRunning;
    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }

    @Override
    public boolean isPano() {
        return isPano;
    }

    @Override
    public void setPano(boolean pano) {
        this.isPano = pano;
    }

    @Override
    public boolean acquire() {
        return true;
    }

    @Override
    public ArrayList<String> getScannedImagesPaths() {
        return scannedImages;
    }

    @Override
    public void stop() throws IOException {
        thread.stop();

    }

    public class PlanmecaThread implements Runnable {

        public final static short DIDAPI_OK = 1;
        public final static int DIDAPI_PTAG_BINNING = 108;
        public final static short USE_CALIBRATION = 1;
        public final static short DIDAPI_DEV_NOT_PRESENT = 2;

        // Dev Types
        public final static short DIDAPI_XRAY_PANO = 1;
        public final static short DIDAPI_XRAY_CEPH = 2;
        public final static short DIDAPI_XRAY_INTRA = 3;

        public final static short DIDAPI_XRAY_NONE = 100;
        public final static short DIDAPI_OS_ERROR = 4;// DIDAPI_FILE_TEMPDIR_ERROR:
        public final static short DIDAPI_FILE_NOT_OPENED = 16;
        public final static short DIDAPI_FILE_DIDAPILOG_ERROR = 222;
        public final static short DIDAPI_FILE_IMAGEDIR_ERROR = 220;
        public final static short DIDAPI_FILE_TEMPDIR_ERROR = 224;
        public final static short DIDAPI_GRAB_NOT_READY = 7;
        public final static short DIDAPI_GRAB_BUSY = 8;
        public final static short DIDAPI_FM_RIGHT = 2;
        public final static short DIDAPI_TIFF = 3;
        public final static short DIDAPI_TIFF16 = 4;
        public final static short DIDAPI_NO_IMAGE_DATA = 24;
        public final static short IMAGE_UNFINISHED = 21;
        public final static short DIDAPI_DEVICE_NOT_SELECTED = 38;
        public final static short DIDAPI_GET_PARAM = 2;
        public final static short DIDAPI_SET_PARAM = 3;
        public final static short MAX_IMAGEPARAM = 30;
        public final static short DIDAPI_PARAM_MODE = 1;
        public final static short DIDAPI_PARAM_PROG = 2;
        public final static short DIDAPI_PAN_MODE_NORMAL_RESOLUTION = 4;
        public final static short DIDAPI_PAN_MODE_MEDIUM_RESOLUTION = 3;
        public final static short DIDAPI_PAN_MODE_HIGH_RESOLUTION = 2;
        public final static short DIDAPI_PAN_MODE_MAX_RESOLUTION = 1;
        public final static short DIDAPI_WARNING_OLD_CAL_FILE = 25;
        public final static short DIDAPI_IMAGE_UNFINISHED = 21;
        public char[] imageAcquired;
        short err = DIDAPI_OK;
        short[] ref = { 1, 2, 3, 4, 5 };
        short scanLen;
        Thread mainSensorThread;
        private String threadName;
        public short[] version_number = { 1, 2 };
        public boolean devicesPresented = true;
        public boolean acquired = false;

        public PlanmecaThread(String name) {

            this.threadName = name;
            mainSensorThread = new Thread(this, threadName);
            // short test=didapi.DIDAPI_initialize(PointerHelper.asPointer(verion_number));
            short test = didapi.DIDAPI_initialize(version_number);
            int i;

            System.out.println("Test   version " + shortToString(version_number));
            switch (test) {
                case DIDAPI_OK:
                    System.out.println("Operation successful, or if called while already initialized\n"
                            + "nothing is done (except a log entry is printed).");
                    break;
                case DIDAPI_OS_ERROR:
                    System.out.println("Applies to windows only. Operating system error\n"
                            + "occurred, winsock initialization failed");
                    break;
                case DIDAPI_FILE_NOT_OPENED:
                    System.out.println("Could not verify that didapi.ini exists.");
                    break;
                case DIDAPI_FILE_DIDAPILOG_ERROR:
                    System.out.println("Could not verify write access to Logsdirectory\n"
                            + "or open didapi.log-file there.");
                    break;
                case DIDAPI_FILE_IMAGEDIR_ERROR:
                    System.out.println("Could not verify write access imagesdirectory");
                    break;
                case DIDAPI_FILE_TEMPDIR_ERROR:
                    System.out.println("Could not verify write access to user's\n"
                            + "temp-directory.");
                    break;
            }

            mainSensorThread.start();

        }

        @Override
        public void run() {

            // check error
            isRunning = true;
            System.out.println("Testtt runninggg");
            short i = -1;
            devIndex = -1;
            short ret = DIDAPI_OK;
            char[] typeID = new char[31];
            short[] devType = new short[1];// PointerHelper.asPointer(DEVICE_NAME)
            short[] HWrevision = new short[31];
            short[] SWrevision = new short[31];
            short[] maxMode = new short[31];
            short[] maxProg = new short[31];
            double[] DValue = new double[31];
            short[] imageWidth = new short[31];
            short[] imageHeight = new short[31];
            short[] pixelSizeH = new short[31];
            short[] pixelSizeV = new short[31];
            short[] pixelSizeDepth = new short[31];
            short[] scanDir = new short[31];
            boolean bGrabReady = false;
            // Initializing inquire devices routine
            // Main.log.info("Initializing inquire devices routine");
            // err = didapi.DIDAPI_inquire_devices(devIndex, typeID, devType, HWrevision,
            // SWrevision, maxMode, maxProg);
            short requiredDevType;
            String SensorType = "";
            requiredDevType = DIDAPI_XRAY_INTRA;

            if (isIntraOral) {
                requiredDevType = DIDAPI_XRAY_INTRA;
                SensorType = "IO";
            }
            if (isPano) {
                requiredDevType = DIDAPI_XRAY_PANO;
                SensorType = "PANO";
                setPano(true);
            }

            System.out.println(SensorType);
            ErrorMessage = "Selecting Device...";
            // Platform.runLater(() -> {
            // try {
            // SensorState(ErrorMessage);
            // } catch (IOException ex) {
            // Logger.getLogger(PlanmecaNative.class.getName()).log(Level.SEVERE, null, ex);
            // }
            // });
            for (i = 0; ret == DIDAPI_OK; i++) {
                ret = didapi.DIDAPI_inquire_devices(i, typeID, devType, HWrevision, SWrevision, maxMode, maxProg);

                if (ret == DIDAPI_OK && devType[0] == requiredDevType) {

                    devIndex = i;
                    System.out.println("Device of index " + i + " is inquired " + devType[0]);
                    break;
                }
            }

            if (devIndex == -1) {

                System.out.println("Failed to inquire devices");
                ErrorMessage = "Failed to inquire devices";

                // Platform.runLater(() -> {
                // try {
                // SensorState(ErrorMessage);
                // } catch (IOException ex) {
                // Logger.getLogger(PlanmecaNative.class.getName()).log(Level.SEVERE, null, ex);
                // }
                // });
                return;// exit from runn
            }

            short ret1 = 0;
            Random rand = new Random();

            err = didapi.DIDAPI_select_device((short) devIndex);

            System.out.println("Testing on select device return value " + err);
            if (err == DIDAPI_DEV_NOT_PRESENT) {
                System.out.println("Device not presented");
                ErrorMessage = "Device not presented";
                // Platform.runLater(() -> {
                // try {
                // SensorState(ErrorMessage);
                // } catch (IOException ex) {
                // Logger.getLogger(PlanmecaNative.class.getName()).log(Level.SEVERE, null, ex);
                // }
                // });
                return;
            }
            if (err != DIDAPI_OK) {
                System.out.println("Failed to select a device . Device not present ==> error code (" + err + ")");
                ErrorMessage = "Failed to select a device";
                // Platform.runLater(() -> {
                // try {
                // SensorState(ErrorMessage);
                // } catch (IOException ex) {
                // Logger.getLogger(PlanmecaNative.class.getName()).log(Level.SEVERE, null, ex);
                // }
                // });
                return;
            }

            err = didapi.DIDAPI_patient_selected((short) 1);
            ErrorMessage = "Ready to acquire";
            // Platform.runLater(() -> {
            // try {
            // SensorState(ErrorMessage);
            // } catch (IOException ex) {
            // Logger.getLogger(PlanmecaNative.class.getName()).log(Level.SEVERE, null, ex);
            // }
            // });
            if (requiredDevType == DIDAPI_XRAY_PANO) {
                err = didapi.DIDAPI_inquire_image((short) -1, (short) -1, USE_CALIBRATION, imageWidth, imageHeight,
                        pixelSizeH, pixelSizeV, pixelSizeDepth, scanDir);
                if (err != DIDAPI_OK) {
                    System.out.println("Inquire image failed");
                }
                err = didapi.DIDAPI_init_grabbing(USE_CALIBRATION);
                if (err == DIDAPI_WARNING_OLD_CAL_FILE) {//
                    System.out.println("Re-calibration is needed");
                }
            }

            while (isRunning) {

                if (requiredDevType == DIDAPI_XRAY_INTRA) {
                    err = didapi.DIDAPI_init_grabbing(USE_CALIBRATION);

                    while (true) {

                        ret1 = didapi.DIDAPI_get_device_status(ref);
                        // Main.log.info("====== Test on ret and on ref[]" + ret1 + "//" + ref[0]);
                        if (ret1 == DIDAPI_GRAB_NOT_READY) {
                            // Main.log.info("NOT READY TO GRAB AN IMAGE");
                        } else if (ret1 == DIDAPI_GRAB_BUSY) {
                            // Main.log.info("BUSY TO GRAB AN IMAGE");
                        } else if (ret1 == DIDAPI_NO_IMAGE_DATA || ret1 == IMAGE_UNFINISHED) {
                            // Main.log.info("Failed to get the image");
                        } else if (ret1 == DIDAPI_DEVICE_NOT_SELECTED) {
                            // Main.log.info("A device is not currently selected, failed to operate.");
                            // Should terminate the loop
                        }
                        if (ret1 == DIDAPI_OK) {
                            bGrabReady = true;
                            break;
                        }
                    }

                    ret1 = didapi.DIDAPI_finish_grabbing();
                    err = didapi.DIDAPI_inquire_image((short) -1, (short) -1, USE_CALIBRATION, imageWidth, imageHeight,
                            pixelSizeH, pixelSizeV, pixelSizeDepth, scanDir);
                    imageAcquired = new char[imageWidth[0] * imageHeight[0]];
                }

                if (requiredDevType == DIDAPI_XRAY_PANO || requiredDevType == DIDAPI_XRAY_CEPH) {
                    // To acquire the image successfully
                    short[] nScanlen = { 1, 2, 3, 4 };
                    ret1 = didapi.DIDAPI_get_device_status(nScanlen);
                    System.out.println("Device status::::::::" + ret1);
                    if (ret1 < 0) {
                        System.out.println("Failed to get device status");
                    }
                    if (ret1 == DIDAPI_WARNING_OLD_CAL_FILE) {
                        System.out.println("Old calibration file");

                    }
                    if (ret1 == DIDAPI_GRAB_BUSY) {
                        short w = imageWidth[0];
                        short h = imageHeight[0];
                        System.out.println("Grabbing still in progress ...");
                        // err = didapi.DIDAPI_get_image(imageAcquired, (short) 8, (short) 1, (short) 0,
                        // (short) 0, w, h);
                        if (err != DIDAPI_OK) {
                            System.out.println("Errorr while grabbing image");
                        }
                    }
                    if (ret1 == DIDAPI_OK) {
                        System.out.println("Exposure done");
                        ret1 = didapi.DIDAPI_finish_grabbing();
                        System.out.println("Finish grabbing::::" + ret1);
                        if (ret1 == DIDAPI_IMAGE_UNFINISHED) {
                            System.out.println("Image UNfinished::::::::::");
                            bGrabReady = false;
                            ret1 = didapi.DIDAPI_init_grabbing(USE_CALIBRATION);
                            if (ret1 != DIDAPI_OK) {
                                System.out.println("Failed to initialize grabbing::" + ret1);
                            }
                        } else if (ret1 == DIDAPI_OK) {
                            System.out.println("Image captured ready to save_image");
                            bGrabReady = true;
                        }
                    } else {
                        ret1 = didapi.DIDAPI_init_grabbing(USE_CALIBRATION);
                        System.out.println("Init Grabbibg:::" + ret1);
                    }
                }

                if (ret1 == DIDAPI_OK && bGrabReady) {
                    short[] OsError = new short[30];
                    short w = imageWidth[0];
                    short h = imageHeight[0];
                    int index = rand.nextInt(1000);
                    String pathForImage = PlanImagesFolder + "planmeca" + index + ".tif";
                    if (isPano) {

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(PlanmecaNative.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }

                    err = didapi.DIDAPI_save_image(pathForImage, OsError, DIDAPI_TIFF);
                    System.out.println("DIDAPI_save_image::" + err);
                    if (err == DIDAPI_OK) {
                        System.out.println(
                                "The image is saved=================The image is saved=====================The image is saved=================The image is saved=================The image is saved====================The image is saved");
                        // save the image
                    didapi.DIDAPI_save_image(pathForImage, OsError, DIDAPI_TIFF);
                        if (isPano) {
                            didapi.DIDAPI_patient_selected((short) 0);
                            didapi.DIDAPI_exit();
                            isRunning = false;
                        }

                        bGrabReady = false;
                    } else {
                        System.out.println("Cant Get and Save Image ::::::::(ErrorCode=" + err + ")");
                        ErrorMessage = "Cant Get and Save Image";
                        // Platform.runLater(() -> {
                        // try {
                        // SensorState(ErrorMessage);
                        // } catch (IOException ex) {
                        // Logger.getLogger(PlanmecaNative.class.getName()).log(Level.SEVERE, null, ex);
                        // }
                        // });
                        isRunning = false;
                    }

                }

            }

        }

        public void stop() {
            isRunning = false;
            didapi.DIDAPI_patient_selected((short) 0);
            didapi.DIDAPI_exit();

        }

        public boolean waitForFile(String filename) {
            File file = new File(filename);
            int trials = 0;
            while (!file.exists() && trials < 4) {
                // while (!file.exists()) {
                trials++;
                timeout(1);
                System.out.println("Trials: " + trials);
            }
            return file.exists();
        }

        public void timeout(int i) {
            try {
                TimeUnit.SECONDS.sleep(i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public String shortToString(short[] array) {
        if (array == null) {
            return "";
        }
        int i;
        String versionTest = "";
        for (i = 0; i < array.length; i++) {
            versionTest += array[i];
        }
        return versionTest;

    }

    @Override
    public void startScanning() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'startScanning'");
    }

    @Override
    public void stopScanning() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'stopScanning'");
    }

    @Override
    public String getStatus() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStatus'");
    }

    @Override
    public void imageSavedNotification() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'imageSavedNotification'");
    }

}
