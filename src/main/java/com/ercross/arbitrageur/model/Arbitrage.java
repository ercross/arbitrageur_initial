package com.ercross.arbitrageur.model;

public class Arbitrage {

    private String bookmaker1Name, bookmaker2Name;
    private String bookmaker1URL, bookmaker2URL;
    private double bookmaker1Odd, bookmaker2Odd;
    private String marketTypeAtBookmaker1, marketTypeAtBookmaker2;

    private Arbitrage(ArbitrageBuilder builder) {
        this.bookmaker1Odd = builder.bookmaker1Odd;
        this.bookmaker2Odd = builder.bookmaker2Odd;
        this.marketTypeAtBookmaker1 = builder.marketTypeAtBookmaker1;
        this.marketTypeAtBookmaker2 = builder.marketTypeAtBookmaker2;
    }

    /**
     * sets other info pertaining to this arbitrage
     * @return
     */
    public Arbitrage setOtherArbitrageInfo(Market marketFromBookmaker1, Market marketFromBookmaker2) {
        this.bookmaker1Name = marketFromBookmaker1.getBookmakerName();
        this.bookmaker2Name = marketFromBookmaker2.getBookmakerName();
        this.bookmaker1URL = marketFromBookmaker1.getMarketURL();
        this.bookmaker2URL = marketFromBookmaker2.getMarketURL();
        return this;
    }

    public String getBookmaker1URL() {
        return bookmaker1URL;
    }

    public String getBookmaker2URL() {
        return bookmaker2URL;
    }

    public double getBookmaker1Odd () {
        return bookmaker1Odd;
    }

    public double getBookmaker2Odd () {
        return bookmaker2Odd;
    }

    public String getBookmaker1Name () {
        return bookmaker1Name;
    }

    public String getBookmaker2Name () {
        return bookmaker2Name;
    }

    @Override
    public String toString () {
        return                  "             " + "          \n "
                + bookmaker1Name + ":" + marketTypeAtBookmaker1 + "=" + bookmaker1Odd

                + "\n " + bookmaker2Name + ":" + marketTypeAtBookmaker2 + "=" + bookmaker2Odd ;
    }

    public static class ArbitrageBuilder {
        private double bookmaker1Odd, bookmaker2Odd;
        private String marketTypeAtBookmaker1, marketTypeAtBookmaker2;

        public ArbitrageBuilder() {

        }

        public ArbitrageBuilder setBookmaker1Odd(double bookmaker1Odd) {
            this.bookmaker1Odd = bookmaker1Odd;
            return this;
        }

        public ArbitrageBuilder setBookmaker2Odd(double bookmaker2Odd) {
            this.bookmaker2Odd = bookmaker2Odd;
            return this;
        }

        public ArbitrageBuilder setMarketTypeAtBookmaker1(String marketTypeAtBookmaker1) {
            this.marketTypeAtBookmaker1 = marketTypeAtBookmaker1;
            return this;
        }

        public ArbitrageBuilder setMarketTypeAtBookmaker2(String marketTypeAtBookmaker2) {
            this.marketTypeAtBookmaker2 = marketTypeAtBookmaker2;
            return this;
        }

        public Arbitrage build() {
            return new Arbitrage(this);
        }
    }
}
