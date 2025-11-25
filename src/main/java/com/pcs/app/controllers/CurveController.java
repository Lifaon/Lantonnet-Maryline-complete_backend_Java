package com.pcs.app.controllers;

import com.pcs.app.domain.CurvePoint;
import com.pcs.app.service.CurvePointService;
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
 * Contrôleur Spring MVC pour la gestion des points de courbes (CurvePoint).
 * <p>
 * Ce contrôleur gère l'ensemble des opérations CRUD (Create, Read, Update, Delete)
 * pour les points de courbes via une interface web.
 * </p>
 *
 * @author Poseidon Capital Solutions
 * @version 1.0
 * @since 1.0
 */
@Controller
public class CurveController {

    @Autowired
    private CurvePointService service;

    /**
     * Affiche la liste de tous les points de courbes.
     *
     * @param model le modèle Spring MVC pour transmettre les données à la vue
     * @return le nom de la vue pour afficher la liste des points de courbes
     */
    @RequestMapping("/curvePoint/list")
    public String home(Model model)
    {
        model.addAttribute("curvePoints", service.getAllCurvePoints());
        return "curvePoint/list";
    }

    /**
     * Affiche le formulaire de création d'un nouveau point de courbe.
     *
     * @param curvePoint l'objet CurvePoint vide qui sera lié au formulaire
     * @return le nom de la vue contenant le formulaire de création
     */
    @GetMapping("/curvePoint/add")
    public String addBidForm(CurvePoint curvePoint) {
        return "curvePoint/add";
    }

    /**
     * Valide et enregistre un nouveau point de courbe.
     *
     * @param curvePoint l'objet CurvePoint contenant les données du formulaire
     * @param result le résultat de la validation des données
     * @param model le modèle pour transmettre les erreurs à la vue
     * @return une redirection en cas de succès, la vue précédente en cas d'erreur
     */
    @PostMapping("/curvePoint/validate")
    public String validate(@Valid CurvePoint curvePoint, BindingResult result, Model model) {
        if (!result.hasErrors()) {
            try {
                service.createCurvePoint(curvePoint);
                return "redirect:/curvePoint/list";
            }
            catch (Exception e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        model.addAttribute("curvePoint", curvePoint);
        return "curvePoint/add";
    }

    /**
     * Affiche le formulaire de modification d'un point de courbe existant.
     *
     * @param id l'identifiant unique du point de courbe à modifier
     * @param model le modèle pour transmettre les données du point de courbe à la vue
     * @return le nom de la vue contenant le formulaire de modification
     */
    @GetMapping("/curvePoint/update/{id}")
    public String showUpdateForm(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("curvePoint", service.getCurvePointById(id));
        return "curvePoint/update";
    }

    /**
     * Valide et met à jour un point de courbe existant.
     *
     * @param id l'identifiant unique du point de courbe à mettre à jour
     * @param curvePoint l'objet CurvePoint contenant les données modifiées
     * @param result le résultat de la validation des données
     * @param model le modèle pour transmettre les erreurs à la vue
     * @return une redirection en cas de succès, la vue précédente en cas d'erreur
     */
    @PostMapping("/curvePoint/update/{id}")
    public String updateBid(@PathVariable("id") Integer id, @Valid CurvePoint curvePoint,
                             BindingResult result, Model model) {
        if (!result.hasErrors()) {
            try {
                curvePoint.setId(id);
                service.updateCurvePoint(curvePoint);
                return "redirect:/curvePoint/list";
            }
            catch (Exception e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        model.addAttribute("curvePoint", curvePoint);
        return "curvePoint/update";
    }

    /**
     * Supprime un point de courbe.
     *
     * @param id l'identifiant unique du point de courbe à supprimer
     * @param model le modèle pour transmettre les éventuels messages d'erreur
     * @return une redirection pour afficher la liste mise à jour
     */
    @GetMapping("/curvePoint/delete/{id}")
    public String deleteBid(@PathVariable("id") Integer id, Model model) {
        try {
            service.deleteCurvePoint(id);
        }
        catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/curvePoint/list";
    }
}
