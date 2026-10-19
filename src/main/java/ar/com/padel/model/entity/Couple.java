package ar.com.padel.model.entity;

import java.time.LocalDateTime;

public class Couple extends AuditInformation {

    private Long surrogate;

    private Player player1;
    private Player player2;

    private String name;
    private LocalDateTime registrationDate;
    
    private boolean active;

    private boolean removed;

    // Constructor con todos los datos de la pareja.
    public Couple(Long surrogate, Player player1, Player player2, String name, LocalDateTime registrationDate,
            LocalDateTime dateAdded, LocalDateTime dateChanged, Employee userAdded, Employee userChanged, boolean removed) {

        super(dateAdded, dateChanged, userAdded, userChanged);

        this.surrogate = surrogate;
        this.player1 = player1;
        this.player2 = player2;
        this.name = name;
        this.registrationDate = registrationDate;
        this.removed = removed;
    }

    // Constructor para nuevas parejas con datos de auditoría.
    public Couple(Employee userAdded, Employee userChanged) {
        super(userAdded, userChanged);
    }

    // Constructor por defecto.
    public Couple() {
        super();
    }

    public Long getSurrogate() {
        return surrogate;
    }

    public void setSurrogate(Long surrogate) {
        this.surrogate = surrogate;
    }

    public Player getPlayer1() {
        return player1;
    }

    public void setPlayer1(Player player1) {
        this.player1 = player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public void setPlayer2(Player player2) {
        this.player2 = player2;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
