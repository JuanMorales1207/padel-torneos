package ar.com.padel.model.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import ar.com.padel.model.entity.Tournament;

public interface TournamentDAO {

    Tournament findById(Long id) throws SQLException;

    List<Tournament> findAll() throws SQLException;

    List<Tournament> findActive() throws SQLException;

    void insert(Tournament tournament) throws SQLException;

    void insert(Connection con, Tournament tournament) throws SQLException;

    void update(Tournament tournament) throws SQLException;

    void update(Connection con, Tournament tournament) throws SQLException;

    void delete(Long tournamentId) throws SQLException;
}