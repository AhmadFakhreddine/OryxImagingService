package com.oryx.imaging.Service.Native.IOSSDK.Filters;

 import java.time.OffsetDateTime;

public class ImageResource {

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public OffsetDateTime getExpires() {
        return expires;
    }

    public void setExpires(OffsetDateTime expires) {
        this.expires = expires;
    }

    public ImageInfo getImageInfo() {
        return imageInfo;
    }

    public void setImageInfo(ImageInfo imageInfo) {
        this.imageInfo = imageInfo;
    }

    public ModalitySession getModalitySession() {
        return modalitySession;
    }

    public void setModalitySession(ModalitySession modalitySession) {
        this.modalitySession = modalitySession;
    }

    private String id;
    private String mediaType;
    private String url;
    private OffsetDateTime createdOn;
    private OffsetDateTime expires;
    private ImageInfo imageInfo;
    private ModalitySession modalitySession;

    // Getters and Setters
    public static class ImageInfo {

        public AcquisitionInfo getAcquisitionInfo() {
            return acquisitionInfo;
        }

        public void setAcquisitionInfo(AcquisitionInfo acquisitionInfo) {
            this.acquisitionInfo = acquisitionInfo;
        }

        public LutInfo getLutInfo() {
            return lutInfo;
        }

        public void setLutInfo(LutInfo lutInfo) {
            this.lutInfo = lutInfo;
        }

        private AcquisitionInfo acquisitionInfo;
        private LutInfo lutInfo;

        // Getters and Setters
        public static class AcquisitionInfo {

            public String getBinning() {
                return binning;
            }

            public void setBinning(String binning) {
                this.binning = binning;
            }

            private String binning;

            // Getters and Setters
        }

        public static class LutInfo {

            public double getGamma() {
                return gamma;
            }

            public void setGamma(double gamma) {
                this.gamma = gamma;
            }

            public double getSlope() {
                return slope;
            }

            public void setSlope(double slope) {
                this.slope = slope;
            }

            public double getOffset() {
                return offset;
            }

            public void setOffset(double offset) {
                this.offset = offset;
            }

            public int getTotalGrays() {
                return totalGrays;
            }

            public void setTotalGrays(int totalGrays) {
                this.totalGrays = totalGrays;
            }

            public int getMinimumGray() {
                return minimumGray;
            }

            public void setMinimumGray(int minimumGray) {
                this.minimumGray = minimumGray;
            }

            public int getMaximumGray() {
                return maximumGray;
            }

            public void setMaximumGray(int maximumGray) {
                this.maximumGray = maximumGray;
            }

            private double gamma;
            private double slope;
            private double offset;
            private int totalGrays;
            private int minimumGray;
            private int maximumGray;

            // Getters and Setters
        }
    }

    public static class ModalitySession {

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getImageId() {
            return imageId;
        }

        public void setImageId(String imageId) {
            this.imageId = imageId;
        }

        private String sessionId;
        private String imageId;

        // Getters and Setters
    }
}

