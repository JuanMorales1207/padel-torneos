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
import ar.com.padel.model.entity.Employee;
import ar.com.padel.model.entity.Tournament;
import ar.com.padel.security.context.SessionContext;

public class TournamentDAOImpl implements TournamentDAO {

    private final DataSource dataSource;

    public TournamentDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Tournament findById(Long id) throws SQLException {

        String sql = """
            SELECT
			    TournamentId,
			    Name,
			    Category,
			    TournamentDate,
			    EndDate,
			    IsActive,
			    IsRemoved,
			    DateAdded,
			    DateChanged
			FROM Tournament
            WHERE IsRemoved = 0
              AND TournamentId = ?
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
    public List<Tournament> findAll() throws SQLException {

        String sql = """
            SELECT
			    TournamentId,
			    Name,
			    Category,
			    TournamentDate,
			    EndDate,
			    IsActive,
			    IsRemoved,
			    DateAdded,
			    DateChanged
			FROM Tournament
            WHERE IsRemoved = 0
            ORDER BY TournamentId
            """;

        List<Tournament> tournaments = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tournaments.add(map(rs));
            }
        }

        return tournaments;
    }

    @Override
    public List<Tournament> findActive() throws SQLException {

        String sql = """
            SELECT
			    TournamentId,
			    Name,
			    Category,
			    TournamentDate,
			    EndDate,
			    IsActive,
			    IsRemoved,
			    DateAdded,
			    DateChanged
			FROM Tournament
            WHERE IsActive = 1
              AND IsRemoved = 0
            ORDER BY TournamentId
            """;

        List<Tournament> tournaments = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tournaments.add(map(rs));
            }
        }

        return tournaments;
    }

    @Override
    public void insert(Tournament tournament) throws SQLException {

        try (Connection con = dataSource.getConnection()) {
            insert(con, tournament);
        }
    }

    @Override
    public void insert(Connection con, Tournament tournament) throws SQLException {

        String sql = """
            INSERT INTO Tournament
                (Name, Category, TournamentDate, EndDate,
                 IsActive, UserAdded, DateAdded)
            VALUES (?, ?, ?, ?, ?, ?, COALESCE(?, CURRENT_TIMESTAMP))
            """;

        try (PreparedStatement ps = con.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, tournament.getName());
            ps.setInt(2, tournament.getCategory().getValue());
            ps.setDate(3, java.sql.Date.valueOf(tournament.getTournamentDate()));
            ps.setDate(4, java.sql.Date.valueOf(tournament.getEndDate()));
            ps.setBoolean(5, tournament.isActive());

            Employee user = SessionContext.getLoggedUser();

            if (user != null) {
                ps.setObject(6, user.getSurrogate());
            } else {
                ps.setNull(6, java.sql.Types.BIGINT);
            }

            if (tournament.getDateAdded() != null) {
                ps.setTimestamp(7, Timestamp.valueOf(tournament.getDateAdded()));
            } else {
                ps.setNull(7, java.sql.Types.TIMESTAMP);
            }

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    tournament.setSurrogate(keys.getLong(1));
                }
            }
        }
    }

    @Override
    public void update(Tournament tournament) throws SQLException {

        try (Connection con = dataSource.getConnection()) {
            update(con, tournament);
        }
    }

    @Override
    public void update(Connection con, Tournament tournament) throws SQLException {

        String sql = """
            UPDATE Tournament
            SET Name = ?,
                Category = ?,
                TournamentDate = ?,
                EndDate = ?,
                IsActive = ?,
                UserChanged = ?,
                DateChanged = CURRENT_TIMESTAMP
            WHERE TournamentId = ?
              AND IsRemoved = 0
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, tournament.getName());
            ps.setInt(2, tournament.getCategory().getValue());
            ps.setDate(3, java.sql.Date.valueOf(tournament.getTournamentDate()));
            ps.setDate(4, java.sql.Date.valueOf(tournament.getEndDate()));
            ps.setBoolean(5, tournament.isActive());

            Employee user = SessionContext.getLoggedUser();

            if (user != null) {
                ps.setObject(6, user.getSurrogate());
            } else {
                ps.setNull(6, java.sql.Types.BIGINT);
            }

            ps.setLong(7, tournament.getSurrogate());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Long tournamentId) throws SQLException {

        String sql = """
            UPDATE Tournament
            SET IsRemoved = 1,
                UserChanged = ?,
                DateChanged = CURRENT_TIMESTAMP
            WHERE TournamentId = ?
              AND IsRemoved = 0
            """;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            Employee user = SessionContext.getLoggedUser();

            if (user != null) {
                ps.setObject(1, user.getSurrogate());
            } else {
                ps.setNull(1, java.sql.Types.BIGINT);
            }

            ps.setLong(2, tournamentId);

            ps.executeUpdate();
        }
    }

    private Tournament map(ResultSet rs) throws SQLException {
        Tournament tournament = new Tournament();

        tournament.setSurrogate(rs.getLong("TournamentId"));
        tournament.setName(rs.getString("Name"));
        tournament.setCategory(Category.fromValue(rs.getInt("Category")));

        java.sql.Date tournamentDate = rs.getDate("TournamentDate");
        if (tournamentDate != null) {
            tournament.setTournamentDate(tournamentDate.toLocalDate());
        }
        
        java.sql.Date endDate = rs.getDate("EndDate");

        if (endDate != null) {
            tournament.setEndDate(endDate.toLocalDate());
        }

        tournament.setActive(rs.getBoolean("IsActive"));
        tournament.setRemoved(rs.getBoolean("IsRemoved"));

        Timestamp dateAdded = rs.getTimestamp("DateAdded");
        if (dateAdded != null) {
            tournament.setDateAdded(dateAdded.toLocalDateTime());
        }

        Timestamp dateChanged = rs.getTimestamp("DateChanged");
        if (dateChanged != null) {
            tournament.setDateChanged(dateChanged.toLocalDateTime());
        }

        return tournament;
    }

}