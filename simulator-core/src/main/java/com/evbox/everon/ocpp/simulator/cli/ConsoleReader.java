package com.evbox.everon.ocpp.simulator.cli;

import com.evbox.everon.ocpp.simulator.station.Station;
import com.evbox.everon.ocpp.simulator.station.StationMessage;
import com.evbox.everon.ocpp.simulator.station.actions.user.UserMessage;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Reads user input from the console
 */
public class ConsoleReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleReader.class);

    private static final String SHOW_STATION_STORE_CMD = "stat";
    private static final String SHOW_STATION_CERTIFICATE_CMD = "cert";

    private int selectedStation;

    private final List<Station> stations;
    private final ExecutorService consoleReaderExecutorService = Executors.newSingleThreadExecutor();

    public ConsoleReader(List<Station> stations) {

        this.stations = stations;

    }

    public void startReading() {

        consoleReaderExecutorService.submit(() -> {
            Scanner in = new Scanner(System.in, StandardCharsets.UTF_8.name());

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
        boolean showStationStoreCommand = commandArgs.size() == 1 && SHOW_STATION_STORE_CMD.equalsIgnoreCase(commandName);
        boolean showStationCertificateCommand = commandArgs.size() == 1 && SHOW_STATION_CERTIFICATE_CMD.equalsIgnoreCase(commandName);

        if (selectStationCommand) {
            selectNewStation(Integer.parseInt(commandName));
        } else if (ConsoleCommand.contains(commandName)) {
            UserMessage userMessage = ConsoleCommand.toUserMessage(commandName, commandArgs.subList(1, commandArgs.size()));

            Station station = stations.get(selectedStation);

            station.sendMessage(new StationMessage(station.getConfiguration().getId(), StationMessage.Type.USER_ACTION, userMessage));

        } else if (showStationStoreCommand) {
            showStationStore();
        } else if (showStationCertificateCommand) {
            showStationCertificate();
        } else if (isNotBlank(commandName)) {
            System.out.println("Unknown command: " + commandName); //NOSONAR
        }
    }

    private void showStationStore() {
        System.out.println(stations.get(selectedStation).getStateView()); //NOSONAR
    }

    private void showStationCertificate() {
        System.out.println(stations.get(selectedStation).getStateView().getStationCertificate()); //NOSONAR

    }

    private void selectNewStation(int newStationIndex) {
        boolean inAvailableStationsRange = newStationIndex == 0 || newStationIndex < stations.size();
        Preconditions.checkArgument(inAvailableStationsRange,
                "Station index is not applicable. Select between 0 and %s", stations.size() - 1);
        selectedStation = newStationIndex;
    }

    private void showStationsList() {
        System.out.println(); //NOSONAR
        System.out.println("List of stations:"); //NOSONAR

        String stationsList = IntStream.range(0, stations.size()).mapToObj(i -> {
            Station station = stations.get(i);
            String stationId = station.getConfiguration().getId();
            return (i == selectedStation ? "[SELECTED]: " : i + ": ") + stationId;
        }).collect(joining("\n"));

        System.out.println(stationsList); //NOSONAR
        if (stations.size() > 1) {
            System.out.println(); //NOSONAR
            System.out.println("Select another station by typing its index [0-" + (stations.size() - 1) + "] and pressing ENTER"); //NOSONAR
        }

        String commands = "Available commands:\n";
        commands += "\tplug {evseId} {connectorId} - plug cable to given connector\n";
        commands += "\tunplug {evseId} {connectorId} - unplug cable from given connector\n";
        commands += "\tauth {tokenId} {evseId} - authorize token at given EVSE\n";
        commands += "\tprofile3 {endpoint} - switch to security profile 3\n";
        commands += "\tcert - print certificate of the station\n";
        commands += "\tstat - show state of selected station";

        System.out.println(commands); //NOSONAR
    }

}
