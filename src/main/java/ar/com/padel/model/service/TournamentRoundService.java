package ar.com.padel.model.service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import ar.com.padel.model.dao.TournamentRoundDAO;
import ar.com.padel.model.entity.Employee;
import ar.com.padel.model.entity.TournamentRound;
import ar.com.padel.security.context.SessionContext;

public class TournamentRoundService {

    private final TournamentRoundDAO roundDAO;

    public TournamentRoundService(TournamentRoundDAO roundDAO) {
        this.roundDAO = roundDAO;
    }

    public TournamentRound findById(Long id) throws SQLException {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El identificador de la jornada es obligatorio.");
        }

        return roundDAO.findById(id);
    }

    public List<TournamentRound> findByTournament(Long tournamentId)
            throws SQLException {

        if (tournamentId == null) {
            throw new IllegalArgumentException(
                    "El torneo es obligatorio.");
        }

        return roundDAO.findByTournament(tournamentId);
    }

    public void create(TournamentRound round) throws SQLException {

        validateRound(round);

        Employee user = SessionContext.getLoggedUser();

        round.setUserAdded(user);
        round.setDateAdded(LocalDateTime.now());

        if (round.getRoundStatus() == null
                || round.getRoundStatus().isBlank()) {
            round.setRoundStatus("PLANNED");
        }

        round.setActive(true);
        round.setRemoved(false);

        roundDAO.insert(round);
    }

    public void update(TournamentRound round) throws SQLException {

        validateRound(round);

        if (round.getSurrogate() == null) {
            throw new IllegalArgumentException(
                    "El identificador de la jornada es obligatorio.");
        }

        Employee user = SessionContext.getLoggedUser();

        round.setUserChanged(user);
        round.setDateChanged(LocalDateTime.now());

        roundDAO.update(round);
    }

    public void delete(Long roundId) throws SQLException {

        if (roundId == null) {
            throw new IllegalArgumentException(
                    "El identificador de la jornada es obligatorio.");
        }

        roundDAO.delete(roundId);
    }

    private void validateRound(TournamentRound round) {

        if (round == null) {
            throw new IllegalArgumentException(
                    "La jornada no puede ser nula.");
        }

        if (round.getTournament() == null
                || round.getTournament().getSurrogate() == null) {
            throw new IllegalArgumentException(
                    "La jornada debe estar asociada a un torneo registrado.");
        }

        if (round.getRoundNumber() <= 0) {
            throw new IllegalArgumentException(
                    "El número de jornada debe ser mayor que cero.");
        }

        LocalDate startDate = round.getStartDate();
        LocalDate endDate = round.getEndDate();

        if (startDate != null && endDate != null
                && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "La fecha de fin no puede ser anterior a la fecha de inicio.");
        }

        LocalDate tournamentStart = round.getTournament().getTournamentDate();
        LocalDate tournamentEnd = round.getTournament().getEndDate();

        if (startDate != null && tournamentStart != null
                && startDate.isBefore(tournamentStart)) {
            throw new IllegalArgumentException(
                    "La jornada no puede comenzar antes del torneo.");
        }

        if (endDate != null && tournamentEnd != null
                && endDate.isAfter(tournamentEnd)) {
            throw new IllegalArgumentException(
                    "La jornada no puede finalizar después del torneo.");
        }
    }
}