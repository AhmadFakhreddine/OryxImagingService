package com.oryx.imaging.Service.Native.IOSSDK;

 

 
/**
 *
 * @author Ahmad Fakhreddine
 */
public class AcquisitionStatus {

    @Override
    public String toString() {
        return "AcquisitionStatus{" + "ready=" + ready + ", state=" + state + ", lastImageId=" + lastImageId + ", totalImages=" + totalImages + '}';
    }

    public enum AcquisitionState {
        ERROR,
        LOW_BATTERY,
        INSUFFICIENT_STORAGE,
        NO_HARDWARE,
        NO_SENSOR,
        INITIALIZING,
        NO_ACQUISITION_INFO,
        READY,
        READING,
        PROCESSING,
        STORING,
        NEW_IMAGE
    }

    private boolean ready;
    private AcquisitionState state;
    private String lastImageId;
    private int totalImages;

    // Getters and Setters
    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public AcquisitionState getState() {
        return state;
    }

    public void setState(AcquisitionState state) {
        this.state = state;
    }

    public String getLastImageId() {
        return lastImageId;
    }

    public void setLastImageId(String lastImageId) {
        this.lastImageId = lastImageId;
    }

    public int getTotalImages() {
        return totalImages;
    }

    public void setTotalImages(int totalImages) {
        this.totalImages = totalImages;
    }
}
