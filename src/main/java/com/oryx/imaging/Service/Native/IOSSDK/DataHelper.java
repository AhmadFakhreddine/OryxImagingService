package com.oryx.imaging.Service.Native.IOSSDK;

 
public class DataHelper {

    public static String extractValue(String json, String key) {
        int startIndex = json.indexOf("\"" + key + "\":") + key.length() + 3;
        if (json.charAt(startIndex) == '"') {
            startIndex++;
            int endIndex = json.indexOf('"', startIndex);
            return json.substring(startIndex, endIndex);
        } else {
            int endIndex = json.indexOf(',', startIndex);
            if (endIndex == -1) {
                endIndex = json.indexOf('}', startIndex);
            }
            return json.substring(startIndex, endIndex).trim();
        }
    }

 

}
