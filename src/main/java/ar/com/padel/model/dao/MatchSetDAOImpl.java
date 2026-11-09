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

import ar.com.padel.model.entity.Employee;
import ar.com.padel.model.entity.MatchSet;
import ar.com.padel.security.context.SessionContext;

public class MatchSetDAOImpl implements MatchSetDAO {

    private final DataSource dataSource;

    public MatchSetDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public MatchSet findById(Long id) throws SQLException {

        String sql = """
            SELECT
                MatchSetId,
                MatchId,
                SetNumber,
                GamesCouple1,
                GamesCouple2,
                TieBreakLoserPoints,
                IsRemoved,
                DateAdded,
                DateChanged
            FROM MatchSet
            WHERE IsRemoved = 0
              AND MatchSetId = ?
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
    public List<MatchSet> findByMatch(Long matchId) throws SQLException {

        String sql = """
            SELECT
                MatchSetId,
                MatchId,
                SetNumber,
                GamesCouple1,
                GamesCouple2,
                TieBreakLoserPoints,
                IsRemoved,
                DateAdded,
                DateChanged
            FROM MatchSet
            WHERE IsRemoved = 0
              AND MatchId = ?
            ORDER BY SetNumber
            """;

        List<MatchSet> sets = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, matchId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sets.add(map(rs));
                }
            }
        }

        return sets;
    }

    @Override
    public void insert(MatchSet matchSet) throws SQLException {

        try (Connection con = dataSource.getConnection()) {
            insert(con, matchSet);
        }
    }

    @Override
    public void insert(Connection con, MatchSet matchSet) throws SQLException {

        String sql = """
            INSERT INTO MatchSet
                (MatchId, SetNumber, GamesCouple1, GamesCouple2,
                 TieBreakLoserPoints, UserAdded, DateAdded)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps = con.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, matchSet.getMatch().getSurrogate());
            ps.setInt(2, matchSet.getSetNumber());
            ps.setInt(3, matchSet.getGamesCouple1());
            ps.setInt(4, matchSet.getGamesCouple2());

            if (matchSet.getTieBreakLoserPoints() != null) {
                ps.setInt(5, matchSet.getTieBreakLoserPoints());
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }

            Employee user = SessionContext.getLoggedUser();

            if (user != null) {
                ps.setObject(6, user.getSurrogate());
            } else {
                ps.setNull(6, java.sql.Types.BIGINT);
            }

            java.time.LocalDateTime dateAdded = matchSet.getDateAdded() != null
                    ? matchSet.getDateAdded()
                    : java.time.LocalDateTime.now();

            ps.setTimestamp(7, Timestamp.valueOf(dateAdded));

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    matchSet.setSurrogate(keys.getLong(1));
                }
            }
        }
    }

    @Override
    public void update(MatchSet matchSet) throws SQLException {

        try (Connection con = dataSource.getConnection()) {
            update(con, matchSet);
        }
    }

    @Override
    public void update(Connection con, MatchSet matchSet) throws SQLException {

        String sql = """
            UPDATE MatchSet
            SET SetNumber = ?,
                GamesCouple1 = ?,
                GamesCouple2 = ?,
                TieBreakLoserPoints = ?,
                UserChanged = ?,
                DateChanged = CURRENT_TIMESTAMP
            WHERE MatchSetId = ?
              AND IsRemoved = 0
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, matchSet.getSetNumber());
            ps.setInt(2, matchSet.getGamesCouple1());
            ps.setInt(3, matchSet.getGamesCouple2());

            if (matchSet.getTieBreakLoserPoints() != null) {
                ps.setInt(4, matchSet.getTieBreakLoserPoints());
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }

            Employee user = SessionContext.getLoggedUser();

            if (user != null) {
                ps.setObject(5, user.getSurrogate());
            } else {
                ps.setNull(5, java.sql.Types.BIGINT);
            }

            ps.setLong(6, matchSet.getSurrogate());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Long matchSetId) throws SQLException {

        try (Connection con = dataSource.getConnection()) {
            delete(con, matchSetId);
        }
    }

    @Override
    public void delete(Connection con, Long matchSetId) throws SQLException {

        String sql = """
            UPDATE MatchSet
            SET IsRemoved = 1,
                UserChanged = ?,
                DateChanged = CURRENT_TIMESTAMP
            WHERE MatchSetId = ?
              AND IsRemoved = 0
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            Employee user = SessionContext.getLoggedUser();

            if (user != null) {
                ps.setObject(1, user.getSurrogate());
            } else {
                ps.setNull(1, java.sql.Types.BIGINT);
            }

            ps.setLong(2, matchSetId);

            ps.executeUpdate();
        }
    }

    private MatchSet map(ResultSet rs) throws SQLException {

        MatchSet matchSet = new MatchSet();

        matchSet.setSurrogate(rs.getLong("MatchSetId"));
        matchSet.setSetNumber(rs.getInt("SetNumber"));
        matchSet.setGamesCouple1(rs.getInt("GamesCouple1"));
        matchSet.setGamesCouple2(rs.getInt("GamesCouple2"));
        matchSet.setTieBreakLoserPoints(
                (Integer) rs.getObject("TieBreakLoserPoints"));

        matchSet.setRemoved(rs.getBoolean("IsRemoved"));

        Timestamp dateAdded = rs.getTimestamp("DateAdded");
        if (dateAdded != null) {
            matchSet.setDateAdded(dateAdded.toLocalDateTime());
        }

        Timestamp dateChanged = rs.getTimestamp("DateChanged");
        if (dateChanged != null) {
            matchSet.setDateChanged(dateChanged.toLocalDateTime());
        }

        return matchSet;
    }
}