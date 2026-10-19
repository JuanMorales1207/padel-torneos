package ar.com.padel.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Player extends Individual {

    private int category;

    // Constructor con todos los datos del jugador.
    public Player(Long surrogate, String name, String lastName, LocalDate birthdate, LocalDateTime createdDate,
            LocalDateTime updatedDate, Employee createdBy, Employee updatedBy, int category) {

        super(surrogate, name, lastName, birthdate, createdDate, updatedDate, createdBy, updatedBy);
        this.category = category;
    }

    // Constructor para nuevos jugadores con datos de auditoría.
    public Player(Employee userAdded, Employee userChanged) {
        super(userAdded, userChanged);
    }

    // Constructor por defecto.
    public Player() {
        super();
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return getName() + " " + getLastname();
    }
}