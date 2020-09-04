package com.ercross.arbitrageur.fetcher;

import com.ercross.arbitrageur.adt.UrlTreeMap;
import com.ercross.arbitrageur.adt.exceptions.NodeNotFoundException;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

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
 * 6. add its mapper to NameReplacer.java using mappers.put("BookmakerName", loadMapperContent("BookmakerName"));
 */

public interface Fetchable {

    //This configuration aligns well with what most bookmakers' websites needed.
    //custom implementation can be made by overriding this in the implementing class
    default WebDriver initWebDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions chromeSettings = new ChromeOptions();
        chromeSettings.setExperimentalOption("excludeSwitches", new String[] {"enable-automation"});
        chromeSettings.addArguments("--disable-gpu");
        chromeSettings.addArguments("--headless");
        chromeSettings.addArguments("--ignore-certificate-errors");
        chromeSettings.addArguments("--silent");
        chromeSettings.addArguments("--disable--notifications");
        chromeSettings.addArguments("--disable-offline-auto-reload");
        return new ChromeDriver(chromeSettings);
    }

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
    Object fetch();
}
