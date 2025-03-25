package com.oryx.imaging.Service.Twain;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.asprise.imaging.core.Imaging;
import com.asprise.imaging.core.Request;
import com.asprise.imaging.core.Result;
import com.asprise.imaging.core.scan.twain.Source;
import com.oryx.imaging.Config.ImagingSensorWebSocketHandler;
import com.oryx.imaging.Service.ImagingSensorService;

public class TwainSensorService implements ImagingSensorService {

    @Override
    public void startScanning() {
        // Code to initialize and start TWAIN scanner
    }

    @Override
    public void stopScanning() {
        // Code to stop TWAIN scanner
    }

    @Override
    public String getStatus() {
        return "TWAIN Sensor Active";
    }

    private TwainSensorThread twainSensorThread;
    private boolean isSensorRunning = false;
    private String fileWatcherPath = "C:\\VixTemp\\";
    private ArrayList<String> scannedImages; // list containing all scanned images paths
    // private ArrayList<BufferedImage> scannedImagesCarestream; // list containing
    // all scanned images paths
    private String twainName;
    private int twainConfig;
    private boolean isAcquisition;
    private boolean isPano;
    private boolean isIntraOral;
    private boolean planmecaTwain = false;
    private boolean visionXpano = false;
    private boolean dentron = false;
    // private boolean gendexPano = false;
    private ImagingSensorWebSocketHandler webSocketHandler; 
    public TwainSensorService(String twainName, int twainConfig, boolean isAcquisition, String templateName,
            int idProduct, int idVendor, boolean isIntraOral,ImagingSensorWebSocketHandler _webSocketHandler) {
        scannedImages = new ArrayList<>();
        if (twainName.contains("Planmeca")) {
            this.fileWatcherPath = System.getProperty("user.home") + "\\AppData\\Local\\Temp\\";
            planmecaTwain = true;
        } else if (twainName.equals("ScanX")) {

            this.fileWatcherPath = "C:\\ProgramData\\Air Techniques\\images\\";

        } else if (twainName.equals("ScanX Intraoral View") || twainName.equals("sensorX")
                || twainName.equals("ProVecta S-Pan")) {

            this.fileWatcherPath = "C:\\ProgramData\\Air Techniques\\VisionX\\Connect\\images\\";
            visionXpano = true;

        } else if (twainName.equals("Dentron TWAIN")) {
            this.fileWatcherPath = "C:\\ProgramData\\DentronTwain\\";
            dentron = true;

        } else {
            // this.fileWatcherPath = Main.getSensorSavingDirectory() + "\\";
        }

        this.twainName = twainName;
        this.twainConfig = twainConfig;
        this.isAcquisition = isAcquisition;
        this.isIntraOral = isIntraOral;
        this.webSocketHandler=_webSocketHandler;

    }

    @Override
    public boolean isPano() {
        return isPano;
    }

    @Override
    public void setPano(boolean pano) {
        isPano = pano;
    }

    @Override
    public boolean canConnect() {
        return true;
    }

    @Override
    public boolean start() {

        System.out.println("Start function" + twainName);

        twainSensorThread = new TwainSensorThread("Twain Sensor Thread");
        return acquire();
    }

    public boolean acquire() {
        return twainSensorThread.isRunning();
    }

