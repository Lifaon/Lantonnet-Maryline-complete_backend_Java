package com.pcs.app.controllers;

import com.pcs.app.domain.BidList;
import com.pcs.app.service.BidListService;
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
 * Contrôleur Spring MVC pour la gestion des enchères (BidList).
 * <p>
 * Ce contrôleur gère l'ensemble des opérations CRUD (Create, Read, Update, Delete)
 * pour les enchères via une interface web.
 * </p>
 *
 * @author Poseidon Capital Solutions
 * @version 1.0
 * @since 1.0
 */
@Controller
public class BidListController {

    @Autowired
    private BidListService service;

    /**
     * Affiche la liste de toutes les enchères.
     *
     * @param model le modèle Spring MVC pour transmettre les données à la vue
     * @return le nom de la vue pour afficher la liste des enchères
     */
    @RequestMapping("/bidList/list")
    public String home(Model model) {
        model.addAttribute("bidLists", service.getAllBidLists());
        return "bidList/list";
    }

    /**
     * Affiche le formulaire de création d'une nouvelle enchère.
     *
     * @param bid l'objet BidList vide qui sera lié au formulaire
     * @return le nom de la vue contenant le formulaire de création
     */
    @GetMapping("/bidList/add")
    public String addBidForm(BidList bid) {
        return "bidList/add";
    }

    /**
     * Valide et enregistre une nouvelle enchère.
     *
     * @param bid l'objet BidList contenant les données du formulaire
     * @param result le résultat de la validation des données
     * @param model le modèle pour transmettre les erreurs à la vue
     * @return une redirection en cas de succès, la vue précédente en cas d'erreur
     */
    @PostMapping("/bidList/validate")
    public String validate(@Valid BidList bid, BindingResult result, Model model) {
        if (!result.hasErrors()) {
            try {
                service.createBidList(bid);
                return "redirect:/bidList/list";
            }
            catch (Exception e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        model.addAttribute("bidList", bid);
        return "bidList/add";
    }

    /**
     * Affiche le formulaire de modification d'une enchère existante.
     *
     * @param id l'identifiant unique de l'enchère à modifier
     * @param model le modèle pour transmettre les données de l'enchère à la vue
     * @return le nom de la vue contenant le formulaire de modification
     */
    @GetMapping("/bidList/update/{id}")
    public String showUpdateForm(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("bidList", service.getBidListById(id));
        return "bidList/update";
    }

    /**
     * Valide et met à jour une enchère existante.
     *
     * @param id l'identifiant unique de l'enchère à mettre à jour
     * @param bidList l'objet BidList contenant les données modifiées
     * @param result le résultat de la validation des données
     * @param model le modèle pour transmettre les erreurs à la vue
     * @return une redirection en cas de succès, la vue précédente en cas d'erreur
     */
    @PostMapping("/bidList/update/{id}")
    public String updateBid(@PathVariable("id") Integer id, @Valid BidList bidList,
                            BindingResult result, Model model) {
        if (!result.hasErrors()) {
            try {
                bidList.setId(id);
                service.updateBidList(bidList);
                return "redirect:/bidList/list";
            }
            catch (Exception e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        model.addAttribute("bidList", bidList);
        return "bidList/update";
    }

    /**
     * Supprime une enchère.
     *
     * @param id l'identifiant unique de l'enchère à supprimer
     * @param model le modèle pour transmettre les éventuels messages d'erreur
     * @return une redirection pour afficher la liste mise à jour
     */
    @GetMapping("/bidList/delete/{id}")
    public String deleteBid(@PathVariable("id") Integer id, Model model) {
        try {
            service.deleteBidList(id);
        }
        catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/bidList/list";
    }
}