package ar.com.padel.model.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import ar.com.padel.model.dao.MatchSetDAO;
import ar.com.padel.model.dao.TournamentMatchDAO;
import ar.com.padel.model.dao.TournamentRoundDAO;
import ar.com.padel.model.entity.Employee;
import ar.com.padel.model.entity.MatchSet;
import ar.com.padel.model.entity.Player;
import ar.com.padel.model.entity.TournamentMatch;
import ar.com.padel.model.entity.TournamentRound;
import ar.com.padel.security.context.SessionContext;

public class TournamentMatchService {

    private final TournamentMatchDAO matchDAO;
    private final TournamentRoundDAO roundDAO;
    private final MatchSetDAO matchSetDAO;
    private final DataSource dataSource;

    public TournamentMatchService(TournamentMatchDAO matchDAO,
            TournamentRoundDAO roundDAO, MatchSetDAO matchSetDAO,
            DataSource dataSource) {

        this.matchDAO = matchDAO;
        this.roundDAO = roundDAO;
        this.matchSetDAO = matchSetDAO;
        this.dataSource = dataSource;
    }

    public TournamentMatch findById(Long id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("El ID del partido es obligatorio.");
        }

        return matchDAO.findById(id);
    }

    public List<TournamentMatch> findAll() throws SQLException {
        return matchDAO.findAll();
    }

    public List<TournamentMatch> findByRound(Long roundId) throws SQLException {
        if (roundId == null) {
            throw new IllegalArgumentException("El ID de la jornada es obligatorio.");
        }

        return matchDAO.findByRound(roundId);
    }

    public List<MatchSet> findSetsByMatch(Long matchId) throws SQLException {
        if (matchId == null) {
            throw new IllegalArgumentException("El ID del partido es obligatorio.");
        }

        TournamentMatch match = matchDAO.findById(matchId);

        if (match == null) {
            throw new IllegalArgumentException("No se encontró el partido.");
        }

        List<MatchSet> sets = matchSetDAO.findByMatch(matchId);

        for (MatchSet set : sets) {
            set.setMatch(match);
        }

        return sets;
    }

    public void create(TournamentMatch match) throws SQLException {
        validateMatch(match);

        Employee logged = SessionContext.getLoggedUser();
        LocalDateTime now = LocalDateTime.now();

        match.setUserAdded(logged);
        match.setDateAdded(now);

        matchDAO.insert(match);
    }

    public void update(TournamentMatch match) throws SQLException {
        validateMatch(match);

        if (match.getSurrogate() == null) {
            throw new IllegalArgumentException("El ID del partido es obligatorio.");
        }

        Employee logged = SessionContext.getLoggedUser();

        match.setUserChanged(logged);
        match.setDateChanged(LocalDateTime.now());

        matchDAO.update(match);
    }

    public void delete(Long matchId) throws SQLException {
        if (matchId == null) {
            throw new IllegalArgumentException("El ID del partido es obligatorio.");
        }

        matchDAO.delete(matchId);
    }

    public void saveWithSets(TournamentMatch match,
            List<MatchSet> sets, List<Long> removedSetIds) throws SQLException {

        validateMatch(match);

        if (sets == null) {
            sets = new ArrayList<>();
        }

        if (removedSetIds == null) {
            removedSetIds = new ArrayList<>();
        }

        validateSets(sets);

        Employee logged = SessionContext.getLoggedUser();
        LocalDateTime now = LocalDateTime.now();

        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);

            try {
                // Crear o actualizar el partido.
                if (match.getSurrogate() == null) {
                    match.setUserAdded(logged);
                    match.setDateAdded(now);

                    matchDAO.insert(con, match);
                } else {
                    match.setUserChanged(logged);
                    match.setDateChanged(now);

                    matchDAO.update(con, match);
                }

                // Crear o actualizar los sets.
                for (MatchSet set : sets) {
                    set.setMatch(match);

                    if (set.getSurrogate() == null) {
                        set.setUserAdded(logged);
                        set.setDateAdded(now);

                        matchSetDAO.insert(con, set);
                    } else {
                        set.setUserChanged(logged);
                        set.setDateChanged(now);

                        matchSetDAO.update(con, set);
                    }
                }

                // Eliminar lógicamente los sets quitados.
                for (Long setId : removedSetIds) {
                    if (setId == null) {
                        throw new IllegalArgumentException(
                                "El ID del set a eliminar no puede ser nulo.");
                    }

                    matchSetDAO.delete(con, setId);
                }

                con.commit();

            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    private void validateMatch(TournamentMatch match) throws SQLException {

        if (match == null) {
            throw new IllegalArgumentException("El partido es obligatorio.");
        }

        if (match.getRound() == null
                || match.getRound().getSurrogate() == null) {
            throw new IllegalArgumentException("Debe seleccionar una jornada.");
        }

        if (match.getRound().getTournament() == null
                || match.getRound().getTournament().getSurrogate() == null) {
            throw new IllegalArgumentException("La jornada debe estar asociada a un torneo.");
        }

        if (match.getCouple1() == null
                || match.getCouple1().getSurrogate() == null) {
            throw new IllegalArgumentException("Debe seleccionar la primera pareja.");
        }

        if (match.getCouple2() == null
                || match.getCouple2().getSurrogate() == null) {
            throw new IllegalArgumentException("Debe seleccionar la segunda pareja.");
        }

        if (match.getCouple1().getSurrogate()
                .equals(match.getCouple2().getSurrogate())) {
            throw new IllegalArgumentException("Las parejas deben ser diferentes.");
        }
        
        Player player1 = match.getCouple1().getPlayer1();
        Player player2 = match.getCouple1().getPlayer2();
        Player player3 = match.getCouple2().getPlayer1();
        Player player4 = match.getCouple2().getPlayer2();

        if (player1.getSurrogate().equals(player3.getSurrogate())
                || player1.getSurrogate().equals(player4.getSurrogate())
                || player2.getSurrogate().equals(player3.getSurrogate())
                || player2.getSurrogate().equals(player4.getSurrogate())) {

            throw new IllegalArgumentException(
                    "Un jugador no puede participar en ambas parejas.");
        }
        
        int tournamentCategory = match.getRound()
                .getTournament()
                .getCategory()
                .getValue();

        if (tournamentCategory != 0) {
            Player[] players = {
                match.getCouple1().getPlayer1(),
                match.getCouple1().getPlayer2(),
                match.getCouple2().getPlayer1(),
                match.getCouple2().getPlayer2()
            };

            for (Player player : players) {
                if (player.getCategory() != tournamentCategory) {
                    throw new IllegalArgumentException(
                            "Todos los jugadores deben pertenecer a la categoría del torneo.");
                }
            }
        }

        // Verificar que la jornada exista y pertenezca al torneo seleccionado.
        TournamentRound round = roundDAO.findById(
                match.getRound().getSurrogate());

        if (round == null) {
            throw new IllegalArgumentException("La jornada seleccionada no existe.");
        }

        if (round.getTournament() == null
                || !round.getTournament().getSurrogate().equals(
                        match.getRound().getTournament().getSurrogate())) {
            throw new IllegalArgumentException(
                    "La jornada seleccionada no pertenece al torneo indicado.");
        }
    }

    private void validateSets(List<MatchSet> sets) {

        Set<Integer> setNumbers = new HashSet<>();

        for (MatchSet set : sets) {

            if (set == null) {
                throw new IllegalArgumentException("La lista contiene un set inválido.");
            }

            if (set.getSetNumber() < 1 || set.getSetNumber() > 5) {
                throw new IllegalArgumentException(
                        "El número de set debe estar entre 1 y 5.");
            }

            if (!setNumbers.add(set.getSetNumber())) {
                throw new IllegalArgumentException(
                        "No puede haber dos sets con el mismo número.");
            }

            if (set.getGamesCouple1() < 0 || set.getGamesCouple2() < 0) {
                throw new IllegalArgumentException(
                        "La cantidad de games no puede ser negativa.");
            }
        }
    }
}
