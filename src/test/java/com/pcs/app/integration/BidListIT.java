package com.pcs.app.integration;

import com.pcs.app.domain.BidList;
import com.pcs.app.domain.User;
import com.pcs.app.repositories.BidListRepository;
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

@SpringBootTest
@AutoConfigureMockMvc
class BidListIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BidListRepository bidListRepository;

    private User user1;
    private BidList bidList1;
    private BidList bidList2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setFullname("Test admin");
        user1.setUsername("user1");
        user1.setRole("ROLE_ADMIN");

        bidListRepository.deleteAll();

        bidList1 = new BidList();
        bidList1.setAccount("Account1");
        bidList1.setType("Type1");
        bidList1.setBidQuantity(100.0);
        bidList1 = bidListRepository.save(bidList1);

        bidList2 = new BidList();
        bidList2.setAccount("Account2");
        bidList2.setType("Type2");
        bidList2.setBidQuantity(200.0);
        bidList2 = bidListRepository.save(bidList2);
    }

    @AfterEach
    void tearDown() {
        bidListRepository.deleteAll();
    }

    /**
     * Test de l'affichage de la liste des enchères.
     * Vérifie que toutes les enchères présentes en base sont affichées.
     */
    @Test
    void testHome_ShouldDisplayAllBidsFromDatabase() throws Exception {
        mockMvc.perform(get("/bidList/list").with(user(user1)))
                .andExpect(status().isOk())
                .andExpect(view().name("bidList/list"))
                .andExpect(model().attributeExists("bidLists"))
                .andExpect(model().attribute("bidLists", hasSize(2)))
                .andExpect(model().attribute("bidLists", hasItem(
                        allOf(
                                hasProperty("account", is("Account1")),
                                hasProperty("type", is("Type1")),
                                hasProperty("bidQuantity", is(100.0))
                        )
                )))
                .andExpect(model().attribute("bidLists", hasItem(
                        allOf(
                                hasProperty("account", is("Account2")),
                                hasProperty("type", is("Type2"))
                        )
                )));
    }

    /**
     * Test de l'affichage du formulaire d'ajout.
     * Vérifie que le formulaire de création s'affiche correctement.
     */
    @Test
    void testAddBidForm_ShouldDisplayAddForm() throws Exception {
        mockMvc.perform(get("/bidList/add").with(user(user1)))
                .andExpect(status().isOk())
                .andExpect(view().name("bidList/add"))
                .andExpect(model().attributeExists("bidList"));
    }

    /**
     * Test de la création d'une nouvelle enchère avec des données valides.
     * Vérifie que l'enchère est bien enregistrée en base de données.
     */
    @Test
    void testValidate_WithValidData_ShouldCreateBidInDatabase() throws Exception {
        // Vérifier le nombre initial d'enchères
        long initialCount = bidListRepository.count();

        mockMvc.perform(post("/bidList/validate")
                        .param("account", "NewAccount")
                        .param("type", "NewType")
                        .param("bidQuantity", "150.0")
                        .with(user(user1)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bidList/list"));

        // Vérifier que l'enchère a été créée en base
        long finalCount = bidListRepository.count();
        assert finalCount == initialCount + 1;

        // Vérifier les données de la nouvelle enchère
        BidList savedBid = bidListRepository.findAll().stream()
                .filter(b -> "NewAccount".equals(b.getAccount()))
                .findFirst()
                .orElseThrow();

        assert savedBid.getType().equals("NewType");
        assert savedBid.getBidQuantity().equals(150.0);
    }

    /**
     * Test de la validation avec des données invalides.
     * Vérifie que l'enchère n'est pas créée et les erreurs sont affichées.
     */
    @Test
    void testValidate_WithInvalidData_ShouldNotCreateBidAndShowErrors() throws Exception {
        long initialCount = bidListRepository.count();

        mockMvc.perform(post("/bidList/validate")
                        .param("account", "")  // Invalide
                        .param("type", "")
                        .param("bidQuantity", "")
                        .with(user(user1)))
                .andExpect(status().isOk())
                .andExpect(view().name("bidList/add"))
                .andExpect(model().attributeExists("bidList"))
                .andExpect(model().attributeHasErrors("bidList"));

        // Vérifier qu'aucune enchère n'a été créée
        long finalCount = bidListRepository.count();
        assert finalCount == initialCount;
    }

    /**
     * Test de l'affichage du formulaire de modification.
     * Vérifie que le formulaire est prérempli avec les données de la base.
     */
    @Test
    void testShowUpdateForm_ShouldDisplayUpdateFormWithDataFromDatabase() throws Exception {
        mockMvc.perform(get("/bidList/update/" + bidList1.getId()).with(user(user1)))
                .andExpect(status().isOk())
                .andExpect(view().name("bidList/update"))
                .andExpect(model().attributeExists("bidList"))
                .andExpect(model().attribute("bidList", hasProperty("id", is(bidList1.getId()))))
                .andExpect(model().attribute("bidList", hasProperty("account", is("Account1"))))
                .andExpect(model().attribute("bidList", hasProperty("type", is("Type1"))));
    }

    /**
     * Test de la mise à jour d'une enchère avec des données valides.
     * Vérifie que les modifications sont bien persistées en base de données.
     */
    @Test
    void testUpdateBid_WithValidData_ShouldUpdateBidInDatabase() throws Exception {
        Integer bidId = bidList1.getId();

        mockMvc.perform(post("/bidList/update/" + bidId)
                        .param("account", "UpdatedAccount")
                        .param("type", "UpdatedType")
                        .param("bidQuantity", "250.0")
                        .with(user(user1)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bidList/list"));

        // Vérifier que l'enchère a été mise à jour en base
        BidList updatedBid = bidListRepository.findById(bidId).orElseThrow();
        assert updatedBid.getAccount().equals("UpdatedAccount");
        assert updatedBid.getType().equals("UpdatedType");
        assert updatedBid.getBidQuantity().equals(250.0);
    }

    /**
     * Test de la mise à jour avec des données invalides.
     * Vérifie que l'enchère n'est pas modifiée en base.
     */
    @Test
    void testUpdateBid_WithInvalidData_ShouldNotUpdateBidAndShowErrors() throws Exception {
        Integer bidId = bidList1.getId();
        String originalAccount = bidList1.getAccount();

        mockMvc.perform(post("/bidList/update/" + bidId)
                        .param("account", "")  // Invalide
                        .param("type", "")
                        .param("bidQuantity", "")
                        .with(user(user1)))
                .andExpect(status().isOk())
                .andExpect(view().name("bidList/update"))
                .andExpect(model().attributeExists("bidList"))
                .andExpect(model().attributeHasErrors("bidList"));

        // Vérifier que l'enchère n'a pas été modifiée
        BidList unchangedBid = bidListRepository.findById(bidId).orElseThrow();
        assert unchangedBid.getAccount().equals(originalAccount);
    }

    /**
     * Test de la suppression d'une enchère.
     * Vérifie que l'enchère est effectivement supprimée de la base de données.
     */
    @Test
    void testDeleteBid_ShouldRemoveBidFromDatabase() throws Exception {
        Integer bidId = bidList1.getId();
        long initialCount = bidListRepository.count();

        mockMvc.perform(get("/bidList/delete/" + bidId).with(user(user1)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bidList/list"));

        // Vérifier que l'enchère a été supprimée
        long finalCount = bidListRepository.count();
        assert finalCount == initialCount - 1;
        assert bidListRepository.findById(bidId).isEmpty();
    }

    /**
     * Test de la liste lorsqu'aucune enchère n'existe en base.
     * Vérifie que la page s'affiche correctement avec une liste vide.
     */
    @Test
    void testHome_WithEmptyDatabase_ShouldDisplayEmptyList() throws Exception {
        bidListRepository.deleteAll();

        mockMvc.perform(get("/bidList/list").with(user(user1)))
                .andExpect(status().isOk())
                .andExpect(view().name("bidList/list"))
                .andExpect(model().attributeExists("bidLists"))
                .andExpect(model().attribute("bidLists", hasSize(0)));
    }

    /**
     * Test du flux complet : création, modification et suppression.
     * Vérifie l'intégration complète de toutes les opérations CRUD.
     */
    @Test
    void testCompleteFlow_CreateUpdateDelete_ShouldWorkEndToEnd() throws Exception {
        // 1. Créer une nouvelle enchère
        mockMvc.perform(post("/bidList/validate")
                        .param("account", "FlowTest")
                        .param("type", "TestType")
                        .param("bidQuantity", "300.0")
                        .with(user(user1)))
                .andExpect(status().is3xxRedirection());

        BidList createdBid = bidListRepository.findAll().stream()
                .filter(b -> "FlowTest".equals(b.getAccount()))
                .findFirst()
                .orElseThrow();

        // 2. Modifier l'enchère
        mockMvc.perform(post("/bidList/update/" + createdBid.getId())
                        .param("account", "FlowTestUpdated")
                        .param("type", "TestType")
                        .param("bidQuantity", "400.0")
                        .with(user(user1)))
                .andExpect(status().is3xxRedirection());

        BidList updatedBid = bidListRepository.findById(createdBid.getId()).orElseThrow();
        assert updatedBid.getAccount().equals("FlowTestUpdated");
        assert updatedBid.getBidQuantity().equals(400.0);

        // 3. Supprimer l'enchère
        mockMvc.perform(get("/bidList/delete/" + createdBid.getId()).with(user(user1)))
                .andExpect(status().is3xxRedirection());

        assert bidListRepository.findById(createdBid.getId()).isEmpty();
    }
}