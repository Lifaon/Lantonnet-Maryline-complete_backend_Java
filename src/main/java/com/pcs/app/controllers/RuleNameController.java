package com.pcs.app.controllers;

import com.pcs.app.domain.RuleName;
import com.pcs.app.service.RuleNameService;
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
 * Contrôleur Spring MVC pour la gestion des noms de règle (RuleName).
 * <p>
 * Ce contrôleur gère l'ensemble des opérations CRUD (Create, Read, Update, Delete)
 * pour les noms de règle via une interface web.
 * </p>
 *
 * @author Poseidon Capital Solutions
 * @version 1.0
 * @since 1.0
 */
@Controller
public class RuleNameController {

    @Autowired
    private RuleNameService service;

    /**
     * Affiche la liste de tous les noms de règle.
     *
     * @param model le modèle Spring MVC pour transmettre les données à la vue
     * @return le nom de la vue pour afficher la liste des noms de règle
     */
    @RequestMapping("/ruleName/list")
    public String home(Model model)
    {
        model.addAttribute("ruleNames", service.getAllRuleNames());
        return "ruleName/list";
    }

    /**
     * Affiche le formulaire de création d'un nouveau nom de règle.
     *
     * @param ruleName l'objet RuleName vide qui sera lié au formulaire
     * @return le nom de la vue contenant le formulaire de création
     */
    @GetMapping("/ruleName/add")
    public String addRuleForm(RuleName ruleName) {
        return "ruleName/add";
    }

    /**
     * Valide et enregistre un nouveau nom de règle.
     *
     * @param ruleName l'objet RuleName contenant les données du formulaire
     * @param result le résultat de la validation des données
     * @param model le modèle pour transmettre les erreurs à la vue
     * @return une redirection en cas de succès, la vue précédente en cas d'erreur
     */
    @PostMapping("/ruleName/validate")
    public String validate(@Valid RuleName ruleName, BindingResult result, Model model) {
        if (!result.hasErrors()) {
            try {
                service.createRuleName(ruleName);
                return "redirect:/ruleName/list";
            }
            catch (Exception e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        model.addAttribute("ruleName", ruleName);
        return "ruleName/add";
    }

    /**
     * Affiche le formulaire de modification d'un nom de règle existant.
     *
     * @param id l'identifiant unique du nom de règle à modifier
     * @param model le modèle pour transmettre les données du nom de règle à la vue
     * @return le nom de la vue contenant le formulaire de modification
     */
    @GetMapping("/ruleName/update/{id}")
    public String showUpdateForm(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("ruleName", service.getRuleNameById(id));
        return "ruleName/update";
    }

    /**
     * Valide et met à jour un nom de règle existant.
     *
     * @param id l'identifiant unique du nom de règle à mettre à jour
     * @param ruleName l'objet RuleName contenant les données modifiées
     * @param result le résultat de la validation des données
     * @param model le modèle pour transmettre les erreurs à la vue
     * @return une redirection en cas de succès, la vue précédente en cas d'erreur
     */
    @PostMapping("/ruleName/update/{id}")
    public String updateRuleName(@PathVariable("id") Integer id, @Valid RuleName ruleName,
                             BindingResult result, Model model) {
        if (!result.hasErrors()) {
            try {
                ruleName.setId(id);
                service.updateRuleName(ruleName);
                return "redirect:/ruleName/list";
            }
            catch (Exception e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        model.addAttribute("ruleName", ruleName);
        return "ruleName/update";
    }

    /**
     * Supprime un nom de règle.
     *
     * @param id l'identifiant unique du nom de règle à supprimer
     * @param model le modèle pour transmettre les éventuels messages d'erreur
     * @return une redirection pour afficher la liste mise à jour
     */
    @GetMapping("/ruleName/delete/{id}")
    public String deleteRuleName(@PathVariable("id") Integer id, Model model) {
        try {
            service.deleteRuleName(id);
        }
        catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }        return "redirect:/ruleName/list";
    }
}
