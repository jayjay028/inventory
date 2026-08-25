package com.joven.inventory.security;

/**
 * Resolves a user's default landing page based on their permission bitmask.
 *
 * <p>Rule: a user who can use the POS terminal but cannot view the dashboard
 * lands on the POS terminal; everyone else lands on the dashboard. This keeps
 * cashier-style accounts on the POS screen while admins and general users start
 * on the dashboard, without requiring a separate configuration field.</p>
 *
 * @author Joven Q. Divinagracia Jr.
 */
public final class LandingPageResolver {

    /** Landing page identifier for the POS terminal. */
    public static final String POS = "POS";

    /** Landing page identifier for the dashboard. */
    public static final String DASHBOARD = "DASHBOARD";

    private LandingPageResolver() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Determines the default landing page for the given access rights.
     *
     * @param accessRights the user's bitwise access rights
     * @return {@link #POS} if the user can use POS but not view the dashboard,
     *         otherwise {@link #DASHBOARD}
     */
    public static String resolve(long accessRights) {
        boolean canUsePos = Permission.hasPermission(accessRights, Permission.USE_POS);
        boolean canViewDashboard = Permission.hasPermission(accessRights, Permission.VIEW_DASHBOARD);

        if (canUsePos && !canViewDashboard) {
            return POS;
        }
        return DASHBOARD;
    }
}
