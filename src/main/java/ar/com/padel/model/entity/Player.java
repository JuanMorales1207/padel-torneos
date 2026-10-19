package ar.com.padel.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Player extends Individual {

    // Constructor con todos los datos del jugador.
    public Player(Long surrogate, String name, String lastName, LocalDate birthdate, LocalDateTime createdDate,
            LocalDateTime updatedDate, Employee createdBy, Employee updatedBy) {

        super(surrogate, name, lastName, birthdate, createdDate, updatedDate, createdBy, updatedBy);
    }

    // Constructor para nuevos jugadores con datos de auditoría.
    public Player(Employee userAdded, Employee userChanged) {
        super(userAdded, userChanged);
    }

    // Constructor por defecto.
    public Player() {
        super();
    }

    @Override
    public String toString() {
        return getName() + " " + getLastname();
    }
}
