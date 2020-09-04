package com.ercross.arbitrageur.util;

import com.ercross.arbitrageur.exception.WrongPatternException;
import com.ercross.arbitrageur.model.Event;

import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Ercross
 */

//todo reduce code duplication between findTeamNamesBestMatch and findLeagueNameBestMatch, if possible.
public class WordMatchFinder {

    public WordMatchFinder() {
        throw new IllegalStateException();
    }

    /**
     * This Fuzzy String Matching algorithm is tailored for this application and does not use the Levenshtein distance algorithm.
     * Therefore it is not advised to use in another type of application.
     * Though algorithm runtime is O(N^2), optimization is unnecessary as the number of strings to process in the app is always very small
     *
     * FindTeamNamesBestMatch finds the string that closely match the target string from the scrapedPage
     *
     * Removing events with different start time increases the correctness of the returned result
     *
     * @param target would usually be in the format "homeTeamName - awayTeamName"
     * @param scrapedPage usually a page scraped from bookmakers' website
     * @param eventPattern a regex to specify how data is arranged on the scraped page, different from website to website.
     *                     Ensure that eventPattern captures the following data in the following groups
     *                     matcher.group(1) = homeTeamName
     *                     matcher.group(2) = awayTeamName
     *                     matcher.group(3) = eventTime
     * @param eventTime kick off time of the event
     * @return null if no match is found, bestMatch otherwise
     */
    public static String findTeamNamesBestMatch(String target, String scrapedPage, Pattern eventPattern, LocalTime eventTime) throws WrongPatternException {

        final Map<Integer, String> closeMatches = new HashMap<>();
        final List<Integer> matchCounts = new ArrayList<>();
        final String[] expectedTeamNames = target.split("-"); //teamName[0] = homeTeamName, teamName[1] = awayTeamName
        final List<Event> events = extractEventsFrom(scrapedPage, eventPattern);
        if (events.isEmpty() || scrapedPage.isEmpty())
            // Could have just returned null here, but a wrong pattern can arise from changes to the bookmaker's website
            // and this would require the dev to make changes to the compile another working regular expression.
            throw new WrongPatternException();
        removeEventsWithDifferentTimeIn(events, eventTime);

        events.forEach(event -> {
            int matchCount = matchStringsCharacterSequence(expectedTeamNames[0], event.getHomeTeamName());
            matchCount = matchCount + matchStringsCharacterSequence(expectedTeamNames[1], event.getAwayTeamName());
            closeMatches.put(matchCount,event.getEventName());
            matchCounts.add(matchCount);
        });
        int highestMatchCount = findHighestMatchCount(matchCounts);
        if (highestMatchCount == 0)
            return null;
        return closeMatches.get(highestMatchCount);
    }

    /**
     *
     * @param expectedLeagueName is the league's name expected to be found on the site, but not.
     * @param scrapedPage is the webpage on which the league name should be found
     * @param leagueNamesPattern a Pattern conforming to how league names are arranged on the site, to enable the page be sorted.
     *                           Ensure that leagueNamePattern captures the league names in matcher.group(1)
     * @return leagueNameClosestMatch
     */
    public static String findLeagueNameBestMatch (String expectedLeagueName, String scrapedPage, Pattern leagueNamesPattern) {
        String leagueNameClosestMatch = "";
        final Map<Integer, String> closeMatches = new HashMap<>();
        final List<Integer> matchCounts = new ArrayList<>();
        final List<String> leagueNames = sortScrapedLeaguesPage(scrapedPage, leagueNamesPattern);
        leagueNames.forEach( leagueName -> {
            int matchCount = matchStringsCharacterSequence(leagueName, expectedLeagueName);
            closeMatches.put(matchCount, leagueName);
            matchCounts.add(matchCount);
        });
        int highestMatchCount = findHighestMatchCount(matchCounts);
        if (highestMatchCount == 0)
            return null;
        return leagueNameClosestMatch;
    }

    private static List<String> sortScrapedLeaguesPage (String scrapedPage, Pattern leagueNamesPattern) {
        final List<String> leaguesNames = new ArrayList<>();
        final Matcher matcher = leagueNamesPattern.matcher(scrapedPage);
        while (matcher.find()) {
             leaguesNames.add(matcher.group(1));
        }
        return leaguesNames;
    }

