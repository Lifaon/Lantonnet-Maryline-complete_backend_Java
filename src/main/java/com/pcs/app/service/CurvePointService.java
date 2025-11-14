package com.pcs.app.service;

import com.pcs.app.domain.CurvePoint;
import com.pcs.app.repositories.CurvePointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CurvePointService {
    @Autowired
    CurvePointRepository repository;

    public List<CurvePoint> getAllCurvePoints(){
        return repository.findAll();
    }

    public CurvePoint getCurvePointById(int curveId){
        return repository.findById(curveId).orElseThrow();
    }

    public CurvePoint createCurvePoint(CurvePoint curve) {
        return repository.save(curve);
    }

    public CurvePoint updateCurvePoint(CurvePoint curve){
        if (curve.getId() == null || !repository.existsById(curve.getId())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid curve id");
        }
        return repository.save(curve);
    }

    public void deleteCurvePoint(int curveId) {
        if (!repository.existsById(curveId)) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "No curve with given id");
        }
        repository.deleteById(curveId);
    }
}
