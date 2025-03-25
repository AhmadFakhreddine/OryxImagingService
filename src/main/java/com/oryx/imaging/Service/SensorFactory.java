// package com.oryx.imaging.Service;

//  import org.springframework.stereotype.Service;

// import com.oryx.imaging.Service.Twain.TwainSensorService;

// @Service
// public class SensorFactory {
//      private TwainSensorService twainSensorService;

//     // @Autowired
//     // private NativeSensorService nativeSensorService;

//     public ImagingSensorService getSensorService(String sensorType) {
//         switch (sensorType) {
//             case "TWAIN":
//                 return twainSensorService;
//             case "NATIVE":
//             default:
//                 throw new IllegalArgumentException("Unknown sensor type");
//         }
//     }
// }
