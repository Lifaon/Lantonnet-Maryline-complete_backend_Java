package com.pcs.app.integration;

import com.pcs.app.domain.Rating;
import com.pcs.app.domain.User;
import com.pcs.app.repositories.RatingRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour le contrôleur RatingController.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RatingIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RatingRepository ratingRepository;

    private User user1;
    private Rating rating1;
    private Rating rating2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setFullname("Test admin");
        user1.setUsername("user1");
        user1.setRole("ROLE_ADMIN");

        ratingRepository.deleteAll();

        rating1 = new Rating();
        rating1.setMoodysRating("Aaa");
        rating1.setSandPRating("AAA");
        rating1.setFitchRating("AAA");
        rating1.setOrderNumber(1);
        rating1 = ratingRepository.save(rating1);

        rating2 = new Rating();
        rating2.setMoodysRating("Aa1");
        rating2.setSandPRating("AA+");
        rating2.setFitchRating("AA+");
        rating2.setOrderNumber(2);
        rating2 = ratingRepository.save(rating2);
    }

    @AfterEach
    void tearDown() {
        ratingRepository.deleteAll();
    }

    @Test
    void testHome_ShouldDisplayAllRatingsFromDatabase() throws Exception {
        mockMvc.perform(get("/rating/list").with(user(user1)))
                .andExpect(status().isOk())
                .andExpect(view().name("rating/list"))
                .andExpect(model().attributeExists("ratings"))
                .andExpect(model().attribute("ratings", hasSize(2)))
                .andExpect(model().attribute("ratings", hasItem(
                        hasProperty("moodysRating", is("Aaa"))
                )));
    }

    @Test
    void testAddRatingForm_ShouldDisplayAddForm() throws Exception {
        mockMvc.perform(get("/rating/add").with(user(user1)))
                .andExpect(status().isOk())
                .andExpect(view().name("rating/add"))
                .andExpect(model().attributeExists("rating"));
    }

    @Test
    void testValidate_WithValidData_ShouldCreateRatingInDatabase() throws Exception {
        long initialCount = ratingRepository.count();

        mockMvc.perform(post("/rating/validate")
                        .with(user(user1))
                        .param("moodysRating", "Baa1")
                        .param("sandPRating", "BBB+")
                        .param("fitchRating", "BBB+")
                        .param("orderNumber", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rating/list"));

        long finalCount = ratingRepository.count();
        assert finalCount == initialCount + 1;

        Rating savedRating = ratingRepository.findAll().stream()
                .filter(r -> "Baa1".equals(r.getMoodysRating()))
                .findFirst()
                .orElseThrow();

        assert savedRating.getSandPRating().equals("BBB+");
        assert savedRating.getOrderNumber().equals(3);
    }

    @Test
    void testValidate_WithInvalidData_ShouldNotCreateRatingAndShowErrors() throws Exception {
        long initialCount = ratingRepository.count();

        mockMvc.perform(post("/rating/validate")
                        .with(user(user1))
                        .param("moodysRating", "toto")
                        .param("sandPRating", "tutu")
                        .param("fitchRating", "tata")
                        .param("orderNumber", "titi"))
                .andExpect(status().isOk())
                .andExpect(view().name("rating/add"))
                .andExpect(model().attributeExists("rating"))
                .andExpect(model().attributeHasErrors("rating"));

        long finalCount = ratingRepository.count();
        assert finalCount == initialCount;
    }

    @Test
    void testShowUpdateForm_ShouldDisplayUpdateFormWithDataFromDatabase() throws Exception {
        mockMvc.perform(get("/rating/update/" + rating1.getId()).with(user(user1)))
                .andExpect(status().isOk())
                .andExpect(view().name("rating/update"))
                .andExpect(model().attributeExists("rating"))
                .andExpect(model().attribute("rating", hasProperty("moodysRating", is("Aaa"))));
    }

    @Test
    void testUpdateRating_WithValidData_ShouldUpdateRatingInDatabase() throws Exception {
        Integer ratingId = rating1.getId();

        mockMvc.perform(post("/rating/update/" + ratingId)
                        .with(user(user1))
                        .param("moodysRating", "A1")
                        .param("sandPRating", "A+")
                        .param("fitchRating", "A+")
                        .param("orderNumber", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rating/list"));

        Rating updatedRating = ratingRepository.findById(ratingId).orElseThrow();
        assert updatedRating.getMoodysRating().equals("A1");
        assert updatedRating.getSandPRating().equals("A+");
        assert updatedRating.getOrderNumber().equals(5);
    }

    @Test
    void testUpdateRating_WithInvalidData_ShouldNotUpdateRatingAndShowErrors() throws Exception {
        Integer ratingId = rating1.getId();
        String originalMoodys = rating1.getMoodysRating();

        mockMvc.perform(post("/rating/update/" + ratingId)
                        .with(user(user1))
                        .param("moodysRating", "toto")
                        .param("sandPRating", "tutu")
                        .param("fitchRating", "tata")
                        .param("orderNumber", "titi"))
                .andExpect(status().isOk())
                .andExpect(view().name("rating/update"))
                .andExpect(model().attributeExists("rating"))
                .andExpect(model().attributeHasErrors("rating"));

        Rating unchangedRating = ratingRepository.findById(ratingId).orElseThrow();
        assert unchangedRating.getMoodysRating().equals(originalMoodys);
    }

    @Test
    void testDeleteRating_ShouldRemoveRatingFromDatabase() throws Exception {
        Integer ratingId = rating1.getId();
        long initialCount = ratingRepository.count();

        mockMvc.perform(get("/rating/delete/" + ratingId).with(user(user1)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rating/list"));

        long finalCount = ratingRepository.count();
        assert finalCount == initialCount - 1;
        assert ratingRepository.findById(ratingId).isEmpty();
    }

    @Test
    void testCompleteFlow_CreateUpdateDelete_ShouldWorkEndToEnd() throws Exception {
        mockMvc.perform(post("/rating/validate")
                        .with(user(user1))
                        .param("moodysRating", "FlowTest")
                        .param("sandPRating", "FlowTest")
                        .param("fitchRating", "FlowTest")
                        .param("orderNumber", "99"))
                .andExpect(status().is3xxRedirection());

        Rating createdRating = ratingRepository.findAll().stream()
                .filter(r -> "FlowTest".equals(r.getMoodysRating()))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(post("/rating/update/" + createdRating.getId())
                        .with(user(user1))
                        .param("moodysRating", "FlowTestUpdated")
                        .param("sandPRating", "FlowTest")
                        .param("fitchRating", "FlowTest")
                        .param("orderNumber", "99"))
                .andExpect(status().is3xxRedirection());

        Rating updatedRating = ratingRepository.findById(createdRating.getId()).orElseThrow();
        assert updatedRating.getMoodysRating().equals("FlowTestUpdated");

        mockMvc.perform(get("/rating/delete/" + createdRating.getId()).with(user(user1)))
                .andExpect(status().is3xxRedirection());

        assert ratingRepository.findById(createdRating.getId()).isEmpty();
    }
}
