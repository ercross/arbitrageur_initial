package com.ercross.arbitrageur.controller.fetchers;

import com.ercross.arbitrageur.util.MarketExtraction;

import static com.ercross.arbitrageur.controller.fetchers.Fetchable.tearDownWebDriver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.ercross.arbitrageur.adt.UrlTreeMap;
import com.ercross.arbitrageur.adt.exceptions.NodeNotFoundException;
import com.ercross.arbitrageur.exception.ZeroValueArgumentException;
import com.ercross.arbitrageur.model.Event;
import com.ercross.arbitrageur.model.Market;

import io.github.bonigarcia.wdm.WebDriverManager;

public class Bet9jaMarketsFetcher implements Fetchable {

    private static final Logger LOG = LogManager.getLogger(Bet9jaMarketsFetcher.class);

    public static Map<String, String> regexToMatchOnScrappedPage = new HashMap<String, String>();
    public static final String ROOT_NODE_KEY = "Homepage";
    public static final String ROOT_NODE_VALUE = "https://web.bet9ja.com/Sport/Default.aspx";
    public static UrlTreeMap<String, String> urlTree = new UrlTreeMap<String, String>(ROOT_NODE_KEY, ROOT_NODE_VALUE );

    private HashMap<Integer, Market> bet9jaMarkets = new HashMap<Integer, Market> ();
    private final Event eventDetails;

    private static WebDriver driver;

    public Bet9jaMarketsFetcher(final Event eventDetails, WebDriver driver) {
        this.eventDetails = eventDetails;
        Bet9jaMarketsFetcher.driver = driver;
        //This field is needed to querry bookmaker's name at runtime and for now, used for logging purpose.
        //The integer key, 1, is uniform across all bookmakers
        final Market bookmakerName = new Market.MarketBuilder().setBookmakerName("Nairabet").build();
        bet9jaMarkets.put(1, bookmakerName);
    }

    /**
     * Recommended to be invoked to fetch bet9ja markets for the event passed to the class instance
     * @return bet9jaMarkets in a map where key is an integer value of markets hashcode
     */
    public HashMap<Integer,Market> getBet9jaMarkets() {
        fetch();
        return bet9jaMarkets;
    }

    public static WebDriver initializeDriver() {
        ChromeOptions chromeSettings = new ChromeOptions();
        WebDriverManager.chromedriver().setup();
        chromeSettings.addArguments("--disable-gpu");
        chromeSettings.addArguments("--headless");
        chromeSettings.addArguments("--ignore-certificate-errors");
        chromeSettings.addArguments("--silent");
        chromeSettings.addArguments("--disable--notifications");
        chromeSettings.setPageLoadStrategy(PageLoadStrategy.EAGER);
        final WebDriver driver = new ChromeDriver(chromeSettings);
        return driver;
    }

    private static String scrapeEventPage() {
        final String marketsDivXPath = "/html/body/form/div[7]/div[1]/div[2]/div/table/tbody/tr/td[2]/div/div/div/div[8]";
        String scrapedEventPage = null;
        try {
            final WebDriverWait wait = new WebDriverWait (driver, Duration.ofSeconds(10));
            LOG.info("Bet9ja: waiting for visibility of markets div xpath...");
            scrapedEventPage = wait.until(page -> driver.findElement(By.xpath(marketsDivXPath)).getText());
            LOG.info("Bet9ja markets div visible. Now scraping the div content...");
            LOG.debug("Bet9a markets div scrape successful");
            tearDownWebDriver(driver);
        }
        catch (NoSuchElementException e) {
            LOG.error("Bet9ja markets div xpath is invisible after wait period", e);
        }
        return scrapedEventPage;
    }

    @Override
    /**
     * This method is only used to satisfy the contract provided by Fetchable and should never be invoked outside its class. Use getBet9jaMarkets()
     */
    //TODO find a better workaround for this method. Should probably be deleted
    public void fetch() {
        try {
            navigateToEventPage();
            String eventPage = scrapeEventPage();
            bet9jaMarkets = MarketExtraction.extractMarketsFromScrapedPage(regexToMatchOnScrappedPage, eventPage);
            LOG.info("Bet9ja markets on the event " + eventDetails.toString()+ " ready");
        }
        catch (ZeroValueArgumentException e) {
            LOG.error("Bet9ja: The scraped page has no content. Maybe you need to checkout what's wrong with Betja page" + e);
        }

    }

    /*
     * This method performs the navigation to the event page and fetching the event page using the private scrapeEventPage helper method
     * It does its navigation by clicking on xpaths containing words a human would click on when manually navigating to an event page
     * For instance, on Bet9ja homepage is a text that reads "Soccer". Clicking on this link navigates to the soccer bet9ja soccer page.
     * This method automates this process
     */
    private void navigateToEventPage() {
        if (urlTree.isContains(eventDetails.getSportType().getValue(), eventDetails.getLeagueName())) {
            String leaguePageUrl = getLeaguePageUrlFromTree();
            driver.get(leaguePageUrl);
            navigateToEventPageFromLeaguePage();
        }else {
            navigateToEventPageFromHomepage();
        }
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
            navigateToEventPageFromLeaguePage();
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

    private void navigateToEventPageFromLeaguePage () {
        try {
            LOG.info("Bet9ja: Now on " + eventDetails.getLeagueName() + " page. Navigating to " + eventDetails.getEventName() + " page");
            driver.findElement(By.linkText("View")).click();
            driver.findElement(By.xpath("//div[contains(@title,'" + eventDetails.getEventName() + "')]")).click();
        }
        catch (NoSuchElementException e) {
            LOG.error("Bet9ja: This element was not found" + e);
        }
    }

    /**
     * This method should be invoked once throughout the entire runtime of the application.
     * It loads regular expression containing patterns the matches expected markets for this bookmaker
     */
    public static void initRegexMap () {
        String resourceFile = "/TwoWayBet9jaSoccer.txt";

        //InputStream resourceStream = ClassLoader.getSystemClassLoader().getResourceAsStream(resourceFile);
        try {
            List<String> lines = Files.readAllLines(Paths.get(resourceFile), StandardCharsets.UTF_8);
            MarketExtraction.putRegularExpressionsToMapFrom(lines);
        }
        catch (IOException e) {
            LOG.error("Bet9ja: an error occurred while attempting to read bet9ja regex text file" + e);
        }
    }
}
