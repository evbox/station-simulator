package com.evbox.everon.ocpp.simulator.station.model;

import com.evbox.everon.ocpp.v201.message.common.Evse;

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
