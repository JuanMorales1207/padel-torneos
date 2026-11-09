package ar.com.padel.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import ar.com.padel.model.entity.Category;
import ar.com.padel.model.entity.Couple;
import ar.com.padel.model.entity.Employee;
import ar.com.padel.model.entity.Player;
import ar.com.padel.model.entity.Tournament;
import ar.com.padel.model.entity.TournamentRegistration;
import ar.com.padel.security.context.SessionContext;

public class TournamentRegistrationDAOImpl implements TournamentRegistrationDAO {

    private final DataSource dataSource;

    public TournamentRegistrationDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private static final String SELECT_BASE = """
        SELECT
            tr.RegistrationId,
            tr.RegistrationDate,
            tr.IsActive,
            tr.IsRemoved,
            tr.DateAdded,
            tr.DateChanged,

            t.TournamentId,
            t.Name AS TournamentName,
            t.Category AS TournamentCategory,
            t.TournamentDate,
            t.EndDate,
            t.IsActive AS TournamentActive,
            t.IsRemoved AS TournamentRemoved,

            c.CoupleId,
            c.Name AS CoupleName,
            c.RegistrationDate AS CoupleRegistrationDate,
            c.IsActive AS CoupleActive,
            c.IsRemoved AS CoupleRemoved,

            c.Player1Id,
            p1.Name AS Player1Name,
            p1.LastName AS Player1LastName,
            p1.Birthdate AS Player1Birthdate,
            p1.Category AS Player1Category,

            c.Player2Id,
            p2.Name AS Player2Name,
            p2.LastName AS Player2LastName,
            p2.Birthdate AS Player2Birthdate,
            p2.Category AS Player2Category

        FROM TournamentRegistration tr
        INNER JOIN Tournament t ON t.TournamentId = tr.TournamentId
        INNER JOIN Couple c ON c.CoupleId = tr.CoupleId
        INNER JOIN Player p1 ON p1.PlayerId = c.Player1Id
        INNER JOIN Player p2 ON p2.PlayerId = c.Player2Id
        """;

    @Override
    public TournamentRegistration findById(Long id) throws SQLException {

        String sql = SELECT_BASE + """
            WHERE tr.RegistrationId = ?
            """;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    @Override
    public List<TournamentRegistration> findAll() throws SQLException {

        String sql = SELECT_BASE + """
            WHERE tr.IsRemoved = 0
            ORDER BY tr.RegistrationDate DESC, tr.RegistrationId DESC
            """;

        List<TournamentRegistration> registrations = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                registrations.add(map(rs));
            }
        }

