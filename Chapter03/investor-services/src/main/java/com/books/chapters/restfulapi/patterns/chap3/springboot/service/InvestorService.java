package com.books.chapters.restfulapi.patterns.chap3.springboot.service;

import com.books.chapters.restfulapi.patterns.chap3.springboot.models.IndividualInvestorPortfolio;
import com.books.chapters.restfulapi.patterns.chap3.springboot.models.Investor;
import com.books.chapters.restfulapi.patterns.chap3.springboot.models.Stock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InvestorService {

  private static final int OPERATION_ADD = 1;
  private static final int OPERATION_DELETE = 2;
  private static List<Investor> investorsList = new ArrayList<>();
  private static Map<String, IndividualInvestorPortfolio> portfoliosMap = new HashMap<>();

  private static final Logger logger = LoggerFactory.getLogger(InvestorService.class);

  static {
    // create some data
    Stock stocksSampleOne = new Stock("EXA", 200, 20);
    Stock stocksSampleTwo = new Stock("EXB", 100, 60);

    Stock stocksSampleThree = new Stock("EXC", 300, 200);
    Stock stocksSampleFour = new Stock("EXD", 150, 40);

    Stock stockSampleFive = new Stock("EX5", 200, 20);
    Stock stockSampleSix = new Stock("EX6", 200, 20);

    ArrayList<Stock> stocksLotOne = new ArrayList<>();
    stocksLotOne.add(stocksSampleOne);
    stocksLotOne.add(stocksSampleTwo);
    stocksLotOne.add(stockSampleFive);
    stocksLotOne.add(stockSampleSix);
    ArrayList<Stock> stocksLotTwo = new ArrayList<>();
    stocksLotTwo.add(stocksSampleThree);
    stocksLotTwo.add(stocksSampleFour);
    stocksLotTwo.add(stockSampleFive);
    stocksLotTwo.add(stockSampleSix);

    Investor investorOne = new Investor("INVR_1", "Investor ONE", "conservative investor",
        stocksLotOne);
    Investor investorTwo = new Investor("INVR_2", "Investor TWO", "Moderate Risk investor",
        stocksLotTwo);

    investorsList.add(investorOne);
    investorsList.add(investorTwo);

    // Design for Intent example
    // get the portfolio of individual investor updated
    IndividualInvestorPortfolio portfolioOfInvestorOne = updateInvestorPortfolioByInvestorId(
        investorOne);
    IndividualInvestorPortfolio portfolioOfInvestorTwo = updateInvestorPortfolioByInvestorId(
        investorTwo);
    portfoliosMap.put(investorOne.getId(), portfolioOfInvestorOne);
    portfoliosMap.put(investorTwo.getId(), portfolioOfInvestorTwo);
  }

  public List<Investor> fetchAllInvestors() {
    return investorsList;
  }

  public Optional<Investor> fetchInvestorById(String investorId) {
    return investorsList.stream()
        .filter(investors -> investorId.equalsIgnoreCase(investors.getId()))
        .findAny();
  }

  public List<Stock> fetchStocksByInvestorId(String investorId, int offset, int limit) {
    return fetchInvestorById(investorId)
        .map(inv -> inv.getStocks()
            .subList(getStartFrom(offset, inv), getToIndex(offset, limit, inv)))
        .orElse(List.of());
  }

  private int getToIndex(int offset, int limit, Investor investor) {
    int toIndex = offset + limit;
    return Math.min((toIndex), investor.getStocks().size());
  }

  private int getStartFrom(int offset, Investor investor) {
    return Math.min((offset), investor.getStocks().size());
  }

  public Optional<Stock> fetchSingleStockByInvestorIdAndStockSymbol(String investorId,
      String symbol) {
    return fetchInvestorById(investorId).flatMap(
        investor -> investor.getStocks().stream()
            .filter(stock -> symbol.equalsIgnoreCase(stock.getSymbol()))
            .findAny());
  }

  public Optional<Stock> addNewStockToTheInvestorPortfolio(String investorId, Stock newStock) {
    Optional<Stock> addedStockToTheInvestorPortfolio = Optional.empty();
    if (isPreConditionSuccess(investorId, newStock) && isNewStockInsertSuccess(investorId,
        newStock)) {
      designForIntentCascadePortfolioAdd(investorId);
      addedStockToTheInvestorPortfolio = fetchSingleStockByInvestorIdAndStockSymbol(investorId,
          newStock.getSymbol());
    }
    return addedStockToTheInvestorPortfolio;
  }

  public boolean deleteStockFromTheInvestorPortfolio(String investorId,
      String stockTobeDeletedSymbol) {
    Stock stockTobeDeleted = fetchSingleStockByInvestorIdAndStockSymbol(investorId,
        stockTobeDeletedSymbol).orElse(null);

    boolean deletedStatus = fetchSingleStockByInvestorIdAndStockSymbol(investorId,
        stockTobeDeletedSymbol)
        .filter(Objects::nonNull)
        .flatMap(stock -> fetchInvestorById(investorId)
            .map(investor -> investor.getStocks().remove(stockTobeDeleted)))
        .orElse(false);

    designForIntentCascadePortfolioDelete(investorId, deletedStatus);
    return deletedStatus;
  }

  public Optional<Stock> updateAStockByInvestorIdAndStock(String investorId,
      Stock stockTobeUpdated) {
    Optional<Stock> currentStock = fetchSingleStockByInvestorIdAndStockSymbol(investorId,
        stockTobeUpdated.getSymbol());

    currentStock
        .map(stock -> stock.setNumberOfSharesHeld(stockTobeUpdated.getNumberOfSharesHeld()));

    currentStock.map(stock -> stock.setTickerPrice(stockTobeUpdated.getTickerPrice()));
    return currentStock;
  }

  // slight variance of updateAStockByInvestorIdAndStock for PATCH method
  // please note that spring boot provides annotations based validations for
  // JSON, however this
  // method is not using those annotations for keeping the scope simple for
  // patching examples
  public Optional<Stock> updateAStockByInvestorIdAndStock(String investorId, String symbol,
      Stock stockTobeUpdated) {
    Optional<Stock> currentStock = fetchSingleStockByInvestorIdAndStockSymbol(investorId, symbol);

    if (stockTobeUpdated.getNumberOfSharesHeld() > 0) {
      currentStock
          .map(stock -> stock.setNumberOfSharesHeld(stockTobeUpdated.getNumberOfSharesHeld()));
    }
    if (stockTobeUpdated.getTickerPrice() > 0) {
      currentStock.map(stock -> stock.setTickerPrice(stockTobeUpdated.getTickerPrice()));
    }
    return currentStock;
  }

  private static IndividualInvestorPortfolio updateInvestorPortfolioByInvestorId(
      Investor investor) {
    return new IndividualInvestorPortfolio(investor.getId(), investor.getStocks().size());
  }

  private boolean isPreConditionSuccess(String investorId, Stock newStock) {
    return fetchInvestorById(investorId).isPresent() && isUnique(investorId, newStock);
  }

  private boolean isNewStockInsertSuccess(String investorId, Stock newStock) {
    ;
    return fetchInvestorById(investorId).map(investor -> investor.getStocks().add(newStock)).get();
  }

  private boolean isUnique(String investorId, Stock newStock) {
    return fetchSingleStockByInvestorIdAndStockSymbol(investorId, newStock.getSymbol())
        .isEmpty();
  }

  private void updateIndividualInvestorPortfolio(UpdateIndividualInvestorPortfolio portfolioOp) {
    portfolioOp.updateIndividualInvestorPortfolio();
  }

  private void designForIntentCascadePortfolioAdd(String investorId) {
    IndividualInvestorPortfolio individualInvestorPortfolio = portfoliosMap
        .get(fetchInvestorById(investorId).map(Investor::getId).orElse(null));

    updateIndividualInvestorPortfolio(() -> {
      individualInvestorPortfolio
          .setStocksHoldCount(individualInvestorPortfolio.getStocksHoldCount() + 1);
    });
  }

  private void designForIntentCascadePortfolioDelete(String investorId, boolean deletedStatus) {
    if (deletedStatus) {
      IndividualInvestorPortfolio individualInvestorPortfolio = portfoliosMap
          .get(fetchInvestorById(investorId).map(Investor::getId).orElse(null));

      updateIndividualInvestorPortfolio(() -> {
        individualInvestorPortfolio
            .setStocksHoldCount(individualInvestorPortfolio.getStocksHoldCount() - 1);
      });
    }
  }
}
