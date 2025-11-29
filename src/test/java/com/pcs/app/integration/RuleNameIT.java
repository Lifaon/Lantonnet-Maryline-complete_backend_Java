package com.pcs.app.integration;

import com.pcs.app.domain.RuleName;
import com.pcs.app.domain.User;
import com.pcs.app.repositories.RuleNameRepository;
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
 * Tests d'intégration pour le contrôleur RuleNameController.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RuleNameIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RuleNameRepository ruleNameRepository;

    private User user1;
    private RuleName ruleName1;
    private RuleName ruleName2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setFullname("Test admin");
        user1.setUsername("user1");
        user1.setRole("ROLE_ADMIN");

        ruleNameRepository.deleteAll();

        ruleName1 = new RuleName();
        ruleName1.setName("Rule1");
        ruleName1.setDescription("Description1");
        ruleName1.setJson("{}");
        ruleName1.setTemplate("Template1");
        ruleName1.setSqlStr("SELECT * FROM table1");
        ruleName1.setSqlPart("WHERE id = 1");
        ruleName1 = ruleNameRepository.save(ruleName1);

        ruleName2 = new RuleName();
        ruleName2.setName("Rule2");
        ruleName2.setDescription("Description2");
        ruleName2.setJson("{}");
        ruleName2.setTemplate("Template2");
        ruleName2.setSqlStr("SELECT * FROM table2");
        ruleName2.setSqlPart("WHERE id = 2");
        ruleName2 = ruleNameRepository.save(ruleName2);
    }

    @AfterEach
    void tearDown() {
        ruleNameRepository.deleteAll();
    }

    @Test
    void testHome_ShouldDisplayAllRuleNamesFromDatabase() throws Exception {
        mockMvc.perform(get("/ruleName/list").with(user(user1)))
                .andExpect(status().isOk())
                .andExpect(view().name("ruleName/list"))
                .andExpect(model().attributeExists("ruleNames"))
                .andExpect(model().attribute("ruleNames", hasSize(2)))
                .andExpect(model().attribute("ruleNames", hasItem(
                        hasProperty("name", is("Rule1"))
                )));
    }

    @Test
    void testAddRuleForm_ShouldDisplayAddForm() throws Exception {
        mockMvc.perform(get("/ruleName/add").with(user(user1)))
                .andExpect(status().isOk())
                .andExpect(view().name("ruleName/add"))
                .andExpect(model().attributeExists("ruleName"));
    }

    @Test
    void testValidate_WithValidData_ShouldCreateRuleNameInDatabase() throws Exception {
        long initialCount = ruleNameRepository.count();

        mockMvc.perform(post("/ruleName/validate")
                        .with(user(user1))
                        .param("name", "NewRule")
                        .param("description", "NewDescription")
                        .param("json", "{}")
                        .param("template", "NewTemplate")
                        .param("sqlStr", "SELECT * FROM new")
                        .param("sqlPart", "WHERE new = 1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ruleName/list"));

        long finalCount = ruleNameRepository.count();
        assert finalCount == initialCount + 1;

        RuleName savedRuleName = ruleNameRepository.findAll().stream()
                .filter(r -> "NewRule".equals(r.getName()))
                .findFirst()
                .orElseThrow();

        assert savedRuleName.getDescription().equals("NewDescription");
    }

    @Test
    void testValidate_WithInvalidData_ShouldNotCreateRuleNameAndShowErrors() throws Exception {
        long initialCount = ruleNameRepository.count();

        mockMvc.perform(post("/ruleName/validate")
                        .with(user(user1))
                        .param("name", "")
                        .param("description", "")
                        .param("json", "")
                        .param("template", "")
                        .param("sqlStr", "")
                        .param("sqlPart", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("ruleName/add"))
                .andExpect(model().attributeExists("ruleName"))
                .andExpect(model().attributeHasErrors("ruleName"));

        long finalCount = ruleNameRepository.count();
        assert finalCount == initialCount;
    }

    @Test
    void testShowUpdateForm_ShouldDisplayUpdateFormWithDataFromDatabase() throws Exception {
        mockMvc.perform(get("/ruleName/update/" + ruleName1.getId()).with(user(user1)))
                .andExpect(status().isOk())
                .andExpect(view().name("ruleName/update"))
                .andExpect(model().attributeExists("ruleName"))
                .andExpect(model().attribute("ruleName", hasProperty("name", is("Rule1"))));
    }

    @Test
    void testUpdateRuleName_WithValidData_ShouldUpdateRuleNameInDatabase() throws Exception {
        Integer ruleId = ruleName1.getId();

        mockMvc.perform(post("/ruleName/update/" + ruleId)
                        .with(user(user1))
                        .param("name", "UpdatedRule")
                        .param("description", "UpdatedDescription")
                        .param("json", "{}")
                        .param("template", "UpdatedTemplate")
                        .param("sqlStr", "SELECT * FROM updated")
                        .param("sqlPart", "WHERE updated = 1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ruleName/list"));

        RuleName updatedRuleName = ruleNameRepository.findById(ruleId).orElseThrow();
        assert updatedRuleName.getName().equals("UpdatedRule");
        assert updatedRuleName.getDescription().equals("UpdatedDescription");
    }

    @Test
    void testUpdateRuleName_WithInvalidData_ShouldNotUpdateRuleNameAndShowErrors() throws Exception {
        Integer ruleId = ruleName1.getId();
        String originalName = ruleName1.getName();

        mockMvc.perform(post("/ruleName/update/" + ruleId)
                        .with(user(user1))
                        .param("name", "")
                        .param("description", "")
                        .param("json", "")
                        .param("template", "")
                        .param("sqlStr", "")
                        .param("sqlPart", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("ruleName/update"))
                .andExpect(model().attributeExists("ruleName"))
                .andExpect(model().attributeHasErrors("ruleName"));

        RuleName unchangedRuleName = ruleNameRepository.findById(ruleId).orElseThrow();
        assert unchangedRuleName.getName().equals(originalName);
    }

    @Test
    void testDeleteRuleName_ShouldRemoveRuleNameFromDatabase() throws Exception {
        Integer ruleId = ruleName1.getId();
        long initialCount = ruleNameRepository.count();

        mockMvc.perform(get("/ruleName/delete/" + ruleId).with(user(user1)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ruleName/list"));

        long finalCount = ruleNameRepository.count();
        assert finalCount == initialCount - 1;
        assert ruleNameRepository.findById(ruleId).isEmpty();
    }

    @Test
    void testCompleteFlow_CreateUpdateDelete_ShouldWorkEndToEnd() throws Exception {
        mockMvc.perform(post("/ruleName/validate")
                        .with(user(user1))
                        .param("name", "FlowTest")
                        .param("description", "TestDescription")
                        .param("json", "{}")
                        .param("template", "TestTemplate")
                        .param("sqlStr", "SELECT * FROM test")
                        .param("sqlPart", "WHERE test = 1"))
                .andExpect(status().is3xxRedirection());

        RuleName createdRuleName = ruleNameRepository.findAll().stream()
                .filter(r -> "FlowTest".equals(r.getName()))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(post("/ruleName/update/" + createdRuleName.getId())
                        .with(user(user1))
                        .param("name", "FlowTestUpdated")
                        .param("description", "TestDescription")
                        .param("json", "{}")
                        .param("template", "TestTemplate")
                        .param("sqlStr", "SELECT * FROM test")
                        .param("sqlPart", "WHERE test = 1"))
                .andExpect(status().is3xxRedirection());

        RuleName updatedRuleName = ruleNameRepository.findById(createdRuleName.getId()).orElseThrow();
        assert updatedRuleName.getName().equals("FlowTestUpdated");

        mockMvc.perform(get("/ruleName/delete/" + createdRuleName.getId()).with(user(user1)))
                .andExpect(status().is3xxRedirection());

        assert ruleNameRepository.findById(createdRuleName.getId()).isEmpty();
    }
}