        return registrations;
    }

    @Override
    public List<TournamentRegistration> findByTournament(Long tournamentId)
            throws SQLException {

        String sql = SELECT_BASE + """
            WHERE tr.TournamentId = ?
              AND tr.IsRemoved = 0
            ORDER BY tr.RegistrationDate DESC
            """;

        List<TournamentRegistration> registrations = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, tournamentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    registrations.add(map(rs));
                }
            }
        }

        return registrations;
    }

    @Override
    public List<TournamentRegistration> findByCouple(Long coupleId)
            throws SQLException {

        String sql = SELECT_BASE + """
            WHERE tr.CoupleId = ?
              AND tr.IsRemoved = 0
            ORDER BY tr.RegistrationDate DESC
            """;

        List<TournamentRegistration> registrations = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, coupleId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    registrations.add(map(rs));
                }
            }
        }

        return registrations;
    }

    @Override
    public TournamentRegistration findByTournamentAndCouple(
            Long tournamentId, Long coupleId) throws SQLException {

        String sql = SELECT_BASE + """
            WHERE tr.TournamentId = ?
              AND tr.CoupleId = ?
            """;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, tournamentId);
            ps.setLong(2, coupleId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    @Override
    public void insert(TournamentRegistration registration) throws SQLException {

        try (Connection con = dataSource.getConnection()) {
            insert(con, registration);
        }
    }

    @Override
    public void insert(Connection con, TournamentRegistration registration)
            throws SQLException {

        String sql = """
            INSERT INTO TournamentRegistration
                (TournamentId, CoupleId, RegistrationDate,
                 IsActive, UserAdded, DateAdded)
            VALUES (?, ?, ?, ?, ?, COALESCE(?, CURRENT_TIMESTAMP))
            """;

        try (PreparedStatement ps = con.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, registration.getTournament().getSurrogate());
            ps.setLong(2, registration.getCouple().getSurrogate());

            if (registration.getRegistrationDate() != null) {
                ps.setTimestamp(3,
                        Timestamp.valueOf(registration.getRegistrationDate()));
            } else {
                ps.setTimestamp(3, Timestamp.valueOf(
                        java.time.LocalDateTime.now()));
            }

            ps.setBoolean(4, registration.isActive());

            Employee user = SessionContext.getLoggedUser();

            if (user != null) {
                ps.setObject(5, user.getSurrogate());
            } else {
                ps.setNull(5, java.sql.Types.BIGINT);
            }

            if (registration.getDateAdded() != null) {
                ps.setTimestamp(6, Timestamp.valueOf(registration.getDateAdded()));
            } else {
                ps.setNull(6, java.sql.Types.TIMESTAMP);
            }

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    registration.setSurrogate(keys.getLong(1));
                }
            }
        }
    }

    @Override
    public void update(TournamentRegistration registration) throws SQLException {

        try (Connection con = dataSource.getConnection()) {
            update(con, registration);
        }
    }

    @Override
    public void update(Connection con, TournamentRegistration registration)
            throws SQLException {

        String sql = """
            UPDATE TournamentRegistration
            SET TournamentId = ?,
                CoupleId = ?,
                RegistrationDate = ?,
                IsActive = ?,
                UserChanged = ?,
                DateChanged = CURRENT_TIMESTAMP
            WHERE RegistrationId = ?
              AND IsRemoved = 0
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, registration.getTournament().getSurrogate());
            ps.setLong(2, registration.getCouple().getSurrogate());

            if (registration.getRegistrationDate() != null) {
                ps.setTimestamp(3,
                        Timestamp.valueOf(registration.getRegistrationDate()));
            } else {
                ps.setNull(3, java.sql.Types.TIMESTAMP);
            }

            ps.setBoolean(4, registration.isActive());

            Employee user = SessionContext.getLoggedUser();

            if (user != null) {
                ps.setObject(5, user.getSurrogate());
            } else {
                ps.setNull(5, java.sql.Types.BIGINT);
            }

            ps.setLong(6, registration.getSurrogate());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Long registrationId) throws SQLException {

        try (Connection con = dataSource.getConnection()) {
            delete(con, registrationId);
        }
    }

    @Override
    public void delete(Connection con, Long registrationId) throws SQLException {

        String sql = """
            UPDATE TournamentRegistration
            SET IsRemoved = 1,
                UserChanged = ?,
                DateChanged = CURRENT_TIMESTAMP
            WHERE RegistrationId = ?
              AND IsRemoved = 0
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            Employee user = SessionContext.getLoggedUser();

            if (user != null) {
                ps.setObject(1, user.getSurrogate());
            } else {
                ps.setNull(1, java.sql.Types.BIGINT);
            }

            ps.setLong(2, registrationId);

            ps.executeUpdate();
        }
    }

    @Override
    public void restore(Connection con, Long registrationId) throws SQLException {

        String sql = """
            UPDATE TournamentRegistration
            SET IsRemoved = 0,
                IsActive = 1,
                UserChanged = ?,
                DateChanged = CURRENT_TIMESTAMP
            WHERE RegistrationId = ?
              AND IsRemoved = 1
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            Employee user = SessionContext.getLoggedUser();

            if (user != null) {
                ps.setObject(1, user.getSurrogate());
            } else {
                ps.setNull(1, java.sql.Types.BIGINT);
            }

            ps.setLong(2, registrationId);

            ps.executeUpdate();
        }
    }

    private TournamentRegistration map(ResultSet rs) throws SQLException {

        TournamentRegistration registration = new TournamentRegistration();

        registration.setSurrogate(rs.getLong("RegistrationId"));
        registration.setActive(rs.getBoolean("IsActive"));
        registration.setRemoved(rs.getBoolean("IsRemoved"));

        Timestamp registrationDate = rs.getTimestamp("RegistrationDate");
        if (registrationDate != null) {
            registration.setRegistrationDate(registrationDate.toLocalDateTime());
        }

        Timestamp dateAdded = rs.getTimestamp("DateAdded");
        if (dateAdded != null) {
            registration.setDateAdded(dateAdded.toLocalDateTime());
        }

        Timestamp dateChanged = rs.getTimestamp("DateChanged");
        if (dateChanged != null) {
            registration.setDateChanged(dateChanged.toLocalDateTime());
        }

        Tournament tournament = new Tournament();
        tournament.setSurrogate(rs.getLong("TournamentId"));
        tournament.setName(rs.getString("TournamentName"));
        tournament.setCategory(Category.fromValue(rs.getInt("TournamentCategory")));

        java.sql.Date tournamentDate = rs.getDate("TournamentDate");
        if (tournamentDate != null) {
            tournament.setTournamentDate(tournamentDate.toLocalDate());
        }

        java.sql.Date endDate = rs.getDate("EndDate");
        if (endDate != null) {
            tournament.setEndDate(endDate.toLocalDate());
        }

        tournament.setActive(rs.getBoolean("TournamentActive"));
        tournament.setRemoved(rs.getBoolean("TournamentRemoved"));

        Couple couple = new Couple();
        couple.setSurrogate(rs.getLong("CoupleId"));
        couple.setName(rs.getString("CoupleName"));
        couple.setActive(rs.getBoolean("CoupleActive"));
        couple.setRemoved(rs.getBoolean("CoupleRemoved"));

        Timestamp coupleRegistrationDate = rs.getTimestamp("CoupleRegistrationDate");
        if (coupleRegistrationDate != null) {
            couple.setRegistrationDate(coupleRegistrationDate.toLocalDateTime());
        }

        Player player1 = new Player();
        player1.setSurrogate(rs.getLong("Player1Id"));
        player1.setName(rs.getString("Player1Name"));
        player1.setLastname(rs.getString("Player1LastName"));
        player1.setCategory(rs.getInt("Player1Category"));

        java.sql.Date birthdate1 = rs.getDate("Player1Birthdate");
        if (birthdate1 != null) {
            player1.setBirthdate(birthdate1.toLocalDate());
        }

        Player player2 = new Player();
        player2.setSurrogate(rs.getLong("Player2Id"));
        player2.setName(rs.getString("Player2Name"));
        player2.setLastname(rs.getString("Player2LastName"));
        player2.setCategory(rs.getInt("Player2Category"));

        java.sql.Date birthdate2 = rs.getDate("Player2Birthdate");
        if (birthdate2 != null) {
            player2.setBirthdate(birthdate2.toLocalDate());
        }

        couple.setPlayer1(player1);
        couple.setPlayer2(player2);

        registration.setTournament(tournament);
        registration.setCouple(couple);

        return registration;
    }
}
