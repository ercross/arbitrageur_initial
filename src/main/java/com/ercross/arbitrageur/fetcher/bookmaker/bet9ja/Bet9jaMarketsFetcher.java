package com.ercross.arbitrageur.fetcher.bookmaker.bet9ja;

import com.ercross.arbitrageur.fetcher.Fetchable;
import com.ercross.arbitrageur.util.MarketExtraction;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.ercross.arbitrageur.exception.ZeroValueArgumentException;
import com.ercross.arbitrageur.model.Event;
import com.ercross.arbitrageur.model.Market;

public class Bet9jaMarketsFetcher implements Fetchable {

    private static final Logger LOG = LogManager.getLogger(Bet9jaMarketsFetcher.class);

    private final Event eventDetails;
    private final WebDriver driver;
    private final static Map<String, String> regexToMatchOnScrappedPage = new HashMap<>();


    //created only to access fetcher interface default method from main
    public Bet9jaMarketsFetcher() {
        eventDetails=null;
        driver=null;
    }

    public Bet9jaMarketsFetcher(final Event eventDetails, WebDriver driver) {
        this.eventDetails = eventDetails;
        this.driver = driver;
    }

    @Override
    public Object fetch() {
        //This field is needed to query bookmaker's name at runtime and for now, used for logging purpose.
        //The integer key, 1, is uniform across all bookmakers
        final Market bookmakerName = new Market.MarketBuilder().setBookmakerName("Nairabet").build();
        HashMap<Integer, Market> bet9jaMarkets = new HashMap<>();
        bet9jaMarkets.put(1, bookmakerName);
        try {
            String pageUrl = new Bet9jaPageNavigator(eventDetails, driver).navigateToEventPage();
            String eventPage = scrapeEventPage(pageUrl);
            bet9jaMarkets = MarketExtraction.extractMarketsFromScrapedPage(regexToMatchOnScrappedPage, eventPage);
            LOG.info("Bet9ja markets on the event " + eventDetails.toString()+ " ready");
        }
        catch (ZeroValueArgumentException e) {
            LOG.error("Bet9ja: The scraped page has no content. Maybe you need to checkout what's wrong with Betja page" + e);
        }
        return bet9jaMarkets;
    }

    private String scrapeEventPage(String eventPageUrl) {
        final String marketsDivXPath = "/html/body/form/div[7]/div[1]/div[2]/div/table/tbody/tr/td[2]/div/div/div/div[8]";
        String scrapedEventPage = null;
        try {
            assert driver != null;
            driver.get(eventPageUrl);
            final WebDriverWait wait = new WebDriverWait (driver, Duration.ofSeconds(10));
            LOG.info("Bet9ja: waiting for visibility of markets div xpath...");
            scrapedEventPage = wait.until(page -> driver.findElement(By.xpath(marketsDivXPath)).getText());
            LOG.info("Bet9ja markets div visible. Now scraping the div content...");
            LOG.debug("Bet9a markets div scrape successful");
        }
        catch (NoSuchElementException e) {
            LOG.error("Bet9ja markets div xpath is invisible after wait period", e);
        }
        return scrapedEventPage;
    }

    /**
     * initRegexMap should be invoked once throughout the entire runtime of the application.
     * It loads regular expression containing patterns the matches expected markets for this bookmaker
     */
    public static void initRegexMap () {
        String resourceFile = "/TwoWayBet9jaSoccer.txt";

        try {
            InputStream resourceStream = Bet9jaMarketsFetcher.class.getResourceAsStream(resourceFile);
            MarketExtraction.putRegularExpressionsToMapFrom(resourceStream);
        }
        catch (IOException e) {
            LOG.error("Bet9ja: an error occurred while attempting to read bet9ja regex text file" + e);
        }
    }
}
