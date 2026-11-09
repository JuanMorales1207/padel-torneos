package ar.com.padel.model.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import ar.com.padel.model.entity.TournamentRound;

public interface TournamentRoundDAO {

    TournamentRound findById(Long id) throws SQLException;

    List<TournamentRound> findByTournament(Long tournamentId) throws SQLException;

    void insert(TournamentRound round) throws SQLException;

    void insert(Connection con, TournamentRound round) throws SQLException;

    void update(TournamentRound round) throws SQLException;

    void update(Connection con, TournamentRound round) throws SQLException;

    void delete(Long roundId) throws SQLException;

    void delete(Connection con, Long roundId) throws SQLException;
}