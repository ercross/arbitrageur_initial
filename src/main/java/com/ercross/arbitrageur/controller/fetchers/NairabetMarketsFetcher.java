package com.ercross.arbitrageur.controller.fetchers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.ercross.arbitrageur.adt.UrlTreeMap;
import com.ercross.arbitrageur.adt.exceptions.NodeNotFoundException;
import com.ercross.arbitrageur.exception.ZeroValueArgumentException;
import com.ercross.arbitrageur.model.Event;
import com.ercross.arbitrageur.model.Market;

import com.ercross.arbitrageur.util.MarketExtraction;

import io.github.bonigarcia.wdm.WebDriverManager;

public class NairabetMarketsFetcher implements Fetchable{

    private static final Logger LOG = LogManager.getLogger(NairabetMarketsFetcher.class);

    public static final String ROOT_NODE_KEY = "Homepage";
    public static final String ROOT_NODE_VALUE = "https://nairabet.com";
    public static UrlTreeMap<String, String> urlTree = new UrlTreeMap<String, String>(ROOT_NODE_KEY, ROOT_NODE_VALUE);
    private static WebDriver driver;

    private Map<String, String> regexToMatchOnScrappedPage = new HashMap<String, String>();
    private HashMap<Integer, Market> nairabetMarkets = new HashMap<Integer, Market> ();
    private Event eventDetails;

    public NairabetMarketsFetcher(final Event eventDetails, WebDriver driver) {
        this.eventDetails = eventDetails;
        NairabetMarketsFetcher.driver = driver;
        //this field is needed to querry bookmaker's name at runtime and used for logging purpose.
        //The key, 1, is uniform across all bookmakers
        Market bookmakerName = new Market.MarketBuilder().setBookmakerName("Nairabet").build();
        nairabetMarkets.put(1, bookmakerName);
    }

