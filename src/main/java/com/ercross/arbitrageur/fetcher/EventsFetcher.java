package com.ercross.arbitrageur.fetcher;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.ercross.arbitrageur.model.Event;
import com.ercross.arbitrageur.model.Event.SportType;

/**
 * @author Ercross
 *
 * Scrapes daily events from flashscore.com
 */
public class EventsFetcher implements Fetchable, Callable<List<Event>> {

    private static final Logger LOG = LogManager.getLogger("EventsFetcher.class");

    private final SportType sportType;
    private final LocalDate date;
    private final static String eventsDivSelector = "//div[@class='event']";

    //driver initialization not invoked within the class to synchronize access to the shared property
    private static WebDriver driver;

    public EventsFetcher() {
        sportType = null;
        date = null;
    }

    public EventsFetcher(SportType sportType) {
        this.sportType = sportType;
        this.date=null;
    }

    public EventsFetcher(SportType sportType, WebDriver driver, LocalDate date) {
        this.sportType = sportType;
        EventsFetcher.driver = driver;
        this.date = date;
    }

    @Override
    public List<Event> call() throws Exception {
        fetch();
        return (List<Event>) fetch();
    }

    @Override
    public Object fetch () {
        List<Event> todaysEvents = new ArrayList<>();
        String sportTypeUrl = prepareSportTypeUrl();
        navigateToSportTypePage(sportTypeUrl);
        findEventsByDate(this.date);
        String scrapedPage = scrapeEventsDiv();
        sortScrapedEventsPage(scrapedPage);
        Fetchable.tearDownWebDriver(EventsFetcher.driver);
        return todaysEvents;
    }

    //flashscore.com organize their web directories as flashscore.com/sportType. This method simply concatenates sportType to the base address
    private String prepareSportTypeUrl() {
        return "https://flashscore.com/" + this.sportType.getValue();
    }

