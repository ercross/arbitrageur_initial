package com.ercross.arbitrageur.fetcher.bookmaker.nairabet;

import com.ercross.arbitrageur.adt.UrlTreeMap;
import com.ercross.arbitrageur.adt.exceptions.NodeNotFoundException;
import com.ercross.arbitrageur.exception.WrongPatternException;
import com.ercross.arbitrageur.model.Event;
import com.ercross.arbitrageur.util.WordMatchFinder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;

import java.util.regex.Pattern;

public class NairabetPageNavigator {

    private static final Logger LOG = LogManager.getLogger(NairabetPageNavigator.class);

    private final Event eventDetails;
    private final WebDriver driver;

    protected NairabetPageNavigator(Event eventDetails, WebDriver driver) {
        this.eventDetails = eventDetails;
        this.driver = driver;
    }

    public static final String ROOT_NODE_KEY = "Homepage";
    public static final String ROOT_NODE_VALUE = "https://nairabet.com";
    public static UrlTreeMap<String, String> urlTree = new UrlTreeMap<>(ROOT_NODE_KEY, ROOT_NODE_VALUE);

    /**
     * @return URL of the event page.
     */
    protected String navigateToEventPage () {
        String eventPageUrl = "";
        if (urlTree.contains(eventDetails.getSportType().getValue(), eventDetails.getLeagueName())) {
            String leaguePageUrl = getLeaguePageUrlFromTree();
            navigateToEventPageFromLeaguePage(leaguePageUrl);
            eventPageUrl = driver.getCurrentUrl();
        }else {
            navigateToEventPageFromHomepage();
        }
        return  eventPageUrl;
    }

    private String getLeaguePageUrlFromTree () {
        String leaguePageUrl = "";
        try {
            leaguePageUrl = urlTree.getValue(eventDetails.getSportType().getValue(), eventDetails.getLeagueName());
        } catch (NodeNotFoundException e) {
            LOG.error("Unable to access Nairabet url tree");
        }
        return leaguePageUrl;
    }

    private void navigateToEventPageFromLeaguePage(String leaguePageUrl) {
        try {
            LOG.info("Nairabet: Attempting to click the " + eventDetails.getEventName() + " on " + eventDetails.getLeagueName() + " page"  );
            driver.get(leaguePageUrl);
            driver.findElement(By.xpath("//p[text()='" + eventDetails.getEventName() + "']")).click();
            LOG.info("Nairabet: Now on " + eventDetails.getEventName() + " page");
        }
        catch (NoSuchElementException e) {

            LOG.error("Nairabet: This element was not found" + e);
        }
    }

    private void findTeamNamesBestMatchOnPage() throws WrongPatternException {
        final String teamNamePattern = "";
        final Pattern eventPattern = Pattern.compile(teamNamePattern); //todo get pattern for the team name
        final String scrapedPage = ""; //todo get xpath for the event names
        if (WordMatchFinder.findTeamNamesBestMatch(eventDetails.getEventName(),scrapedPage, eventPattern, eventDetails.getEventTime()) != null) {

        }
    }

    private void navigateToEventPageFromHomepage () {
        try {
            final String homepageUrl = urlTree.getRootNode().getValue();
            driver.get(homepageUrl);
            navigateToSportTypePageFromHomepage();
            navigateToEventCountryPageFromSportTypePage();
            navigateToLeaguePageFromCountryPage();
            navigateToEventPageFromLeaguePage(driver.getCurrentUrl());
        }
        catch (NoSuchElementException e) {
            LOG.error("Nairabet: This element was not found");
            e.printStackTrace();
        }
        catch (UnreachableBrowserException e) {
            LOG.error("Nairabet: Unable to communicate with Browser");
            navigateToEventPageFromHomepage();
        }
    }

    private void navigateToSportTypePageFromHomepage() {
        LOG.info("Nairabet: Attempting to click on " + eventDetails.getSportType().getValue());
        try {
            //driver.findElement(By.xpath("//span[@class='VerticalSportList__Name-zv6yg9-4 fYZPUO'][contains(text(),'FOOTBALL')]")).click();
            driver.findElement(By.xpath("//div[a[span[contains(text(),'" + eventDetails.getSportType().getValue() + "')]]]")).click();
            LOG.info("Nairabet: Now on " + eventDetails.getSportType().getValue() + " page");
            String sportTypeUrl = driver.getCurrentUrl();
            urlTree.add(null, urlTree.getRootNode().getKey(), eventDetails.getSportType().getValue(), sportTypeUrl);
        }
        catch (NodeNotFoundException e) {
            LOG.error("Nairabet: rootNode not found on url tree" + e);
        }
        catch (NoSuchElementException e) {
            LOG.error("Nairabet: " + eventDetails.getSportType().getValue() + " element not found on homepage" + e);
        }
    }

    private void navigateToEventCountryPageFromSportTypePage() {
        LOG.info("Nairabet: Attempting to click " + eventDetails.getEventCountry() + " on " + eventDetails.getSportType().getValue() + " page");
        try {
            driver.findElement(By.xpath("//span[contains(text(),'" + eventDetails.getEventCountry() + "')]]")).click();
            String eventCountryUrl = driver.getCurrentUrl();
            urlTree.add(eventDetails.getSportType().getValue(), eventDetails.getSportType().getValue(), eventDetails.getEventCountry(), eventCountryUrl);
            LOG.info("Nairabet: " + eventDetails.getEventCountry() + " clicked");
        }
        catch (NoSuchElementException e) {
            LOG.error("Nairabet: " + eventDetails.getEventCountry() + " element not found");
        }
        catch (NodeNotFoundException e) {
            LOG.error("Nairabet: " + eventDetails.getEventCountry() + " url not found on tree");
        }
    }

    private void navigateToLeaguePageFromCountryPage() {
        LOG.info("Nairabet: Attempting to click on " + eventDetails.getLeagueName());
        try {
            driver.findElement(By.xpath("//div[span[contains(text(),'" + eventDetails.getLeagueName() + "')]]")).click();
            String leaguePageUrl = driver.getCurrentUrl();
            urlTree.add(eventDetails.getSportType().getValue(), eventDetails.getEventCountry(), eventDetails.getLeagueName(), leaguePageUrl);
            LOG.info("Nairabet: Now on " + eventDetails.getLeagueName() + " page");
        }
        catch (NoSuchElementException e) {
            LOG.error("Nairabet: " + eventDetails.getLeagueName() + " element not found");
        }
        catch (NodeNotFoundException e) {
            LOG.error("Nairabet: " + eventDetails.getEventCountry() + " url not found on tree");
        }
    }
}
