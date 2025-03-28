package com.oryx.imaging.Service.Native.IOSSDK;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.OffsetDateTime;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oryx.imaging.Service.Native.IOSSDK.Filters.AEFilterParameters;
import com.oryx.imaging.Service.Native.IOSSDK.Filters.ImageResource;
import com.oryx.imaging.Service.Native.IOSSDK.Filters.SelectFiltersParameters;
import com.oryx.imaging.Service.Native.IOSSDK.Filters.SupremeFilterParameters;

public class ApiClient {
    private static final String BASE_ADDRESS = "https://localhost:43809/api/dsio/modality/v1/";
    private static final String FILTERS_BASE_ADDRESS = "https://localhost:43809/api/dsio/filters/v1/";

    private static final String USERNAME = "3e2f265696283f8d5466eb6355c82f43e5a2570b1d3140be95e2e181c1744ac6@OryxImaging.OryxDentalSoftwareInc";
    private static final String PASSWORD = "OSd3Cr9qH9uukwx6YOCrI5UsJFnsi6EBZYFwPhX4bEWufe1r6lEsG0LFt2wYEYXqFWGzbyIgUWHgUlOuY56NpKi/GfzmrIyD8JGRS1P1o28DqkbsLjwDVWRw0DgMQ5+sipUxuWV3TCW+5z7WcmsywipALXfJpKr4v42gggOZ5oxlamfIxJKWyBdgEBtZV8wZiIgK0vGIl9qiREubMbk2Vtv64kC9XbmlCpyOyvZtfX0CeSCK8ZgTV007VxO0JMCs1t/TkKtZWowlvzTLf6c1Z8eyWVBExK1W0lXg3BvOHll0rPEjJiFiZw/7lP80jMNzPPX1DWJBUm3PfKjTfQg8yZt2Iz9kt54Ki7SNV3coDtibw3C2AxGlnZ5ztLact31tN2u5sVBeBuDAXRx7fcm+hFoo2sE/w0FUjB1l905jmY10wv4xnryQxFIO/k5lZmIbN515kg5aiysA3rI9+21Ba4Ad4VYqluuWZ32XeOgWz192ae05BeJxiQ6xp+MDK1FQka70vIx9fdaH2iqiIWGUyw==";

    private HttpClient httpClient = HttpClient.newHttpClient();

    public ApiClient() {
        try {
            httpClient = createHttpClient();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    public String authenticate() throws Exception {
        String auth = USERNAME + ":" + PASSWORD;
        String base64Auth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(BASE_ADDRESS))
                .header("Authorization", "Basic " + base64Auth)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            System.out.println("Authentication successful.");
            return response.body();
        } else {
            throw new RuntimeException("Authentication failed: " + response.body());
        }
    }

