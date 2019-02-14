package com.evbox.everon.ocpp.simulator.cli;

import com.evbox.everon.ocpp.simulator.StationSimulator;
import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationInboxMessage;
import com.evbox.everon.ocpp.simulator.user.interaction.UserAction;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

/**
 * Reads user input from the console
 */
public class ConsoleReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleReader.class);

    private static final String SHOW_STATION_STATE_CMD = "stat";

    private final StationSimulator simulator;

    private int selectedStation = 0;

    public ConsoleReader(StationSimulator simulator) {
        this.simulator = simulator;
        Executors.newSingleThreadExecutor().submit(() -> {
            Scanner in = new Scanner(System.in);

            showStationsList();

            String rawCommand;

            while ((rawCommand = in.nextLine()) != null) {
                LOGGER.debug("Received command: {}", rawCommand);
                try {
                    processCommand(asList(rawCommand.split("\\s")));
                } catch (Exception e) {
                    LOGGER.error("Unable to process:", e);
                }

                showStationsList();
            }
        });
    }

    private void processCommand(List<String> commandArgs) {
        String commandName = commandArgs.get(0);

        boolean selectStationCommand = commandArgs.size() == 1 && StringUtils.isNumeric(commandName);
        boolean showStationStateCommand = commandArgs.size() == 1 && SHOW_STATION_STATE_CMD.equalsIgnoreCase(commandName);

        if (selectStationCommand) {
            selectNewStation(Integer.valueOf(commandName));
        } else if (ConsoleCommand.contains(commandName)) {
            UserAction userAction = ConsoleCommand.toUserAction(commandName, commandArgs.subList(1, commandArgs.size()));
            simulator.getStations().get(selectedStation).getInbox().add(new StationInboxMessage(StationInboxMessage.Type.USER_ACTION, userAction));
        } else if (showStationStateCommand) {
            showStationState();
        }
    }

    private void showStationState() {
        System.out.println(simulator.getStations().get(selectedStation).getState());
    }

    private void selectNewStation(int newStationIndex) {
        boolean inAvailableStationsRange = newStationIndex == 0 || newStationIndex < simulator.getStations().size();
        Preconditions.checkArgument(inAvailableStationsRange,
                "Station index is not applicable. Select between 0 and %s", simulator.getStations().size() - 1);
        selectedStation = newStationIndex;
    }

    private void showStationsList() {
        System.out.println();
        System.out.println("List of stations:");
        List<Station> stations = simulator.getStations();
        String stationsList = IntStream.range(0, stations.size()).mapToObj(i -> {
            Station station = stations.get(i);
            String stationId = station.getConfiguration().getStationId();
            return (i == selectedStation ? "[SELECTED]: " : i + ": ") + stationId;
        }).collect(joining("\n"));

        System.out.println(stationsList);
        if (stations.size() > 1) {
            System.out.println();
            System.out.println("Select another station by typing its index [0-" + (stations.size() - 1) + "] and pressing ENTER");
        }

        String commands = "Available commands:\n";
        commands += "\tplug {connectorId} - plug cable to given connector\n";
        commands += "\tunplug {connectorId} - unplug cable from given connector\n";
        commands += "\tauth {tokenId} {evseId} - authorize token at given evse\n";
        commands += "\tstat - show state of selected station";

        System.out.println(commands);
    }
}
