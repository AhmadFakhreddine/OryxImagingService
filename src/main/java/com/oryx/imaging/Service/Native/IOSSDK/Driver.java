package com.oryx.imaging.Service.Native.IOSSDK;



import com.oryx.imaging.Config.ImagingSensorWebSocketHandler;
import com.oryx.imaging.Sensor.Sensor;
import com.oryx.imaging.Service.ImagingSensorService;
import com.oryx.imaging.Service.Native.ImageSavedCallBack;
import  com.oryx.imaging.Service.Native.IOSSDK.Filters.AEFilterParameters;
import com.oryx.imaging.Service.Native.IOSSDK.Filters.Filter;
import  com.oryx.imaging.Service.Native.IOSSDK.Filters.ImageResource;
import  com.oryx.imaging.Service.Native.IOSSDK.Filters.SelectFiltersParameters;
import com.oryx.imaging.Service.Native.IOSSDK.Filters.SupremeFilterParameters;
 
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Ahmad Fakhreddine
 */
public class Driver implements  ImagingSensorService, ImageSavedCallBack {

    private static AcquisitionStatus currentStatus;
    private static int totalImages = 0;
    private static AcquisitionSession session;
    private static ApiClient client;
    private static AcquisitionInfo acqInfo;
    private static String LAST_IMAGE_ID;
    private static String SESSION_ID;
    private static String sessionId;
    private static String lastProcessedImageId;
    private ExecutorService executor;
    private boolean isRunning;
    private boolean isPano;
    public ArrayList<String> scannedImages;
     public String imagesDir = "C:\\VixTemp";
     public static Filter filter;
    private ScheduledExecutorService deviceChecker;
    private boolean deviceConnected = false;
    private static ImagingSensorWebSocketHandler imagingSensorWebSocketHandler;

    public static Filter getFilter() {
        return filter;
    }

    public static void setFilter(Filter filter) {
        Driver.filter = filter;
    }
     private Sensor sensor;

    public AcquisitionInfo getAcqInfo() {
        return acqInfo;
    }

    public void setAcqInfo(AcquisitionInfo acqInfo) {
        this.acqInfo = acqInfo;
    }

    public AcquisitionSession getSession() {
        return session;
    }

    public void setSession(AcquisitionSession session) {
        this.session = session;
    }

    public ApiClient getClient() {
        return client;
    }

    public void setClient(ApiClient client) {
        this.client = client;
    }

  

    public Driver(ImagingSensorWebSocketHandler _imagingSensorWebSocketHandler) {
        scannedImages = new ArrayList<String>();
        executor = Executors.newSingleThreadExecutor();
        imagingSensorWebSocketHandler=_imagingSensorWebSocketHandler;
    }

    @Override
    public boolean start() {

        client = new ApiClient();
        acqInfo = new AcquisitionInfo();
        acqInfo.setEnable(true);
         if (executor == null || executor.isShutdown()) {
            executor = Executors.newFixedThreadPool(10); // Create a new thread pool
            System.out.println("Thread pool started.");
        }

        try {

            ArrayList<ActiveSession> sessions = new ArrayList<ActiveSession>();
            client.authenticate();
            ArrayList<Device> devices = client.getDeviceList();
            if (devices.size() == 0) {
                // if (acquireController != null) {
                //     acquireController.updateUI("Sensor Not Found", Color.web("#D32F2F"));
                // }
                if (executor != null && !executor.isShutdown()) {
                    executor.shutdown();
                    System.out.println("Executor service shut down.");
                }

                currentStatus = null;

                totalImages = 0;
                System.out.println("Driver stopped and state reset.");
                 return false;
            }
            Device device = devices.get(0);
            deviceConnected = true;
            // selectDeviceFilter(device);
            //startDeviceMonitor();
            sessions = client.getAcquisitionSessions(device.getDeviceId());

            if (sessions != null && !sessions.isEmpty()) {
                sessions.forEach(session -> {
                    try {
                        client.deleteAcquisitionSession(session.getSessionId());
                    } catch (Exception ex) {
                        System.out.println(ex.toString());
                    }

                });
            } else {
                System.out.println("No sessions");
            }
            AcquisitionSessionInfo sessionInfo = new AcquisitionSessionInfo();
            sessionInfo.setDeviceId(device.getDeviceId());
            sessionInfo.setClientName("Oryx");
            sessionInfo.setContext("");

            try {
                 session = client.createAcquisitionSession(sessionInfo);
                System.out.println("Acquisition Session created: " + session);
                try {

                     AcquisitionStatusSubscription subscription = client.subscribeToAcquisitionStatus(session.getSessionId(), Driver::processAcquisitionStatus, 1000);
                    subscription.start();
                    executor.submit(() -> {
                        try {
                            System.out.println("Fetching Acquisition Status...");
                            AcquisitionStatus status = client.getAcquisitionStatus(session.getSessionId());
                            System.out.println("Processing Acquisition Status...");
                            processAcquisitionStatus(status);
                    
                            System.out.println("Updating Acquisition Info...");
                            client.updateAcquisitionInfo(session.getSessionId(), acqInfo);
                            System.out.println("Acquisition Info Updated Successfully.");
                        } catch (Exception e) {
                            System.out.println("Error in executor task:");
                            e.printStackTrace();
                        }
                    });
                    
                } catch (Exception e) {
                    System.out.println("1");
                    System.out.println(e.toString());
                }
            } catch (Exception e) {
                System.out.println("2");
                System.out.println(e.toString());
            }
        } catch (Exception ex) {
            System.out.println("3");
            System.out.println(ex.toString());
        }
        return true;
    }