    private HttpClient createHttpClient() throws Exception {
        TrustManager[] trustAllCertificates = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType)
                            throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType)
                            throws CertificateException {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCertificates, new SecureRandom());

        return HttpClient.newBuilder()
                .sslContext(sslContext)
                .build();
    }

    public InputStream getImageData(String sessionId, String imageId) throws Exception {
        String auth = USERNAME + ":" + PASSWORD;
        String base64Auth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(BASE_ADDRESS + "acquisition/" + sessionId + "/images/" + imageId + "/media"))
                .header("Authorization", "Basic " + base64Auth)
                .GET()
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() == 200) {
            System.out.println("Image data retrieved successfully.");
            return response.body();
        } else {
            throw new RuntimeException("Failed to fetch image data: " + response.body());
        }
    }

    public ArrayList<Device> getDeviceList() throws Exception {
        String auth = USERNAME + ":" + PASSWORD;
        String base64Auth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(BASE_ADDRESS + "devices/"))
                .header("Authorization", "Basic " + base64Auth)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            System.out.println("Device list fetched successfully.");
            String responseBody = response.body();
            System.out.println(responseBody);
            return Device.getDevices(responseBody);
        } else {
            throw new RuntimeException("Failed to fetch device list: " + response.body());
        }
    }

    public boolean deleteAcquisitionSession(String sessionId) throws Exception {
        String auth = USERNAME + ":" + PASSWORD;
        String base64Auth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(BASE_ADDRESS + "acquisition/" + sessionId))
                .header("Authorization", "Basic " + base64Auth)
                .DELETE()
                .build();

        HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());

        if (response.statusCode() == 200 || response.statusCode() == 204) {

            System.out.println("Acquisition session deleted successfully.");
            return true;
        } else {
            System.err.println("Failed to delete acquisition session. Status: " + response.statusCode());
            return false;
        }
    }

    public ArrayList<ActiveSession> getAcquisitionSessions(String deviceId) throws Exception {
        String uri = BASE_ADDRESS + "acquisition/";
        if (deviceId != null && !deviceId.isEmpty()) {
            uri += "?deviceId=" + deviceId;
        }

        String auth = USERNAME + ":" + PASSWORD;
        String base64Auth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(uri))
                .header("Authorization", "Basic " + base64Auth)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {

            ArrayList<ActiveSession> avtiveSessions = new ArrayList<ActiveSession>();
            System.out.println("Acquisition sessions fetched successfully.");
            System.out.println("Response: " + response.body());
            if (response.body().equals("[]")) {
                return avtiveSessions;
            }
            return ActiveSession.getSessions(response.body());
        } else {
            throw new RuntimeException("Failed to fetch acquisition sessions: " + response.body());
        }
    }

    public String updateAcquisitionInfo(String sessionId, AcquisitionInfo acquisitionInfo) {
        try {
            System.out.println("updateAcquisitionInfo==============================================================");
            String uri = BASE_ADDRESS + "acquisition/" + sessionId + "/info";

            String auth = USERNAME + ":" + PASSWORD;
            String base64Auth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(uri))
                    .header("Authorization", "Basic " + base64Auth)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(
                            "{\"enable\":true,\"rotation\":0,\"binning\":null,\"applyLut\":true,\"context\":null}"))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("Acquisition info updated successfully.");
                System.out.println("Response: " + response.body());
                return response.body(); // Return the updated acquisition info as JSON
            } else {
                throw new RuntimeException("Failed to update acquisition info: " + response.body());
            }
        } catch (Exception ex) {
            System.out.println("" + ex.toString());
            return null;
        }

    }

    public void getAllImages(String sessionId) throws Exception {
        String uri = BASE_ADDRESS + "acquisition/" + sessionId + "/images";

        String auth = USERNAME + ":" + PASSWORD;
        String base64Auth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(uri))
                .header("Authorization", "Basic " + base64Auth)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            System.out.println("Images retrieved successfully.");
            System.out.println("Response: " + response.body());
        } else {
            throw new RuntimeException("Failed to retrieve images: " + response.body());
        }
    }

    public AcquisitionSession createAcquisitionSession(AcquisitionSessionInfo sessionInfo) throws Exception {

        // Manually creating JSON request body
        StringBuilder jsonRequestBuilder = new StringBuilder();
        jsonRequestBuilder.append("{");
        jsonRequestBuilder.append("\"deviceId\":\"").append(sessionInfo.getDeviceId()).append("\",");
        jsonRequestBuilder.append("\"clientName\":\"").append(sessionInfo.getClientName()).append("\"");
        jsonRequestBuilder.append("}");

        String jsonRequest = jsonRequestBuilder.toString();

        System.out.println(jsonRequest);

        // Basic Authentication setup
        String auth = USERNAME + ":" + PASSWORD;
        String base64Auth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        // HTTP request setup
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(BASE_ADDRESS + "acquisition"))
                .header("Authorization", "Basic " + base64Auth)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

        // Sending the request
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) {
            System.out.println("new response");
            System.out.println(response.body());

            // Manually parse the response body
            String responseBody = response.body();

            // Extract sessionId
            String sessionId = DataHelper.extractValue(responseBody, "sessionId");
            // Extract deviceId
            String deviceId = DataHelper.extractValue(responseBody, "deviceId");
            // Extract clientName
            String clientName = DataHelper.extractValue(responseBody, "clientName");

            // Create a new AcquisitionSession object
            AcquisitionSession session = new AcquisitionSession();
            session.setSessionId(sessionId.replace("\"", ""));
            session.setDeviceId(deviceId.replace("\"", ""));
            session.setClientName(clientName.replace("\"", ""));
            session.setCreatedOn(OffsetDateTime.MIN);

            return session;
        } else {
            throw new RuntimeException("Failed to create acquisition session: " + response.body());
        }
    }

    public AcquisitionStatus getAcquisitionStatus(String sessionId) throws Exception {
        String auth = USERNAME + ":" + PASSWORD;
        String base64Auth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(BASE_ADDRESS + "/acquisition/" + sessionId + "/status"))
                .header("Authorization", "Basic " + base64Auth)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            String responseBody = response.body();
            System.out.println(responseBody);
            AcquisitionStatus status = new AcquisitionStatus();
            boolean isReady = responseBody.contains("\"ready\":true");
            status.setReady(isReady);
            String stateValue = extractJsonValue(responseBody, "state").toUpperCase();
            try {
                status.setState(AcquisitionStatus.AcquisitionState.valueOf(stateValue));
            } catch (IllegalArgumentException e) {
                System.err.println("Unknown state received: " + stateValue);
                status.setState(AcquisitionStatus.AcquisitionState.ERROR); // Fallback to ERROR
            }

            status.setLastImageId(extractJsonValue(responseBody, "lastImageId"));
            String totalImagesStr = extractJsonValue(responseBody, "totalImages").trim(); // Trim spaces and newlines
            status.setTotalImages(totalImagesStr.isEmpty() ? 0 : Integer.parseInt(totalImagesStr));

            return status;
        } else {
            throw new RuntimeException("Failed to retrieve acquisition status: " + response.body());
        }
    }

     private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\":\"?([^\"]*?)\"?(,|\\})";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(json);
        return matcher.find() ? matcher.group(1) : "";
    }

    public AcquisitionStatusSubscription subscribeToAcquisitionStatus(String sessionId,
            AcquisitionStatusProcessor processor, int heartbeat) throws Exception {
        String auth = USERNAME + ":" + PASSWORD;
        String base64Auth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(BASE_ADDRESS + "/acquisition/" + sessionId + "/status/subscribe?heartbeat=" + heartbeat))
                .header("Authorization", "Basic " + base64Auth)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() == 200) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()));
            return new AcquisitionStatusSubscription(reader, processor);
        } else {
            throw new RuntimeException("Failed to subscribe to acquisition status: " + response.body());
        }
    }

    public InputStream selectFilter(String imageId, SelectFiltersParameters selectFilterParameters) throws Exception {
        String auth = USERNAME + ":" + PASSWORD;
        ObjectMapper objectMapper = new ObjectMapper();
        String base64Auth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        String jsonRequest = objectMapper.writeValueAsString(selectFilterParameters);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(FILTERS_BASE_ADDRESS + "images/" + imageId + "/filters/select"))
                .header("Authorization", "Basic " + base64Auth)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        } else {
            throw new IllegalStateException(
                    "HTTP request to select filter failed with status code: " + response.statusCode());
        }
    }

    public InputStream supremeFilter(String imageId, SupremeFilterParameters selectFilterParameters) throws Exception {
        String auth = USERNAME + ":" + PASSWORD;
        ObjectMapper objectMapper = new ObjectMapper();
        String base64Auth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        String jsonRequest = objectMapper.writeValueAsString(selectFilterParameters);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(FILTERS_BASE_ADDRESS + "images/" + imageId + "/filters/supreme"))
                .header("Authorization", "Basic " + base64Auth)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        } else {
            throw new IllegalStateException(
                    "HTTP request to select filter failed with status code: " + response.statusCode());
        }
    }

    public InputStream aeFilter(String imageId, AEFilterParameters selectFilterParameters) throws Exception {
        String auth = USERNAME + ":" + PASSWORD;
        ObjectMapper objectMapper = new ObjectMapper();
        String base64Auth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        String jsonRequest = objectMapper.writeValueAsString(selectFilterParameters);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(FILTERS_BASE_ADDRESS + "images/" + imageId + "/filters/ae"))
                .header("Authorization", "Basic " + base64Auth)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        } else {
            throw new IllegalStateException(
                    "HTTP request to select filter failed with status code: " + response.statusCode());
        }
    }

    public InputStream unmap(String imageId) throws Exception {
        String auth = USERNAME + ":" + PASSWORD;
        String base64Auth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(FILTERS_BASE_ADDRESS + "images/" + imageId + "/filters/unmap"))
                .header("Authorization", "Basic " + base64Auth)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(null))
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        } else {
            throw new IllegalStateException(
                    "HTTP request to select filter failed with status code: " + response.statusCode());
        }
    }

    public CompletableFuture<ImageResource> createImage(String sessionId, String imageId) throws Exception {
        ModalitySession modalitySession = new ModalitySession(sessionId, imageId);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        String auth = USERNAME + ":" + PASSWORD;
        String base64Auth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        String jsonRequest = objectMapper.writeValueAsString(modalitySession);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(FILTERS_BASE_ADDRESS + "images/modality"))
                .header("Authorization", "Basic " + base64Auth)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        try {

                            return parseResponseImage(response.body());
                        } catch (Exception e) {
                            throw new RuntimeException("Error parsing response", e);
                        }
                    } else {
                        throw new RuntimeException("HTTP request failed with status code: " + response.statusCode());
                    }
                });
    }

    public ImageResource parseResponseImage(String jsonResponse) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode jsonNode = objectMapper.readTree(jsonResponse);

        ImageResource imageResource = new ImageResource();

        imageResource.setId(jsonNode.get("id").asText());
        imageResource.setMediaType(jsonNode.get("mediaType").asText());
        imageResource.setUrl(jsonNode.get("url").asText());
        imageResource.setCreatedOn(OffsetDateTime.parse(jsonNode.get("createdOn").asText()));
        imageResource.setExpires(OffsetDateTime.parse(jsonNode.get("expires").asText()));

        JsonNode imageInfoNode = jsonNode.get("imageInfo");
        ImageResource.ImageInfo imageInfo = new ImageResource.ImageInfo();

        JsonNode acquisitionInfoNode = imageInfoNode.get("acquisitionInfo");
        ImageResource.ImageInfo.AcquisitionInfo acquisitionInfo = new ImageResource.ImageInfo.AcquisitionInfo();
        acquisitionInfo.setBinning(acquisitionInfoNode.get("binning").asText());
        imageInfo.setAcquisitionInfo(acquisitionInfo);

        JsonNode lutInfoNode = imageInfoNode.get("lutInfo");
        ImageResource.ImageInfo.LutInfo lutInfo = new ImageResource.ImageInfo.LutInfo();
        lutInfo.setGamma(lutInfoNode.get("gamma").asDouble());
        lutInfo.setSlope(lutInfoNode.get("slope").asDouble());
        lutInfo.setOffset(lutInfoNode.get("offset").asDouble());
        lutInfo.setTotalGrays(lutInfoNode.get("totalGrays").asInt());
        lutInfo.setMinimumGray(lutInfoNode.get("minimumGray").asInt());
        lutInfo.setMaximumGray(lutInfoNode.get("maximumGray").asInt());
        imageInfo.setLutInfo(lutInfo);

        imageResource.setImageInfo(imageInfo);

        JsonNode modalitySessionNode = jsonNode.get("modalitySession");
        ImageResource.ModalitySession modalitySession = new ImageResource.ModalitySession();
        modalitySession.setSessionId(modalitySessionNode.get("sessionId").asText());
        modalitySession.setImageId(modalitySessionNode.get("imageId").asText());
        imageResource.setModalitySession(modalitySession);

        return imageResource;
    }

    public class ModalitySession {

        @JsonProperty("SessionId")
        private String sessionId;

        @JsonProperty("ImageId")
        private String imageId;

        public ModalitySession(String sessionId, String imageId) {
            this.sessionId = sessionId;
            this.imageId = imageId;
        }

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
    }

}
