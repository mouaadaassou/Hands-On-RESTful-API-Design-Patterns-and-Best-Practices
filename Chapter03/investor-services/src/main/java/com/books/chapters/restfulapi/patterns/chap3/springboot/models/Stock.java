package com.books.chapters.restfulapi.patterns.chap3.springboot.models;

public class Stock {

  private String symbol;
  private int numberOfSharesHeld;
  private double tickerPrice = 0.0;

  Stock() {
    // default constructor, otherwise the spring boot runtime complains that
    // it can not instantiate this Stock class when we try to do POST to
    // insert a new Stock from the controller classes
  }

  public Stock(String symbol, int numberOfSharesHeld, double tickerPrice) {

    this.symbol = symbol;
    this.numberOfSharesHeld = numberOfSharesHeld;
    this.tickerPrice = tickerPrice;
  }

  public String getSymbol() {
    return symbol;
  }

  public Stock setSymbol(String symbol) {
    this.symbol = symbol;
    return this;
  }

  public int getNumberOfSharesHeld() {
    return numberOfSharesHeld;
  }

  public Stock setNumberOfSharesHeld(int numberOfShares) {
    this.numberOfSharesHeld = numberOfShares;
    return this;
  }

  public double getTickerPrice() {
    return tickerPrice;
  }

  public Stock setTickerPrice(double tickerPrice) {
    this.tickerPrice = tickerPrice;
    return this;
  }

  @Override
  public String toString() {
    String pattern = "Stock [symbol: %s, numberOfSharesHeld: %d, tickerPrice: %6.2f]";
    return String.format(pattern, symbol, numberOfSharesHeld, tickerPrice);
  }
}
