package com.oryx.imaging.Service.Native.IOSSDK;
 

 
/**
 *
 * @author Ahmad Fakhreddine
 */
public class AcquisitionSessionInfo {

    private String deviceId;
    private String clientName;
    private Object context; // Use Object for flexibility, can be replaced with a specific type

    // Getters and setters
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Object getContext() {
        return context;
    }

    public void setContext(Object context) {
        this.context = context;
    }

    @Override
    public String toString() {
        return "AcquisitionSessionInfo{"
                + "deviceId='" + deviceId + '\''
                + ", clientName='" + clientName + '\''
                + ", context=" + context
                + '}';
    }
}
