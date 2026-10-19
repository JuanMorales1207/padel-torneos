package ar.com.padel.model.dao;

import java.sql.SQLException;
import java.util.List;

import ar.com.padel.model.entity.Player;

public interface PlayerDAO {

    Player findById(Long id) throws SQLException;

    List<Player> findAll() throws SQLException;

    void insert(Player player) throws SQLException;

    void update(Player player) throws SQLException;

    void delete(Long playerId) throws SQLException;
}
