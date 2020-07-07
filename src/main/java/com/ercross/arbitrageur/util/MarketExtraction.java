package com.ercross.arbitrageur.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ercross.arbitrageur.exception.ZeroValueArgumentException;
import com.ercross.arbitrageur.model.Market;

import static com.ercross.arbitrageur.util.DataValidator.validate;

/**
 * @author Ercross
 *
 * Utility class to help bookmakers extract markets from the scraped event page
 */

public class MarketExtraction {

    public MarketExtraction() {
        throw new IllegalStateException();
    }

    /**
     *  This method extracts two-way as well as three-way markets from scrapedEventPage by matching text pattern found on the page with user defined regular expressions
     *	Used by all BookmakerNameMarketsFetcher class where
     *  @return Market.hashcode(),Market pair
     */
    public static HashMap<Integer, Market> extractMarketsFromScrapedPage (Map<String, String> regexToMatchOnScrappedPage, String scrapedEventPage) throws ZeroValueArgumentException {
        HashMap<Integer, Market> markets = new HashMap<Integer, Market>();

        validate(scrapedEventPage);
        regexToMatchOnScrappedPage.forEach((key, value) -> {
            final Pattern pattern = Pattern.compile(value.toString(), Pattern.MULTILINE);
            final Matcher matcher = pattern.matcher(scrapedEventPage);

            setMarketInfo(matcher, markets, key);
        });

        return markets;
    }

    //TODO ensure that needed matcher group is uniform throughout all bookmakers
    private static void setMarketInfo(Matcher matcher, Map<Integer, Market> markets, String marketType) {
        while (matcher.find()) {
            Market marketData = new Market.MarketBuilder()
                    .setMarketConventionalName(marketType)
                    .setFirstPossibleOutcomeOdd(Double.parseDouble(matcher.group(5)))
                    .setSecondPossibleOutcomeOdd(Double.parseDouble(matcher.group(7)))
                    .setFirstPossibleOutcome(matcher.group(4))
                    .setSecondPossibleOutcome(matcher.group(6))
                    .setMarketType(matcher.group(2))
                    .build();
            markets.put(marketData.hashCode(), marketData);
        }
    }

    //TODO Ensure arg is InputStream rather than file and let caller cast InputStream to FileInputStream(File)
    //Each bookmaker has a text file of regular expressions which is to be loaded/copied into a map just once for a run instance of this app
    //For every event, text scraped on each bookmaker's site is run against the map entry whose key corresponds to the bookmaker's name
    public static Map<String, String> putRegularExpressionsToMapFrom (List<String> fileLines) throws IOException{
        Map<String, String> regularExpressions = new HashMap<String, String>();

        fileLines.forEach(line -> {
            String[] lineParts = line.split(",");
            String key = lineParts[0].trim();
            String value = lineParts[1].trim();

            //put key and value into map if they are not empty
            if(!key.equals("") && !value.equals("")) {
                regularExpressions.put(key, value);
            }
        });
		/*try(BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			String key, value, line;
			String[] lineParts;

			while((line = br.readLine()) != null) {
				lineParts = line.split(",");
				key = lineParts[0].trim();
				value = lineParts[1].trim();

				//put key and value into map if they are not empty
				if(!key.equals("") && !value.equals("")) {
					regularExpressions.put(key, value);
				}
			}
		}
		catch (IOException e) {
			throw new IOException();
		}*/
        return regularExpressions;
    }
}