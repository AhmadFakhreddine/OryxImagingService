package com.oryx.imaging.Sensor;

import com.oryx.imaging.Service.Native.IOSSDK.Filters.*;

 

public class Sensor implements Cloneable {

    public enum TYPE {
        X_RAY("X_RAY"),
        CAMERA("CAMERA"),
        PANO("PANO");

        public final String label;

        TYPE(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return this.label;
        }
    }

    private int rotationVertical; // rotation in degrees (multiple of 90)
    private int rotationHorizontal; // rotation in degrees (multiple of 90)

    private boolean isDefault;

    private int idProduct;
    private int idVendor;

    private static int sensorIdAct = 0;
    private String name;
    private String setupURL;
    private TYPE sensorType;
    private String camName;
    private int twainConfig;
    private boolean isNative;

    private boolean isListed; // is listed is checked whenever a new device added to the list or a device is selected
    // from the list of available sensors in the system
    private boolean isAvailable; // if sensor is tested then it is available
    private boolean isActivated;

    private boolean isConfigured;

    private double length = -1;         // set default to -1
    private double computedLength = -1; // set default to -1

    private String selectedEnhancementName;
    private String twainKey;
    private String driverMac;
    private String driverWin;
    private String scanDirMac;
    private String scanDirWin;
    private String filterPath;
    private AEFilterParameters aeFilter; 

    public AEFilterParameters getAeFilter() {
        return aeFilter;
    }

    public void setAeFilter(AEFilterParameters aeFilter) {
        this.aeFilter = aeFilter;
    }

    public SelectFiltersParameters getEliteFilter() {
        return eliteFilter;
    }

    public void setEliteFilter(SelectFiltersParameters eliteFilter) {
        this.eliteFilter = eliteFilter;
    }

    public SupremeFilterParameters get33Filter() {
        return _33Filter;
    }

    public void set33Filter(SupremeFilterParameters _33Filter) {
        this._33Filter = _33Filter;
    }
    private SelectFiltersParameters eliteFilter; 
    private SupremeFilterParameters _33Filter; 

 

    public Sensor(String name, TYPE sensorType) {
        this.name = name;
        this.sensorType = sensorType;
        this.rotationVertical = 0;
        this.rotationHorizontal = 0;
        this.isDefault = false;
        this.twainKey = "";
    }

    /**
     *
     * Used for Twain
     *
     * @param name
     * @param twainKey
     * @param sensorType
     * @param twainConfig
     * @param idProduct
     * @param idVendor
     */
    public Sensor(String name, String twainKey, TYPE sensorType, int twainConfig, int idProduct, int idVendor) {
        this.name = name;
        this.sensorType = sensorType;
        this.rotationVertical = 0;
        this.rotationHorizontal = 0;
        this.twainConfig = twainConfig;
        this.twainKey = twainKey;
        this.isDefault = false;
        this.idProduct = idProduct;
        this.idVendor = idVendor;
    }

    /**
     * Used for cam
     *
     * @param name
     * @param camName
     * @param sensorType
     */
    public Sensor(String name, String camName, TYPE sensorType) {
        this.name = name;
        this.sensorType = sensorType;
        this.rotationVertical = 0;
        this.rotationHorizontal = 0;
        this.isDefault = false;
        this.camName = camName;
    }

    public Sensor() {

    }

    public boolean isNative() {
        return isNative;
    }

    public void setNative(boolean aNative) {
        isNative = aNative;
    }

    public String getCamName() {
        if (camName == null) {
            return "";
        }
        return camName;
    }

    public void setCamName(String camName) {
        this.camName = camName;
    }

    @Override
    public Sensor clone() throws CloneNotSupportedException {
        return (Sensor) super.clone();
    }

    public String getTwainKey() {
        if (twainKey == null) {
            return "";
        }
        return twainKey;
    }

    public void setTwainKey(String twainKey) {
        this.twainKey = twainKey;
    }

