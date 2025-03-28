package com.oryx.imaging.Service.Native.IOSSDK;

import java.io.BufferedReader;

public class AcquisitionStatusSubscription {

    private final BufferedReader reader;
    private final AcquisitionStatusProcessor processor;

    public AcquisitionStatusSubscription(BufferedReader reader, AcquisitionStatusProcessor processor) {
        this.reader = reader;
        this.processor = processor;
    }

    public void start() {
        new Thread(() -> {
            try {
                String line;
                while ((line = reader.readLine()) != null) {

                    try {
                        if (line.startsWith("data: ")) {
                            // Remove the "data: " prefix to extract the JSON part
                            String jsonPart = line.substring(6).trim();
                            System.out.print(jsonPart);

                            if (!jsonPart.contains("heartbeatTimeout")) {
                                System.out.print(jsonPart);
                            }
                            System.out.print(jsonPart);

                            AcquisitionStatus status = parseJson(jsonPart);
                            processor.process(status);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private AcquisitionStatus parseJson(String json) {
        AcquisitionStatus status = new AcquisitionStatus();

        status.setReady(extractBoolean(json, "\"ready\""));
        status.setState(extractState(json, "\"state\""));
        status.setLastImageId(extractString(json, "\"lastImageId\""));
        status.setTotalImages(extractInt(json, "\"totalImages\""));

        return status;
    }

    private boolean extractBoolean(String json, String key) {
        int index = json.indexOf(key);
        if (index != -1) {
            int valueStart = json.indexOf(":", index) + 1;
            String value = json.substring(valueStart, json.indexOf(",", valueStart)).trim();
            return value.equals("true");
        }
        return false;
    }

    private int extractInt(String json, String key) {
        int index = json.indexOf(key);
        if (index != -1) {
            int valueStart = json.indexOf(":", index) + 1;
            int valueEnd = json.indexOf(",", valueStart);
            if (valueEnd == -1) { 
                valueEnd = json.indexOf("}", valueStart); // If last element, look for closing brace
            }
            String value = json.substring(valueStart, valueEnd).trim();
            return Integer.parseInt(value);
        }
        return 0;
    }
    

    private String extractString(String json, String key) {
        int index = json.indexOf(key);
        if (index != -1) {
            int valueStart = json.indexOf(":", index) + 2; // Skip ": "
            int valueEnd = json.indexOf("\"", valueStart);
            return json.substring(valueStart, valueEnd);
        }
        return null;
    }

    private AcquisitionStatus.AcquisitionState extractState(String json, String key) {
        String stateStr = extractString(json, key);
        if (stateStr != null) {
            try {
                return AcquisitionStatus.AcquisitionState.valueOf(stateStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null; // Handle unknown states gracefully
            }
        }
        return null;
    }
}
