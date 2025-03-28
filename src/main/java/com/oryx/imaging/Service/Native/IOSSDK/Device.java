package com.oryx.imaging.Service.Native.IOSSDK;
 

 
import java.util.ArrayList;

/**
 *
 * @author Ahmad Fakhreddine
 */
public class Device {

    private String deviceId;
    private String name;
    private String iconUrl;
    private boolean hasSensor;
    private String status;
    private String interfaceType;
    private String modelName;
    private String serialNumber;
    private String version;

     public String getDeviceId() {
        return deviceId.replace("\"", "");
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId.replace("\"", "");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public boolean isHasSensor() {
        return hasSensor;
    }

    public void setHasSensor(boolean hasSensor) {
        this.hasSensor = hasSensor;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(String interfaceType) {
        this.interfaceType = interfaceType;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "DeviceInfo{"
                + "deviceId='" + deviceId + '\''
                + ", name='" + name + '\''
                + ", iconUrl='" + iconUrl + '\''
                + ", hasSensor=" + hasSensor
                + ", status='" + status + '\''
                + ", interfaceType='" + interfaceType + '\''
                + ", modelName='" + modelName + '\''
                + ", serialNumber='" + serialNumber + '\''
                + ", version='" + version + '}';

    }

    public static ArrayList<Device> getDevices(String jsonResponse) {
        ArrayList<Device> devices = new ArrayList<>();

        try {
             jsonResponse = jsonResponse.trim();
            jsonResponse = jsonResponse.substring(1, jsonResponse.length() - 1);  
            String[] objects = jsonResponse.split("(?<=\\}),\\s*(?=\\{)");  

            for (String object : objects) {
                Device device = new Device();

                 device.deviceId = DataHelper.extractValue(object, "deviceId");
                device.name = DataHelper.extractValue(object, "name");
                device.iconUrl = DataHelper.extractValue(object, "iconUrl");
                device.hasSensor = Boolean.parseBoolean(DataHelper.extractValue(object, "hasSensor"));
                device.status = DataHelper.extractValue(object, "status");
                device.interfaceType = DataHelper.extractValue(object, "interfaceType");
                device.modelName = DataHelper.extractValue(object, "modelName");
                device.serialNumber = DataHelper.extractValue(object, "serialNumber");
                device.version = DataHelper.extractValue(object, "version");
                devices.add(device);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return devices;
    }


}