    public String getName() {
        return name;
    }
    /**
     * 
     * @return stripped name of the sensor, should be used for upload and saved images
     */


    public void setName(String name) {
        this.name = name;
    }

    public int getTwainConfig() {
        return twainConfig;
    }

    public void setTwainConfig(int twainConfig) {
        this.twainConfig = twainConfig;
    }

    public int getStudentId() {
        return sensorIdAct;
    }

    public void setSetupURL(String setupURL) {
        this.setupURL = setupURL;
    }

    public boolean isListed() {
        return isListed;
    }

    public void setListed(boolean listed) {
        isListed = listed;
    }

    public boolean isActivated() {
        return isActivated;
    }

    public void setActivated(boolean activated) {
        isActivated = activated;
    }

    public String getSetupURL() {
        return setupURL;
    }

    public TYPE getSensorType() {
        return sensorType;
    }

    public void setSensorType(TYPE sensorType) {
        this.sensorType = sensorType;
    }

    // public void setIsDefault(boolean isDefault) {
    //     Main.setDefaultSensor(name, sensorType.label);
    // }

    // public boolean getIsDefault() {
    //     String isPrefDefault = Main.getDefaultSensor(sensorType.label);
    //     if (isPrefDefault != null) {
    //         return Main.getDefaultSensor(sensorType.label).equals(name);
    //     }
    //     return false;

    // }

    public boolean isConfigured() {
        return isConfigured;
    }

    public void setConfigured(boolean configured) {
        isConfigured = configured;
    }

    public int rotateVerticalClockwise() {
        rotationVertical += 90;
        rotationVertical %= 360;
        return rotationVertical;
    }

    public int rotateVerticalAntiClockwise() {
        rotationVertical -= 90;
        rotationVertical %= 360;
        return rotationVertical;
    }

    public int rotateHorizontalClockwise() {
        rotationHorizontal += 90;
        rotationHorizontal %= 360;
        return rotationHorizontal;
    }

    public int rotateHorizontalAntiClockwise() {
        rotationHorizontal -= 90;
        rotationHorizontal %= 360;
        return rotationHorizontal;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getComputedLength() {
        return computedLength;
    }

    public void setComputedLength(double computedLength) {
        this.computedLength = computedLength;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public int getRotationVertical() {
        return rotationVertical;
    }

    public int getRotationHorizontal() {
        return rotationHorizontal;
    }

    public void setRotationVertical(int rotationVertical) {
        if(rotationVertical==-180)
            rotationVertical=0;
        this.rotationVertical = rotationVertical;
    }

    public void setRotationHorizontal(int rotationHorizontal) {
         if(rotationHorizontal==-180)
            rotationHorizontal=0;
        this.rotationHorizontal = rotationHorizontal;
    }

    public String getSelectedEnhancementName() {
        return selectedEnhancementName;
    }

    public void setSelectedEnhancementName(String selectedEnhancementName) {
        this.selectedEnhancementName = selectedEnhancementName;
    }

    public int getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(int idProduct) {
        this.idProduct = idProduct;
    }

    public int getIdVendor() {
        return idVendor;
    }

    public void setIdVendor(int idVendor) {
        this.idVendor = idVendor;
    }

    public String getDriverMac() {
        return driverMac;
    }

    public void setDriverMac(String driverMac) {
        this.driverMac = driverMac;
    }

    public String getDriverWin() {
        return driverWin;
    }

    public void setDriverWin(String driverWin) {
        this.driverWin = driverWin;
    }

    public String getScanDirMac() {
        return scanDirMac;
    }

    public void setScanDirMac(String scanDirMac) {
        this.scanDirMac = scanDirMac;
    }

    public String getScanDirWin() {
        return scanDirWin;
    }

    public void setScanDirWin(String scanDirWin) {
        this.scanDirWin = scanDirWin;
    }

    public String getFilterPath() {
        return filterPath;
    }

    public void setFilterPath(String filterPath) {
        this.filterPath = filterPath;
    }

}

