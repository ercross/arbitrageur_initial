package com.ercross.arbitrageur.controller.fetchers;

import com.ercross.arbitrageur.adt.UrlTreeMap;
import com.ercross.arbitrageur.adt.exceptions.NodeNotFoundException;
import org.openqa.selenium.WebDriver;

/**
 * @author ercross
 *
 * Contains methods relevant to all classes that uses selenium for fetching data on the internet
 */

/*
 * Implemented by all classes (including all bookmakers) that use a webdriver to obtain their output from a third party website.
 * To add a new bookmaker:
 * 1. Set browser setting specific to bookmaker's webpage in private initializeBrowser()
 * 2. Navigate to and fetch event webpage by clicking on anchor tag texts on bookmaker's page in private fetchEventPage()
 * 3. Scrape the event's markets section on the event page in private scrapeEventPage
 * 4. Extract markets found in the scraped page using util.MarketExtraction.extractMarket() using fetch()
 * 5. Add a reloadUrlTree in Main.clearAllUrlTrees();
 */

public interface Fetchable {

    //Used in classes that uses selenium webdriver to set preclose and close operations on the driver
    public static void tearDownWebDriver(WebDriver driver) {
        driver.manage().deleteAllCookies();
        driver.close();
    }

    public static void reloadUrlTree(UrlTreeMap<String, String> urlTree, String rootNodeKey, String rootNodeValue) throws NodeNotFoundException {
        urlTree.clear();
        urlTree.add(null, null, rootNodeKey, rootNodeValue);
    }

    /**
     * fetches the needed resource (specified in the class name) from a target webpage and stores them in appropriate class instance data structures.
     * All parameters needed are supplied as class instances and unique for each implementing class
     */
    public void fetch();
}
