package com.books.chapters.restfulapi.patterns.chap3.springboot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;

import com.books.chapters.restfulapi.patterns.chap3.springboot.models.Investor;
import com.books.chapters.restfulapi.patterns.chap3.springboot.models.Stock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.Test;

public class InvestorServiceTest {

  @Test
  public void testFetchAllInvestors() {
    int expectedSize = 2;

    // When:
    int actualListSize = getInvestorServiceForTest().fetchAllInvestors().size();

    // Then:
    assertThat(actualListSize).isEqualTo(expectedSize);
  }


  @Test
  public void testFetchInvestorById() {
    // Given:
    Investor expectedInvestor = new Investor("INVR_1", "Investor ONE", "conservative investor",
        getStocksSetOne());

    // When:
    Investor actualInvestor = getInvestorServiceForTest().fetchInvestorById("invr_1").orElse(null);

    // Then:
    assertThat(actualInvestor).isNotNull();
    assertThat(actualInvestor.getId()).isEqualTo(expectedInvestor.getId());

  }


  @Test
  public void testFetchStocksByInvestorId() {
    // Given:
    Investor expectedInvestor = getInvestorServiceForTest().fetchInvestorById("invr_1")
        .orElse(null);
    String expectedStockSymbol = "EXB";

    // When:
    Stock expectedStock = Objects.requireNonNull(expectedInvestor).getStocks().stream()
        .filter(stock -> expectedStockSymbol.equalsIgnoreCase(stock.getSymbol()))
        .findAny()
        .orElse(null);

    // Then:
    assertThat(expectedStock).isNotNull();
    assertThat(expectedInvestor.getStocks().size()).isEqualTo(4);
  }

  @Test
  public void testFetchSingleStockByInvestorIdAndStockSymbol() {
    // Given:
    String investorId = "Invr_1";
    String expectedSymbol = "EXA";

    // When:
    String actualSymbol = getInvestorServiceForTest()
        .fetchSingleStockByInvestorIdAndStockSymbol(investorId, expectedSymbol)
        .map(Stock::getSymbol).orElse(null);

    // Then:
    assertThat(actualSymbol).isNotNull();
    assertThat(expectedSymbol).isEqualTo(actualSymbol);
  }


  @Test
  public void testAddNewStocksToTheInvestorPortfolio() {
    // Given:
    Stock actualStock = new Stock("EXe", 150, 18.5);
    InvestorService localInvestorService = getInvestorServiceForTest();

    // When:
    localInvestorService.addNewStockToTheInvestorPortfolio("invr_1", actualStock);
    Stock expectedStock = localInvestorService
        .fetchSingleStockByInvestorIdAndStockSymbol("invr_1", "EXe").orElse(null);

    // Then:
    assertThat(expectedStock).isNotNull();
    assertThat(expectedStock.getSymbol()).isEqualTo(actualStock.getSymbol());
  }

  @Test
  public void testAddNewStocksToTheInvestorPortfolioFailsWhenTryInsertingDuplicate() {
    // Given:
    String investorId = "invr_1";
    String actualStockSymbol = "EXe";
    Stock actualStock = new Stock(actualStockSymbol, 150, 18.5);
    InvestorService localInvestorService = getInvestorServiceForTest();

    // when:
    Stock addedStock = localInvestorService
        .addNewStockToTheInvestorPortfolio(investorId, actualStock).orElse(null);
    Stock secondAddedStock = localInvestorService
        .addNewStockToTheInvestorPortfolio(investorId, actualStock).orElse(null);

    // Then:
    assertThat(addedStock).isNotNull();
    assertThat(secondAddedStock).isNull();
  }

  @Test
  public void testDeleteAStockToTheInvestorPortfolio() {
    // Given:
    Stock actualStock = new Stock("EXA", 150, 18.5);
    InvestorService localInvestorService = getInvestorServiceForTest();

    // When:
    localInvestorService.deleteStockFromTheInvestorPortfolio("invr_1", actualStock.getSymbol());
    Stock expectedStock = localInvestorService
        .fetchSingleStockByInvestorIdAndStockSymbol("invr_1", "EXa").orElse(null);

    // Then:
    assertNull(expectedStock);
  }

  @Test
  public void testUpdateAStockByInvestorId() {
    // Given:
    Stock expectedStock = new Stock("EXA", 150, 18.5);
    InvestorService localInvestorService = getInvestorServiceForTest();
    String investorId = "invr_1";

    // When:
    Stock actualStock = localInvestorService
        .updateAStockByInvestorIdAndStock(investorId, expectedStock).orElse(null);

    // Then:
    assertThat(actualStock).isNotNull();
    assertThat(expectedStock.getNumberOfSharesHeld())
        .isEqualTo(actualStock.getNumberOfSharesHeld());
    assertThat(expectedStock.getTickerPrice()).isEqualTo(actualStock.getTickerPrice());
  }

  @Test
  public void testUpdateAStockByInvestorIdWhenInvestorIdNotFound() {
    // Given:
    Stock expectedStock = new Stock("EXA", 150, 18.5);
    InvestorService localInvestorService = getInvestorServiceForTest();
    String investorId = "invr_not_found";

    // When:
    Stock actualStock = localInvestorService
        .updateAStockByInvestorIdAndStock(investorId, expectedStock).orElse(null);

    // Then:
    assertThat(actualStock).isNull();
  }


  @Test
  public void testUpdateAStockByInvestorIdWhenStockSymbolNotFound() {
    // Given:
    Stock expectedStock = new Stock("not_found", 150, 18.5);
    InvestorService localInvestorService = getInvestorServiceForTest();
    String investorId = "invr_1";

    //When:
    Stock actualStock = localInvestorService
        .updateAStockByInvestorIdAndStock(investorId, expectedStock).orElse(null);

    //Then:
    assertThat(actualStock).isNull();
  }

  private InvestorService getInvestorServiceForTest() {
    return new InvestorService();
  }

  private List<Stock> getStocksSetOne() {
    ArrayList<Stock> stocksLotOne = new ArrayList<>();
    Stock stocksSampleOne = new Stock("EXA", 200, 20);
    Stock stocksSampleTwo = new Stock("EXB", 100, 60);

    stocksLotOne.add(stocksSampleTwo);
    stocksLotOne.add(stocksSampleOne);
    return stocksLotOne;
  }

  private List<Stock> getStocksSetTwo() {
    ArrayList<Stock> stocksLotTwo = new ArrayList<>();
    Stock stocksSampleThree = new Stock("EXC", 300, 200);
    Stock stocksSampleFour = new Stock("EXD", 150, 40);

    stocksLotTwo.add(stocksSampleThree);
    stocksLotTwo.add(stocksSampleFour);
    return stocksLotTwo;
  }
}
