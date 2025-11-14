package com.pcs.app.service;

import com.pcs.app.domain.RuleName;
import com.pcs.app.repositories.RuleNameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class RuleNameService {
    @Autowired
    private RuleNameRepository repository;

    public List<RuleName> getAllRuleNames(){
        return repository.findAll();
    }

    public RuleName getRuleNameById(int ruleNameId){
        return repository.findById(ruleNameId).orElseThrow();
    }

    public RuleName createRuleName(RuleName ruleName) {
        return repository.save(ruleName);
    }

    public RuleName updateRuleName(RuleName ruleName){
        if (ruleName.getId() == null || !repository.existsById(ruleName.getId())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ruleName id");
        }
        return repository.save(ruleName);
    }

    public void deleteRuleName(int ruleNameId) {
        if (!repository.existsById(ruleNameId)) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "No ruleName with given id");
        }
        repository.deleteById(ruleNameId);
    }
}
