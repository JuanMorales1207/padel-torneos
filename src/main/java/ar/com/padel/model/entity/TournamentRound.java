package ar.com.padel.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TournamentRound extends AuditInformation {

    private Long surrogate;

    private Tournament tournament;

    private String description;

    private int roundNumber;

    private LocalDate startDate;

    private LocalDate endDate;

    private String roundStatus;

    private boolean active;

    private boolean removed;

    // Constructor con todos los datos de la jornada.
    public TournamentRound(Long surrogate, Tournament tournament, String description,
            int roundNumber, LocalDate startDate, LocalDate endDate, String roundStatus,
            LocalDateTime dateAdded, LocalDateTime dateChanged,
            Employee userAdded, Employee userChanged,
            boolean active, boolean removed) {

        super(dateAdded, dateChanged, userAdded, userChanged);

        this.surrogate = surrogate;
        this.tournament = tournament;
        this.description = description;
        this.roundNumber = roundNumber;
        this.startDate = startDate;
        this.endDate = endDate;
        this.roundStatus = roundStatus;
        this.active = active;
        this.removed = removed;
    }

    // Constructor para nuevas jornadas con datos de auditoría.
    public TournamentRound(Employee userAdded, Employee userChanged) {
        super(userAdded, userChanged);
    }

    // Constructor por defecto.
    public TournamentRound() {
        super();
    }

    public Long getSurrogate() {
        return surrogate;
    }

    public void setSurrogate(Long surrogate) {
        this.surrogate = surrogate;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getRoundStatus() {
        return roundStatus;
    }

    public void setRoundStatus(String roundStatus) {
        this.roundStatus = roundStatus;
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
