package com.books.chapters.restfulapi.patterns.chap3.springboot.controller;

import java.net.URI;
import java.util.List;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.books.chapters.restfulapi.patterns.chap3.springboot.models.Investor;
import com.books.chapters.restfulapi.patterns.chap3.springboot.models.Stock;
import com.books.chapters.restfulapi.patterns.chap3.springboot.models.errorsandexceptions.InvestorNotFoundException;
import com.books.chapters.restfulapi.patterns.chap3.springboot.service.InvestorService;

@RestController
public class InvestorController {

  private static final String ID = "/{id}";
  @Autowired
  private InvestorService investorService;

  @GetMapping("/investors")
  public List<Investor> fetchAllInvestors() {
    return investorService.fetchAllInvestors();
  }

  @GetMapping(value = "/investors/welcome", produces = "text/plain;charset=UTF-8")
  public String welcomePageWhichProducesCharset() {
    return "ウェルカムインベスター (\"Welcome Investor!\" in Japanese)";
  }


  @GetMapping("/investors/{investorId}")
  public Optional<Investor> fetchInvestorById(@PathVariable String investorId) {
    Optional<Investor> resultantInvestor = investorService.fetchInvestorById(investorId);
    if (resultantInvestor.isEmpty()) {
      throw new InvestorNotFoundException("Investor Id-" + investorId);
    }
    return resultantInvestor;
  }

  @GetMapping(path = "/investors/{investorId}/stocks")
  public List<Stock> fetchStocksByInvestorId(@PathVariable String investorId,
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "5") int limit) {
    return investorService.fetchStocksByInvestorId(investorId, offset, limit);
  }

  /**
   * method example which produces both xml and json output, other methods produces only json
   * response and for other content type it errors out 406 content not allowed
   */
  @GetMapping(path = "/investors/{investorId}/stocks/{symbol}", produces = {
      MediaType.APPLICATION_JSON_VALUE,
      MediaType.APPLICATION_XML_VALUE})
  public Optional<Stock> fetchAStockByInvestorIdAndStockId(@PathVariable String investorId,
      @PathVariable String symbol) {
    return investorService.fetchSingleStockByInvestorIdAndStockSymbol(investorId, symbol);
  }

  @PostMapping("/investors/{investorId}/stocks")
  public ResponseEntity<Void> addNewStockToTheInvestorPortfolio(@PathVariable String investorId,
      @RequestBody Stock newStock) {
    Optional<Stock> insertedStock = investorService
        .addNewStockToTheInvestorPortfolio(investorId, newStock);
    if (insertedStock.isEmpty()) {
      return ResponseEntity.noContent().build();
    }

    URI location = ServletUriComponentsBuilder.fromCurrentRequest().path(ID)
        .buildAndExpand(insertedStock.get().getSymbol()).toUri();
    return ResponseEntity.created(location).build();
  }

  @PutMapping("/investors/{investorId}/stocks")
  public ResponseEntity<Void> updateAStockOfTheInvestorPortfolio(@PathVariable String investorId,
      @RequestBody Stock stockTobeUpdated) {
    Optional<Stock> updatedStock = investorService
        .updateAStockByInvestorIdAndStock(investorId, stockTobeUpdated);
    if (updatedStock.isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    URI location = ServletUriComponentsBuilder.fromCurrentRequest().path(ID)
        .buildAndExpand(updatedStock.get().getSymbol()).toUri();
    return ResponseEntity.created(location).build();
  }

  @PatchMapping("/investors/{investorId}/stocks/{symbol}")
  public ResponseEntity<Void> updateAStockOfTheInvestorPortfolio(@PathVariable String investorId,
      @PathVariable String symbol, @RequestBody Stock stockTobeUpdated) {
    Optional<Stock> updatedStock = investorService
        .updateAStockByInvestorIdAndStock(investorId, symbol, stockTobeUpdated);
    if (updatedStock.isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    URI location = ServletUriComponentsBuilder.fromCurrentRequest().path(ID)
        .buildAndExpand(updatedStock.get().getSymbol()).toUri();
    return ResponseEntity.created(location).build();
  }

  @DeleteMapping("/investors/{investorId}/stocks/{symbol}")
  public ResponseEntity<Void> deleteAStockFromTheInvestorPortfolio(@PathVariable String investorId,
      @PathVariable String symbol) {
    if (investorService.deleteStockFromTheInvestorPortfolio(investorId, symbol)) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(null);
  }
}
