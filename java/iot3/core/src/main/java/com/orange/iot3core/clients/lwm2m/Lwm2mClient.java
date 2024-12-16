/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @authors
    Mathieu LEFEBVRE   <mathieu1.lefebvre@orange.com>
    Zbigniew Krawczyk  <zbigniew2.krawczyk@orange.com>
    Maciej Ćmiel       <maciej.cmiel@orange.com>
 */
package com.orange.iot3core.clients.lwm2m;

import com.orange.iot3core.clients.lwm2m.model.Lwm2mConfig;
import com.orange.iot3core.clients.lwm2m.model.Lwm2mDevice;
import org.eclipse.leshan.client.californium.LeshanClient;
import org.eclipse.leshan.client.californium.LeshanClientBuilder;
import org.eclipse.leshan.client.object.Security;
import org.eclipse.leshan.client.object.Server;
import org.eclipse.leshan.client.resource.ObjectsInitializer;
import org.eclipse.leshan.core.LwM2mId;
import org.eclipse.leshan.core.request.BindingMode;
import org.eclipse.leshan.core.util.Hex;

public class Lwm2mClient {

    private LeshanClient client;
    private Lwm2mConfig lwm2mConfig;

    public Lwm2mClient(
            Lwm2mConfig lwm2mConfig,
            Lwm2mDevice lwm2mDevice,
            boolean autoConnect
    ) {
        this.lwm2mConfig = lwm2mConfig;

        LeshanClientBuilder builder = new LeshanClientBuilder(lwm2mConfig.getEndpointName());

        // create objects
        ObjectsInitializer initializer = new ObjectsInitializer();

        if (lwm2mConfig instanceof Lwm2mConfig.Lwm2mBootstrapConfig lwm2mBootstrapConfig) {
            initializer.setInstancesForObject(
                    LwM2mId.SECURITY,
                    Security.pskBootstrap(
                            lwm2mBootstrapConfig.getUri(),
                            lwm2mBootstrapConfig.getPskIdentity().getBytes(),
                            Hex.decodeHex(lwm2mBootstrapConfig.getPrivateKey().toCharArray())
                    )
            );
        }
        if (lwm2mConfig instanceof Lwm2mConfig.Lwm2mClassicConfig lwm2mClassicConfig) {
            initializer.setInstancesForObject(
                    LwM2mId.SECURITY,
                    Security.psk(
                            lwm2mClassicConfig.getUri(),
                            lwm2mClassicConfig.getShortServerId(),
                            lwm2mClassicConfig.getPskIdentity().getBytes(),
                            Hex.decodeHex(lwm2mClassicConfig.getPrivateKey().toCharArray())
                    )
            );
        }

        initializer.setInstancesForObject(
                LwM2mId.SERVER,
                new Server(
                        lwm2mConfig.getServerConfig().getShortServerId(),
                        lwm2mConfig.getServerConfig().getLifetime(),
                        BindingMode.valueOf(lwm2mConfig.getServerConfig().getBindingMode()),
                        lwm2mConfig.getServerConfig().getNotifyWhenDisable()
                )
        );

        initializer.setInstancesForObject(LwM2mId.DEVICE, lwm2mDevice.getDevice());

        builder.setObjects(initializer.createAll());
//        initializer.setInstancesForObject(3441, new LwM2mTestObject());

        client = builder.build();

        if (autoConnect) connect();
    }

    public Lwm2mClient(
            Lwm2mConfig lwm2mConfig,
            Lwm2mDevice lwm2mDevice
    ) {
        this(lwm2mConfig, lwm2mDevice, true);
    }

    public void disconnect() {
        disconnect(true);
    }

    /**
     * Stops the client. This can either be a graceful shutdown with de-registration or an immediate stop without notifying the server.
     * The client can be restarted later using {@link #connect()}.
     *
     * @param deregister If {true}, the client sends a DEREGISTER request to the LwM2M server before stopping,
     *                   informing the server that it is intentionally disconnecting.
     */
    public void disconnect(boolean deregister) {
        client.stop(deregister);
    }

    public void connect() {
        System.out.println("LwM2M connecting with: " + lwm2mConfig.getUri());
        client.start();
    }

}
