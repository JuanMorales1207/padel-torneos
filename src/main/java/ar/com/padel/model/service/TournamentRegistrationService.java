package ar.com.padel.model.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import javax.sql.DataSource;

import ar.com.padel.model.dao.PlayerDAO;
import ar.com.padel.model.dao.TournamentDAO;
import ar.com.padel.model.dao.TournamentRegistrationDAO;
import ar.com.padel.model.entity.Couple;
import ar.com.padel.model.entity.Employee;
import ar.com.padel.model.entity.Player;
import ar.com.padel.model.entity.Tournament;
import ar.com.padel.model.entity.TournamentRegistration;
import ar.com.padel.security.context.SessionContext;

public class TournamentRegistrationService {

    private final TournamentRegistrationDAO registrationDAO;
    private final TournamentDAO tournamentDAO;
    private final PlayerDAO playerDAO;
    private final CoupleService coupleService;
    private final DataSource dataSource;

    public TournamentRegistrationService(
            TournamentRegistrationDAO registrationDAO,
            TournamentDAO tournamentDAO,
            PlayerDAO playerDAO,
            CoupleService coupleService,
            DataSource dataSource) {

        this.registrationDAO = registrationDAO;
        this.tournamentDAO = tournamentDAO;
        this.playerDAO = playerDAO;
        this.coupleService = coupleService;
        this.dataSource = dataSource;
    }

    // -------------------------------------------------
    // Consultas
    // -------------------------------------------------

    public TournamentRegistration findById(Long id) throws SQLException {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El ID de la inscripción es obligatorio.");
        }

