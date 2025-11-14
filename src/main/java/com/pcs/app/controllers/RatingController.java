package com.pcs.app.controllers;

import com.pcs.app.domain.Rating;
import com.pcs.app.service.RatingService;
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
public class RatingController {
    @Autowired
    private RatingService service;

    @RequestMapping("/rating/list")
    public String home(Model model)
    {
        model.addAttribute("ratings", service.getAllRatings());
        return "rating/list";
    }

    @GetMapping("/rating/add")
    public String addRatingForm(Rating rating) {
        return "rating/add";
    }

    @PostMapping("/rating/validate")
    public String validate(@Valid Rating rating, BindingResult result, Model model) {
        if (!result.hasErrors()) {
            try {
                service.createRating(rating);
                return "redirect:/rating/list";
            }
            catch (Exception e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        model.addAttribute("rating", rating);
        return "rating/add";
    }

    @GetMapping("/rating/update/{id}")
    public String showUpdateForm(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("rating", service.getRatingById(id));
        return "rating/update";
    }

    @PostMapping("/rating/update/{id}")
    public String updateRating(@PathVariable("id") Integer id, @Valid Rating rating,
                             BindingResult result, Model model) {
        if (!result.hasErrors()) {
            try {
                rating.setId(id);
                service.updateRating(rating);
                return "redirect:/rating/list";
            }
            catch (Exception e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        model.addAttribute("rating", rating);
        return "redirect:/rating/list";
    }

    @GetMapping("/rating/delete/{id}")
    public String deleteRating(@PathVariable("id") Integer id, Model model) {
        try {
            service.deleteRating(id);
        }
        catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/rating/list";
    }
}
