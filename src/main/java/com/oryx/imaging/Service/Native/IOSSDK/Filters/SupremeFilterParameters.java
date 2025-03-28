
package com.oryx.imaging.Service.Native.IOSSDK.Filters;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 *
 * @author Ahmad Fakhreddine
 */
public class SupremeFilterParameters implements Filter {

    public enum TaskNames {
        GENERAL("GENERAL"),
        ENDODONTIC("ENDODONTIC"),
        PERIODONTIC("PERIODONTIC"),
        RESTORATIVE("RESTORATIVE"),
        HYGIENE("HYGIENE");

        private final String value;

        TaskNames(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }
    }

    private TaskNames task;
    private double sharpness;

    public SupremeFilterParameters() {
    }

    public SupremeFilterParameters(TaskNames task, double sharpness) {
        this.task = task;
        this.sharpness = sharpness;
    }

    public TaskNames getTask() {
        return task;
    }

    public void setTask(TaskNames task) {
        this.task = task;
    }

    public double getSharpness() {
        return sharpness;
    }

    public void setSharpness(double sharpness) {
        this.sharpness = sharpness;
    }

    @Override
    public String toString() {
        return "SupremeFilterParameters{"
                + "task=" + task
                + ", sharpness=" + sharpness
                + '}';
    }

    @Override
    public ObjectNode toJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonNode = objectMapper.createObjectNode();
        jsonNode.put("task", task.getValue());
        jsonNode.put("sharpness", sharpness);
        jsonNode.put("filtername", "SUPREME");
        return jsonNode;
    }

}
