package com.pcs.app.service;

import com.pcs.app.domain.BidList;
import com.pcs.app.repositories.BidListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BidListService {
    @Autowired
    private BidListRepository repository;

    public List<BidList> getAllBidLists(){
        return repository.findAll();
    }

    public BidList getBidListById(int bidListId){
        return repository.findById(bidListId).orElseThrow();
    }

    public BidList createBidList(BidList bidList) {
        return repository.save(bidList);
    }

    public BidList updateBidList(BidList bidList){
        if (bidList.getId() == null || !repository.existsById(bidList.getId())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid bidList id");
        }
        return repository.save(bidList);
    }

    public void deleteBidList(int bidListId) {
        if (!repository.existsById(bidListId)) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "No bidList with given id");
        }
        repository.deleteById(bidListId);
    }
}
