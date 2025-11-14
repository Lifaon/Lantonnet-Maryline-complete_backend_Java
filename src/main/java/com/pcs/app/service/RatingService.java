package com.pcs.app.service;

import com.pcs.app.domain.Rating;
import com.pcs.app.repositories.RatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class RatingService {
    @Autowired
    private RatingRepository repository;

    public List<Rating> getAllRatings(){
        return repository.findAll();
    }

    public Rating getRatingById(int bidListId){
        return repository.findById(bidListId).orElseThrow();
    }

    public Rating createRating(Rating rating) {
        return repository.save(rating);
    }

    public Rating updateRating(Rating rating){
        if (rating.getId() == null || !repository.existsById(rating.getId())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid rating id");
        }
        return repository.save(rating);
    }

    public void deleteRating(int bidListId) {
        if (!repository.existsById(bidListId)) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "No rating with given id");
        }
        repository.deleteById(bidListId);
    }
}
