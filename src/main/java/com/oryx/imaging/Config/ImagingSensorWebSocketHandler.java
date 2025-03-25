package com.oryx.imaging.Config;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.oryx.imaging.Service.ImagingSensorService;
import com.oryx.imaging.Service.Twain.TwainSensorService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CopyOnWriteArraySet;

public class ImagingSensorWebSocketHandler extends TextWebSocketHandler {

    public ImagingSensorService sensor;
    private static final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("New WebSocket connection established: " + session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("Received message: " + payload);

        if (payload.startsWith("scan/twain/")) {
            String twainKey = payload.substring(11);
            startScanning(twainKey, session);
        } else if (payload.startsWith("scan/native/")) {
            String twainKey = payload.substring(12);
            startScanning(twainKey, session);
        } else if (payload.equals("stop/")) {
            stopScanning(session);
        } else if (payload.equals("source")) {
            getAvailableSources(session);
        } else {
            sendTextMessage(session, "Unknown command: " + payload);
        }
    }

    @Override
    public void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        byte[] imageData = message.getPayload().array();
        System.out.println("Received image data");

        try {
            sendImage(session, imageData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startScanning(String twainKey, WebSocketSession session) {
        TwainSensorService twainSensorService = new TwainSensorService(twainKey, 3, true, twainKey, 0, 0, false, this);
        sensor = twainSensorService;
        twainSensorService.start();
    }

    private void stopScanning(WebSocketSession session) {
        System.out.println("Stopping scan");
        try {
            if (sensor != null) {
                sensor.stop();
                sensor = null;
            }
            sendTextMessage(session, "Scan stopped.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getAvailableSources(WebSocketSession session) {
        // Implement logic for fetching available sources
    }

    private void sendTextMessage(WebSocketSession session, String message) throws IOException {
        session.sendMessage(new TextMessage(message));
    }

    private void sendImage(WebSocketSession session, byte[] imageData) throws IOException {
        BinaryMessage binaryMessage = new BinaryMessage(imageData);
        session.sendMessage(binaryMessage);
    }
 
    public  void sendImageFromPath(String imagePath) {
        System.out.println(imagePath);
        try {
            Path path = Paths.get(imagePath);
            if (!Files.exists(path)) {
                System.err.println("Error: Image file not found at " + imagePath);
                return;
            }

            byte[] imageData = Files.readAllBytes(path);
            BinaryMessage binaryMessage = new BinaryMessage(imageData);

            // Send the image to all connected clients
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(binaryMessage);
                }
            }
            System.out.println("Image sent to all clients from path: " + imagePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        sessions.remove(session);
        System.out.println("WebSocket session closed: " + session.getId());
    }
}
