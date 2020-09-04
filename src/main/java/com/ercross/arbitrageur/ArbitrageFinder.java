package com.ercross.arbitrageur;

import static com.ercross.arbitrageur.util.DataValidator.validate;

import com.ercross.arbitrageur.exception.ZeroValueArgumentException;
import com.ercross.arbitrageur.model.Arbitrage;
import com.ercross.arbitrageur.model.Event;
import com.ercross.arbitrageur.model.Market;
import com.ercross.arbitrageur.model.Profit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
 * List of markets from all bookmakers, as well as the particular event, are injected into the constructor
 * (so that for each unique event, a new instance of the class may be instantiated to find arbitrage in this markets list)
 * A helper method pairs the bookmakers, ensuring all bookmakers get paired with each other at one point in time
 * Arbitrage found in each cycle is sent within the find method to another destination even before the method returns
 * The method eventually returns a list of found arbitrage, which is the main aim of this software
 */
public class ArbitrageFinder {

    private static final Logger LOG = LogManager.getLogger(ArbitrageFinder.class);

    private List<Arbitrage> arbitrages = new ArrayList<Arbitrage>();
    private final Event eventDetails;

    //LDD aid 1: This field holds names and market types of the two bookmakers whose markets are currently being probed for arbitrage opportunity
    private String currentMarketPairInfo;

    public ArbitrageFinder(Event eventDetails) {
        this.eventDetails = eventDetails;
    }

    /**
     * @param marketsFromAllBookmakerOnAnEvent is a list of markets found on an event across all bookmaker's website
     * @return a list of arbitrages; an empty list otherwise
     */
    public List<Arbitrage> findArbitrageAcrossAllMarkets(List<HashMap<Integer,Market>> marketsFromAllBookmakerOnAnEvent) {
        LOG.debug("ArbitrageFinder.findArbitrageInAllMarkets invoked on event: " + eventDetails.toString());
        pairBookmakers(marketsFromAllBookmakerOnAnEvent);
        if (!(this.arbitrages.isEmpty()))
            LOG.debug("At least one arbitrage found on: " + eventDetails.toString());
        LOG.debug("Arbitrage not found on: " + eventDetails.toString() + "across all bookmakers. Exiting ArbitrageFinder.isMarketsContainArbitrage()");
        return arbitrages;
    }

    //Both odds are from different bookmakers and represent the odd for two exclusinve possible outcomes on an event
    //An example of two exclusive possible outcome on an event is: a soccer game may see goals or no goals at all
    private boolean isArbitrageFoundBetween(double bookmaker1Odd, double bookmaker2Odd) {
        boolean value = false;
        try
        {
            validate (bookmaker1Odd, bookmaker2Odd, currentMarketPairInfo);
            value = ( (1/bookmaker1Odd) + (1/bookmaker2Odd) < 1);
        }
        catch(ZeroValueArgumentException e) {
            String errorMessage = e.getMessage("One of the odds in ArbitrageFinder.isArbitrageFoundBetween() is zero. Check validate() log above");
            LOG.error(errorMessage, e);
        }
        return value;
    }

    /*
     * An algorithm to pair bookmakers' markets container, one each from different bookmakers, such that no pairing is repeated
     * Could have been implemented with a pair of nested for loop, but I prefer this clarity over brevity
     *
     * Say List contains elements [1,2,3,4,5,6,7,8,9,10], at a point in time, paired elements are indexes i and j, such that i is never equal to j
     * For a start, if i=1, j will be 2 and is incremented on each iteration until j == list.size() while i remains unchanged
     * At this point, j would be 9 which is the index of the last element in the list, list[j] == 10
     * i is then incremented by 1. j is reset to i+1, is incremented again until j == list.size() while i remains unchanged
     */
    private void pairBookmakers (List<HashMap<Integer,Market>> marketsFromAllBookmakerOnAnEvent) {
        String bookmaker1Name, bookmaker2Name;
        for(int i = 0, j=1; i < marketsFromAllBookmakerOnAnEvent.size()-1; j++) {

            if(j==marketsFromAllBookmakerOnAnEvent.size()) {
                i++;
                j=i+1;
            }else {

                //LDD aid 2
                bookmaker1Name = marketsFromAllBookmakerOnAnEvent.get(i).get(1).toString();
                bookmaker2Name = marketsFromAllBookmakerOnAnEvent.get(j).get(1).toString();
                LOG.debug("finding arbitrage between " + bookmaker1Name + " and " + bookmaker2Name + " markets on event " + eventDetails.toString());

                findArbitrageBetween(marketsFromAllBookmakerOnAnEvent.get(i), marketsFromAllBookmakerOnAnEvent.get(j));
            }

        }
    }

