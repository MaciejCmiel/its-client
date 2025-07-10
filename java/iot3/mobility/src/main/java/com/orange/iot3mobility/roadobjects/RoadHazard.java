/*
 Copyright 2016-2024 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 */
package com.orange.iot3mobility.roadobjects;

import com.orange.iot3mobility.TrueTime;
import com.orange.iot3mobility.its.json.denm.DENM;
import com.orange.iot3mobility.quadkey.LatLng;

public class RoadHazard {

    private final String uuid;
    private final int cause;
    private final int subcause;
    private LatLng position;
    private long timestamp;
    private int lifetime;
    private HazardType hazardType;
    private DENM denm;

    public RoadHazard(String uuid, int cause, int subcause, LatLng position, int lifetime, long timestamp, DENM denm) {
        this.uuid = uuid;
        this.cause = cause;
        this.subcause = subcause;
        this.position = position;
        this.lifetime = lifetime;
        this.denm = denm;
        this.timestamp = timestamp;
        findHazardType();
    }

    public String getUuid() {
        return uuid;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void updateTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
    }

    public boolean stillLiving() {
        return TrueTime.getAccurateTime() - timestamp < lifetime;
    }

    public HazardType getHazardType() {
        return hazardType;
    }

    public void setDenm(DENM denm) {
        this.denm = denm;
    }

    public DENM getDenm() {
        return denm;
    }

    private void findHazardType() {
        for(HazardType hazard: HazardType.values()) {
            if(hazard.getCause() == cause && hazard.getSubcause() == subcause) {
                hazardType = hazard;
                break;
            }
        }
        if(hazardType == null) hazardType = HazardType.UNDEFINED;
    }

}
