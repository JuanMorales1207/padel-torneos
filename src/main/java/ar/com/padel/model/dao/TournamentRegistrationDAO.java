package ar.com.padel.model.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import ar.com.padel.model.entity.TournamentRegistration;

public interface TournamentRegistrationDAO {

    TournamentRegistration findById(Long id) throws SQLException;

    List<TournamentRegistration> findAll() throws SQLException;

    List<TournamentRegistration> findByTournament(Long tournamentId) throws SQLException;

    List<TournamentRegistration> findByCouple(Long coupleId) throws SQLException;

    TournamentRegistration findByTournamentAndCouple(
            Long tournamentId, Long coupleId) throws SQLException;

    void insert(TournamentRegistration registration) throws SQLException;

    void insert(Connection con, TournamentRegistration registration) throws SQLException;

    void update(TournamentRegistration registration) throws SQLException;

    void update(Connection con, TournamentRegistration registration) throws SQLException;

    void delete(Long registrationId) throws SQLException;

    void delete(Connection con, Long registrationId) throws SQLException;

    void restore(Connection con, Long registrationId) throws SQLException;
}
