package com.pcs.app.controllers;

import com.pcs.app.domain.Trade;
import com.pcs.app.service.TradeService;
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
 * Contrôleur Spring MVC pour la gestion des échanges (Trade).
 * <p>
 * Ce contrôleur gère l'ensemble des opérations CRUD (Create, Read, Update, Delete)
 * pour les échanges via une interface web.
 * </p>
 *
 * @author Poseidon Capital Solutions
 * @version 1.0
 * @since 1.0
 */
@Controller
public class TradeController {

    @Autowired
    private TradeService service;

    /**
     * Affiche la liste de tous les échanges.
     *
     * @param model le modèle Spring MVC pour transmettre les données à la vue
     * @return le nom de la vue pour afficher la liste des échanges
     */
    @RequestMapping("/trade/list")
    public String home(Model model)
    {
        model.addAttribute("trades", service.getAllTrades());
        return "trade/list";
    }

    /**
     * Affiche le formulaire de création d'un nouvel échange.
     *
     * @param trade l'objet Trade vide qui sera lié au formulaire
     * @return le nom de la vue contenant le formulaire de création
     */
    @GetMapping("/trade/add")
    public String addUser(Trade trade) {
        return "trade/add";
    }

    /**
     * Valide et enregistre un nouvel échange.
     *
     * @param trade l'objet Trade contenant les données du formulaire
     * @param result le résultat de la validation des données
     * @param model le modèle pour transmettre les erreurs à la vue
     * @return une redirection en cas de succès, la vue précédente en cas d'erreur
     */
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

    /**
     * Affiche le formulaire de modification d'un échange existant.
     *
     * @param id l'identifiant unique de l'échange à modifier
     * @param model le modèle pour transmettre les données de l'échange à la vue
     * @return le nom de la vue contenant le formulaire de modification
     */
    @GetMapping("/trade/update/{id}")
    public String showUpdateForm(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("trade", service.getTradeById(id));
        return "trade/update";
    }

    /**
     * Valide et met à jour un échange existant.
     *
     * @param id l'identifiant unique de l'échange à mettre à jour
     * @param trade l'objet Trade contenant les données modifiées
     * @param result le résultat de la validation des données
     * @param model le modèle pour transmettre les erreurs à la vue
     * @return une redirection en cas de succès, la vue précédente en cas d'erreur
     */
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
        return "trade/update";
    }

    /**
     * Supprime un échange.
     *
     * @param id l'identifiant unique de l'échange à supprimer
     * @param model le modèle pour transmettre les éventuels messages d'erreur
     * @return une redirection pour afficher la liste mise à jour
     */
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
