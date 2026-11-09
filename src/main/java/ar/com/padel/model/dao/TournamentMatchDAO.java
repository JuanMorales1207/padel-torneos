package ar.com.padel.model.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import ar.com.padel.model.entity.TournamentMatch;

public interface TournamentMatchDAO {

    TournamentMatch findById(Long id) throws SQLException;

    List<TournamentMatch> findAll() throws SQLException;

    List<TournamentMatch> findByRound(Long roundId) throws SQLException;

    void insert(TournamentMatch match) throws SQLException;

    void update(TournamentMatch match) throws SQLException;

    void delete(Long matchId) throws SQLException;

    void insert(Connection con, TournamentMatch match) throws SQLException;

    void update(Connection con, TournamentMatch match) throws SQLException;

    void delete(Connection con, Long matchId) throws SQLException;
}