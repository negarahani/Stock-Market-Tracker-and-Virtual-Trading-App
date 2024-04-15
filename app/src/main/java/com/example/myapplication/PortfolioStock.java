package com.example.myapplication;

public class PortfolioStock {
    private String tickerSymbol;
    private Integer quantity;
    private Double totalCost;

    private Double marketValue;
    private Double change;
    private Double changePercent;

    public PortfolioStock(String tickerSymbol, Integer quantity, Double totalCost, Double marketValue, Double change, Double changePercent) {
        this.tickerSymbol = tickerSymbol;
        this.quantity = quantity;
        this.totalCost = totalCost;
        this.marketValue = marketValue;
        this.change = change;
        this.changePercent = changePercent;

    }

    @Override
    public String toString() {
        return "PortfolioStock{" +
                "tickerSymbol='" + tickerSymbol + '\'' +
                ", quantity=" + quantity +
                ", totalCost=" + totalCost +
                ", marketValue=" + marketValue +
                ", change=" + change +
                ", changePercent=" + changePercent +
                '}';
    }

    public String getTickerSymbol() {

        return tickerSymbol;
    }

    public Integer getQuantity(){
        return quantity;
    }
    public Double getTotalCost(){
        return totalCost;
    }
    public Double getMarketValue(){ return marketValue;}
    public Double getChange() { return change;}
    public Double getChangePercent(){ return changePercent;}
}
