/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.its.json.denm;

import com.orange.iot3mobility.its.json.JsonKey;

import com.orange.iot3mobility.its.json.JsonUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public class EventType {

    private static final Logger LOGGER = Logger.getLogger(EventType.class.getName());

    private final JSONObject jsonEventType = new JSONObject();
    private final int cause;
    private final int subcause;

    public EventType(
            final int cause,
            final int subcause)
    {
        if(cause > 255 || cause < 0) {
            throw new IllegalArgumentException("DENM EventType Cause should be in the range of [0 - 255]."
                    + " Value: " + cause);
        }
        this.cause = cause;
        if(subcause > 255 || subcause < 0) {
            throw new IllegalArgumentException("DENM EventType Subcause should be in the range of [0 - 255]."
                    + " Value: " + subcause);
        }
        this.subcause = subcause;

        createJson();
    }

    private void createJson() {
        try {
            jsonEventType.put(JsonKey.EventType.CAUSE.key(), cause);
            jsonEventType.put(JsonKey.EventType.SUBCAUSE.key(), subcause);
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "EventType JSON build error", "Error: " + e);
        }
    }

    public JSONObject getJsonEventType() {
        return jsonEventType;
    }

    public int getCause() {
        return cause;
    }

    public int getSubcause() {
        return subcause;
    }

    public static EventType jsonParser(JSONObject jsonEventType) {
        if(JsonUtil.isNullOrEmpty(jsonEventType)) return null;
        try {
            int cause = jsonEventType.getInt(JsonKey.EventType.CAUSE.key());
            int subcause = jsonEventType.getInt(JsonKey.EventType.SUBCAUSE.key());

            return new EventType(cause, subcause);
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "EventType JSON parsing error", "Error: " + e);
        }
        return null;
    }

}
