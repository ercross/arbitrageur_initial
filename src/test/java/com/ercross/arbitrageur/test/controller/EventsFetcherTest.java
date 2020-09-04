package com.ercross.arbitrageur.test.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ercross.arbitrageur.fetcher.EventsFetcher;
import com.ercross.arbitrageur.model.Event;
import com.ercross.arbitrageur.model.Event.SportType;

public class EventsFetcherTest {

    private static List<Event> expectedEvents = new ArrayList<Event>(3);

    @BeforeEach
    public void init() {
        Event event1 = new Event.EventBuilder()
                .setEventCountry("BURUNDI")
                .setLeagueName("National League")
                .setEventName("Buhumuza - Moso Sugar")
                .setHomeTeamName("Buhumaza")
                .setAwayTeamName("Moso Sugar")
                .setSportType(SportType.SOCCER)
                .setEventTime(LocalTime.of(14, 00))
                .build();

        Event event2 = new Event.EventBuilder()
                .setEventCountry("NICARAGUA")
                .setLeagueName("Liga Primera - Clausura")
                .setEventName("Ferretti - Sabanas")
                .setHomeTeamName("Ferreti")
                .setAwayTeamName("Sabanas")
                .setSportType(SportType.SOCCER)
                .setEventTime(LocalTime.of(23, 30))
                .build();

        Event event3 = new Event.EventBuilder()
                .setEventCountry("NICARAGUA")
                .setLeagueName("Liga Primera - Clausura")
                .setEventName("Juventus Managua - Ocotal")
                .setHomeTeamName("Juventus Managua")
                .setAwayTeamName("Ocotal")
                .setSportType(SportType.SOCCER)
                .setEventTime(LocalTime.of(21, 30))
                .build();

        expectedEvents.add(event1);
        expectedEvents.add(event2);
        expectedEvents.add(event3);
    }

    @Test
    public void testSortScrapedPage() {

        EventsFetcher fetcher = new EventsFetcher(SportType.SOCCER);

        List<Event> actualEvents = fetcher.sortScrapedEventsPage(scrapedEventPage);
        assertEquals(expectedEvents.size(), actualEvents.size());
    }

    private String scrapedEventPage = "\n" +

            // *****Event1*****
            "BURUNDI\n" + 						//eventCountry
            "National League B\n" + 			//leagueName
            "Standings\n" + 					//
            "14:00\n" + 						//eventTime
            "Buhumuza\n" + 						//homeTeamName
            "Moso Sugar\n" 						//awayTeamName

            +

            // *****Event2*****
            "NICARAGUA\n" +
            "Liga Primera - Clausura\n" +
            "Standings\n" +
            "23:30\n" +
            "Ferretti\n" +
            "Sabanas\n"

            +

            // ******Event3*****
            //eventCountry and leagueName should be same as that of event2
            "21:30\n" +
            "Juventus Managua U20\n" +
            "Ocotal U20\n";

}
