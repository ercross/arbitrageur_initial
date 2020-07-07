package com.ercross.arbitrageur.model;

import java.time.LocalTime;

public class Event {

    public enum SportType {
        HOCKEY("hockey"), SOCCER("soccer"), BASKETBALL("basketball"), TENNIS("tennis"),
        BOXING("boxing"), TABLE_TENNIS("table-tennis"), AUSSIE_RULES("aussie-rules"), RUGBY("rugby"),
        AMERICAN_FOOTBALL("american-football"), VOLLEYBALL("volleyball"), MOTOR_SPORT("motor-sport");

        private String value;

        private SportType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private String eventName;
    private SportType sportType;
    private String eventCountry;
    private String leagueName;
    private String homeTeamName;
    private String awayTeamName;
    private LocalTime eventTime;

    public String getHomeTeamName() {
        return homeTeamName;
    }

    public LocalTime getEventTime() {
        return eventTime;
    }

    public String getAwayTeamName() {
        return awayTeamName;
    }

    public String getEventName() {
        return eventName;
    }

    public SportType getSportType() {
        return sportType;
    }

    public String getEventCountry() {
        return eventCountry;
    }

    public String getLeagueName() {
        return leagueName;
    }


    @Override
    public String toString() {
        return "event name: " + eventName + "\n" +
                "sport type:" + sportType.getValue() + "\n" +
                "event country:" + eventCountry + "\n" +
                "home team:" + homeTeamName + " vs " +
                "away team:" + awayTeamName + " at " +
                "event time:" + eventTime + "\n";
    }

    public static class EventBuilder {
        private String eventName;
        private SportType sportType;
        private String eventCountry;
        private String leagueName;
        private String homeTeamName;
        private String awayTeamName;
        private LocalTime eventTime;

        public EventBuilder setEventName(String eventName) {
            this.eventName = eventName;
            return this;
        }

        public EventBuilder setEventTime(LocalTime eventTime) {
            this.eventTime = eventTime;
            return this;
        }

        public EventBuilder setSportType(SportType sportType) {
            this.sportType = sportType;
            return this;
        }
        public EventBuilder setEventCountry(String eventCountry) {
            this.eventCountry = eventCountry;
            return this;
        }
        public EventBuilder setLeagueName(String leagueName) {
            this.leagueName = leagueName;
            return this;
        }
        public EventBuilder setHomeTeamName(String homeTeamName) {
            this.homeTeamName = homeTeamName;
            return this;
        }
        public EventBuilder setAwayTeamName(String awayTeamName) {
            this.awayTeamName = awayTeamName;
            return this;
        }

        public Event build() {
            return new Event(this);
        }
    }

    private Event (EventBuilder eventBuilder) {
        eventName = eventBuilder.eventName;
        sportType = eventBuilder.sportType;
        eventCountry = eventBuilder.eventCountry;
        leagueName = eventBuilder.leagueName;
        homeTeamName = eventBuilder.homeTeamName;
        awayTeamName = eventBuilder.awayTeamName;
        eventTime = eventBuilder.eventTime;
    }
}