    //Each bookmaker may need different configurations of the WebDriver instance w.r.t chromeSettings
    //Hence the reason each bookmaker should configure their choice browser according to their requirement
    public static WebDriver initializeDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions chromeSettings = new ChromeOptions();
        chromeSettings.setExperimentalOption("excludeSwitches", new String[] {"enable-automation"});
        chromeSettings.addArguments("--headless", "--disable-gpu", "--ignore-certificate-errors", "--silent", "--disable--notifications");
        chromeSettings.setPageLoadStrategy(PageLoadStrategy.EAGER);
        final WebDriver driver = new ChromeDriver(chromeSettings);
        return driver;
    }

    public HashMap<Integer, Market> getNairabetMarkets() {
        fetch();
        return nairabetMarkets;
    }

    @Override
    /**
     * This method is only used to satisfy the contract provided by Fetchable and should never be invoked outside its class. Use getNairabetMarkets()
     */
    //TODO find a better workaround for this method. Should probably be deleted
    public void fetch() {
        try {
            initRegexMap();
            navigateToEventPage();
            String eventPage = scrapeEventPage();
            nairabetMarkets = MarketExtraction.extractMarketsFromScrapedPage(regexToMatchOnScrappedPage, eventPage);
            LOG.info("Nairabet markets on the event " + eventDetails.toString()+ " ready");
        }
        catch (ZeroValueArgumentException e) {
            LOG.error("Nairabet: The scraped page has no content. Maybe you need to checkout what's wrong with the page" + e);
        }
    }

    /*
     * This method performs the navigation to the event page and fetching the event page using the private scrapeEventPage helper method
     * It does its navigation by clicking on xpaths containing words a human would click on when manually navigating to an event page
     * For instance, on nairabet's homepage is a text that reads "Football". Clicking on this link navigates to the n soccer page.
     * This method automates this process
     */
    private void navigateToEventPage () {

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
            LOG.error("This code will never be reached");
        }
        return leaguePageUrl;
    }

    private void navigateToEventPageFromLeaguePage() {
        try {
            LOG.info("Nairabet: Attempting to click the " + eventDetails.getEventName() + " on " + eventDetails.getLeagueName() + " page"  );
            driver.findElement(By.xpath("//p[text()='" + eventDetails.getEventName() + "']")).click();
            LOG.info("Nairabet: Now on " + eventDetails.getEventName() + " page");
        }
        catch (NoSuchElementException e) {
            LOG.error("Nairabet: This element was not found" + e);
        }
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
            LOG.error("Nairabet: Unable to communicate with Browser");
            navigateToEventPageFromHomepage();
        }
    }

    private void navigateToSportTypePageFromHomepage() {
        LOG.info("Nairabet: Attempting to click on " + eventDetails.getSportType().getValue());
        try {
            if(eventDetails.getSportType().getValue() == "SOCCER") {
                driver.findElement(By.xpath("//div[a[span[contains(text(),'FOOTBALL')]]]")).click();
            }
            else {
                //the sportType text on nairabet's webpage are in uppercase and it seems selenium webdriver is case sensitive since it's written in Java
                driver.findElement(By.xpath("//div[a[span[contains(text(),'" + eventDetails.getSportType().getValue().toUpperCase() + "')]]]")).click();
            }
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
            driver.findElement(By.xpath("//div[span[contains(text(),'" + eventDetails.getEventCountry() + "')]]")).click();
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

    private static String scrapeEventPage() {
        String allMarketTabXPath = "//div[contains(text(),'All Markets')]";
        String marketsContainerXPath = "//div[@class='row']";
        String scrapedEventPage = null;

        try {
            final WebDriverWait wait = new WebDriverWait (driver, Duration.ofSeconds(15));
            scrapedEventPage = wait.until(page -> driver.findElement(By.xpath(allMarketTabXPath)).getText());
            driver.findElement(By.xpath(allMarketTabXPath)).click();
            clickOpenAllOddContainersOnPage(driver);
        }
        catch(NoSuchElementException e) {
            //Log element not found exception while trying to scrapeEventPage
        }
        return scrapedEventPage;
    }

    /**
     * This method loads regular expression containing patterns the matches expected markets for this bookmaker
     *
     * Nairabet's events are usually written with the actual homeTeamName and awayTeamName instead of the generic home and away as it is with bet9ja
     * Hence, this method should be invoked for every event to enable changeHomeAndAwayTeamNameInRegex() run for each event
     */
    private void initRegexMap () {
        final String resourceFile = "/TwoWayNairabetSoccer.txt";


        //InputStream resourceStream = ClassLoader.getSystemClassLoader().getResourceAsStream(resourceFile);
        try {
            List<String> lines = Files.readAllLines(Paths.get(resourceFile), StandardCharsets.UTF_8);
            MarketExtraction.putRegularExpressionsToMapFrom(lines);
            changeHomeAndAwayTeamNameInRegex(eventDetails);
        }
        catch (IOException e) {
            LOG.error("An error occurred while attempting to read Nairabet regex file" + e);
        }
    }

    //TODO ensure that the value of home and away team are put in brackets so it can be captured as a group since () is used for grouping regex
    //TODO edit replaceAll arg to tally with what's inside the txt file
    private void changeHomeAndAwayTeamNameInRegex(Event eventDetails) {
        regexToMatchOnScrappedPage.forEach((key, value) -> {
            if(value.contains("homeTeam") ) {
                value.replaceAll("homeTeam", eventDetails.getHomeTeamName());
            }
            if (value.contains("awayTeam") ) {
                value.replaceAll("awayTeam", eventDetails.getAwayTeamName());
            }
        });
    }


    //Nairabet place most of their market info in a CSS container which must be click opened to reveal the needed market. This method automates this
    private static void clickOpenAllOddContainersOnPage (WebDriver driver) throws NoSuchElementException {
        String closedOddsContainerXPath = "//div[@class='Market__Arrow-sc-1xtygzl-14 bApWvO']";
        List<WebElement> arrowsList = driver.findElements(By.xpath(closedOddsContainerXPath));
        System.out.println(arrowsList.size());
        for (WebElement arrow: arrowsList) {
            arrow.click();
        }
    }
}