        return registrationDAO.findById(id);
    }

    public List<TournamentRegistration> findAll() throws SQLException {
        return registrationDAO.findAll();
    }

    public List<TournamentRegistration> findByTournament(
            Long tournamentId) throws SQLException {

        if (tournamentId == null) {
            throw new IllegalArgumentException(
                    "El ID del torneo es obligatorio.");
        }

        return registrationDAO.findByTournament(tournamentId);
    }

    public List<TournamentRegistration> findByCouple(
            Long coupleId) throws SQLException {

        if (coupleId == null) {
            throw new IllegalArgumentException(
                    "El ID de la pareja es obligatorio.");
        }

        return registrationDAO.findByCouple(coupleId);
    }

    // -------------------------------------------------
    // Crear inscripción con pareja existente
    // -------------------------------------------------

    public void create(TournamentRegistration registration)
            throws SQLException {

        if (registration == null) {
            throw new IllegalArgumentException(
                    "La inscripción es obligatoria.");
        }

        if (registration.getCouple() == null
                || registration.getCouple().getSurrogate() == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar una pareja.");
        }

        Tournament tournament = validateTournament(
                registration.getTournament());

        Couple couple = coupleService.findById(
                registration.getCouple().getSurrogate());

        if (couple == null || !couple.isActive()) {
            throw new IllegalArgumentException(
                    "La pareja seleccionada no existe o está inactiva.");
        }

        registration.setTournament(tournament);
        registration.setCouple(couple);

        validateRegistration(registration, null);

        saveRegistration(registration);
    }

    // -------------------------------------------------
    // Crear inscripción seleccionando dos jugadores
    // -------------------------------------------------

    public void createWithPlayers(
            TournamentRegistration registration,
            Player player1,
            Player player2) throws SQLException {

        if (registration == null) {
            throw new IllegalArgumentException(
                    "La inscripción es obligatoria.");
        }

        Tournament tournament = validateTournament(
                registration.getTournament());

        Player p1 = validatePlayer(player1);
        Player p2 = validatePlayer(player2);

        if (p1.getSurrogate().equals(p2.getSurrogate())) {
            throw new IllegalArgumentException(
                    "Una pareja debe estar formada por dos jugadores distintos.");
        }

        validatePlayerCategories(tournament, p1, p2);

        // Buscar una pareja existente, sin importar el orden.
        Couple couple = coupleService.findByPlayers(
                p1.getSurrogate(),
                p2.getSurrogate());

        // Si no existe, crearla.
        if (couple == null) {

            couple = new Couple();

            couple.setPlayer1(p1);
            couple.setPlayer2(p2);

            couple.setName(
                    p1.getLastname() + "-" + p2.getLastname());

            couple.setRegistrationDate(LocalDateTime.now());
            couple.setActive(true);
            couple.setRemoved(false);

            Employee logged = SessionContext.getLoggedUser();

            coupleService.create(couple);
        }

        registration.setTournament(tournament);
        registration.setCouple(couple);

        validateRegistration(registration, null);

        saveRegistration(registration);
    }

    // -------------------------------------------------
    // Actualizar inscripción
    // -------------------------------------------------

    public void update(TournamentRegistration registration)
            throws SQLException {

        if (registration == null
                || registration.getSurrogate() == null) {

            throw new IllegalArgumentException(
                    "La inscripción y su ID son obligatorios.");
        }

        TournamentRegistration existing = registrationDAO.findById(
                registration.getSurrogate());

        if (existing == null || existing.isRemoved()) {
            throw new IllegalArgumentException(
                    "No se encontró la inscripción.");
        }

        if (registration.getCouple() == null
                || registration.getCouple().getSurrogate() == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar una pareja.");
        }

        Tournament tournament = validateTournament(
                registration.getTournament());

        Couple couple = coupleService.findById(
                registration.getCouple().getSurrogate());

        if (couple == null || !couple.isActive()) {
            throw new IllegalArgumentException(
                    "La pareja seleccionada no existe o está inactiva.");
        }

        registration.setTournament(tournament);
        registration.setCouple(couple);

        validateRegistration(
                registration,
                registration.getSurrogate());

        Employee logged = SessionContext.getLoggedUser();

        registration.setUserChanged(logged);
        registration.setDateChanged(LocalDateTime.now());

        registrationDAO.update(registration);
    }

    // -------------------------------------------------
    // Baja lógica
    // -------------------------------------------------

    public void delete(Long registrationId) throws SQLException {

        if (registrationId == null) {
            throw new IllegalArgumentException(
                    "El ID de la inscripción es obligatorio.");
        }

        TournamentRegistration registration =
                registrationDAO.findById(registrationId);

        if (registration == null || registration.isRemoved()) {
            throw new IllegalArgumentException(
                    "No se encontró la inscripción.");
        }

        registrationDAO.delete(registrationId);
    }

    // -------------------------------------------------
    // Validaciones
    // -------------------------------------------------

    private Tournament validateTournament(Tournament tournament)
            throws SQLException {

        if (tournament == null || tournament.getSurrogate() == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un torneo.");
        }

        Tournament existing = tournamentDAO.findById(
                tournament.getSurrogate());

        if (existing == null
                || !existing.isActive()
                || existing.isRemoved()) {

            throw new IllegalArgumentException(
                    "El torneo seleccionado no existe o está inactivo.");
        }

        return existing;
    }

    private Player validatePlayer(Player player) throws SQLException {

        if (player == null || player.getSurrogate() == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar dos jugadores registrados.");
        }

        Player existing = playerDAO.findById(player.getSurrogate());

        if (existing == null) {
            throw new IllegalArgumentException(
                    "Uno de los jugadores no existe o fue eliminado.");
        }

        return existing;
    }

    private void validatePlayerCategories(
            Tournament tournament,
            Player player1,
            Player player2) {

        int tournamentCategory = tournament.getCategory().getValue();

        if (tournamentCategory == 0) {
            return;
        }

        if (player1.getCategory() != tournamentCategory
                || player2.getCategory() != tournamentCategory) {

            throw new IllegalArgumentException(
                    "Los jugadores deben pertenecer a la categoría del torneo.");
        }
    }

    private void validateRegistration(
            TournamentRegistration registration,
            Long excludeRegistrationId) throws SQLException {

        Tournament tournament = registration.getTournament();
        Couple couple = registration.getCouple();

        validatePlayerCategories(
                tournament,
                couple.getPlayer1(),
                couple.getPlayer2());

        // Comprobar si ya existe una inscripción de esa pareja.
        TournamentRegistration existing =
                registrationDAO.findByTournamentAndCouple(
                        tournament.getSurrogate(),
                        couple.getSurrogate());

        if (existing != null
                && !existing.getSurrogate().equals(excludeRegistrationId)) {

            if (existing.isRemoved()) {
                throw new IllegalArgumentException(
                        "La pareja ya tiene una inscripción dada de baja. "
                        + "Debe recuperarse la inscripción existente.");
            }

            throw new IllegalArgumentException(
                    "La pareja ya está inscripta en este torneo.");
        }

        // Comprobar que ninguno de los jugadores esté inscripto
        // en otra pareja del mismo torneo.
        List<TournamentRegistration> registrations =
                registrationDAO.findByTournament(
                        tournament.getSurrogate());

        Long player1Id = couple.getPlayer1().getSurrogate();
        Long player2Id = couple.getPlayer2().getSurrogate();

        for (TournamentRegistration item : registrations) {

            if (item.getSurrogate().equals(excludeRegistrationId)) {
                continue;
            }

            Couple registeredCouple = item.getCouple();

            Long registeredPlayer1 =
                    registeredCouple.getPlayer1().getSurrogate();

            Long registeredPlayer2 =
                    registeredCouple.getPlayer2().getSurrogate();
            
            //Jugador ya registrado, de momento se omite
            /*
            if (player1Id.equals(registeredPlayer1)
                    || player1Id.equals(registeredPlayer2)
                    || player2Id.equals(registeredPlayer1)
                    || player2Id.equals(registeredPlayer2)) {

                throw new IllegalArgumentException(
                        "Uno de los jugadores ya está inscripto "
                        + "en este torneo con otra pareja.");
            }*/
        }
    }

    // -------------------------------------------------
    // Guardar o recuperar inscripción
    // -------------------------------------------------

    private void saveRegistration(
            TournamentRegistration registration) throws SQLException {

        Employee logged = SessionContext.getLoggedUser();
        LocalDateTime now = LocalDateTime.now();

        TournamentRegistration existing =
                registrationDAO.findByTournamentAndCouple(
                        registration.getTournament().getSurrogate(),
                        registration.getCouple().getSurrogate());

        if (existing != null && existing.isRemoved()) {

            try (Connection con = dataSource.getConnection()) {

                registrationDAO.restore(con, existing.getSurrogate());
            }

            return;
        }

        registration.setRegistrationDate(now);
        registration.setUserAdded(logged);
        registration.setDateAdded(now);
        registration.setActive(true);
        registration.setRemoved(false);

        registrationDAO.insert(registration);
    }
}
