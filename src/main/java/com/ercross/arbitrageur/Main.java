package com.ercross.arbitrageur;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;

import com.ercross.arbitrageur.adt.exceptions.NodeNotFoundException;
import com.ercross.arbitrageur.controller.fetchers.Bet9jaMarketsFetcher;
import com.ercross.arbitrageur.controller.fetchers.Fetchable;
import com.ercross.arbitrageur.model.Arbitrage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

import com.ercross.arbitrageur.controller.fetchers.EventsFetcher;
import com.ercross.arbitrageur.controller.fetchers.NairabetMarketsFetcher;
import com.ercross.arbitrageur.model.Event;
import com.ercross.arbitrageur.model.Event.SportType;

/*
 * Ensure it's one event at a time. Some bookmakers regexMap, e.g, nairabet, are static
 */
public class Main {
    private static final Logger LOG = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        LOG.info("Initializing SportArbitrageur web server...");
        List<Event> events;
        initAllStaticRegexMap();
        final Map<String, WebDriver> drivers = initAllDrivers();
        final ExecutorService executorService = Executors.newFixedThreadPool(2);


        while (true) {
            LocalDate tomorrowsDate = LocalDate.now().plusDays(1);
            events = fetchEvents(executorService, drivers, tomorrowsDate);
            while (!events.isEmpty()) {
                findArbitrageOnEachEvent(executorService, events, drivers);
            }
            clearAllUrlTrees();
        }
    }

    private static List<Event> fetchEvents(ExecutorService executorService,Map<String, WebDriver> drivers, LocalDate date) throws ExecutionException, InterruptedException {
        Future<List<Event>> eventsFetcherThread = executorService.submit(new EventsFetcher(SportType.SOCCER, drivers.get("FlashscoreDriver"),date ));
        return eventsFetcherThread.get();
    }

    //clearing all url trees in case any bookmaker makes a modification to site directories
    private static void clearAllUrlTrees() {
        try {
            Fetchable.reloadUrlTree(Bet9jaMarketsFetcher.urlTree, Bet9jaMarketsFetcher.ROOT_NODE_KEY, Bet9jaMarketsFetcher.ROOT_NODE_VALUE);
            Fetchable.reloadUrlTree(NairabetMarketsFetcher.urlTree, NairabetMarketsFetcher.ROOT_NODE_KEY, NairabetMarketsFetcher.ROOT_NODE_VALUE);
        } catch (NodeNotFoundException e) {
            LOG.error("Error encountered while reloading a urlTree" + e);
        }
    }

    private static void findArbitrageOnEachEvent (ExecutorService executorService, List<Event> events, Map<String, WebDriver> drivers) throws ExecutionException, InterruptedException {
        Event event;
        for (Iterator<Event> iterator = events.iterator(); iterator.hasNext(); ) {
            event = iterator.next();
            if (isEventStartsIn10Minutes(event.getEventTime(), LocalTime.now())) {
                iterator.remove();
                continue;
            }
            Future<List<Arbitrage>> arbitrageurThread = executorService.submit(new Arbitrageur(event, drivers));
            List<Arbitrage> arbitrages = arbitrageurThread.get();
            //TODO send arbitrage to client
        }
    }

    private static void sendArbitrageToClient(List<Arbitrage> arbitrages) {
        if (!arbitrages.isEmpty()) {
            //TODO send to client
        }
    }

    private static boolean isEventStartsIn10Minutes (LocalTime eventTime, LocalTime currentTime) {
        double eventTimeInMinutes = (eventTime.getHour() * 60) + eventTime.getMinute();
        double currentTimeInMinutes = (currentTime.getHour() * 60) + currentTime.getMinute();
        return (10 >= eventTimeInMinutes - currentTimeInMinutes);
    }

    private static void initAllStaticRegexMap () {
        Bet9jaMarketsFetcher.initRegexMap();
    }

    private static Map<String, WebDriver> initAllDrivers() {
        Map<String, WebDriver> drivers = new HashMap<String, WebDriver>();

        WebDriver bet9jaDriver = Bet9jaMarketsFetcher.initializeDriver();
        WebDriver nairabetDriver = NairabetMarketsFetcher.initializeDriver();
        WebDriver flashscoreDriver = EventsFetcher.initializeDriver();

        drivers.put("Bet9jaDriver", bet9jaDriver);
        drivers.put("NairabetDriver", nairabetDriver);
        drivers.put("FlashscoreDriver", flashscoreDriver);

        return drivers;
    }
}
