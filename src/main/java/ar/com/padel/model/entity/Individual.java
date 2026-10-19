package ar.com.padel.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Individual extends AuditInformation {

    private LocalDate birthdate;
    private String name;
    private String lastName;
    private Long surrogate;

    // Constructor con todos los datos
    public Individual(Long surrogate, String name, String lastName, LocalDate birthdate, LocalDateTime createdDate, 
    		LocalDateTime updatedDate, Employee createdBy, Employee updatedBy) {

        super(createdDate, updatedDate, createdBy, updatedBy);
        setBirthdate(birthdate);
        setName(name);
        setLastname(lastName);
        setSurrogate(surrogate);
    }

    // Constructor para inicializar los datos de auditoría
    public Individual(Employee userAdded, Employee userChanged) {
        super(userAdded, userChanged);
    }

    // Constructor por defecto
    public Individual() {
        super();
    }

    // Getters
    public LocalDate getBirthdate() {
        return birthdate;
    }

    public String getName() {
        return name;
    }

    public String getLastname() {
        return lastName;
    }

    public Long getSurrogate() {
        return surrogate;
    }

    // Setters
    public void setBirthdate(LocalDate date) {
        this.birthdate = date;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastname(String lastName) {
        this.lastName = lastName;
    }

    public void setSurrogate(Long surrogate) {
        this.surrogate = surrogate;
    }
}
