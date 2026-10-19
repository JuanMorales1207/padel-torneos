package ar.com.padel.model.dao;

import java.sql.SQLException;
import java.util.List;

import ar.com.padel.model.entity.Couple;

public interface CoupleDAO {

    Couple findById(Long id) throws SQLException;

    List<Couple> findAll() throws SQLException;

    void insert(Couple couple) throws SQLException;

    void update(Couple couple) throws SQLException;

    void delete(Long coupleId) throws SQLException;
    
    boolean existsByPlayers(Long player1Id, Long player2Id, Long excludeCoupleId) throws SQLException;
}
