package com.evbox.everon.ocpp.simulator.station.model;

import com.evbox.everon.ocpp.v201.message.common.Evse;

//TODO introduced as stand-in for import com.evbox.everon.ocpp.v201.message.station.Reservation in OCPP 2.0 -> check this works for OCPP 2.0.1
public class Reservation {

    private final Integer id;
    private final Evse evse;

    public Reservation(Integer id, Evse evse) {
        this.id = id;
        this.evse = evse;
    }

    public Integer getId() {
        return id;
    }

    public Evse getEvse() {
        return evse;
    }
}
