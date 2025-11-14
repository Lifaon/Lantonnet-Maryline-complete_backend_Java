package com.pcs.app.controllers;

import com.pcs.app.domain.Trade;
import com.pcs.app.service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;

@Controller
public class TradeController {
    @Autowired
    private TradeService service;

    @RequestMapping("/trade/list")
    public String home(Model model)
    {
        model.addAttribute("trades", service.getAllTrades());
        return "trade/list";
    }

    @GetMapping("/trade/add")
    public String addUser(Trade bid) {
        return "trade/add";
    }

    @PostMapping("/trade/validate")
    public String validate(@Valid Trade trade, BindingResult result, Model model) {
        if (!result.hasErrors()) {
            try {
                service.createTrade(trade);
                return "redirect:/trade/list";
            }
            catch (Exception e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        model.addAttribute("trade", trade);
        return "trade/add";
    }

    @GetMapping("/trade/update/{id}")
    public String showUpdateForm(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("trade", service.getTradeById(id));
        return "trade/update";
    }

    @PostMapping("/trade/update/{id}")
    public String updateTrade(@PathVariable("id") Integer id, @Valid Trade trade,
                             BindingResult result, Model model) {
        if (!result.hasErrors()) {
            try {
                trade.setId(id);
                service.updateTrade(trade);
                return "redirect:/trade/list";
            }
            catch (Exception e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        model.addAttribute("trade", trade);
        return "redirect:/trade/list";
    }

    @GetMapping("/trade/delete/{id}")
    public String deleteTrade(@PathVariable("id") Integer id, Model model) {
        try {
            service.deleteTrade(id);
        }
        catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/trade/list";
    }
}
