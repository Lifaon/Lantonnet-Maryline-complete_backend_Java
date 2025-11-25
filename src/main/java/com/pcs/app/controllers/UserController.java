package com.pcs.app.controllers;

import com.pcs.app.domain.User;
import com.pcs.app.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Contrôleur Spring MVC pour la gestion des utilisateurs (User).
 * <p>
 * Ce contrôleur gère l'ensemble des opérations CRUD (Create, Read, Update, Delete)
 * pour les utilisateurs via une interface web. Il est restreint aux utilisateurs
 * ayant le rôle d'administrateur
 * </p>
 *
 * @author Poseidon Capital Solutions
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Controller
public class UserController {

    @Autowired
    private UserService service;
    @Autowired
    private ApplicationContext context;

    /**
     * Affiche la liste de tous les utilisateurs.
     *
     * @param model le modèle Spring MVC pour transmettre les données à la vue
     * @return le nom de la vue pour afficher la liste des utilisateurs
     */
    @RequestMapping("/user/list")
    public String home(Model model)
    {
        model.addAttribute("users", service.getAllUsers());
        return "user/list";
    }

    /**
     * Affiche le formulaire de création d'un nouvel utilisateur.
     *
     * @param user l'objet User vide qui sera lié au formulaire
     * @return le nom de la vue contenant le formulaire de création
     */
    @GetMapping("/user/add")
    public String addUser(User user) {
        return "user/add";
    }

    /**
     * Valide et enregistre un nouvel utilisateur.
     *
     * @param user l'objet User contenant les données du formulaire
     * @param result le résultat de la validation des données
     * @param model le modèle pour transmettre les erreurs à la vue
     * @return une redirection en cas de succès, la vue précédente en cas d'erreur
     */
    @PostMapping("/user/validate")
    public String validate(@Valid User user, BindingResult result, Model model) {
        if (!result.hasErrors()) {
            PasswordEncoder encoder = context.getBean(PasswordEncoder.class);
            user.setPassword(encoder.encode(user.getPassword()));
            try {
                service.createUser(user);
                return "redirect:/user/list";
            }
            catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        model.addAttribute("user", user);
        return "user/add";
    }

    /**
     * Affiche le formulaire de modification d'un utilisateur existant.
     *
     * @param id l'identifiant unique de l'utilisateur à modifier
     * @param model le modèle pour transmettre les données de l'utilisateur à la vue
     * @return le nom de la vue contenant le formulaire de modification
     */
    @GetMapping("/user/update/{id}")
    public String showUpdateForm(@PathVariable("id") Integer id, Model model) {
        User user = service.getUserById(id);
        user.setPassword("");
        model.addAttribute("user", user);
        return "user/update";
    }

    /**
     * Valide et met à jour un utilisateur existant.
     *
     * @param id l'identifiant unique de l'utilisateur à mettre à jour
     * @param user l'objet User contenant les données modifiées
     * @param result le résultat de la validation des données
     * @param model le modèle pour transmettre les erreurs à la vue
     * @return une redirection en cas de succès, la vue précédente en cas d'erreur
     */
    @PostMapping("/user/update/{id}")
    public String updateUser(@PathVariable("id") Integer id, @Valid User user,
                             BindingResult result, Model model) {
        if (!result.hasErrors()) {
            PasswordEncoder encoder = context.getBean(PasswordEncoder.class);
            user.setPassword(encoder.encode(user.getPassword()));
            user.setId(id);
            try {
                service.updateUser(user);
                return "redirect:/user/list";
            }
            catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        model.addAttribute("user", user);
        return "user/update";
    }

    /**
     * Supprime un utilisateur.
     *
     * @param id l'identifiant unique de l'utilisateur à supprimer
     * @param model le modèle pour transmettre les éventuels messages d'erreur
     * @return une redirection pour afficher la liste mise à jour
     */
    @GetMapping("/user/delete/{id}")
    public String deleteUser(@PathVariable("id") Integer id, Model model) {
        try {
            service.deleteUser(id);
        }
        catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/user/list";
    }
}
