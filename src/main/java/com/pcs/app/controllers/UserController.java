package com.pcs.app.controllers;

import com.pcs.app.domain.User;
import com.pcs.app.service.UserService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    @Autowired
    private UserService service;
    @Autowired
    private ApplicationContext context;

    @RequestMapping("/user/list")
    public String home(Model model)
    {
        model.addAttribute("users", service.getAllUsers());
        return "user/list";
    }

    @GetMapping("/user/add")
    public String addUser(User bid) {
        return "user/add";
    }

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

    @GetMapping("/user/update/{id}")
    public String showUpdateForm(@PathVariable("id") Integer id, Model model) {
        User user = service.getUserById(id);
        user.setPassword("");
        model.addAttribute("user", user);
        return "user/update";
    }

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
