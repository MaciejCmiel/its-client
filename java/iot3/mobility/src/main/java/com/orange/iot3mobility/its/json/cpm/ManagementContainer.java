/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.cpm;

import com.orange.iot3mobility.its.json.JsonUtil;
import com.orange.iot3mobility.its.json.Position;
import com.orange.iot3mobility.its.json.PositionConfidence;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ManagementContainer {

    private static final Logger LOGGER = Logger.getLogger(ManagementContainer.class.getName());

    private final JSONObject jsonManagementContainer = new JSONObject();

    /**
     * Type of the emitting ITS-station.
     *
     * unknown(0), pedestrian(1), cyclist(2), moped(3), motorcycle(4), passengerCar(5), bus(6),
     * lightTruck(7), heavyTruck(8), trailer(9), specialVehicles(10), tram(11), roadSideUnit(15).
     */
    private final int stationType;

    /**
     * Position of the emitting ITS-station.
     */
    private final Position referencePosition;

    /**
     * Confidence of the emitting ITS-station position.
     */
    private final PositionConfidence confidence;

    public ManagementContainer(
          final int stationType,
          final Position referencePosition,
          final PositionConfidence confidence
    ) throws IllegalArgumentException {
        if(stationType > 255 || stationType < 0) {
            throw new IllegalArgumentException("CPM ManagementContainer StationType should be in the range of [0 - 255]."
                    + " Value: " + stationType);
        }
        this.stationType = stationType;
        if(referencePosition == null) {
            throw new IllegalArgumentException("CPM ManagementContainer ReferencePosition missing.");
        }
        this.referencePosition = referencePosition;
        if(confidence == null) {
            throw new IllegalArgumentException("CPM ManagementContainer Confidence missing.");
        }
        this.confidence = confidence;

        createJson();
    }

    private void createJson() {
        try {
            jsonManagementContainer.put(JsonCpmKey.ManagementContainer.STATION_TYPE.key(), stationType);
            jsonManagementContainer.put(JsonCpmKey.ManagementContainer.REFERENCE_POSITION.key(), referencePosition.getJson());
            jsonManagementContainer.put(JsonCpmKey.ManagementContainer.CONFIDENCE.key(), confidence.getJson());
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "CPM ManagementContainer JSON build error", "Error: " + e);
        }
    }

    public int getStationType() {
        return stationType;
    }

    public Position getReferencePosition() {
        return referencePosition;
    }

    public PositionConfidence getConfidence() {
        return confidence;
    }

    public JSONObject getJson() {
        return jsonManagementContainer;
    }

    public static ManagementContainer jsonParser(JSONObject json) {
        if(JsonUtil.isNullOrEmpty(json)) return null;
        try {
            int stationType = json.getInt(JsonCpmKey.ManagementContainer.STATION_TYPE.key());
            JSONObject jsonReferencePosition = json.getJSONObject(JsonCpmKey.ManagementContainer.REFERENCE_POSITION.key());
            Position referencePosition = Position.jsonParser(jsonReferencePosition);
            JSONObject jsonConfidence = json.getJSONObject(JsonCpmKey.ManagementContainer.CONFIDENCE.key());
            PositionConfidence confidence = PositionConfidence.jsonParser(jsonConfidence);

            return new ManagementContainer(stationType, referencePosition, confidence);
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "CPM ManagementContainer JSON parsing error", "Error: " + e);
        }
        return null;
    }

}
