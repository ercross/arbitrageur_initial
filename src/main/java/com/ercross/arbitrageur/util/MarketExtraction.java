package com.ercross.arbitrageur.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
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
            final Pattern pattern = Pattern.compile(value, Pattern.MULTILINE);
            final Matcher matcher = pattern.matcher(scrapedEventPage);
            setMarketInfo(matcher, markets, key); });
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

    //Each bookmaker has a text file of regular expressions which is to be loaded/copied into a map just once for a run instance of this app
    //For every event, text scraped on each bookmaker's site is run against the map entry whose key corresponds to the bookmaker's name
    public static Map<String, String> putRegularExpressionsToMapFrom (InputStream inputStream) throws IOException{
        Map<String, String> regularExpressions = new HashMap<String, String>();

		try(BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			String key, value, line;
			String[] lineParts;

			while((line = reader.readLine()) != null) {
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
		}
        return regularExpressions;
    }

    public static void main(String args[]) throws IOException, ZeroValueArgumentException {
        String scrapedPage = "1X2\n" +
                "open\n" +
                "17.70X4.3021.46\n" +
                "Double Chance\n" +
                "open\n" +
                "1X2.63121.19X21.07\n" +
                "O/U 2.5open\n" +
                "Over1.82Under1.98\n" +
                "First Goal\n" +
                "open\n" +
                "13.60X10.7521.37\n" +
                "Handicap\n" +
                "open\n" +
                "(1:0) 1 H2.63(1:0) X H3.55(1:0) 2 H2.13(0:1) 1 H18.00(0:1) X H7.10(0:1) 2 H1.07(2:0) 1 H1.51(2:0) X H4.50(2:0) 2 H4.05(0:2) 1 H-(0:2) X H-(0:2) 2 H-(3:0) 1 H1.13(3:0) X H7.60(3:0) 2 H8.60(0:3) 1 H-(0:3) X H-(0:3) 2 H-\n" +
                "HT/FT\n" +
                "open\n" +
                "1/112.751/X19.001/225.00X/115.00X/X6.00X/24.202/153.002/X19.002/22.12\n" +
                "GG/NG\n" +
                "open\n" +
                "GG1.97NG1.80\n" +
                "DNB\n" +
                "open\n" +
                "15.5021.11\n" +
                "Correct Score\n" +
                "open\n" +
                "1-018.002-046.002-123.003-0122.003-175.003-269.004-0216.004-1178.004-2172.004-3190.00--0-16.600-26.801-28.700-310.001-312.252-329.000-419.001-423.002-450.003-4126.00--0-010.751-18.202-219.003-380.004-4214.00Other14.25\n" +
                "C.Score Multi2\n" +
                "open\n" +
                "0-0 / 1-15.102-2 / 3-3 / 4-414.251-0 / 2-0 / 2-18.600-1 / 0-2 / 1-22.583-0 / 3-1 / 3-224.000-3 / 1-3 / 2-34.954-1 / 4-2 / 4-344.001-4 / 2-4 / 3-413.754-0 / 0-4 / other8.10\n" +
                "C.Score Multi\n" +
                "open\n" +
                "0-0 / 1-1 / 0-1 / 1-02.542-0 / 2-1 / 3-0 / 3-111.750-2 / 1-2 / 0-3 / 1-32.382-2 / 2-3 / 3-2 / 3-39.204-0 / 4-1 / 4-2 / 4-342.000-4 / 1-4 / 2-4 / 3-48.404-4 / other12.25-\n" +
                "Highest Scoring Half\n" +
                "open\n" +
                "1st2.992nd2.05Equal3.60\n" +
                "Tot Goals\n" +
                "open\n" +
                "15.0023.6534.1045.4058.906 G.15.00\n" +
                "Multi Goal\n" +
                "open\n" +
                "1-2 Goals2.171-3 Goals1.431-4 Goals1.151-5 Goals1.041-6 Goals-2-3 Goals1.972-4 Goals1.472-5 Goals1.292-6 Goals1.213-4 Goals2.413-5 Goals1.963-6 Goals1.794-5 Goals3.654-6 Goals3.105-6 Goals6.60\n" +
                "Odd/Even\n" +
                "open\n" +
                "Odd1.94Even1.84\n" +
                "1X2 - 5min\n" +
                "open\n" +
                "1 - 5min19.00X - 5min1.042 - 5min9.50\n" +
                "1X2-10min\n" +
                "open\n" +
                "1-10min13.75X-10min1.162-10min5.60\n" +
                "1X2 - 15min\n" +
                "open\n" +
                "1-15min10.75X-15min1.312-15min4.15\n" +
                "1x2 - 20min\n" +
                "open\n" +
                "1-20min9.20X-20min1.452-20min3.30\n" +
                "1X2 - 30min\n" +
                "open\n" +
                "1 - 30min7.30X - 30min1.782 - 30min2.49\n" +
                "1X2 - 60min\n" +
                "open\n" +
                "1 - 60min6.40X - 60min2.892 - 60min1.67\n" +
                "At Least a Half X\n" +
                "open\n" +
                "Yes1.56No2.24\n" +
                "Away 2 in a Row\n" +
                "open\n" +
                "Yes-A Score 2 Row1.72No-A Score 2 Row1.97\n" +
                "Away Sc.3 in a Row\n" +
                "open\n" +
                "Yes-A Score 3 Row3.55No-A Score 3 Row1.24\n" +
                "Away Score 2HT\n" +
                "open\n" +
                "YES-Away Score 2HT1.37NO-Away Score 2HT2.84\n" +
                "Away Score HT\n" +
                "open\n" +
                "Yes-Away Score HT1.58NO-Away Score HT2.24\n" +
                "Away Win to Nil HT\n" +
                "Yes-Away Win to Nil HT2.17NO-Away Win to Nil HT1.57\n" +
                "Handicap 2HT\n" +
                "(1:0) 1 H - 2HT1.88(1:0) X H - 2HT3.15(1:0) 2 H - 2HT3.70(0:1) 1 H - 2HT21.00(0:1) X H - 2HT7.70(0:1) 2 H - 2HT1.06(2:0) 1 H - 2HT1.18(2:0) X H - 2HT5.90(2:0) 2 H - 2HT9.70(0:2) 1 H - 2HT-(0:2) X H - 2HT-(0:2) 2 H - 2HT-\n" +
                "Handicap HT\n" +
                "open\n" +
                "(1:0) 1 H - 1HT1.69(1:0) X H - 1HT3.10(1:0) 2 H - 1HT4.80(0:1) 1 H - 1HT24.00(0:1) X H - 1HT7.60(0:1) 2 H - 1HT1.05(2:0) 1 H - 1HT1.10(2:0) X H - 1HT7.30(2:0) 2 H - 1HT14.00(0:2) 1 H - 1HT-(0:2) X H - 1HT-(0:2) 2 H - 1HT-\n" +
                "Home Sc.2 in a Row\n" +
                "open\n" +
                "Yes-H Score 2 Row7.70No-H Score 2 Row1.04\n" +
                "Home Sc.3 in a Row\n" +
                "open\n" +
                "Yes-H Score 3 Row34.00No-H Score 3 Row-\n" +
                "Home Score 2HT\n" +
                "open\n" +
                "YES-Home Score 2HT2.65NO-Home Score 2HT1.42\n" +
                "Home Score HT\n" +
                "open\n" +
                "Yes-Home Score HT3.20NO-Home Score HT1.30\n" +
                "Home Win to Nil HT\n" +
                "Yes-Home Win to Nil HT7.30NO-Home Win to Nil HT1.04\n" +
                "HT/FT & O/U 4.5\n" +
                "open\n" +
                "1/1+Ov47.001/X+Ov65.001/2+Ov43.00X/1+Ov58.00X/X+Ov65.00X/2+Ov25.002/1+Ov66.002/X+Ov65.002/2+Ov8.601/1+Un13.751/X+Un19.001/2+Un32.00X/1+Un16.00X/X+Un6.00X/2+Un4.652/1+Un56.002/X+Un19.002/2+Un2.66\n" +
                "Score 5min\n" +
                "open\n" +
                "Yes - Score 5 min7.40No - Score 5 min1.04";
        InputStream resourceStream = MarketExtraction.class.getResourceAsStream("/Bet9jaRegex.txt");
        Map<String, String> regexes = putRegularExpressionsToMapFrom(resourceStream);

        Map<Integer,Market> markets = extractMarketsFromScrapedPage(regexes,scrapedPage);
        System.out.println(markets.size());
    }
}