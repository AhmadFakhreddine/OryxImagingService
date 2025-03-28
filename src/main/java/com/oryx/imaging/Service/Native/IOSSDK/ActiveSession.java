package com.oryx.imaging.Service.Native.IOSSDK;

 

 
import java.util.ArrayList;

/**
 *
 * @author Ahmad Fakhreddine
 */
public class ActiveSession {

    public String getSessionId() {
        return sessionId.replace("\"", "");
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId.replace("\"", "");
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

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public Object getContext() {
        return context;
    }

    public void setContext(Object context) {
        this.context = context;
    }

    public static ArrayList<ActiveSession> getSessions(String jsonResponse) {
        ArrayList<ActiveSession> sessions = new ArrayList<>();

        try {
            // Remove outer brackets and split the JSON objects
            jsonResponse = jsonResponse.trim();
            jsonResponse = jsonResponse.substring(1, jsonResponse.length() - 1); // Remove the square brackets
            String[] objects = jsonResponse.split("(?<=\\}),\\s*(?=\\{)"); // Split at }, {
            for (String object : objects) {
                ActiveSession session = new ActiveSession();
                session.setSessionId(DataHelper.extractValue(object, "sessionId"));
                session.setDeviceId(DataHelper.extractValue(object, "deviceId"));
                session.setClientName(DataHelper.extractValue(object, "clientName"));
                session.setCreatedOn(DataHelper.extractValue(object, "createdOn"));
                session.setCreatedOn(DataHelper.extractValue(object, "context"));
                sessions.add(session);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sessions;
    }

    private String sessionId;
    private String deviceId;
    private String clientName;
    private String createdOn;
    private Object context = new Object();
}

