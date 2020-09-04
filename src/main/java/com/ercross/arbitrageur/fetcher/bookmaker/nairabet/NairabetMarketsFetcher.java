package com.ercross.arbitrageur.fetcher.bookmaker.nairabet;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ercross.arbitrageur.fetcher.Fetchable;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.ercross.arbitrageur.exception.ZeroValueArgumentException;
import com.ercross.arbitrageur.model.Event;
import com.ercross.arbitrageur.model.Market;

import com.ercross.arbitrageur.util.MarketExtraction;

public class NairabetMarketsFetcher implements Fetchable {

    private static final Logger LOG = LogManager.getLogger(NairabetMarketsFetcher.class);

    protected final WebDriver driver;
    protected final Event eventDetails;

    //created only to access fetcher interface default method from main
    public NairabetMarketsFetcher() {
        eventDetails=null;
        driver=null;
    }

    public NairabetMarketsFetcher(final Event eventDetails, final WebDriver driver) {
        this.eventDetails = eventDetails;
        this.driver = driver;
    }

    @Override
    public Object fetch() {

        //this field is needed to obtain bookmaker's name at runtime and used for logging purpose.
        //The key, 1, is uniform across all bookmakers
        final Market bookmakerName = new Market.MarketBuilder().setBookmakerName("Nairabet").build();

        final HashMap<Integer, Market> nairabetMarkets = new HashMap<> ();
        nairabetMarkets.put(1, bookmakerName);
        try {
            final Map<String, String> regexToMatchOnScrappedPage = fetchRegexMap();
            final String url = new NairabetPageNavigator( eventDetails, driver).navigateToEventPage();
            final String eventPage = scrapeEventPage(url);
            nairabetMarkets.putAll( MarketExtraction.extractMarketsFromScrapedPage(regexToMatchOnScrappedPage, eventPage) );
            LOG.info("Nairabet markets on the event " + eventDetails.toString()+ " ready");
        }
        catch (ZeroValueArgumentException e) {
            LOG.error("Nairabet: The scraped page has no content. Maybe you need to checkout what's wrong with the page" + e);
        }
        return nairabetMarkets;
    }

    private String scrapeEventPage(String pageUrl) {
        final String allMarketTabXPath = "//div[contains(text(),'All Markets')]";
        final String marketsContainerXPath = "//div[@class='row']";
        String scrapedEventPage = "";

        try {
            driver.get(pageUrl);
            final WebDriverWait wait = new WebDriverWait (driver, Duration.ofSeconds(15));
            driver.findElement(By.xpath(allMarketTabXPath)).click();
            clickOpenAllOddContainersOnPage(driver);
            scrapedEventPage = wait.until(page -> driver.findElement(By.xpath(marketsContainerXPath)).getText());
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
    private Map<String, String> fetchRegexMap () {
        final String resourceFile = "/NairabetRegex.txt";
        final Map<String, String> regexToMatchOnScrappedPage = new HashMap<>();

        try {
            InputStream resourceStream = NairabetMarketsFetcher.class.getResourceAsStream(resourceFile);
            MarketExtraction.putRegularExpressionsToMapFrom(resourceStream);
            changeHomeAndAwayTeamNameInRegex(eventDetails, regexToMatchOnScrappedPage);
        }
        catch (IOException e) {
            LOG.error("An error occurred while attempting to read Nairabet regex file" + e);
        }
        return  regexToMatchOnScrappedPage;
    }

    //TODO ensure that the value of home and away team are put in brackets so it can be captured as a group since () is used for grouping regex
    //TODO edit replaceAll arg to tally with what's inside the txt file
    //TODO use the return value of replaceAll.
    private void changeHomeAndAwayTeamNameInRegex(final Event eventDetails, final Map<String, String> regexToMatchOnScrappedPage) {
        regexToMatchOnScrappedPage.forEach((key, value) -> {
            if(value.contains("homeTeam") ) {
                regexToMatchOnScrappedPage.replace(key, value,
                        value.replaceAll("homeTeam", eventDetails.getHomeTeamName()));
            }
            if (value.contains("awayTeam") ) {
                regexToMatchOnScrappedPage.replace(key, value,
                    value.replaceAll("awayTeam", eventDetails.getAwayTeamName()));
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

    public static void main (String args[]) {
        Event event = new Event.EventBuilder()
                .setEventCountry("England")
                .setEventName("Arsenal FC-Liverpool FC")
                .setEventTime(LocalTime.of(19,45))
                .setAwayTeamName("Arsenal FC")
                .setLeagueName("Community Shields")
                .setHomeTeamName("Liverpool Fc")
                .setSportType(Event.SportType.SOCCER)
                .build();

        WebDriverManager.chromedriver().setup();
        ChromeOptions chromeSettings = new ChromeOptions();
        chromeSettings.setExperimentalOption("excludeSwitches", new String[] {"enable-automation"});
        chromeSettings.addArguments("--ignore-certificate-errors");
        chromeSettings.addArguments("--silent");
        chromeSettings.addArguments("--disable-gpu");
        chromeSettings.addArguments("--disable--notifications");
        chromeSettings.addArguments("--disable-offline-auto-reload");
        chromeSettings.setPageLoadStrategy(PageLoadStrategy.EAGER);
        WebDriver driver = new ChromeDriver(chromeSettings);


        NairabetMarketsFetcher fetcher = new NairabetMarketsFetcher(event, driver);
        String pageUrl = new NairabetPageNavigator(event, driver).navigateToEventPage();
        String page = fetcher.scrapeEventPage(pageUrl);
        System.out.println(page);

    }
}