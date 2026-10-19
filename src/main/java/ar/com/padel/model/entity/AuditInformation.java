package ar.com.padel.model.entity;

import java.time.LocalDateTime;

public class AuditInformation {

    private LocalDateTime dateAdded;
    private LocalDateTime dateChanged;
    private Employee userAdded;
    private Employee userChanged;

    // Constructor con datos de auditoría existentes
    public AuditInformation(LocalDateTime createdDate, LocalDateTime updatedDate, Employee createdBy, Employee updatedBy) {
        setDateAdded(createdDate);
        setDateChanged(updatedDate);
        setUserAdded(createdBy);
        setUserChanged(updatedBy);
    }

    // Constructor para nuevos registros
    public AuditInformation(Employee userAdded, Employee userChanged) {
        setDateAdded(LocalDateTime.now());
        setDateChanged(LocalDateTime.now());
        setUserAdded(userAdded);
        setUserChanged(userChanged);
    }

    // Constructor por defecto
    public AuditInformation() {
        setDateAdded(LocalDateTime.now());
        setDateChanged(LocalDateTime.now());
    }

    // Getters
    public LocalDateTime getDateAdded() {
        return dateAdded;
    }

    public LocalDateTime getDateChanged() {
        return dateChanged;
    }

    public Employee getUserAdded() {
        return userAdded;
    }

    public Employee getUserChanged() {
        return userChanged;
    }

    // Setters
    public void setDateAdded(LocalDateTime date) {
        this.dateAdded = date;
    }

    public void setDateChanged(LocalDateTime date) {
        this.dateChanged = date;
    }

    public void setUserAdded(Employee employee) {
        this.userAdded = employee;
    }

    public void setUserChanged(Employee employee) {
        this.userChanged = employee;
    }
}
