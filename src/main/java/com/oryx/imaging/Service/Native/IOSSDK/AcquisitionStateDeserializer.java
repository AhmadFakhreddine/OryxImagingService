package com.oryx.imaging.Service.Native.IOSSDK;



import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;

public class AcquisitionStateDeserializer extends JsonDeserializer<AcquisitionStatus.AcquisitionState> {
    @Override
    public AcquisitionStatus.AcquisitionState deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        try {
            return AcquisitionStatus.AcquisitionState.valueOf(p.getText().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null; // Handle invalid values gracefully
        }
    }
}
