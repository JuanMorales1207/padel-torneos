package ar.com.padel.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Employee extends Individual {

    private List<RoleType> roles;
    private LocalDate startDate;
    private LocalDate terminationDate;
    private RoleType role;
    private String passwordHash;
    private boolean enabled;
    private LocalDateTime lastLogin;
    private int failedAttempts;

    // Constructor
    public Employee(RoleType role, String name, String lastName, Long individualId, LocalDate birthdate, LocalDate startDate,
            LocalDate terminationDate, LocalDateTime createdDate, LocalDateTime updatedDate,Employee createdBy, Employee updatedBy) {
    	
        super(individualId, name, lastName, birthdate, createdDate, updatedDate, createdBy,updatedBy);
        setRoleType(role);
        setStartDate(startDate);
        setTerminationDate(terminationDate);
        setEnabled(false);
    }

    public Employee() {
        super();
    }

    // Getters
    public boolean getEnabled() {
        return enabled;
    }

    public RoleType getRoleType() {
        return role;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getTerminationDate() {
        return terminationDate;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public List<RoleType> getRoles() {
        return roles;
    }

    public String getFullName() {
        return getName() + " " + getLastname();
    }

    // Setters
    public void setRoleType(RoleType role) {
        this.role = role;
    }

    public void setStartDate(LocalDate date) {
        this.startDate = date;
    }

    public void setTerminationDate(LocalDate date) {
        this.terminationDate = date;
    }

    public void setPasswordHash(String hash) {
        this.passwordHash = hash;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setLastLogin(LocalDateTime date) {
        this.lastLogin = date;
    }

    public void setFailedAttempts(int attempts) {
        this.failedAttempts = attempts;
    }

    public void setRoles(List<RoleType> roles) {
        this.roles = roles;
    }

    // equals y hashCode basados en el identificador
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Employee employee = (Employee) obj;

        return getSurrogate() != null
                && getSurrogate().equals(employee.getSurrogate());
    }

    @Override
    public int hashCode() {
        return getSurrogate() != null ? getSurrogate().hashCode() : 0;
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
