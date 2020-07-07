package com.ercross.arbitrageur.model;

public class Market {

    private String bookmakerName;
    private String marketURL;
    private String marketType;
    private String marketConventionalName;
    private String firstPossibleOutcome;
    private String secondPossibleOutcome;
    private double firstPossibleOutcomeOdd;
    private double secondPossibleOutcomeOdd;


    private Market(MarketBuilder marketBuilder) {
        this.bookmakerName = marketBuilder.bookmakerName;
        this.marketURL = marketBuilder.marketURL;
        this.marketType = marketBuilder.marketType;
        this.firstPossibleOutcome = marketBuilder.firstPossibleOutcome;
        this.secondPossibleOutcome = marketBuilder.secondPossibleOutcome;
        this.firstPossibleOutcomeOdd = marketBuilder.firstPossibleOutcomeOdd;
        this.secondPossibleOutcomeOdd = marketBuilder.secondPossibleOutcomeOdd;
        this.marketConventionalName = marketBuilder.marketConventionalName;
    }

    public String getMarketType() {
        return marketType;
    }

    public String getMarketURL() {
        return marketURL;
    }

    public String getBookmakerName() {
        return bookmakerName;
    }

    public String getFirstPossibleOutcome() {
        return firstPossibleOutcome;
    }

    public String getSecondPossibleOutcome() {
        return secondPossibleOutcome;
    }

    public double getFirstPossibleOutcomeOdd() {
        return firstPossibleOutcomeOdd;
    }

    public double getSecondPossibleOutcomeOdd() {
        return secondPossibleOutcomeOdd;
    }

    public String getMarketConventionalName() {
        return marketConventionalName;
    }

    @Override
    public String toString() {
        return  "Bookmaker: " + this.getBookmakerName() +
                " Market Conventional Name: " +this.getMarketConventionalName() +
                "\n Market Type: " + this.getMarketType() +
                "\n First Possible Outcome: " + this.getFirstPossibleOutcome() + " at " + this.getFirstPossibleOutcomeOdd() +
                "\n Second Possible Outcome: " + this.getSecondPossibleOutcome() + " at " + this.getSecondPossibleOutcomeOdd();
    }

    @Override
    public int hashCode() {
        return this.hash();
    }

    private int hash() {

        byte hash[] = this.getMarketConventionalName().getBytes();
        return 7*hash[1] + hash[2] * hash[3] + hash[4];
    }

    @Override
    public boolean equals(Object market) {
        if (this.getClass() != market.getClass()) {
            return false;
        }
        if (this.getMarketConventionalName().equals(((Market) market).getMarketConventionalName())) {
            return true;
        }
        else return false;
    }

    public static class MarketBuilder {
        private String bookmakerName;
        private String marketURL;
        private String marketType;
        private String marketConventionalName;
        private String firstPossibleOutcome;
        private String secondPossibleOutcome;
        private double firstPossibleOutcomeOdd;
        private double secondPossibleOutcomeOdd;

        public MarketBuilder() {

        }

        public MarketBuilder setMarketURL(String marketURL) {
            this.marketURL = marketURL;
            return this;
        }

        public MarketBuilder setBookmakerName(String bookmakerName) {
            this.bookmakerName = bookmakerName;
            return this;
        }

        public MarketBuilder setMarketType(String marketType) {
            this.marketType = marketType;
            return this;
        }

        public MarketBuilder setFirstPossibleOutcome(String firstPossibleOutcome) {
            this.firstPossibleOutcome = firstPossibleOutcome;
            return this;
        }

        public MarketBuilder setSecondPossibleOutcome(String secondPossibleOutcome) {
            this.secondPossibleOutcome = secondPossibleOutcome;
            return this;
        }

        public MarketBuilder setFirstPossibleOutcomeOdd(double firstPossibleOutcomeOdd) {
            this.firstPossibleOutcomeOdd = firstPossibleOutcomeOdd;
            return this;
        }

        public MarketBuilder setSecondPossibleOutcomeOdd(double secondPossibleOutcomeOdd) {
            this.secondPossibleOutcomeOdd = secondPossibleOutcomeOdd;
            return this;
        }

        public MarketBuilder setMarketConventionalName(String marketConventionalName) {
            this.marketConventionalName = marketConventionalName;
            return this;
        }
        public Market build() {
            return new Market(this);
        }
    }
}
