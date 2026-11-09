package ar.com.padel.model.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import ar.com.padel.model.dao.TournamentDAO;
import ar.com.padel.model.dao.TournamentRoundDAO;
import ar.com.padel.model.entity.Category;
import ar.com.padel.model.entity.Employee;
import ar.com.padel.model.entity.Tournament;
import ar.com.padel.model.entity.TournamentRound;
import ar.com.padel.security.context.SessionContext;

public class TournamentService {

    private final TournamentDAO tournamentDAO;
    private final TournamentRoundDAO roundDAO;
    private final DataSource dataSource;

    public TournamentService(
            TournamentDAO tournamentDAO,
            TournamentRoundDAO roundDAO,
            DataSource dataSource) {

        this.tournamentDAO = tournamentDAO;
        this.roundDAO = roundDAO;
        this.dataSource = dataSource;
    }

    public Tournament findById(Long id) throws SQLException {
        return tournamentDAO.findById(id);
    }

    public List<Tournament> findAll() throws SQLException {
        return tournamentDAO.findAll();
    }

    public List<Tournament> findActive() throws SQLException {
        return tournamentDAO.findActive();
    }

    public void create(Tournament tournament) throws SQLException {
        validateTournament(tournament);

        Employee logged = SessionContext.getLoggedUser();

        tournament.setUserAdded(logged);
        tournament.setDateAdded(LocalDateTime.now());

        tournamentDAO.insert(tournament);
    }

    public void update(Tournament tournament) throws SQLException {
        validateTournament(tournament);

        Employee logged = SessionContext.getLoggedUser();

        tournament.setUserChanged(logged);
        tournament.setDateChanged(LocalDateTime.now());

        tournamentDAO.update(tournament);
    }

    public void delete(Long tournamentId) throws SQLException {
        if (tournamentId == null) {
            throw new IllegalArgumentException("El identificador del torneo es obligatorio.");
        }

        tournamentDAO.delete(tournamentId);
    }

    private void validateTournament(Tournament tournament) {
        if (tournament == null) {
            throw new IllegalArgumentException("El torneo no puede ser nulo.");
        }

        if (tournament.getName() == null || tournament.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del torneo es obligatorio.");
        }

        if (tournament.getCategory() == null) {
            throw new IllegalArgumentException("La categoría del torneo es obligatoria.");
        }

        if (tournament.getTournamentDate() == null) {
            throw new IllegalArgumentException("La fecha del torneo es obligatoria.");
        }
        if (tournament.getEndDate() == null) {
            throw new IllegalArgumentException("La fecha de finalización del torneo es obligatoria.");
        }
        if (tournament.getEndDate().isBefore(tournament.getTournamentDate())) {
            throw new IllegalArgumentException(
                "La fecha de finalización no puede ser anterior a la fecha de inicio."
            );
        }
    }
    
    public void saveWithRounds(
            Tournament tournament,
            List<TournamentRound> rounds,
            List<Long> removedRoundIds) throws SQLException {

        validateTournament(tournament);

        if (rounds == null) {
            rounds = new ArrayList<>();
        }

        if (removedRoundIds == null) {
            removedRoundIds = new ArrayList<>();
        }

        // Validar todas las jornadas antes de iniciar la transacción.
        for (TournamentRound round : rounds) {
            validateRound(round, tournament);
        }
        
        long uniqueRoundNumbers = rounds.stream()
                .map(TournamentRound::getRoundNumber)
                .distinct()
                .count();

        if (uniqueRoundNumbers != rounds.size()) {
            throw new IllegalArgumentException(
                    "No puede haber dos jornadas con el mismo número.");
        }

        // Validar identificadores de jornadas eliminadas.
        for (Long roundId : removedRoundIds) {
            if (roundId == null) {
                throw new IllegalArgumentException(
                        "El identificador de una jornada eliminada no puede ser nulo.");
            }
        }

        Employee logged = SessionContext.getLoggedUser();
        LocalDateTime now = LocalDateTime.now();

        try (Connection con = dataSource.getConnection()) {

            con.setAutoCommit(false);

            try {

                // 1. Guardar el torneo.
                if (tournament.getSurrogate() == null) {

                    tournament.setUserAdded(logged);
                    tournament.setDateAdded(now);

                    tournamentDAO.insert(con, tournament);

                } else {

                    tournament.setUserChanged(logged);
                    tournament.setDateChanged(now);

                    tournamentDAO.update(con, tournament);
                }

                // 2. Guardar las jornadas.
                for (TournamentRound round : rounds) {

                    round.setTournament(tournament);

                    if (round.getSurrogate() == null) {

                        round.setUserAdded(logged);
                        round.setDateAdded(now);
                        round.setRoundStatus(
                                round.getRoundStatus() == null
                                        || round.getRoundStatus().isBlank()
                                        ? "PLANNED"
                                        : round.getRoundStatus());
                        round.setActive(true);
                        round.setRemoved(false);

                        roundDAO.insert(con, round);

                    } else {

                        round.setUserChanged(logged);
                        round.setDateChanged(now);

                        roundDAO.update(con, round);
                    }
                }

                // 3. Eliminar lógicamente las jornadas quitadas.
                for (Long roundId : removedRoundIds) {
                    roundDAO.delete(con, roundId);
                }

                // 4. Confirmar todos los cambios.
                con.commit();

            } catch (SQLException | RuntimeException e) {

                // Si algo falla, deshacer todos los cambios.
                con.rollback();

                throw e;
            }
        }
    }
    
    private void validateRound(
            TournamentRound round,
            Tournament tournament) {

        if (round == null) {
            throw new IllegalArgumentException(
                    "La jornada no puede ser nula.");
        }

        if (round.getRoundNumber() <= 0) {
            throw new IllegalArgumentException(
                    "El número de jornada debe ser mayor que cero.");
        }

        if (round.getDescription() == null
                || round.getDescription().isBlank()) {
            throw new IllegalArgumentException(
                    "La descripción de la jornada es obligatoria.");
        }

        LocalDate startDate = round.getStartDate();
        LocalDate endDate = round.getEndDate();

        if (startDate != null && endDate != null
                && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "La fecha de finalización no puede ser anterior a la fecha de inicio.");
        }

        if (startDate != null
                && startDate.isBefore(tournament.getTournamentDate())) {
            throw new IllegalArgumentException(
                    "La jornada no puede comenzar antes del torneo.");
        }

        if (endDate != null
                && endDate.isAfter(tournament.getEndDate())) {
            throw new IllegalArgumentException(
                    "La jornada no puede finalizar después del torneo.");
        }
    }
    
    public List<TournamentRound> findRoundsByTournament(Long tournamentId)
            throws SQLException {

        if (tournamentId == null) {
            throw new IllegalArgumentException(
                    "El identificador del torneo es obligatorio.");
        }

        return roundDAO.findByTournament(tournamentId);
    }

}
