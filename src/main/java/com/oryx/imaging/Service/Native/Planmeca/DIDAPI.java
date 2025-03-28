package com.oryx.imaging.Service.Native.Planmeca;


import com.sun.jna.Library;


/**
 * @author ORYX 1
 */
    public interface DIDAPI extends  Library{
          short DIDAPI_initialize(short[] pt);
          short DIDAPI_inquire_devices(short devIndex,char[] typeID,short[] devType,short[] HWrevision,short[] SWrevision,short[] maxMode,short[] maxProg);
          // still need the HWrevision and SWrevision 
          short DIDAPI_select_device(short devIndex);//knowing the device index
          short DIDAPI_set_Dparam(int tag,double value);//setting the device params  
          short DIDAPI_init_grabbing(short enableCalibration);
          short DIDAPI_inquire_image(short mode,short prog,short enableCalibration,short[] imageWidth, short[] imageHeight,short[] pixelSizeH,short[] pixelSizeV,short[] pixelDepth,short[] scanDir);
          short DIDAPI_patient_selected(short flag);
          short DIDAPI_get_device_status(short[] scanLen);
          short DIDAPI_finish_grabbing();
          short DIDAPI_get_image(char[] buff,short depth,short skipFactor,short x0,short y0,short w,short h);
          short DIDAPI_save_image(String filename,short[] OSerror,short format);
          short DIDAPI_get_set_params(short operation,short[] paramData);
          short DIDAPI_get_max_param();
          short DIDAPIPM_inquire_devices(short devIndex,char[] typeID,short[] devType,short[] HWrevision,short[] SWrevision,short[] maxMode,short[] maxProg);
          short DIDAPI_exit();
          
    }

