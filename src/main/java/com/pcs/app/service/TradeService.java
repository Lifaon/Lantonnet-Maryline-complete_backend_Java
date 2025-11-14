package com.pcs.app.service;

import com.pcs.app.domain.Trade;
import com.pcs.app.repositories.TradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TradeService {
    @Autowired
    private TradeRepository repository;

    public List<Trade> getAllTrades(){
        return repository.findAll();
    }

    public Trade getTradeById(int bidListId){
        return repository.findById(bidListId).orElseThrow();
    }

    public Trade createTrade(Trade trade) {
        return repository.save(trade);
    }

    public Trade updateTrade(Trade trade){
        if (trade.getId() == null || !repository.existsById(trade.getId())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid trade id");
        }
        return repository.save(trade);
    }

    public void deleteTrade(int bidListId) {
        if (!repository.existsById(bidListId)) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "No trade with given id");
        }
        repository.deleteById(bidListId);
    }
}
