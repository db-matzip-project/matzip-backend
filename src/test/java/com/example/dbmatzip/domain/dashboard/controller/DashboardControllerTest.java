package com.example.dbmatzip.domain.dashboard.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.dbmatzip.domain.dashboard.dto.DashboardStatsResponse;
import com.example.dbmatzip.domain.dashboard.service.DashboardService;
import com.example.dbmatzip.global.security.MemberPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @Test
    void myDashboard_returnsStatsForAuthenticatedUser() throws Exception {
        MemberPrincipal principal = new MemberPrincipal(1L, "member3");
        var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        DashboardStatsResponse response = new DashboardStatsResponse(
                1L,
                3L,
                8L,
                4L,
                5L,
                java.util.List.of(new com.example.dbmatzip.domain.analytics.dto.TopRestaurantResponse(10L, "청양칼국수", 3L, 2L)));
        when(dashboardService.getMyStats(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/dashboard/me").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.scheduleCount").value(3))
                .andExpect(jsonPath("$.preferenceCount").value(4))
                .andExpect(jsonPath("$.similarTasteTopRestaurants[0].restaurantId").value(10));
    }
}
