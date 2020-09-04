package com.ercross.arbitrageur.fetcher.bookmaker.bet9ja;

import com.ercross.arbitrageur.adt.UrlTreeMap;
import com.ercross.arbitrageur.adt.exceptions.NodeNotFoundException;
import com.ercross.arbitrageur.model.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;

public class Bet9jaPageNavigator {

    private final Event eventDetails;
    private final WebDriver driver;

    protected Bet9jaPageNavigator(Event eventDetails, WebDriver driver) {
        this.eventDetails = eventDetails;
        this.driver = driver;
    }

    public static final String ROOT_NODE_KEY = "Homepage";
    public static final String ROOT_NODE_VALUE = "https://web.bet9ja.com/Sport/Default.aspx";
    public static UrlTreeMap<String, String> urlTree = new UrlTreeMap<>(ROOT_NODE_KEY, ROOT_NODE_VALUE );
    private static final Logger LOG = LogManager.getLogger(Bet9jaPageNavigator.class);

    /**
     * @return URL of the event page.
     */
    protected String navigateToEventPage() {
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
        String leaguePageUrl = null;
        try {
            leaguePageUrl = urlTree.getValue(eventDetails.getSportType().getValue(), eventDetails.getLeagueName());
        } catch (NodeNotFoundException e) {
            LOG.error("This code would never be reached");
        }
        return leaguePageUrl;
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
            LOG.error("Bet9ja: This element was not found" + e);
        }
        catch (UnreachableBrowserException e) {
            LOG.error("Bet9ja: Unable to communicate with Browser");
            navigateToEventPageFromHomepage();
        }
    }

    private void navigateToSportTypePageFromHomepage () {
        try {
            LOG.info("Bet9ja: on bet9ja homepage to get " + eventDetails.getEventName() + " markets. Start: navigating to " + eventDetails.getSportType().getValue() + " page");
            driver.findElement(By.xpath("//div[contains(@title,'" + eventDetails.getSportType().getValue() + "')]")).click();
            String sportTypePageUrl = driver.getCurrentUrl();
            urlTree.add(null, "Homepage", eventDetails.getSportType().toString(), sportTypePageUrl);
        }
        catch (NodeNotFoundException e) {
            LOG.error("Bet9ja: rootNode was not found on the urlTree" + e);
        }
    }

    private void navigateToEventCountryPageFromSportTypePage () {
        try {
            LOG.info("Bet9ja: Now on " + eventDetails.getSportType().getValue() + " page. Navigating to " + eventDetails.getEventCountry() + " page");
            driver.findElement(By.xpath("//div[contains(@title,'" + eventDetails.getEventCountry() + "')]")).click();
            String eventCountryPageUrl = driver.getCurrentUrl();
            urlTree.add(eventDetails.getSportType().getValue(), eventDetails.getSportType().getValue(), eventDetails.getEventCountry(), eventCountryPageUrl);
        }
        catch (NodeNotFoundException e) {
            LOG.error("Bet9ja: Parent sportType url was not found on the urlTree" + e);
        }
    }

    private void navigateToLeaguePageFromCountryPage () {
        try {
            LOG.info("Bet9ja: Now on " + eventDetails.getEventCountry() + " page. Navigating to " + eventDetails.getLeagueName() + " page");
            driver.findElement(By.xpath("//div[contains(@title,'" + eventDetails.getLeagueName() + "')]")).click();
            String leaguePageUrl = driver.getCurrentUrl();
            urlTree.add(eventDetails.getSportType().getValue(), eventDetails.getEventCountry(), eventDetails.getLeagueName(), leaguePageUrl);
        }
        catch (NoSuchElementException e) {
            LOG.error("Bet9ja: The " + eventDetails.getEventCountry() +  " element was not found" + e);
        } catch (NodeNotFoundException e) {
            LOG.error("Bet9ja: Parent country url was not found on the urlTree" + e);
        }
    }

    private void navigateToEventPageFromLeaguePage (String leaguePageUrl) {
        try {
            LOG.info("Bet9ja: Now on " + eventDetails.getLeagueName() + " page. Navigating to " + eventDetails.getEventName() + " page");
            driver.get(leaguePageUrl);
            driver.findElement(By.linkText("View")).click();
            driver.findElement(By.xpath("//div[contains(@title,'" + eventDetails.getEventName() + "')]")).click();
        }
        catch (NoSuchElementException e) {
            LOG.error("Bet9ja: This element was not found" + e);
        }
    }


}
