package com.oryx.imaging.Service.Native.IOSSDK;

 

 

/**
 *
 * @author Ahmad Fakhreddine
 */
    public  class AcquisitionInfo {

        private boolean enable = true; // Default to true
        private int rotation;
        private String binning; // Assuming BinningMode is an enum, map it to String or create a similar enum
        private boolean applyLut = true; // Default to true
        private Object context; // Use Object for custom data

        // Getters and setters
        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public int getRotation() {
            return rotation;
        }

        public void setRotation(int rotation) {
            this.rotation = rotation;
        }

        public String getBinning() {
            return binning;
        }

        public void setBinning(String binning) {
            this.binning = binning;
        }

        public boolean isApplyLut() {
            return applyLut;
        }

        public void setApplyLut(boolean applyLut) {
            this.applyLut = applyLut;
        }

        public Object getContext() {
            return context;
        }

        public void setContext(Object context) {
            this.context = context;
        }
    }