    private static Integer findHighestMatchCount(List<Integer> matchCounts) {
        matchCounts = matchCounts.stream().sorted().collect(Collectors.toList()); //sorted in ascending order
        return matchCounts.get((matchCounts.size()-1)); //return the last element in the list. -1 accounts for the zero based array
    }

    /*
     * matches the sequence of characters not the number of same characters in both strings
     * Man will have a higher matchCount (3) than chester (0) in Manchester because the loop exits at encounter of the first character mismatch
     *
     * This method is suitable for one word strings i.e., strings not containing whitespaces
     */
    private static int matchOneWordCharacterSequence(String targetWord, String potentialMatch) {
        int matchCount = 0;

        for(int i=0; i<targetWord.length() && i<potentialMatch.length() ;i++) {
            if (targetWord.charAt(i) == potentialMatch.charAt(i))
                matchCount = matchCount + 1;
            else break;
        }
        return matchCount;
    }

    /*
     * MatchStringsCharacterSequence finds how closely a given string s1 matches with another string s2
     * taking into account that some strings/team names may occur as a two-word string.
     * Consider if s1 = Afc Bournemouth and s2 = Bournemouth, simply using matchOneWordCharacterSequence would return zero match count
     * since the method returns at first mismatch.
     *
     * explicit check for boolean evaluation in if conditions is intentional and should be preserved except changes would optimize the code
     */
    private static int matchStringsCharacterSequence (String s1, String s2) {
        int matchCount = 0;
        boolean s1IsTwoWord = false;
        boolean s2IsTwoWord = false;
        String[] firstWord, secondWord;

        if(s1.contains("\\s"))
            s1IsTwoWord = true;
        if(s2.contains("\\s"))
            s2IsTwoWord = true;

        if (s1IsTwoWord == true && s2IsTwoWord == true) {
            firstWord = s1.split("\\s");
            secondWord = s2.split("\\s");
            matchCount = matchOneWordCharacterSequence(firstWord[0], secondWord[0]);
            matchCount = matchCount + matchOneWordCharacterSequence(firstWord[1], secondWord[1]);
            return matchCount;
        }

        if (s1IsTwoWord == true && s2IsTwoWord == false) {
            firstWord = s1.split("\\s");
            matchCount = matchOneWordCharacterSequence(firstWord[0], s2);
            matchCount = matchCount + matchOneWordCharacterSequence(firstWord[1], s2);
            return matchCount;
        }

        if (s1IsTwoWord == false && s2IsTwoWord == true) {
            secondWord = s1.split("\\s");
            matchCount = matchOneWordCharacterSequence(s1, secondWord[0]);
            matchCount = matchCount + matchOneWordCharacterSequence(s1, secondWord[1]);
            return matchCount;
        }

        if (s1IsTwoWord == false && s2IsTwoWord == false) {
            matchCount = matchOneWordCharacterSequence(s1, s2);
            return matchCount;
        }
        return matchCount;
    }

    private static List<Event> removeEventsWithDifferentTimeIn(List<Event> events, LocalTime eventTime) {
        Iterator<Event> iterator = events.iterator();
        Event event;
        while (iterator.hasNext()) {
            event = iterator.next();
            if (event.getEventTime().isAfter(eventTime) || event.getEventTime().isBefore(eventTime))
                iterator.remove();
        }
        return events;
    }

    private static List<Event> extractEventsFrom (String page, Pattern eventPattern) {
        Event event;
        final List<Event> events = new ArrayList<>();
        final Matcher matcher = eventPattern.matcher(page);
        while (matcher.find()) {
           event = new Event.EventBuilder()
                    .setHomeTeamName(matcher.group(1))
                    .setAwayTeamName(matcher.group(2))
                    .setEventName(matcher.group(1) + "-" + matcher.group(2))
                    //time would always be in the format hh:mm.
                    .setEventTime(LocalTime.parse(matcher.group(3)))
                    .build();
            events.add(event);
        }
        return events;
    }
}
