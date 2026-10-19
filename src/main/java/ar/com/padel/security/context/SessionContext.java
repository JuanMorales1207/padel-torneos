package ar.com.padel.security.context;

import ar.com.padel.model.entity.Employee;
import ar.com.padel.model.entity.RoleType;

public final class SessionContext {

    private static Employee loggedUser;
    private static RoleType roleType;

    private SessionContext() {}

    public static Employee getLoggedUser() {
        return loggedUser;
    }

    public static void setLoggedUser(Employee user) {
        loggedUser = user;
        // Mantener sincronizado el rol de la sesión con el usuario.
        roleType = user != null ? user.getRoleType() : null;
    }

    public static boolean isLogged() {
        return loggedUser != null;
    }

    public static void clear() {
        loggedUser = null;
        roleType = null;
    }

    public static RoleType getRoleType() {
        return roleType;
    }

    public static void setRoleType(RoleType roleType) {
        SessionContext.roleType = roleType;
    }

    public static boolean hasAnyRole(String role1, String role2) {
        return roleType != null && (role1.equals(roleType.getShortDescription())
                || role2.equals(roleType.getShortDescription()));
    }

    public static boolean hasAdminRole(String adminRole) {
        return roleType != null && adminRole.equals(roleType.getShortDescription());
    }

    public static boolean isAdmin() {
        return hasAdminRole("ADMIN");
    }

    public static boolean isSuper() {
        return hasAdminRole("SUPER");
    }

    public static void logout() {
        clear();
    }
}
