package com.pcs.app.controllers;

import com.pcs.app.domain.Rating;
import com.pcs.app.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Contrôleur Spring MVC pour la gestion des taux (Rating).
 * <p>
 * Ce contrôleur gère l'ensemble des opérations CRUD (Create, Read, Update, Delete)
 * pour les taux via une interface web.
 * </p>
 *
 * @author Poseidon Capital Solutions
 * @version 1.0
 * @since 1.0
 */
@Controller
public class RatingController {

    @Autowired
    private RatingService service;

    /**
     * Affiche la liste de tous les taux.
     *
     * @param model le modèle Spring MVC pour transmettre les données à la vue
     * @return le nom de la vue pour afficher la liste des taux
     */
    @RequestMapping("/rating/list")
    public String home(Model model)
    {
        model.addAttribute("ratings", service.getAllRatings());
        return "rating/list";
    }

    /**
     * Affiche le formulaire de création d'un nouveau taux.
     *
     * @param rating l'objet Rating vide qui sera lié au formulaire
     * @return le nom de la vue contenant le formulaire de création
     */
    @GetMapping("/rating/add")
    public String addRatingForm(Rating rating) {
        return "rating/add";
    }

    /**
     * Valide et enregistre un nouveau taux.
     *
     * @param rating l'objet Rating contenant les données du formulaire
     * @param result le résultat de la validation des données
     * @param model le modèle pour transmettre les erreurs à la vue
     * @return une redirection en cas de succès, la vue précédente en cas d'erreur
     */
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

    /**
     * Affiche le formulaire de modification d'un taux existant.
     *
     * @param id l'identifiant unique du taux à modifier
     * @param model le modèle pour transmettre les données du taux à la vue
     * @return le nom de la vue contenant le formulaire de modification
     */
    @GetMapping("/rating/update/{id}")
    public String showUpdateForm(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("rating", service.getRatingById(id));
        return "rating/update";
    }

    /**
     * Valide et met à jour un taux existant.
     *
     * @param id l'identifiant unique du taux à mettre à jour
     * @param rating l'objet Rating contenant les données modifiées
     * @param result le résultat de la validation des données
     * @param model le modèle pour transmettre les erreurs à la vue
     * @return une redirection en cas de succès, la vue précédente en cas d'erreur
     */
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
        return "rating/update";
    }

    /**
     * Supprime un taux.
     *
     * @param id l'identifiant unique du taux à supprimer
     * @param model le modèle pour transmettre les éventuels messages d'erreur
     * @return une redirection pour afficher la liste mise à jour
     */
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
