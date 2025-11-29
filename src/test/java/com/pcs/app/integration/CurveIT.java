package com.pcs.app.integration;

import com.pcs.app.domain.CurvePoint;
import com.pcs.app.domain.User;
import com.pcs.app.repositories.CurvePointRepository;
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
class CurveIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CurvePointRepository curvePointRepository;

    private User user1;
    private CurvePoint curvePoint1;
    private CurvePoint curvePoint2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setFullname("Test admin");
        user1.setUsername("user1");
        user1.setRole("ROLE_ADMIN");

        curvePointRepository.deleteAll();

        curvePoint1 = new CurvePoint();
        curvePoint1.setCurveId(1);
        curvePoint1.setTerm(10.0);
        curvePoint1.setValue(100.0);
        curvePoint1 = curvePointRepository.save(curvePoint1);

        curvePoint2 = new CurvePoint();
        curvePoint2.setCurveId(2);
        curvePoint2.setTerm(20.0);
        curvePoint2.setValue(200.0);
        curvePoint2 = curvePointRepository.save(curvePoint2);
    }

    @AfterEach
    void tearDown() {
        curvePointRepository.deleteAll();
    }

    @Test
    void testHome_ShouldDisplayAllCurvePointsFromDatabase() throws Exception {
        mockMvc.perform(get("/curvePoint/list").with(user(user1)))
                .andExpect(status().isOk())
                .andExpect(view().name("curvePoint/list"))
                .andExpect(model().attributeExists("curvePoints"))
                .andExpect(model().attribute("curvePoints", hasSize(2)))
                .andExpect(model().attribute("curvePoints", hasItem(
                        hasProperty("curveId", is(1))
                )));
    }

    @Test
    void testAddBidForm_ShouldDisplayAddForm() throws Exception {
        mockMvc.perform(get("/curvePoint/add").with(user(user1)))
                .andExpect(status().isOk())
                .andExpect(view().name("curvePoint/add"))
                .andExpect(model().attributeExists("curvePoint"));
    }

    @Test
    void testValidate_WithValidData_ShouldCreateCurvePointInDatabase() throws Exception {
        long initialCount = curvePointRepository.count();

        mockMvc.perform(post("/curvePoint/validate")
                        .with(user(user1))
                        .param("curveId", "3")
                        .param("term", "30.0")
                        .param("value", "300.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/curvePoint/list"));

        long finalCount = curvePointRepository.count();
        assert finalCount == initialCount + 1;

        CurvePoint savedCurvePoint = curvePointRepository.findAll().stream()
                .filter(cp -> cp.getCurveId() == 3)
                .findFirst()
                .orElseThrow();

        assert savedCurvePoint.getTerm().equals(30.0);
        assert savedCurvePoint.getValue().equals(300.0);
    }

    @Test
    void testValidate_WithInvalidData_ShouldNotCreateCurvePointAndShowErrors() throws Exception {
        long initialCount = curvePointRepository.count();

        mockMvc.perform(post("/curvePoint/validate")
                        .with(user(user1))
                        .param("curveId", "toto")
                        .param("term", "tutu")
                        .param("value", "tata"))
                .andExpect(status().isOk())
                .andExpect(view().name("curvePoint/add"))
                .andExpect(model().attributeExists("curvePoint"))
                .andExpect(model().attributeHasErrors("curvePoint"));

        long finalCount = curvePointRepository.count();
        assert finalCount == initialCount;
    }

    @Test
    void testShowUpdateForm_ShouldDisplayUpdateFormWithDataFromDatabase() throws Exception {
        mockMvc.perform(get("/curvePoint/update/" + curvePoint1.getId()).with(user(user1)))
                .andExpect(status().isOk())
                .andExpect(view().name("curvePoint/update"))
                .andExpect(model().attributeExists("curvePoint"))
                .andExpect(model().attribute("curvePoint", hasProperty("curveId", is(1))));
    }

    @Test
    void testUpdateBid_WithValidData_ShouldUpdateCurvePointInDatabase() throws Exception {
        Integer curvePointId = curvePoint1.getId();

        mockMvc.perform(post("/curvePoint/update/" + curvePointId)
                        .with(user(user1))
                        .param("curveId", "5")
                        .param("term", "50.0")
                        .param("value", "500.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/curvePoint/list"));

        CurvePoint updatedCurvePoint = curvePointRepository.findById(curvePointId).orElseThrow();
        assert updatedCurvePoint.getCurveId() == 5;
        assert updatedCurvePoint.getTerm().equals(50.0);
        assert updatedCurvePoint.getValue().equals(500.0);
    }

    @Test
    void testUpdateBid_WithInvalidData_ShouldNotUpdateCurvePointAndShowErrors() throws Exception {
        Integer curvePointId = curvePoint1.getId();
        Integer originalCurveId = curvePoint1.getCurveId();

        mockMvc.perform(post("/curvePoint/update/" + curvePointId)
                        .with(user(user1))
                        .param("curveId", "toto")
                        .param("term", "tutu")
                        .param("value", "tata"))
                .andExpect(status().isOk())
                .andExpect(view().name("curvePoint/update"))
                .andExpect(model().attributeExists("curvePoint"))
                .andExpect(model().attributeHasErrors("curvePoint"));

        CurvePoint unchangedCurvePoint = curvePointRepository.findById(curvePointId).orElseThrow();
        assert unchangedCurvePoint.getCurveId().equals(originalCurveId);
    }

    @Test
    void testDeleteBid_ShouldRemoveCurvePointFromDatabase() throws Exception {
        Integer curvePointId = curvePoint1.getId();
        long initialCount = curvePointRepository.count();

        mockMvc.perform(get("/curvePoint/delete/" + curvePointId).with(user(user1)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/curvePoint/list"));

        long finalCount = curvePointRepository.count();
        assert finalCount == initialCount - 1;
        assert curvePointRepository.findById(curvePointId).isEmpty();
    }

    @Test
    void testCompleteFlow_CreateUpdateDelete_ShouldWorkEndToEnd() throws Exception {
        mockMvc.perform(post("/curvePoint/validate")
                        .with(user(user1))
                        .param("curveId", "99")
                        .param("term", "99.0")
                        .param("value", "999.0"))
                .andExpect(status().is3xxRedirection());

        CurvePoint createdCurvePoint = curvePointRepository.findAll().stream()
                .filter(cp -> cp.getCurveId() == 99)
                .findFirst()
                .orElseThrow();

        mockMvc.perform(post("/curvePoint/update/" + createdCurvePoint.getId())
                        .with(user(user1))
                        .param("curveId", "88")
                        .param("term", "88.0")
                        .param("value", "888.0"))
                .andExpect(status().is3xxRedirection());

        CurvePoint updatedCurvePoint = curvePointRepository.findById(createdCurvePoint.getId()).orElseThrow();
        assert updatedCurvePoint.getCurveId() == 88;

        mockMvc.perform(get("/curvePoint/delete/" + createdCurvePoint.getId()).with(user(user1)))
                .andExpect(status().is3xxRedirection());

        assert curvePointRepository.findById(createdCurvePoint.getId()).isEmpty();
    }
}