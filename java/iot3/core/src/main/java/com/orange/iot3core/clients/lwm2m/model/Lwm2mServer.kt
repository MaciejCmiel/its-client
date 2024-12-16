/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author
    Maciej Ćmiel       <maciej.cmiel@orange.com>
 */
package com.orange.iot3core.clients.lwm2m.model

/**
 * Configuration for the LwM2M Server (1) Object.
 *
 * @property shortServerId The short server ID assigned to the target server.
 * @property lifetime The registration lifetime in seconds with the LwM2M server.
 * @property bindingMode The binding mode specifying supported communication protocol. Needed to identify the server in a multi-server setup.
 * @property notifyWhenDisable Indicates whether the client should notify the server upon disabling.
 */
data class Lwm2mServer(
        val shortServerId: Int,
        val lifetime: Int,
        val bindingMode: String,
        val notifyWhenDisable: Boolean = false

) {

    constructor(shortServerId: Int, lifetime: Int, bindingMode: String) :
            this(shortServerId, lifetime, bindingMode, false)

}
