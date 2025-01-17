/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.cpm;

import com.orange.iot3mobility.its.json.JsonUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public class StationDataContainer {

    private static final Logger LOGGER = Logger.getLogger(StationDataContainer.class.getName());

    private final JSONObject json = new JSONObject();

    /**
     * Contains the description of the emitting ITS-station if it is a vehicle
     */
    private final OriginatingVehicleContainer originatingVehicleContainer;

    /**
     * Contains the description of the emitting ITS-station if it is a Road-Side Unit
     */
    private final OriginatingRsuContainer originatingRsuContainer;

    public StationDataContainer(
            final OriginatingVehicleContainer originatingVehicleContainer
    ) {
        this(originatingVehicleContainer, null);
    }

    public StationDataContainer(
            final OriginatingRsuContainer originatingRsuContainer
    ) {
        this(null, originatingRsuContainer);
    }

    private StationDataContainer(
            final OriginatingVehicleContainer originatingVehicleContainer,
            final OriginatingRsuContainer originatingRsuContainer
    ) {
        this.originatingVehicleContainer = originatingVehicleContainer;
        this.originatingRsuContainer = originatingRsuContainer;

        createJson();
    }

    public void createJson() {
        try {
            if(originatingVehicleContainer != null)
                json.put(JsonCpmKey.StationDataContainer.ORIGINATING_VEHICLE_CONTAINER.key(),
                        originatingVehicleContainer.getJson());
            if(originatingRsuContainer != null)
                json.put(JsonCpmKey.StationDataContainer.ORIGINATING_RSU_CONTAINER.key(),
                        originatingRsuContainer.getJson());
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "CPM StationDataContainer JSON build error", "Error: " + e);
        }
    }

    public JSONObject getJson() {
        return json;
    }

    public OriginatingVehicleContainer getOriginatingVehicleContainer() {
        return originatingVehicleContainer;
    }

    public OriginatingRsuContainer getOriginatingRsuContainer() {
        return originatingRsuContainer;
    }

    public static StationDataContainer jsonParser(JSONObject json) {
        if(JsonUtil.isNullOrEmpty(json)) return null;
        JSONObject jsonOriginatingVehicleContainer = json.optJSONObject(JsonCpmKey.StationDataContainer.ORIGINATING_VEHICLE_CONTAINER.key());
        OriginatingVehicleContainer originatingVehicleContainer = OriginatingVehicleContainer.jsonParser(jsonOriginatingVehicleContainer);
        JSONObject jsonOriginatingRsuContainer = json.optJSONObject(JsonCpmKey.StationDataContainer.ORIGINATING_RSU_CONTAINER.key());
        OriginatingRsuContainer originatingRsuContainer = OriginatingRsuContainer.jsonParser(jsonOriginatingRsuContainer);

        return new StationDataContainer(originatingVehicleContainer, originatingRsuContainer);
    }

}