    private void selectDeviceFilter(Device device) {
        if (device.getName().contains("Schick 33")) {
            System.out.println("Schick 33 Model Selected");
            filter = sensor.get33Filter();
        } else if (device.getName().contains("AE")) {
            System.out.println("Schick AE Model Selected");
            filter = sensor.getAeFilter();
        } else if (device.getName().contains("Elite")) {
            System.out.println("Schick Elite Model Selected");
            filter = sensor.getEliteFilter();
        } else {
            filter = null;
        }
    }

    private static void processAcquisitionStatus(AcquisitionStatus status) {
        // updateUI(status);
    
        currentStatus = status;
        if (status.getState() == AcquisitionStatus.AcquisitionState.READY) {
            System.out.println("Ready to Acquire");
        } else if (status.getState() == AcquisitionStatus.AcquisitionState.NO_ACQUISITION_INFO) {
            System.out.println("No acquisition info available. Initializing...");
        } else if (status.getLastImageId() != null) {
            if ((status.getState() == AcquisitionStatus.AcquisitionState.NEW_IMAGE || status.getTotalImages() != totalImages) && (lastProcessedImageId == null || !status.getLastImageId().equals(lastProcessedImageId))) {
                totalImages = status.getTotalImages();
                System.out.println("New image received. Total images: " + totalImages);
                lastProcessedImageId = status.getLastImageId();

                // Call saveImage asynchronously
                Driver.saveImage(session.getSessionId(), status.getLastImageId(), filter).thenAccept(newImage -> {
                    if (newImage != null) {
                        try {
                            Path imagePath;
                            if (newImage.startsWith("file:/")) {
                                URI newImageUri = new URI(newImage); // Convert to URI
                                imagePath = Paths.get(newImageUri.getPath()); // Use only the path part
                            } else {
                                imagePath = Paths.get(newImage); // Convert Windows path directly
                            }
                
                            imagingSensorWebSocketHandler.sendImageFromPath(imagePath.toString());
                            System.out.println("Image processed and saved: " + imagePath.toString());
                
                        } catch (Exception ex) {
                            System.out.println("Error accessing saved image: " + ex.toString());
                        }
                    } else {
                        System.err.println("Failed to save image. No new image path returned.");
                    }
                }).exceptionally(ex -> {
                    System.err.println("Error during image saving: " + ex.getMessage());
                    return null;
                });
                
            }
        }
    }

