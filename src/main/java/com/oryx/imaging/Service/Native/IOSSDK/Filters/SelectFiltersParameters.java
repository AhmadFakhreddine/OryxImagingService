package com.oryx.imaging.Service.Native.IOSSDK.Filters;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 *
 * @author Ahmad Fakhreddine
 */
public class SelectFiltersParameters implements Filter {



    // Enum to represent enhancement modes
    public enum EnhancementModes {
        SMOOTH("smooth"),
        EDGE_LOW("edgeLow"),
        EDGE_HIGH("edgeHigh"),
        EDGE_PRO("edgePro"),
        NONE("none");

        private final String value;

        EnhancementModes(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        // Add this method to get an enum from a string
        public static EnhancementModes fromString(String value) {
            for (EnhancementModes mode : EnhancementModes.values()) {
                if (mode.value.equalsIgnoreCase(value)) { // Case-insensitive match
                    return mode;
                }
            }
            throw new IllegalArgumentException("Invalid EnhancementMode: " + value);
        }
    }

    public SelectFiltersParameters(EnhancementModes _enhancementMode) {
        enhancementMode = _enhancementMode;
    }

    public SelectFiltersParameters() {
    }

    // Field to store the enhancement mode
    private EnhancementModes enhancementMode;

    // Getter for enhancementMode
    public EnhancementModes getEnhancementMode() {
        return enhancementMode;
    }

    // Setter for enhancementMode
    public void setEnhancementMode(EnhancementModes enhancementMode) {
        this.enhancementMode = enhancementMode;
    }

    @Override
    public ObjectNode toJson() {

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonNode = objectMapper.createObjectNode();
        jsonNode.put("task", enhancementMode.getValue());
        jsonNode.put("filtername", "SELECT");
        return jsonNode;
    }
}
