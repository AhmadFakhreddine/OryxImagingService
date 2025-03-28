package com.oryx.imaging.Service.Native.IOSSDK;

 

 
/**
 *
 * @author Ahmad Fakhreddine
 */
@FunctionalInterface
public interface AcquisitionStatusProcessor {

    void process(AcquisitionStatus status);
}
