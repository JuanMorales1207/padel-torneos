package ar.com.padel.model.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import ar.com.padel.model.entity.Category;
import ar.com.padel.model.entity.Employee;
import ar.com.padel.model.entity.Tournament;
import ar.com.padel.model.entity.TournamentRound;
import ar.com.padel.security.context.SessionContext;

public class TournamentRoundDAOImpl implements TournamentRoundDAO {

    private final DataSource dataSource;

    public TournamentRoundDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public TournamentRound findById(Long id) throws SQLException {

        String sql = """
            SELECT
                tr.RoundId,
                tr.TournamentId,
                tr.Description,
                tr.RoundNumber,
                tr.StartDate,
                tr.EndDate,
                tr.RoundStatus,
                tr.IsActive,
                tr.IsRemoved,
                tr.DateAdded,
                tr.DateChanged,
                t.Name AS TournamentName,
                t.Category AS TournamentCategory,
                t.TournamentDate,
                t.EndDate AS TournamentEndDate
            FROM TournamentRound tr
            INNER JOIN Tournament t ON t.TournamentId = tr.TournamentId
            WHERE tr.IsRemoved = 0
              AND t.IsRemoved = 0
              AND tr.RoundId = ?
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
    public List<TournamentRound> findByTournament(Long tournamentId)
            throws SQLException {

        String sql = """
            SELECT
                tr.RoundId,
                tr.TournamentId,
                tr.Description,
                tr.RoundNumber,
                tr.StartDate,
                tr.EndDate,
                tr.RoundStatus,
                tr.IsActive,
                tr.IsRemoved,
                tr.DateAdded,
                tr.DateChanged,
                t.Name AS TournamentName,
                t.Category AS TournamentCategory,
                t.TournamentDate,
                t.EndDate AS TournamentEndDate
            FROM TournamentRound tr
            INNER JOIN Tournament t ON t.TournamentId = tr.TournamentId
            WHERE tr.IsRemoved = 0
              AND t.IsRemoved = 0
              AND tr.TournamentId = ?
            ORDER BY tr.RoundNumber
            """;

        List<TournamentRound> rounds = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, tournamentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rounds.add(map(rs));
                }
            }
        }

        return rounds;
    }

    @Override
    public void insert(TournamentRound round) throws SQLException {

        try (Connection con = dataSource.getConnection()) {
            insert(con, round);
        }
    }

    @Override
    public void insert(Connection con, TournamentRound round) throws SQLException {

        String sql = """
            INSERT INTO TournamentRound
                (TournamentId, Description, RoundNumber,
                 StartDate, EndDate, RoundStatus, IsActive,
                 UserAdded, DateAdded)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps = con.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, round.getTournament().getSurrogate());
            ps.setString(2, round.getDescription());
            ps.setInt(3, round.getRoundNumber());

            if (round.getStartDate() != null) {
                ps.setDate(4, Date.valueOf(round.getStartDate()));
            } else {
                ps.setNull(4, java.sql.Types.DATE);
            }

            if (round.getEndDate() != null) {
                ps.setDate(5, Date.valueOf(round.getEndDate()));
            } else {
                ps.setNull(5, java.sql.Types.DATE);
            }

            ps.setString(6, round.getRoundStatus());
            ps.setBoolean(7, round.isActive());

            Employee user = SessionContext.getLoggedUser();

            if (user != null) {
                ps.setObject(8, user.getSurrogate());
            } else {
                ps.setNull(8, java.sql.Types.BIGINT);
            }

            if (round.getDateAdded() != null) {
                ps.setTimestamp(9, Timestamp.valueOf(round.getDateAdded()));
            } else {
                ps.setNull(9, java.sql.Types.TIMESTAMP);
            }

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    round.setSurrogate(keys.getLong(1));
                }
            }
        }
    }

    @Override
    public void update(TournamentRound round) throws SQLException {

        try (Connection con = dataSource.getConnection()) {
            update(con, round);
        }
    }

    @Override
    public void update(Connection con, TournamentRound round) throws SQLException {

        String sql = """
            UPDATE TournamentRound
            SET Description = ?,
                RoundNumber = ?,
                StartDate = ?,
                EndDate = ?,
                RoundStatus = ?,
                IsActive = ?,
                UserChanged = ?,
                DateChanged = CURRENT_TIMESTAMP
            WHERE RoundId = ?
              AND IsRemoved = 0
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, round.getDescription());
            ps.setInt(2, round.getRoundNumber());

            if (round.getStartDate() != null) {
                ps.setDate(3, Date.valueOf(round.getStartDate()));
            } else {
                ps.setNull(3, java.sql.Types.DATE);
            }

            if (round.getEndDate() != null) {
                ps.setDate(4, Date.valueOf(round.getEndDate()));
            } else {
                ps.setNull(4, java.sql.Types.DATE);
            }

            ps.setString(5, round.getRoundStatus());
            ps.setBoolean(6, round.isActive());

            Employee user = SessionContext.getLoggedUser();

            if (user != null) {
                ps.setObject(7, user.getSurrogate());
            } else {
                ps.setNull(7, java.sql.Types.BIGINT);
            }

            ps.setLong(8, round.getSurrogate());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Long roundId) throws SQLException {

        try (Connection con = dataSource.getConnection()) {
            delete(con, roundId);
        }
    }

    @Override
    public void delete(Connection con, Long roundId) throws SQLException {

        String sql = """
            UPDATE TournamentRound
            SET IsRemoved = 1,
                UserChanged = ?,
                DateChanged = CURRENT_TIMESTAMP
            WHERE RoundId = ?
              AND IsRemoved = 0
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            Employee user = SessionContext.getLoggedUser();

            if (user != null) {
                ps.setObject(1, user.getSurrogate());
            } else {
                ps.setNull(1, java.sql.Types.BIGINT);
            }

            ps.setLong(2, roundId);

            ps.executeUpdate();
        }
    }

    private TournamentRound map(ResultSet rs) throws SQLException {

        TournamentRound round = new TournamentRound();

        round.setSurrogate(rs.getLong("RoundId"));
        round.setDescription(rs.getString("Description"));
        round.setRoundNumber(rs.getInt("RoundNumber"));
        round.setRoundStatus(rs.getString("RoundStatus"));
        round.setActive(rs.getBoolean("IsActive"));
        round.setRemoved(rs.getBoolean("IsRemoved"));

        Date startDate = rs.getDate("StartDate");
        if (startDate != null) {
            round.setStartDate(startDate.toLocalDate());
        }

        Date endDate = rs.getDate("EndDate");
        if (endDate != null) {
            round.setEndDate(endDate.toLocalDate());
        }

        Timestamp dateAdded = rs.getTimestamp("DateAdded");
        if (dateAdded != null) {
            round.setDateAdded(dateAdded.toLocalDateTime());
        }

        Timestamp dateChanged = rs.getTimestamp("DateChanged");
        if (dateChanged != null) {
            round.setDateChanged(dateChanged.toLocalDateTime());
        }

        Tournament tournament = new Tournament();

        tournament.setSurrogate(rs.getLong("TournamentId"));
        tournament.setName(rs.getString("TournamentName"));
        tournament.setCategory(
                Category.fromValue(rs.getInt("TournamentCategory")));

        Date tournamentDate = rs.getDate("TournamentDate");
        if (tournamentDate != null) {
            tournament.setTournamentDate(tournamentDate.toLocalDate());
        }

        Date tournamentEndDate = rs.getDate("TournamentEndDate");
        if (tournamentEndDate != null) {
            tournament.setEndDate(tournamentEndDate.toLocalDate());
        }

        round.setTournament(tournament);

        return round;
    }
}