    private static void updateUI(AcquisitionStatus status) {
        // if (status.getState() == null || acquireController == null) {
        //     return;
        // }

        // // Define state messages and corresponding colors
        // String stateMessage;
        // Color stateColor;

        // switch (status.getState()) {
        //     case ERROR:
        //         stateMessage = "Error: Something went wrong!";
        //         stateColor = Color.web("#D32F2F"); // Red
        //         break;
        //     case LOW_BATTERY:
        //         stateMessage = "Warning: Low Battery!";
        //         stateColor = Color.web("#F57C00"); // Orange
        //         break;
        //     case INSUFFICIENT_STORAGE:
        //         stateMessage = "Error: Insufficient Storage!";
        //         stateColor = Color.web("#D32F2F"); // Red
        //         break;
        //     case NO_HARDWARE:
        //         stateMessage = "Error: No Hardware Detected!";
        //         stateColor = Color.web("#D32F2F"); // Red
        //         break;
        //     case NO_SENSOR:
        //         stateMessage = "Error: Sensor Not Found!";
        //         stateColor = Color.web("#D32F2F"); // Red
        //         break;
        //     case INITIALIZING:
        //         stateMessage = "Initializing the system...";
        //         stateColor = Color.web("#1976D2"); // Blue
        //         break;
        //     case NO_ACQUISITION_INFO:
        //         stateMessage = "No Acquisition Information Available.";
        //         stateColor = Color.web("#616161"); // Gray
        //         break;
        //     case READY:
        //         stateMessage = "Ready for Acquisition.";
        //         stateColor = Color.web("#388E3C"); // Green
        //         break;
        //     case READING:
        //         stateMessage = "Reading Data...";
        //         stateColor = Color.web("#1976D2"); // Blue
        //         break;
        //     case PROCESSING:
        //         stateMessage = "Processing Data...";
        //         stateColor = Color.web("#1976D2"); // Blue
        //         break;
        //     case STORING:
        //         stateMessage = "Storing Data...";
        //         stateColor = Color.web("#FBC02D"); // Yellow
        //         break;
        //     case NEW_IMAGE:
        //         stateMessage = "New Image Acquired!";
        //         stateColor = Color.web("#4CAF50"); // Success Green
        //         break;
        //     default:
        //         stateMessage = "Unknown State.";
        //         stateColor = Color.web("#616161"); // Gray
        // }

        // // Call UI update with animation & colors
        // acquireController.updateUI(stateMessage, stateColor);
    }

