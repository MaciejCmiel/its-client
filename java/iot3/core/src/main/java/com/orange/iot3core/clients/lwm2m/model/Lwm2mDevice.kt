/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author
    Maciej Ćmiel       <maciej.cmiel@orange.com>
 */
package com.orange.iot3core.clients.lwm2m.model

import org.eclipse.leshan.client.`object`.Device

/**
 * A simple Lwm2mDevice for the Device (3) object.
 */
class Lwm2mDevice(manufacturer: String, modelNumber: String, serialNumber: String, supportedBinding: String) {

    val device: Device = Device(manufacturer, modelNumber, serialNumber, supportedBinding)

}