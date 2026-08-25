package com.joven.inventory.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link LandingPageResolver}.
 * Verifies the permission-inferred landing page rules.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@DisplayName("LandingPageResolver Tests")
class LandingPageResolverTest {

    @Test
    @DisplayName("resolve - USE_POS without VIEW_DASHBOARD - returns POS")
    void resolve_posOnly_returnsPos() {
        long rights = Permission.USE_POS;
        assertThat(LandingPageResolver.resolve(rights)).isEqualTo(LandingPageResolver.POS);
    }

    @Test
    @DisplayName("resolve - USE_POS with VIEW_DASHBOARD - returns DASHBOARD")
    void resolve_posAndDashboard_returnsDashboard() {
        long rights = Permission.USE_POS | Permission.VIEW_DASHBOARD;
        assertThat(LandingPageResolver.resolve(rights)).isEqualTo(LandingPageResolver.DASHBOARD);
    }

    @Test
    @DisplayName("resolve - VIEW_DASHBOARD only - returns DASHBOARD")
    void resolve_dashboardOnly_returnsDashboard() {
        long rights = Permission.VIEW_DASHBOARD;
        assertThat(LandingPageResolver.resolve(rights)).isEqualTo(LandingPageResolver.DASHBOARD);
    }

    @Test
    @DisplayName("resolve - no relevant permissions - returns DASHBOARD")
    void resolve_none_returnsDashboard() {
        assertThat(LandingPageResolver.resolve(0L)).isEqualTo(LandingPageResolver.DASHBOARD);
    }

    @Test
    @DisplayName("resolve - all permissions - returns DASHBOARD")
    void resolve_all_returnsDashboard() {
        assertThat(LandingPageResolver.resolve(Permission.ALL_PERMISSIONS))
                .isEqualTo(LandingPageResolver.DASHBOARD);
    }
}
