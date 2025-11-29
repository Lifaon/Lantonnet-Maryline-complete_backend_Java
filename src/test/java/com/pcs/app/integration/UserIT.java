package com.pcs.app.integration;

import com.pcs.app.domain.User;
import com.pcs.app.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour le contrôleur UserController.
 */
@SpringBootTest
@AutoConfigureMockMvc
class UserIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        user1 = new User();
        user1.setUsername("user1");
        user1.setPassword(passwordEncoder.encode("Password1*"));
        user1.setFullname("User One");
        user1.setRole("ROLE_USER");
        user1 = userRepository.save(user1);

        user2 = new User();
        user2.setUsername("user2");
        user2.setPassword(passwordEncoder.encode("Password2*"));
        user2.setFullname("User Two");
        user2.setRole("ROLE_ADMIN");
        user2 = userRepository.save(user2);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void testHome_ShouldDisplayAllUsersFromDatabase() throws Exception {
        mockMvc.perform(get("/user/list").with(user(user2)))
                .andExpect(status().isOk())
                .andExpect(view().name("user/list"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attribute("users", hasSize(2)))
                .andExpect(model().attribute("users", hasItem(
                        hasProperty("username", is("user1"))
                )));
    }

    @Test
    void testAddUser_ShouldDisplayAddForm() throws Exception {
        mockMvc.perform(get("/user/add").with(user(user2)))
                .andExpect(status().isOk())
                .andExpect(view().name("user/add"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void testValidate_WithValidData_ShouldCreateUserInDatabaseWithEncodedPassword() throws Exception {
        long initialCount = userRepository.count();

        mockMvc.perform(post("/user/validate")
                        .with(user(user2))
                        .param("username", "newuser")
                        .param("password", "Newpassword1*")
                        .param("fullname", "New User")
                        .param("role", "ROLE_USER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/list"));

        long finalCount = userRepository.count();
        assert finalCount == initialCount + 1;

        User savedUser = userRepository.findAll().stream()
                .filter(u -> "newuser".equals(u.getUsername()))
                .findFirst()
                .orElseThrow();

        assert savedUser.getFullname().equals("New User");
        assert savedUser.getRole().equals("ROLE_USER");
        // Vérifier que le mot de passe est encodé
        assert !savedUser.getPassword().equals("Newpassword1*");
        assert passwordEncoder.matches("Newpassword1*", savedUser.getPassword());
    }

    @Test
    void testValidate_WithInvalidData_ShouldNotCreateUserAndShowErrors() throws Exception {
        long initialCount = userRepository.count();

        mockMvc.perform(post("/user/validate")
                        .with(user(user2))
                        .param("username", "")
                        .param("password", "")
                        .param("fullname", "")
                        .param("role", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("user/add"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeHasErrors("user"));

        long finalCount = userRepository.count();
        assert finalCount == initialCount;
    }

    @Test
    void testShowUpdateForm_ShouldDisplayUpdateFormWithDataFromDatabaseAndEmptyPassword() throws Exception {
        mockMvc.perform(get("/user/update/" + user1.getId()).with(user(user2)))
                .andExpect(status().isOk())
                .andExpect(view().name("user/update"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("user", hasProperty("username", is("user1"))))
                .andExpect(model().attribute("user", hasProperty("password", is(""))));
    }

    @Test
    void testUpdateUser_WithValidData_ShouldUpdateUserInDatabaseWithEncodedPassword() throws Exception {
        Integer userId = user1.getId();

        mockMvc.perform(post("/user/update/" + userId)
                        .with(user(user2))
                        .param("username", "updateduser")
                        .param("password", "Updatedpassword*1")
                        .param("fullname", "Updated User")
                        .param("role", "ROLE_ADMIN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/list"));

        User updatedUser = userRepository.findById(userId).orElseThrow();
        assert updatedUser.getUsername().equals("updateduser");
        assert updatedUser.getFullname().equals("Updated User");
        assert updatedUser.getRole().equals("ROLE_ADMIN");
        // Vérifier que le nouveau mot de passe est encodé
        assert passwordEncoder.matches("Updatedpassword*1", updatedUser.getPassword());
    }

    @Test
    void testUpdateUser_WithInvalidData_ShouldNotUpdateUserAndShowErrors() throws Exception {
        Integer userId = user1.getId();
        String originalUsername = user1.getUsername();

        mockMvc.perform(post("/user/update/" + userId)
                        .with(user(user2))
                        .param("username", "")
                        .param("password", "")
                        .param("fullname", "")
                        .param("role", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("user/update"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeHasErrors("user"));

        User unchangedUser = userRepository.findById(userId).orElseThrow();
        assert unchangedUser.getUsername().equals(originalUsername);
    }

    @Test
    void testDeleteUser_ShouldRemoveUserFromDatabase() throws Exception {
        Integer userId = user1.getId();
        long initialCount = userRepository.count();

        mockMvc.perform(get("/user/delete/" + userId).with(user(user2)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/list"));

        long finalCount = userRepository.count();
        assert finalCount == initialCount - 1;
        assert userRepository.findById(userId).isEmpty();
    }

    @Test
    void testCompleteFlow_CreateUpdateDelete_ShouldWorkEndToEnd() throws Exception {
        mockMvc.perform(post("/user/validate")
                        .with(user(user2))
                        .param("username", "flowtest")
                        .param("password", "Flowtestpass1*")
                        .param("fullname", "Flow Test")
                        .param("role", "ROLE_USER"))
                .andExpect(status().is3xxRedirection());

        User createdUser = userRepository.findAll().stream()
                .filter(u -> "flowtest".equals(u.getUsername()))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(post("/user/update/" + createdUser.getId())
                        .with(user(user2))
                        .param("username", "flowtestupdated")
                        .param("password", "Newtestpass1*")
                        .param("fullname", "Flow Test Updated")
                        .param("role", "ROLE_ADMIN"))
                .andExpect(status().is3xxRedirection());

        User updatedUser = userRepository.findById(createdUser.getId()).orElseThrow();
        assert updatedUser.getUsername().equals("flowtestupdated");
        assert updatedUser.getRole().equals("ROLE_ADMIN");

        mockMvc.perform(get("/user/delete/" + createdUser.getId()).with(user(user2)))
                .andExpect(status().is3xxRedirection());

        assert userRepository.findById(createdUser.getId()).isEmpty();
    }
}