/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.edu.pg.eti.bikecounter;

import java.util.HashMap;

public class CyclingGattAttributes {
    private static HashMap<String, String> attributes = new HashMap<>();
    static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    static String CSC_MEASUREMENT_CHARACTERISTICS = "00002a5b-0000-1000-8000-00805f9b34fb";
    static String CYCLING_SPEED_AND_CADENCE_SERVICE = "00001816-0000-1000-8000-00805f9b34fb";

    static {
        // Service
        attributes.put(CYCLING_SPEED_AND_CADENCE_SERVICE, "Cycling Speed and Cadence");
        // Characteristic
        attributes.put(CSC_MEASUREMENT_CHARACTERISTICS, "CSC Measurement");
    }
}