    public static void saveInputStreamToFile(InputStream inputStream, String savePath) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream is null. Cannot save the file.");
        }

        try ( OutputStream outputStream = Files.newOutputStream(Paths.get(savePath))) {
            byte[] buffer = new byte[4096]; // 4KB buffer
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            System.out.println("Image saved successfully at: " + savePath);
        } finally {
            inputStream.close(); // Ensure the stream is closed
        }
    }

    public static CompletableFuture<String> saveImage(String session, String lastImageId, Filter filter) {
        System.out.println(lastImageId);
        String uniqueFileName = "Sirona_" + UUID.randomUUID() + ".jpg";
        String filePath = "C:/VixTemp/" + uniqueFileName;
        System.out.println("Generated unique file name: " + uniqueFileName);
        System.out.println("Generated file path: " + filePath);

        if (lastImageId != null) {
            System.out.println("Last image ID provided: " + lastImageId);
            try {
                sessionId = session;
                System.out.println("Session ID retrieved: " + sessionId);
                SESSION_ID = sessionId;
                LAST_IMAGE_ID = lastImageId;

                if (filter == null) {

                    InputStream imageStream = client.getImageData(sessionId, lastImageId);
                    saveInputStreamToFile(imageStream, filePath);
                    return CompletableFuture.completedFuture(filePath);

                } else {
                    if (filter instanceof SelectFiltersParameters) {
                        SelectFiltersParameters select = (SelectFiltersParameters) filter;
                        if (select.getEnhancementMode().equals(SelectFiltersParameters.EnhancementModes.NONE)) {
                            InputStream imageStream = client.getImageData(sessionId, lastImageId);
                            saveInputStreamToFile(imageStream, filePath);
                            System.out.println(filePath);
                            return CompletableFuture.completedFuture(filePath);
                        }
                    }

                    CompletableFuture<ImageResource> imageResourceFuture = client.createImage(sessionId, lastImageId);
                    System.out.println("Image creation initiated for lastImageId: " + lastImageId);

                    return imageResourceFuture.thenApply(imageResource -> {
                        System.out.println("ImageResource received:");
                        System.out.println("  Image ID: " + imageResource.getId());
                        System.out.println("  Media Type: " + imageResource.getMediaType());
                        System.out.println("  Image URL: " + imageResource.getUrl());
                        System.out.println("  Expires: " + imageResource.getExpires());
                        try ( InputStream imageInfo = applyFilter(imageResource.getId(), filter)) {
                            if (imageInfo != null) {
                                Files.copy(imageInfo, Paths.get(filePath));
                                System.out.println("Image saved successfully to: " + filePath);
                            } else {
                                System.err.println("Filter application returned no data.");
                            }
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to save image: " + e.getMessage());
                        }

                        return Paths.get(filePath).toUri().toString();
                    }).exceptionally(ex -> {
                        System.err.println("Error occurred during image creation or filtering: " + ex.getMessage());
                        return null;
                    });
                }
            } catch (Exception ex) {
                System.err.println("Error in saveImage: " + ex.getMessage());
            }
        } else {
            System.err.println("No lastImageId provided. Skipping image save.");
        }

        return CompletableFuture.completedFuture(
                null);
    }

    private static InputStream applyFilter(String imageId, Filter filter) throws Exception {

        System.out.println("Applying filter to image ID: " + imageId);
        if (filter instanceof SupremeFilterParameters) {
            SupremeFilterParameters sp = (SupremeFilterParameters) filter;
            System.out.println("Using Supreme Filter:  " + sp.getTask() + " , " + sp.getSharpness());
            return client.supremeFilter(imageId, (SupremeFilterParameters) filter);
        } else if (filter instanceof AEFilterParameters) {
            AEFilterParameters ae = (AEFilterParameters) filter;
            System.out.println("Using AE Filter:  " + ae.getTask() + " , " + ae.getSharpness());
            return client.aeFilter(imageId, (AEFilterParameters) filter);
        } else if (filter instanceof SelectFiltersParameters) {
            SelectFiltersParameters se = (SelectFiltersParameters) filter;
            System.out.println("Using Select Filter:  " + se.getEnhancementMode());
            return client.selectFilter(imageId, (SelectFiltersParameters) filter);
        } else {
            String errorMsg = "Unsupported filter type: " + filter.getClass().getName();
            System.err.println(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
    }

    @Override
    public void stop() {
        try {
      

            if (executor != null && !executor.isShutdown()) {
                executor.shutdown();
                System.out.println("Executor service shut down.");
            }
            if(deviceChecker!=null && !deviceChecker.isShutdown()){
                deviceChecker.shutdown();
            }

            currentStatus = null;

            totalImages = 0;
            System.out.println("Driver stopped and state reset.");
 
        } catch (Exception e) {
            System.err.println("Error during stop: " + e.getMessage());
        }
    }

    @Override
    public boolean canConnect() throws IOException {
        return true;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
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
    public boolean acquire() {
        return isRunning;
    }

    @Override
    public ArrayList<String> getScannedImagesPaths() {
        return scannedImages;
    }

  

    public void timeout(int i) {
        try {
            TimeUnit.SECONDS.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean waitForFile(String filename) {
        File file = new File(filename);
        int trials = 0;
        while (!file.exists() && trials < 4) {
//            while (!file.exists()) {
            trials++;
            timeout(1);
            System.out.println("Trials: " + trials);
        }
        return file.exists();
    }

    @Override
    public void imageSavedNotification() {
        try {
            client.updateAcquisitionInfo(session.getSessionId(), acqInfo);

        } catch (Exception ex) {
            Logger.getLogger(Driver.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

    }

  

    private void startDeviceMonitor() {
        if (deviceChecker == null || deviceChecker.isShutdown()) {
            deviceChecker = Executors.newScheduledThreadPool(1);
            deviceChecker.scheduleAtFixedRate(() -> {
                try {
                    ArrayList<Device> devices = client.getDeviceList();

                    if (devices.isEmpty()) {
                        if (deviceConnected) {
                            deviceConnected = false; // hub was unplugged
                            handleDeviceDisconnected();
                        }
                    } else if (!devices.isEmpty() && !devices.get(0).isHasSensor()) {
                        if (deviceConnected) {
                            deviceConnected = false; // hub plugged and  Device was unplugged
                            handleDeviceDisconnected();
                        }
                    } else if (!devices.isEmpty() && devices.get(0).isHasSensor()) {
                        if (!deviceConnected) {
                            deviceConnected = true; // Device is back
                            handleDeviceReconnected(devices.get(0)); // Restart acquisition
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error checking device connection: " + e.toString());
                }
            }, 0, 5, TimeUnit.SECONDS); // Check every 5 seconds
        }
    }

    private void handleDeviceDisconnected() {
        System.out.println("Device unplugged!");
        System.out.println("Device unplugged! Updating UI...");

   

        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }


        session = null;
        totalImages = 0;

        // Don't shut down deviceChecker, let it continue monitoring
    }

    private void handleDeviceReconnected(Device device) {
        System.out.println("Device reconnected: " + device.getName());
        System.out.println("Device reconnected. Restarting acquisition...");

        // if (acquireController != null) {
        //     acquireController.updateUI("Sensor Reconnected", Color.web("#388E3C")); // Green color
        // }

        start();
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

}
