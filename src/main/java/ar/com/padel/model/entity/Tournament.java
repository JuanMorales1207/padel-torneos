package ar.com.padel.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Tournament extends AuditInformation {

    private Long surrogate;
    private Category category;
    private boolean active;
    private boolean removed;
    private String name;
    private LocalDate tournamentDate;
    private LocalDate endDate;

    // Constructor con todos los datos del torneo.
    public Tournament(Long surrogate, Category category,
            LocalDateTime dateAdded, LocalDateTime dateChanged,
            Employee userAdded, Employee userChanged,
            boolean active, boolean removed) {

        super(dateAdded, dateChanged, userAdded, userChanged);

        this.surrogate = surrogate;
        this.category = category;
        this.active = active;
        this.removed = removed;
    }

    // Constructor para nuevos torneos con datos de auditoría.
    public Tournament(Employee userAdded, Employee userChanged) {
        super(userAdded, userChanged);
    }

    // Constructor por defecto.
    public Tournament() {
        super();
    }

    public Long getSurrogate() {
        return surrogate;
    }

    public void setSurrogate(Long surrogate) {
        this.surrogate = surrogate;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getTournamentDate() {
        return tournamentDate;
    }

    public void setTournamentDate(LocalDate tournamentDate) {
        this.tournamentDate = tournamentDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

}