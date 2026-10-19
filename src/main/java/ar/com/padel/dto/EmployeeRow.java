package ar.com.padel.dto;

import java.time.LocalDate;

public class EmployeeRow {

    private Long employeeId;
    private String userId;
    private String name;
    private String lastName;
    private String role;
    private LocalDate startDate;
    private LocalDate terminationDate;
    private boolean enabled;

    public EmployeeRow( Long employeeId, String userId, String name, String lastName, 
    		String role, LocalDate startDate, LocalDate terminationDate, boolean enabled) {

        this.employeeId = employeeId;
        this.userId = userId;
        this.name = name;
        this.lastName = lastName;
        this.role = role;
        this.startDate = startDate;
        this.terminationDate = terminationDate;
        this.enabled = enabled;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }

    public String getRole() {
        return role;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getTerminationDate() {
        return terminationDate;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getFullName() {
        return name + " " + lastName;
    }
}
