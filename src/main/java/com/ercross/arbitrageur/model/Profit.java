package com.ercross.arbitrageur.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.ercross.arbitrageur.exception.ZeroValueArgumentException;

import static com.ercross.arbitrageur.util.DataValidator.validate;

public class Profit {

    private Arbitrage arbitrage;
    private double profitAtBookmaker1, profitAtBookmaker2;
    private double stakeAtBookmaker1, stakeAtBookmaker2;
    private double totalStakeAmount, profitPercentage;

    public Profit(double totalStakeAmount, Arbitrage arbitrage) {
        this.totalStakeAmount = totalStakeAmount;
        this.arbitrage = arbitrage;
    }

    public Profit (Arbitrage arbitrage) {
        this.arbitrage = arbitrage;
    }

    private Profit () {

    }

    /**
     * @param totalStakeAmount to be staked on this.arbitrage
     * @return percentage and monetary profit associated with an arbitrage instance
     */
    public Profit computeProfitAssociatedWith(double totalStakeAmount) {

        Profit profitDetails = new Profit();
        profitDetails = this.computeStakeWithEachBookmaker()
                .computeProfitWithEachBookmaker()
                .computeProfitPercentage();
        try {
            validate (this.arbitrage.getBookmaker1Odd(),this.arbitrage.getBookmaker2Odd(), arbitrage.toString());
        }
        catch (ZeroValueArgumentException e) {
            //Log this
        }
        return profitDetails;
    }

    //computes only profit percentage associated with an arbitrage
    public Profit computeProfitPercentage () {
        this.profitPercentage = roundUpValue((
                100-(((1/this.arbitrage.getBookmaker1Odd())+(1/this.arbitrage.getBookmaker2Odd()))*100)), 2);

        return this;
    }

    private Profit computeStakeWithEachBookmaker() {
        this.stakeAtBookmaker1 = roundUpValue(
                (this.totalStakeAmount*(1/this.arbitrage.getBookmaker1Odd()) /
                        ((1/this.arbitrage.getBookmaker1Odd())+(1/this.arbitrage.getBookmaker2Odd())) ) ,2);

        this.stakeAtBookmaker2 = roundUpValue(
                (this.totalStakeAmount*(1/this.arbitrage.getBookmaker2Odd()) /
                        ((1/this.arbitrage.getBookmaker1Odd())+(1/this.arbitrage.getBookmaker2Odd())) ) ,2);
        return this;
    }

    private Profit computeProfitWithEachBookmaker () {
        this.profitAtBookmaker1 = roundUpValue(
                ((this.stakeAtBookmaker1*this.arbitrage.getBookmaker1Odd())-this.totalStakeAmount), 2);

        this.profitAtBookmaker2 = roundUpValue(
                ((this.stakeAtBookmaker2*this.arbitrage.getBookmaker2Odd())-this.totalStakeAmount), 2);

        return this;
    }

    private static double roundUpValue(double value, int precision) {
        if (precision < 0) throw new IllegalArgumentException();

        BigDecimal bigDecimal = BigDecimal.valueOf(value);
        bigDecimal = bigDecimal.setScale(precision, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }

    @Override
    public String toString() {
        return "Profit associated with this arbitrage: " + this.arbitrage +  " is: " + this.profitPercentage +
                " \n over the total stake amount: "  + this.totalStakeAmount + "\n"
                + "On staking: " + this.stakeAtBookmaker1 + " at " + this.arbitrage.getBookmaker1Name() + " you get " + this.profitAtBookmaker1 + "\n"
                + "On staking: " + this.stakeAtBookmaker2 + " at " + this.arbitrage.getBookmaker2Name() + " you get " + this.profitAtBookmaker2;
    }
}