    public void deleteFromDentronFolder() {
        String folderPath = this.fileWatcherPath;
        Path path = Paths.get(folderPath);

        if (Files.exists(path) && Files.isDirectory(path)) {
            try {
                Files.walkFileTree(path, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE,
                        new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                // Delete the file
                                Files.delete(file);
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                                System.err.println("Error visiting file: " + file);
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                                    throws IOException {
                                // Skip the CorrectionFiles directory
                                if (dir.getFileName().toString().equals("CorrectionFiles")) {
                                    return FileVisitResult.SKIP_SUBTREE;
                                }
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                                if (exc == null) {
                                    // Skip deleting the CorrectionFiles directory itself
                                    if (!dir.getFileName().toString().equals("CorrectionFiles")) {
                                        Files.delete(dir);
                                    }
                                    return FileVisitResult.CONTINUE;
                                } else {
                                    throw exc;
                                }
                            }
                        });

                System.out.println("All contents in the folder '" + folderPath
                        + "' have been deleted except for 'CorrectionFiles'.");
            } catch (IOException e) {
                System.err.println("An error occurred: " + e.getMessage());
            }
        } else {
            System.out.println("The folder '" + folderPath + "' does not exist.");
        }
    }

    public void deleteFromFolder() {
        String folderPath = this.fileWatcherPath;
        Path path = Paths.get(folderPath);

        if (Files.exists(path) && Files.isDirectory(path)) {
            try {
                Files.walkFileTree(path, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE,
                        new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                Files.delete(file);
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                                System.err.println("Error visiting file: " + file);
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                                if (exc == null) {
                                    Files.delete(dir);
                                    return FileVisitResult.CONTINUE;
                                } else {
                                    throw exc;
                                }
                            }
                        });

                System.out.println("All contents in the folder '" + folderPath + "' have been deleted.");
            } catch (IOException e) {
                System.err.println("An error occurred: " + e.getMessage());
            }
        } else {
            System.out.println("The folder '" + folderPath + "' does not exist.");
        }
    }

    @Override
    public void stop() throws IOException {
        System.out.println("The stop of the sensor");

        if (twainSensorThread != null) {
            System.out.println("The stop of the sensorThread");
            twainSensorThread.stop();
            twainSensorThread = null;//
        }

        isSensorRunning = false;
        if (visionXpano) {
            deleteFromFolder();
            File watcher = new File(fileWatcherPath);
            watcher.mkdirs();
        }
        if (dentron) {
            deleteFromDentronFolder();
        }

    }

    @Override
    public boolean isRunning() {
        return isSensorRunning;
    }

    @Override
    public ArrayList<String> getScannedImagesPaths() {
        return scannedImages;
    }

    public void deletingPmImage() {
        if (planmecaTwain) {
            try {
                File f = new File(System.getProperty("user.home") + "\\AppData\\Local\\Temp\\PmImage.TIF");
                System.out.println(f.getAbsolutePath());
                if (f.exists()) {
                    f.delete();
                }
            } catch (Exception ex) {
                System.out.println("Exception occured while deleting PmImage file:" + "\n" + ex.toString());
            }
        }

    }

    public class TwainSensorThread implements Runnable {

        private boolean isTwainRunning;
        Thread mainSensorThread;
        Imaging imagingAsprise;
        // boolean isAcquisition;

        private String threadName;
        private Result result;

        /**
         *
         * @param threadName
         */
        TwainSensorThread(String threadName) {
            this.threadName = threadName;
            mainSensorThread = new Thread(this, this.threadName);
            mainSensorThread.start(); // Starting the thread
        }

        public boolean isRunning() {
            return isTwainRunning;
        }

        @Override
        public void run() {

            registerTwainLib(false);
            String save_path = fileWatcherPath + File.separatorChar;
            save_path = save_path.replace("\\", "/");
            File tmpDir = new File(save_path);
            if (!tmpDir.exists()) {
                tmpDir.mkdirs();
            }
            String p = tmpDir.getPath();
            isSensorRunning = true;
            System.out.println("Twain config  " + twainConfig);
            if (imagingAsprise != null) {
                if (twainConfig == 1) {
                    if (twainName.equals("USBCam Intraoral Camera")) {
                        try {
                            twainScan(true, save_path);
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                    } else {
                        twainScan(true, save_path);

                    }

                } else if (!isAcquisition) {
                    if (twainConfig == 3) {
                        try {
                            twainScan(true, save_path);
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                        timeout(3);
                        if (!isIntraOral) {
                            System.out.println("Will stopp");
                            try {
                                stopSensoreExe();
                            } catch (IOException ex) {
                                System.out.println(ex.toString());
                            }
                        }
                    } else if (twainConfig == 2) {
                        result = null;
                        result = twainScan(true, save_path);
                        try {
                            if (result != null && !result.getImages().isEmpty()) {

                                String savedImage = result.getOutputItems().get(result.getOutputItems().size() - 1)
                                        .getOutputRecords().get(0);
                                System.out.println(savedImage);
                                // Very important line of code
                                // check if file exists before firing event
                                boolean fileExists = waitForFile(savedImage);

                                if (fileExists) {
                                    scannedImages.add(savedImage);
                                     webSocketHandler.sendImageFromPath(savedImage);

                                    if (!isIntraOral) {
                                        System.out.println("Here the exceptionnn");
                                        Runtime.getRuntime().exec("taskkill /F /IM scnhelp.exe"); // carestream and
                                                                                                  // vatech
                                        Runtime.getRuntime().exec("taskkill /F /IM MAS2W.exe");
                                    }
                                } else {
                                    System.out.println("FILE NOT SAVED PROPERLY - Calibration");
                                }

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            stopSensoreExe();
                        } catch (IOException ex) {
                            System.out.println(ex);
                        }
                    }
                } else {
                    if (twainConfig == 3) {
                        System.out.println("The sensor is sopix");
                        while (isSensorRunning) {
                            try {
                                twainScan(true, save_path);
                            } catch (Exception e) {
                                // do nothing
                                System.out.println(e);
                            }

                            timeout(3);
                            try {
                                stopSensoreExe();
                            } catch (IOException ex) {
                                System.out.println(ex);
                            }

                            if (isPano) {
                                break;
                            }
                        }

                    } else if (twainConfig == 2) {
                        while (isSensorRunning) {
                            result = null;
                            try {
                                result = twainScan(true, save_path);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                System.out.println("=========" + result);
                            }
                            try {
                                if (result != null && !result.getImages().isEmpty()) {

                                    String savedImage = result.getOutputItems().get(result.getOutputItems().size() - 1)
                                            .getOutputRecords().get(0);

                                    // Very important line of code
                                    // check if file exists before firing event
                                    boolean fileExists = waitForFile(savedImage);

                                    if (fileExists) {
                                        scannedImages.add(savedImage);
                                        webSocketHandler.sendImageFromPath(savedImage);
                                        stopSensoreExe();
                                    } else {
                                        System.out.println("FILE NOT SAVED PROPERLY - Acquisition");
                                    }
                                    if (isPano) {
                                        break;
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            timeout(1);
                        }
                    }
                }
            }
            isTwainRunning = true;
        }

        private Boolean isSensorsInstancesAlive() {
            Boolean isActive = false;

            // try {
            // Process process = null;
            // switch (Main.operating_system) {

            // case "WINDOWS":
            // process = new ProcessBuilder("tasklist.exe", "/fo", "csv", "/nh").start();
            // break;
            // case "MAC":
            // process = new ProcessBuilder("ps").start();
            // break;
            // }
            // Scanner sc = new Scanner(process.getInputStream());

            // if (sc.hasNextLine()) {
            // sc.nextLine();
            // }
            // while (sc.hasNextLine()) {
            // String line1 = sc.nextLine();

            // if (line1.contains("MAS2W.exe") || line1.contains("scnhelp.exe")) {
            // isActive = true;
            // }

            // //System.out.println("--Running Task["+line1+"]----");
            // }
            // } catch (IOException ex) {
            // Main.log.log(Level.SEVERE, null, ex);
            // return isActive;
            // }

            return isActive;
        }

        @SuppressWarnings("deprecation")
        private void stopSensoreExe() throws IOException {
            boolean isVatech = twainName.toLowerCase().equals("EzSensor");
            boolean isCareStream = twainName.toLowerCase().equals("RVG Twain");

            boolean isGxTwain = twainName.toLowerCase().equals("GxTwain");
            boolean isSchick = twainName.toLowerCase().equals("CDR Intra-oral X-Ray");

            boolean isSOPIX = twainName.toLowerCase().equals("TWAIN SOPIX / SOPIX ²");

            // vatech or CareStream or Schick
            if ((isVatech && !isSensorRunning) || (isCareStream && !isSensorRunning) || (isSchick && !isSensorRunning)
                    || isGxTwain) {
                try {
                    Runtime.getRuntime().exec("taskkill /F /IM scnhelp.exe"); // carestream and vatech
                } catch (IOException e) {
                    System.out.println(e.toString());
                }
                try {
                    Runtime.getRuntime().exec("taskkill /F /IM EzSensor.exe"); // carestream and vatech
                } catch (IOException e) {
                    System.out.println(e.toString());
                }
            } else if (isSOPIX) { // SOPIX
                try {
                    Runtime.getRuntime().exec("taskkill /F /IM MAS2W.exe");
                } catch (IOException e) {
                    System.out.println(e.toString());
                }
            } else if (twainName.contains("Series")) {
                try {
                    Runtime.getRuntime().exec("taskkill /F /IM AXAM.exe");
                } catch (IOException e) {
                    System.out.println(e.toString());
                }

            } else if (visionXpano) {
                try {
                    Runtime.getRuntime().exec("taskkill /F /IM VisionX.exe");
                } catch (IOException e) {
                    System.out.println(e.toString());
                }
            } else if (twainName.contains("CARINA") || twainName.equals("e2v Sensor TWAIN")
                    || twainName.startsWith("BAE")) {
                String processName = "DiamondDX Capture.exe";

                try {
                    String command = "taskkill /F /IM \"" + processName + "\"";

                    Process process = Runtime.getRuntime().exec(command);

                    int exitCode = process.waitFor();
                    if (exitCode == 0) {
                        System.out.println(processName + " terminated successfully.");
                    } else {
                        System.out.println("Failed to terminate " + processName);
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            } else if (twainName.equals("Intraoral x-rays")) {
                tryToStopSensorExe("IngxSrvr.exe");
            }

            if (isSensorsInstancesAlive() && !isSensorRunning) {
                try {
                    Runtime.getRuntime().exec("taskkill /F /IM scnhelp.exe");
                    Runtime.getRuntime().exec("taskkill /F /IM MAS2W.exe");
                    Runtime.getRuntime().exec("taskkill /F /IM EzSensor.exe");
                } catch (IOException ex) {
                    System.out.println(ex.toString());
                }
            }
            if (twainName.startsWith("QuickRay") || twainName.startsWith("Quick Ray")) {
                String processName = "Quick RaytoIS.exe";

                Runtime.getRuntime().exec("taskkill /F /IM \"" + processName + "\"");
            }
            if (twainName.startsWith("TWAIN2")) {

                Runtime.getRuntime().exec("taskkill /F /IM I-SensorTWAINEXE.exe");
            }
            if (twainName.equals("TwainCapture")) {

                Runtime.getRuntime().exec("taskkill /F /IM TwainCapture.exe");
            }
            if (twainName.equals("iRayIntraoral Sensor")) {

                Runtime.getRuntime().exec("taskkill /F /IM FPDChildDriver.exe");
            }
            if (twainName.equals("GxTwain") && isPano) {
                // Main.stopGxStart();
            }
            if (twainName.equals("ACTEON Universal TWAIN")) {
                Runtime.getRuntime().exec("taskkill /F /IM MAWRON.exe");
                tryToStopSensorExe("MMS2.exe");
                tryToStopSensorExe("AXMM.exe");
                tryToStopSensorExe("AVMM.exe");// Intraoral x-rays
            }

        }

        private void tryToStopSensorExe(String processName) {
            try {
                String command = "taskkill /F /IM \"" + processName + "\"";

                Process process = Runtime.getRuntime().exec(command);

                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    System.out.println(processName + " terminated successfully.");
                } else {
                    System.out.println("Failed to terminate " + processName);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void timeout(int i) {
            try {
                TimeUnit.SECONDS.sleep(i);
            } catch (InterruptedException e) {
                System.out.println(e.toString());
            }
        }

        private void registerTwainLib(boolean enableLogging) {
            System.setProperty("ASCAN_LICENSE_NAME", "Evidentiae-STD");
            System.setProperty("ASCAN_LICENSE_CODE", "9D3C0-F53F0-A758E-BDD83");
            if (enableLogging) {
                Imaging.configureNativeLogging(4, "stdout");
            }
            imagingAsprise = new Imaging("com.oryx.imaging", 0);
        }

        private Result twainScan(boolean showUI, String save_path) {
            boolean twainKeyExists = false;
            List<Source> listOfSources = imagingAsprise.scanListSources();

            boolean vatechEZExists = false;

            // if (twainName.contains("Series")) {
            // twainName = "TWAIN SOPIX-SOPIX² Series";
            // }
            for (Source s : listOfSources) {

                System.out.println("Twain Source:::::" + s.toString());
                if (twainName.contains("Series") || twainName.contains("Serie")) {
                    twainName = "TWAIN SOPIX-SOPIX² Series";
                }

                if (s.toString().startsWith("Instrumentarium Dental TWAIN")
                        && twainName.equals("Instrumentarium Dental TWAIN")) {
                    twainName = s.toString();
                    twainKeyExists = true;
                    break;
                } else if ((s.toString().startsWith("RVG Twain") || s.toString().startsWith("RVGTwain"))
                        && twainName.equals("RVG Twain")) {
                    twainName = s.toString();
                    twainKeyExists = true;
                    break;
                } else if ((s.toString().startsWith("Intraoral USB Sensor") && twainName.equals("VATech EzSensor"))
                        && !vatechEZExists) {
                    twainName = s.toString();
                    twainKeyExists = true;
                } else if (s.toString().startsWith(twainName) && s.toString().length() >= twainName.length()) {

                    if (twainName.equals("VATech EzSensor")) {
                        vatechEZExists = true;
                    }

                    twainName = s.toString();

                    twainKeyExists = true;
                }
            }
            if (twainName.startsWith("Native XDR")) {
                twainName = "Native XDR® Capture";
                twainKeyExists = true;
            }

            // if (!twainKeyExists) {
            // if (twainName.equals("VATech EzSensor")) {
            // twainName = "Intraoral USB Sensor";
            // twainKeyExists = true;
            // }
            // if (twainName.equals("RVG Twain")) {
            // twainName = "RVGTwain";
            // twainKeyExists = true;
            // }
            // }
            if (!twainKeyExists) {
                System.out.println("Unfounded TwainKey");
            }
            runimagingAspriseScan(showUI, save_path).run();
            return result;
        }

        private Runnable runimagingAspriseScan(boolean showUI, String save_path) {
            System.out.println("runimagingAspriseScan");
            return () -> {
                try {
                    if (this.imagingAsprise == null) {
                        registerTwainLib(false);
                    }

                    System.out.println("testtt " + twainName);
                    result = imagingAsprise.scan(Request.fromJson(
                            "{"
                                    + "\"output_settings\" : [ {"
                                    + "  \"type\" : \"save\","
                                    + "  \"format\" : \"jpg\","
                                    + "  \"save_path\" : \"" + save_path + "${TMS}${EXT}\","
                                    + "  \"request_id\" :\"1\""
                                    + "} ]"
                                    + "}"),
                            twainName, showUI, false);

                } catch (IOException ex) {
                    System.out.println(ex.toString());
                }
            };

        }

        private boolean waitForFile(String filename) {
            File file = new File(filename);
            int trials = 0;
            while (!file.exists() && trials < 4) {
                // while (!file.exists()) {
                trials++;
                timeout(1);
                System.out.println("Trials: " + trials);
            }

            // if ( !twainName.contains("Planmeca") && !twainName.equals("GxTwain") &&
            // !twainName.equals("TWAIN SOPIX / SOPIX ²") && !twainName.startsWith("Jazz")
            // && !twainName.startsWith("VATech") && !twainName.equals("Intraoral USB
            // Sensor") && !twainName.contains("DS Orthophos X-ray") &&
            // !twainName.startsWith("EVO TWAIN") && !twainName.contains("Dexis")) {
            // Main.log.info("Wait to mature");
            // while (Main.isFileAccessed(Path.of(filename)));
            // }
            if (twainName.startsWith("QuickRay") || twainName.contains("e2v") || twainName.startsWith("Quick Ray")
                    || twainName.startsWith("SOREDEX") || twainName.startsWith("soredex") || twainName.contains("Apex")
                    || (twainName.contains("Prime"))) {

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    System.out.println(ex.toString());
                }
            }
            return file.exists();
        }

        public boolean waitForFileAvailability(String filePath) {
            int retryDelay = 500; // milliseconds
            int maxRetries = 10; // Maximum number of attempts
            File file = new File(filePath);

            for (int i = 0; i < maxRetries; i++) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    // If we can open the stream, the file is no longer in use
                    System.out.println("File is available for use.");
                    return true;
                } catch (IOException e) {
                    System.out.println("File is still in use. Retrying in " + retryDelay + "ms...");
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt(); // Restore interrupted status
                        System.out.println("Retry interrupted.");
                        return false;
                    }
                }
            }

            System.out.println("File was not available after maximum retries.");
            return false;
        }

        public void stop() throws IOException {

            if (imagingAsprise != null) {
                imagingAsprise.resetIt();
                imagingAsprise = null;
            }

            isSensorRunning = false;
            isTwainRunning = false;
            stopSensoreExe();
        }
    }
}
