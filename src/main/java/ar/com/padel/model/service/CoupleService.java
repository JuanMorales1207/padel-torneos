package ar.com.padel.model.service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import ar.com.padel.model.dao.CoupleDAO;
import ar.com.padel.model.entity.Couple;
import ar.com.padel.model.entity.Employee;
import ar.com.padel.security.context.SessionContext;

public class CoupleService {

    private final CoupleDAO coupleDAO;

    public CoupleService(CoupleDAO coupleDAO) {
        this.coupleDAO = coupleDAO;
    }

    public Couple findById(Long id) throws SQLException {
        return coupleDAO.findById(id);
    }

    public List<Couple> findAll() throws SQLException {
        return coupleDAO.findAll();
    }


	public void create(Couple couple) throws SQLException {
	    validateCouple(couple);
	
	    if (coupleDAO.existsByPlayers(couple.getPlayer1().getSurrogate(), couple.getPlayer2().getSurrogate(), null)) {
		        throw new IllegalArgumentException("Ya existe una pareja registrada con esos jugadores.");
	    }
	
	    Employee logged = SessionContext.getLoggedUser();
	
	    couple.setUserAdded(logged);
	    couple.setDateAdded(LocalDateTime.now());
	
	    if (couple.getRegistrationDate() == null) {
	        couple.setRegistrationDate(LocalDateTime.now());
	    }
		    coupleDAO.insert(couple);
	}
	
	public void update(Couple couple) throws SQLException {
	    validateCouple(couple);
	
	    if (coupleDAO.existsByPlayers(couple.getPlayer1().getSurrogate(), couple.getPlayer2().getSurrogate(), couple.getSurrogate())) {
		        throw new IllegalArgumentException("Ya existe otra pareja registrada con esos jugadores.");
	    }
	
	    Employee logged = SessionContext.getLoggedUser();
	
	    couple.setUserChanged(logged);
	    couple.setDateChanged(LocalDateTime.now());
	
	    coupleDAO.update(couple);
	}


    public void delete(Long coupleId) throws SQLException {
        coupleDAO.delete(coupleId);
    }

    private void validateCouple(Couple couple) {
        if (couple == null) {
            throw new IllegalArgumentException("La pareja no puede ser nula.");
        }

        if (couple.getName() == null || couple.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre de la pareja es obligatorio.");
        }

        if (couple.getPlayer1() == null || couple.getPlayer2() == null) {
            throw new IllegalArgumentException("La pareja debe tener dos jugadores.");
        }

        if (couple.getPlayer1().getSurrogate() == null
                || couple.getPlayer2().getSurrogate() == null) {
            throw new IllegalArgumentException("Los jugadores deben estar registrados.");
        }

        if (couple.getPlayer1().getSurrogate()
                .equals(couple.getPlayer2().getSurrogate())) {
            throw new IllegalArgumentException("Una pareja debe estar formada por dos jugadores distintos.");
        }
    }
}
