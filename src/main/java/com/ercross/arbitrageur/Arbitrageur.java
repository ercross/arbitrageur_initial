package com.ercross.arbitrageur;

import com.ercross.arbitrageur.fetcher.bookmaker.bet9ja.Bet9jaMarketsFetcher;
import com.ercross.arbitrageur.fetcher.bookmaker.nairabet.NairabetMarketsFetcher;
import com.ercross.arbitrageur.model.Arbitrage;
import com.ercross.arbitrageur.model.Event;
import com.ercross.arbitrageur.model.Market;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class Arbitrageur implements Callable<List<Arbitrage>> {

    private final Event eventDetails;
    private final Map<String, WebDriver> drivers;

    public Arbitrageur (Event eventDetails,  Map<String, WebDriver> drivers) {
        this.eventDetails = eventDetails;
        this.drivers = drivers;
    }

    @Override
    //contains code that implements a complete process of finding arbitrages
    public List<Arbitrage> call() throws Exception {
        List<HashMap<Integer, Market>> allBookmakersMarkets = new ArrayList<HashMap<Integer, Market>>();
        ArbitrageFinder arbitrageFinder = new ArbitrageFinder(eventDetails);

        HashMap<Integer, Market> bet9jaMarkets = new Bet9jaMarketsFetcher(eventDetails, drivers.get("Bet9ja")).getBet9jaMarkets();
        HashMap<Integer, Market> nairabetMarkets = new NairabetMarketsFetcher(eventDetails, drivers.get("Nairabet")).getNairabetMarkets();

        allBookmakersMarkets.add(bet9jaMarkets);
        allBookmakersMarkets.add(nairabetMarkets);

        return arbitrageFinder.findArbitrageAcrossAllMarkets(allBookmakersMarkets);
    }
}
