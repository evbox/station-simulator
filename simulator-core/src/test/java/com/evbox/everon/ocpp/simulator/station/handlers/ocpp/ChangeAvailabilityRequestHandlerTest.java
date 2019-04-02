package com.evbox.everon.ocpp.simulator.station.handlers.ocpp;

import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.AvailabilityManager;
import com.evbox.everon.ocpp.simulator.station.handlers.ocpp.support.AvailabilityStateMapper;
import com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.evbox.everon.ocpp.mock.constants.StationConstants.*;
import static com.evbox.everon.ocpp.simulator.station.evse.EvseStatus.AVAILABLE;
import static com.evbox.everon.ocpp.v20.message.station.ChangeAvailabilityRequest.OperationalStatus.OPERATIVE;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class ChangeAvailabilityRequestHandlerTest {


    @Mock
    AvailabilityManager availabilityManagerMock;
    @Mock
    AvailabilityStateMapper availabilityStateMapperMock;

    @InjectMocks
    ChangeAvailabilityRequestHandler changeAvailabilityRequestHandler;

    @Test
    @DisplayName("Should throw an IllegalArgumentException on invalid operational status")
    void shouldThrowExceptionOnInvalidOperationalStatus() {

        when(availabilityStateMapperMock.mapFrom(any(ChangeAvailabilityRequest.OperationalStatus.class))).thenThrow(new IllegalArgumentException("some runtime exception"));

        ChangeAvailabilityRequest request = new ChangeAvailabilityRequest().withEvseId(DEFAULT_EVSE_ID).withOperationalStatus(OPERATIVE);

        assertThrows(IllegalArgumentException.class, () -> changeAvailabilityRequestHandler.handle(DEFAULT_MESSAGE_ID, request));

    }

    @Test
    @DisplayName("Should change evse availability when evseId is not equal to 0")
    void shouldCallChangeEvseAvailability() {
        when(availabilityStateMapperMock.mapFrom(eq(OPERATIVE))).thenReturn(AVAILABLE);

        ChangeAvailabilityRequest request = new ChangeAvailabilityRequest().withEvseId(DEFAULT_EVSE_ID).withOperationalStatus(OPERATIVE);

        changeAvailabilityRequestHandler.handle(DEFAULT_MESSAGE_ID, request);

        verify(availabilityManagerMock).changeEvseAvailability(anyString(), any(ChangeAvailabilityRequest.class), eq(AVAILABLE));

    }

    @Test
    @DisplayName("Should change station availability when evseId is equal to 0")
    void shouldCallChangeStationAvailability() {
        when(availabilityStateMapperMock.mapFrom(eq(OPERATIVE))).thenReturn(AVAILABLE);

        ChangeAvailabilityRequest request = new ChangeAvailabilityRequest().withEvseId(EVSE_ID_ZERO).withOperationalStatus(OPERATIVE);

        changeAvailabilityRequestHandler.handle(DEFAULT_MESSAGE_ID, request);

        verify(availabilityManagerMock).changeStationAvailability(anyString(), eq(AVAILABLE));

    }
}