    private void findEventsByDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException();
        }
        //int day = date.getDayOfMonth();
        driver.findElement(By.xpath("//div[@class='calendar__direction calendar__direction--tomorrow']")).click();
        //driver.findElement(By.xpath("//div[@class='day'][contains(text()," + day + ")]")).click();
        final WebDriverWait wait = new WebDriverWait (driver, Duration.ofSeconds(5));
        wait.until(page -> driver.findElement(By.xpath(eventsDivSelector)).isDisplayed());
    }

    private void navigateToSportTypePage(String sportTypeUrl) {
        try {
            driver.get(sportTypeUrl);
            final WebDriverWait wait = new WebDriverWait (driver, Duration.ofSeconds(10));
            wait.until(page -> driver.findElement(By.xpath(eventsDivSelector)).isDisplayed());
        }
        catch(NoSuchElementException e) {
            LOG.error("Flashscore: eventsDivSelector not found on page. It's either page is unavailable or the xpath has been changed");
        }
        catch (UnreachableBrowserException e) {
            LOG.error("flashscore: Unable to communicate with Browser while fetching events");
            navigateToSportTypePage(sportTypeUrl);
        }
    }

    //method scrappes the section where events are placed is
    private String scrapeEventsDiv() {
        String scrapedEventsPage = null;
        try {
            scrapedEventsPage = driver.findElement(By.xpath(eventsDivSelector)).getText();
        }
        catch(NoSuchElementException e) {
            LOG.error("Flashscore: eventsDivSelector not found on page. It's either page is unavailable or the xpath has been changed");
        }
        return scrapedEventsPage;
    }

    /*
     * Regular expressions written for event website, flashscore.com, text pattern
     * "Standings\n" matches exactly the occurrence of the string Standings followed by a new line
     * * matches zero or more occurrence of a regex
     * . matches any character. Combining this with * matches any number of characters
     * word pattern: [A-Za-z0-9_]+ matches one or more uppercase and lowercase characters
     * [0-9]+ matches one or more digits, same as \d+ above, but [0-9]+ is much clearer
     * () is used to group regex, so we can refer to particular groups by the syntax matcher.group(index)
     * Any string not placed within a pair of parentheses is not considered a group
     * ? makes the statement in the bracket optional or matches zero or one time
     * \s is any whitespace character. But since \s is not a valid escape sequence in Java, not in regex, we use \\ to imply \
     * \d+(\.\d+)?  \d+ matches one or more digits. The ? sign makes the pattern in parentheses, (\.\d+) optional
     * {x} means the regex it follows would occur x times
     * ^ specifies that the regex it precedes must start on a new line
     * $ checks if a line end follows. It is appended at the end of the regex like this "regex$" check if the string regex is followed by a line end
     * A pair of square bracket is used to define a character set.
     * Check https://www.regular-expressions.info/charclass.html for more info
     * Regex syntaxes culled from https://www.vogella.com/tutorials/JavaRegularExpressions/article.html
     */

    //this pattern contains:-EVENT_COUNTRY  leagueName Standings"
    //some leagueName occurs as "Liga Primera - Clausura\n" for example. matcher.group(2) accounts for this using the .*
    //In matcher.group(2), Liga Primera, for example, is two words, so \\s{1}[A-Za-z0-9_]* accounts for the other part, which is whitespacePrimera
    //******************************************matcher.group(1)****matcher.group(2)***********matcher.group(3)**********************
    private static final String EVENT_HEADER_INFO = "([A-Z]+)\\n" + "([A-Za-z0-9 ]+[A-Za-z0-9 ]*)\\n" + "Standings\\n";
    private static final Pattern HEADER_INFO_PATTERN= Pattern.compile(EVENT_HEADER_INFO, Pattern.MULTILINE);

    //this pattern contains:- eventTime HomeTeamName - awayTeamName
    //The non-grouped string between group2 and group3 accounts for the occasional hyphen between team names. Sometimes, its there, sometimes not
    //***************************************************matcher.group(1)**************matcher.group(2)*********************************matcher.group(3)
    private static final String EVENT_SPECIFIC_INFO = "([0-9]+:[0-9]+)\\n" + "([A-Za-z0-9 ]+[A-Za-z0-9() ]*)\\n" + "[[-]*\\n]*" + "([A-Za-z0-9 ]+[A-Za-z0-9() ]*)\\n";
    private static final Pattern EVENT_SPECIFIC_INFO_PATTERN = Pattern.compile(EVENT_SPECIFIC_INFO, Pattern.MULTILINE);


    /*
     * For each line of the scrappedPage, method checks if the line matches the form of either the header info or specific info
     * Once a header info is set, it can be used to set the field for one or more consecutive lines of specific info until another header info is matched
     */
    public List<Event> sortScrapedEventsPage (String scrapedEventPage) {
        Matcher headerInfoMatcher;
        Matcher eventSpecificInfoMatcher;

        String eventCountry = "";
        String leagueName = "";

        final List<String> lines = prepareEventInfo(scrapedEventPage);
        final List<Event> todaysEvents =  new ArrayList<>();
        for(String line: lines) {
            headerInfoMatcher = HEADER_INFO_PATTERN.matcher(line);
            eventSpecificInfoMatcher = EVENT_SPECIFIC_INFO_PATTERN.matcher(line);

            if (headerInfoMatcher.find()) {
                eventCountry = headerInfoMatcher.group(1);
                leagueName = headerInfoMatcher.group(2);
            } else if (eventSpecificInfoMatcher.find()) {
                Event event = new Event.EventBuilder()
                        .setEventCountry(toSentenceCase(eventCountry))
                        .setLeagueName(leagueName)
                        .setEventTime(LocalTime.parse(eventSpecificInfoMatcher.group(1)))
                        .setEventName(eventSpecificInfoMatcher.group(2) + "-" + eventSpecificInfoMatcher.group(3))
                        .setHomeTeamName(eventSpecificInfoMatcher.group(2))
                        .setAwayTeamName(eventSpecificInfoMatcher.group(3))
                        .setSportType(this.sportType)
                        .build();
                todaysEvents.add(event);
            }
        }
        return todaysEvents;
    }

    //The country name scraped from flashscore.com is in uppercase. This method converts to sentence case, the case used on most bookmakers' website
    private static String toSentenceCase (String word){
        Character firstLetter = word.charAt(0);
        String sentenceCasedWord = word.toLowerCase();
        StringBuilder build = new StringBuilder(sentenceCasedWord);
        build.replace(0,1,firstLetter.toString());
        sentenceCasedWord = build.toString();
        return sentenceCasedWord;
    }

    /*
     * On Flashscore.com, data to match HEADER_INFO or EVENT_SPECIFIC_INFO is usually spread across three consecutive lines
     * This method sorts the scrapedPage into the format recognized by the regexes, HEADER_INFO and EVENT_SPECIFIC_INFO
     */
    private List<String> prepareEventInfo(String scrapedPage) {
        String[] scrapPageLines = scrapedPage.split("\\n");
        List<String> lines = new ArrayList<>(Arrays.asList(scrapPageLines)); //to allow for modification of List in loops
        lines = removeRedundantStrings(lines);
        lines = conjuctEventInfo(lines);
        return lines;
    }

    private List<String> removeRedundantStrings(List<String> lines) {
        String currentLine;
        for (Iterator<String> iterator = lines.iterator(); iterator.hasNext();){
            currentLine = iterator.next();
            if (currentLine.equals("\\n")|| currentLine.equals("-"))
                iterator.remove();
        }
        return lines;
    }

    /*
     * Since both HEADER_INFO or EVENT_SPECIFIC_INFO are spread across three consecutively lines
     * This method concatenates every three lines of the scrapedPage together
     */
    private List<String> conjuctEventInfo (List<String> lines) {
        StringBuilder info = new StringBuilder();
        List<String> eventInfo = new ArrayList<>();
        for (int j=0; j<lines.size();) {
            for(int i=0; i<3;i++,j++) {
                if (j == lines.size())
                    break;
                info.append(lines.get(j) + "\n"); //StringBuilder strips off the newline character. So I appended it manually
            }
            eventInfo.add(info.toString());
            info.delete(0,info.length());
        }
        return eventInfo;
    }
}