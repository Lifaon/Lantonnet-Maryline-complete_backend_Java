package com.pcs.app.integration;

import com.pcs.app.domain.Trade;
import com.pcs.app.domain.User;
import com.pcs.app.repositories.TradeRepository;
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
 * Tests d'intégration pour le contrôleur TradeController.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TradeIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TradeRepository tradeRepository;

    private User user1;
    private Trade trade1;
    private Trade trade2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setFullname("Test admin");
        user1.setUsername("user1");
        user1.setRole("ROLE_ADMIN");

        tradeRepository.deleteAll();

        trade1 = new Trade();
        trade1.setAccount("Account1");
        trade1.setType("Type1");
        trade1.setBuyQuantity(100.0);
        trade1 = tradeRepository.save(trade1);

        trade2 = new Trade();
        trade2.setAccount("Account2");
        trade2.setType("Type2");
        trade2.setBuyQuantity(200.0);
        trade2 = tradeRepository.save(trade2);
    }

    @AfterEach
    void tearDown() {
        tradeRepository.deleteAll();
    }

    @Test
    void testHome_ShouldDisplayAllTradesFromDatabase() throws Exception {
        mockMvc.perform(get("/trade/list").with(user(user1)))
                .andExpect(status().isOk())
                .andExpect(view().name("trade/list"))
                .andExpect(model().attributeExists("trades"))
                .andExpect(model().attribute("trades", hasSize(2)))
                .andExpect(model().attribute("trades", hasItem(
                        hasProperty("account", is("Account1"))
                )));
    }

    @Test
    void testAddUser_ShouldDisplayAddForm() throws Exception {
        mockMvc.perform(get("/trade/add").with(user(user1)))
                .andExpect(status().isOk())
                .andExpect(view().name("trade/add"))
                .andExpect(model().attributeExists("trade"));
    }

    @Test
    void testValidate_WithValidData_ShouldCreateTradeInDatabase() throws Exception {
        long initialCount = tradeRepository.count();

        mockMvc.perform(post("/trade/validate")
                        .with(user(user1))
                        .param("account", "NewAccount")
                        .param("type", "NewType")
                        .param("buyQuantity", "150.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/trade/list"));

        long finalCount = tradeRepository.count();
        assert finalCount == initialCount + 1;

        Trade savedTrade = tradeRepository.findAll().stream()
                .filter(t -> "NewAccount".equals(t.getAccount()))
                .findFirst()
                .orElseThrow();

        assert savedTrade.getType().equals("NewType");
        assert savedTrade.getBuyQuantity().equals(150.0);
    }

    @Test
    void testValidate_WithInvalidData_ShouldNotCreateTradeAndShowErrors() throws Exception {
        long initialCount = tradeRepository.count();

        mockMvc.perform(post("/trade/validate")
                        .with(user(user1))
                        .param("account", "toto")
                        .param("type", "tutu")
                        .param("buyQuantity", "tata"))
                .andExpect(status().isOk())
                .andExpect(view().name("trade/add"))
                .andExpect(model().attributeExists("trade"))
                .andExpect(model().attributeHasErrors("trade"));

        long finalCount = tradeRepository.count();
        assert finalCount == initialCount;
    }

    @Test
    void testShowUpdateForm_ShouldDisplayUpdateFormWithDataFromDatabase() throws Exception {
        mockMvc.perform(get("/trade/update/" + trade1.getId()).with(user(user1)))
                .andExpect(status().isOk())
                .andExpect(view().name("trade/update"))
                .andExpect(model().attributeExists("trade"))
                .andExpect(model().attribute("trade", hasProperty("account", is("Account1"))));
    }

    @Test
    void testUpdateTrade_WithValidData_ShouldUpdateTradeInDatabase() throws Exception {
        Integer tradeId = trade1.getId();

        mockMvc.perform(post("/trade/update/" + tradeId)
                        .with(user(user1))
                        .param("account", "UpdatedAccount")
                        .param("type", "UpdatedType")
                        .param("buyQuantity", "250.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/trade/list"));

        Trade updatedTrade = tradeRepository.findById(tradeId).orElseThrow();
        assert updatedTrade.getAccount().equals("UpdatedAccount");
        assert updatedTrade.getType().equals("UpdatedType");
        assert updatedTrade.getBuyQuantity().equals(250.0);
    }

    @Test
    void testUpdateTrade_WithInvalidData_ShouldNotUpdateTradeAndShowErrors() throws Exception {
        Integer tradeId = trade1.getId();
        String originalAccount = trade1.getAccount();

        mockMvc.perform(post("/trade/update/" + tradeId)
                        .with(user(user1))
                        .param("account", "toto")
                        .param("type", "tutu")
                        .param("buyQuantity", "tata"))
                .andExpect(status().isOk())
                .andExpect(view().name("trade/update"))
                .andExpect(model().attributeExists("trade"))
                .andExpect(model().attributeHasErrors("trade"));

        Trade unchangedTrade = tradeRepository.findById(tradeId).orElseThrow();
        assert unchangedTrade.getAccount().equals(originalAccount);
    }

    @Test
    void testDeleteTrade_ShouldRemoveTradeFromDatabase() throws Exception {
        Integer tradeId = trade1.getId();
        long initialCount = tradeRepository.count();

        mockMvc.perform(get("/trade/delete/" + tradeId).with(user(user1)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/trade/list"));

        long finalCount = tradeRepository.count();
        assert finalCount == initialCount - 1;
        assert tradeRepository.findById(tradeId).isEmpty();
    }

    @Test
    void testCompleteFlow_CreateUpdateDelete_ShouldWorkEndToEnd() throws Exception {
        mockMvc.perform(post("/trade/validate")
                        .with(user(user1))
                        .param("account", "FlowTest")
                        .param("type", "TestType")
                        .param("buyQuantity", "300.0"))
                .andExpect(status().is3xxRedirection());

        Trade createdTrade = tradeRepository.findAll().stream()
                .filter(t -> "FlowTest".equals(t.getAccount()))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(post("/trade/update/" + createdTrade.getId())
                        .with(user(user1))
                        .param("account", "FlowTestUpdated")
                        .param("type", "TestType")
                        .param("buyQuantity", "400.0"))
                .andExpect(status().is3xxRedirection());

        Trade updatedTrade = tradeRepository.findById(createdTrade.getId()).orElseThrow();
        assert updatedTrade.getAccount().equals("FlowTestUpdated");

        mockMvc.perform(get("/trade/delete/" + createdTrade.getId()).with(user(user1)))
                .andExpect(status().is3xxRedirection());

        assert tradeRepository.findById(createdTrade.getId()).isEmpty();
    }
}