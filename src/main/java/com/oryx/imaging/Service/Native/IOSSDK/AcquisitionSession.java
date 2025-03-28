package com.oryx.imaging.Service.Native.IOSSDK;

 

 
import java.time.OffsetDateTime;

/**
 *
 * @author Ahmad Fakhreddine
 */
    public class AcquisitionSession {

        private String sessionId;
        private String deviceId;
        private String clientName;

        private OffsetDateTime createdOn;

        private String context;

        // Getters and Setters
        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

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

        public OffsetDateTime getCreatedOn() {
            return createdOn;
        }

        public void setCreatedOn(OffsetDateTime createdOn) {
            this.createdOn = createdOn;
        }

        public String getContext() {
            return context;
        }

        public void setContext(String context) {
            this.context = context;
        }

        // Optionally, override toString() for easy logging
        @Override
        public String toString() {
            return "AcquisitionSession{"
                    + "sessionId='" + sessionId + '\''
                    + ", deviceId='" + deviceId + '\''
                    + ", clientName='" + clientName + '\''
                    + ", createdOn=" + createdOn
                    + ", context='" + context + '\''
                    + '}';
        }
    }