    /*
     * For efficiency, bookmakers' markets are saved into Hashmaps where the hashcode of each Market instance serves as the key
     * Since the same market type across all bookmakers have the same hashcode due to the way hashcode is implemented,
     * then take a market instance from marketsFromBookmaker1, obtain its hashcode, and supply it as the key for the element to be retrieved from marketsFromBookmaker2
     * The retrieved element from marketsFromBookmaker2 contains the same market type as the one taken from marketsFromBookmaker1
     */
    private void findArbitrageBetween (Map<Integer,Market> marketsFromBookmaker1, Map<Integer,Market> marketsFromBookmaker2) {
        LOG.debug("ArbitrageFinder.findArbitrageBetween() invoked");
        marketsFromBookmaker1.forEach((key, marketFromABookmaker) -> {
            Market marketFromAnotherBookmaker = marketsFromBookmaker2.get(key);
            currentMarketPairInfo = marketFromABookmaker.toString() + marketFromAnotherBookmaker.toString();

            if (isArbitrageFoundBetween (marketFromABookmaker.getFirstPossibleOutcomeOdd(), marketFromAnotherBookmaker.getSecondPossibleOutcomeOdd())) {

                LOG.info("One arbitrage found. Preparing details...");
                Arbitrage arbitrage = new Arbitrage.ArbitrageBuilder()
                        .setBookmaker1Odd(marketFromABookmaker.getFirstPossibleOutcomeOdd())
                        .setBookmaker2Odd(marketFromAnotherBookmaker.getSecondPossibleOutcomeOdd())
                        .setMarketTypeAtBookmaker1(marketFromABookmaker.getFirstPossibleOutcome())
                        .setMarketTypeAtBookmaker2(marketFromAnotherBookmaker.getSecondPossibleOutcome())
                        .build();
                arbitrage.setOtherArbitrageInfo(marketFromABookmaker, marketFromAnotherBookmaker);
                Profit profit = new Profit(arbitrage);
                profit.computeProfitPercentage();
                LOG.info("Arbitrage details ready: " + arbitrage.toString() + " and profit percentage: " + profit.toString() + "%");
                this.arbitrages.add(arbitrage);
            }

            if (isArbitrageFoundBetween (marketFromABookmaker.getSecondPossibleOutcomeOdd(), marketFromAnotherBookmaker.getFirstPossibleOutcomeOdd())) {

                LOG.info("One arbitrage found. Preparing details...");
                Arbitrage arbitrage = new Arbitrage.ArbitrageBuilder()
                        .setBookmaker1Odd(marketFromABookmaker.getSecondPossibleOutcomeOdd())
                        .setBookmaker2Odd(marketFromAnotherBookmaker.getFirstPossibleOutcomeOdd())
                        .setMarketTypeAtBookmaker1(marketFromABookmaker.getSecondPossibleOutcome())
                        .setMarketTypeAtBookmaker2(marketFromAnotherBookmaker.getFirstPossibleOutcome())
                        .build();
                arbitrage.setOtherArbitrageInfo(marketFromABookmaker, marketFromAnotherBookmaker);
                Profit profit = new Profit(arbitrage);
                profit.computeProfitPercentage();
                LOG.info("Arbitrage details ready: " + arbitrage.toString() + " and profit percentage: " + profit.toString() + "%");
                this.arbitrages.add(arbitrage);
            }
        });
    }

    //TODO this method should be in the main
    public static void sendArbitragesToClient(List<Arbitrage> arbitrages, Event eventDetails) {
        //TODO for now, just print out to console. Standard implementation to involve conversion to JSON object through an utility method
    }
}