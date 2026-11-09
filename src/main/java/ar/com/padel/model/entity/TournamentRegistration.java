package ar.com.padel.model.entity;

import java.time.LocalDateTime;

public class TournamentRegistration extends AuditInformation {

    private Long surrogate;
    private Tournament tournament;
    private Couple couple;
    private LocalDateTime registrationDate;
    private boolean active;
    private boolean removed;

    // Constructor con todos los datos de la inscripción.
    public TournamentRegistration(Long surrogate, Tournament tournament,
            Couple couple, LocalDateTime registrationDate,
            LocalDateTime dateAdded, LocalDateTime dateChanged,
            Employee userAdded, Employee userChanged,
            boolean active, boolean removed) {

        super(dateAdded, dateChanged, userAdded, userChanged);

        this.surrogate = surrogate;
        this.tournament = tournament;
        this.couple = couple;
        this.registrationDate = registrationDate;
        this.active = active;
        this.removed = removed;
    }

    // Constructor para nuevas inscripciones con datos de auditoría.
    public TournamentRegistration(Employee userAdded, Employee userChanged) {
        super(userAdded, userChanged);
    }

    // Constructor por defecto.
    public TournamentRegistration() {
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

    public Couple getCouple() {
        return couple;
    }

    public void setCouple(Couple couple) {
        this.couple = couple;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
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
