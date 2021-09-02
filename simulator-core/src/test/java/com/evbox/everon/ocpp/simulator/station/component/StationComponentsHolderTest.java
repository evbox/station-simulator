package com.evbox.everon.ocpp.simulator.station.component;

import com.evbox.everon.ocpp.common.CiString;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationStore;
import com.evbox.everon.ocpp.v201.message.centralserver.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class StationComponentsHolderTest {

    @Mock
    Station station;
    @Mock
    StationStore stationStore;

    private StationComponentsHolder victim;

    @BeforeEach
    void setup() {
        victim = new StationComponentsHolder(station, stationStore);
    }

    @Test
    void testGetAllMonitored() {
        final int size = 5;

        ComponentVariable cv = getComponentVariable("component", "variable");
        ComponentVariable cv2 = getComponentVariable("component2", "variable2");

        for (int i = 0; i < size; i++) {
            victim.monitorComponent(1, cv, generateDatum(1, Monitor.DELTA, cv.getComponent(), cv.getVariable()));
            victim.monitorComponent(2, cv2, generateDatum(2, Monitor.DELTA, cv2.getComponent(), cv2.getVariable()));
            victim.monitorComponent(2, cv, generateDatum(3, Monitor.PERIODIC, cv.getComponent(), cv.getVariable()));
        }

        Map<ComponentVariable, List<SetMonitoringData>> result = victim.getAllMonitoredComponents();
        assertTrue(result.containsKey(cv));
        assertTrue(result.containsKey(cv2));
        assertThat(result).hasSize(2);
        assertThat(result.get(cv)).hasSize(size * 2);
        assertThat(result.get(cv2)).hasSize(size);
    }

    @Test
    void testClearMonitor() {
        ComponentVariable cv = getComponentVariable("component", "variable");
        ComponentVariable cv2 = getComponentVariable("component2", "variable2");

        victim.monitorComponent(1, cv, generateDatum(1, Monitor.DELTA, cv.getComponent(), cv.getVariable()));
        victim.monitorComponent(2, cv2, generateDatum(2, Monitor.DELTA, cv2.getComponent(), cv2.getVariable()));

        assertFalse(victim.clearMonitor(3));
        assertTrue(victim.clearMonitor(2));
        assertThat(victim.getAllMonitoredComponents()).hasSize(1);
        assertThat(victim.getAllMonitoredComponents().get(cv)).isNotNull();
    }

    @Test
    void testGetByComponentAndVariable() {
        final int size = 5;

        ComponentVariable cv = getComponentVariable("component", "variable");
        ComponentVariable cv2 = getComponentVariable("component2", "variable2");

        for (int i = 0; i < size; i++) {
            victim.monitorComponent(1, cv, generateDatum(1, Monitor.DELTA, cv.getComponent(), cv.getVariable()));
            victim.monitorComponent(2, cv2, generateDatum(2, Monitor.DELTA, cv2.getComponent(), cv2.getVariable()));
            victim.monitorComponent(2, cv, generateDatum(3, Monitor.PERIODIC, cv.getComponent(), cv.getVariable()));
        }

        Map<ComponentVariable, List<SetMonitoringData>> result = victim.getByComponentAndVariable(Collections.singletonList(cv));

        assertThat(result).hasSize(1);
        assertThat(result.get(cv)).hasSize(2 * size);

        result = victim.getByComponentAndVariable(Arrays.asList(cv, cv2));
        assertThat(result).hasSize(2);
        assertThat(result.get(cv)).hasSize(2 * size);
        assertThat(result.get(cv2)).hasSize(size);
    }

    private ComponentVariable getComponentVariable(String componentName, String variableName) {
        return new ComponentVariable()
                .withComponent(new Component().withName(new CiString.CiString50(componentName)))
                .withVariable(new Variable().withName(new CiString.CiString50(variableName)));
    }

    private SetMonitoringData generateDatum(int id, Monitor type, Component component, Variable variable) {
        return new SetMonitoringData()
                .withId(id)
                .withComponent(component)
                .withVariable(variable)
                .withSeverity(1)
                .withTransaction(false)
                .withValue(BigDecimal.ONE)
                .withType(type);
    }

}
