package ar.com.padel.model.service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import ar.com.padel.model.dao.PlayerDAO;
import ar.com.padel.model.entity.Employee;
import ar.com.padel.model.entity.Player;
import ar.com.padel.security.context.SessionContext;

public class PlayerService {

    private final PlayerDAO playerDAO;

    public PlayerService(PlayerDAO playerDAO) {
        this.playerDAO = playerDAO;
    }

    public Player findById(Long id) throws SQLException {
        return playerDAO.findById(id);
    }

    public List<Player> findAll() throws SQLException {
        return playerDAO.findAll();
    }

    public void create(Player player) throws SQLException {
        validateCategory(player.getCategory());

        Employee logged = SessionContext.getLoggedUser();
        player.setUserAdded(logged);
        player.setDateAdded(LocalDateTime.now());

        playerDAO.insert(player);
    }

    public void update(Player player) throws SQLException {
        validateCategory(player.getCategory());

        Employee logged = SessionContext.getLoggedUser();
        player.setUserChanged(logged);
        player.setDateChanged(LocalDateTime.now());

        playerDAO.update(player);
    }

    public void delete(Long playerId) throws SQLException {
        playerDAO.delete(playerId);
    }

    private void validateCategory(int category) {
        if (category < 1 || category > 8) {
            throw new IllegalArgumentException("La categoría debe estar entre 1 y 8.");
        }
    }
}
