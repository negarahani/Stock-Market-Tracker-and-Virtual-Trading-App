package com.example.myapplication;

public class FavoriteStock {
    private String tickerSymbol;
    private String companyName;

    private Double currentPrice;
    private Double change;
    private Double changePercent;

    public FavoriteStock(String tickerSymbol, String companyName, Double currentPrice, Double change, Double changePercent) {
        this.tickerSymbol = tickerSymbol;
        this.companyName = companyName;
        this.currentPrice = currentPrice;
        this.change = change;
        this.changePercent = changePercent;
    }

    //constructor for when we don't care about paramters other than ticker and name
    public FavoriteStock(String tickerSymbol, String companyName) {
        this(tickerSymbol, companyName, null, null, null);
    }

    @Override
    public String toString() {
        return "FavoriteStock{" +
                "tickerSymbol='" + tickerSymbol + '\'' +
                ", companyName='" + companyName + '\'' +
                ", currentPrice=" + currentPrice +
                ", change=" + change +
                ", changePercent=" + changePercent +
                '}';
    }

    public String getTickerSymbol() {
        return tickerSymbol;
    }

    public String getCompanyName(){
        return companyName;
    }

    public Double getCurrentPrice(){
        return currentPrice;
    }

    public Double getChange(){
        return change;
    }

    public Double getChangePercent(){
        return changePercent;
    }
}
