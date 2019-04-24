package com.evbox.everon.ocpp.simulator.cli;

import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationMessage;
import com.evbox.everon.ocpp.simulator.station.actions.UserMessage;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
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

    private int selectedStation = 0;

    private final List<Station> stations;
    private final ExecutorService consoleReaderExecutorService = Executors.newSingleThreadExecutor();;

    public ConsoleReader(List<Station> stations) {

        this.stations = stations;

    }

    public void startReading() {

        consoleReaderExecutorService.submit(() -> {
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
            UserMessage userMessage = ConsoleCommand.toUserMessage(commandName, commandArgs.subList(1, commandArgs.size()));

            Station station = stations.get(selectedStation);

            station.sendMessage(new StationMessage(station.getConfiguration().getId(), StationMessage.Type.USER_ACTION, userMessage));

        } else if (showStationStateCommand) {
            showStationState();
        }
    }

    private void showStationState() {
        System.out.println(stations.get(selectedStation).getStateView());
    }

    private void selectNewStation(int newStationIndex) {
        boolean inAvailableStationsRange = newStationIndex == 0 || newStationIndex < stations.size();
        Preconditions.checkArgument(inAvailableStationsRange,
                "Station index is not applicable. Select between 0 and %s", stations.size() - 1);
        selectedStation = newStationIndex;
    }

    private void showStationsList() {
        System.out.println();
        System.out.println("List of stations:");

        String stationsList = IntStream.range(0, stations.size()).mapToObj(i -> {
            Station station = stations.get(i);
            String stationId = station.getConfiguration().getId();
            return (i == selectedStation ? "[SELECTED]: " : i + ": ") + stationId;
        }).collect(joining("\n"));

        System.out.println(stationsList);
        if (stations.size() > 1) {
            System.out.println();
            System.out.println("Select another station by typing its index [0-" + (stations.size() - 1) + "] and pressing ENTER");
        }

        String commands = "Available commands:\n";
        commands += "\tplug {evseId} {connectorId} - plug cable to given connector\n";
        commands += "\tunplug {evseId} {connectorId} - unplug cable from given connector\n";
        commands += "\tauth {tokenId} {evseId} - authorize token at given EVSE\n";
        commands += "\tstat - show state of selected station";

        System.out.println(commands);
    }

}
