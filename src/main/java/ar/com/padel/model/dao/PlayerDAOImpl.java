package ar.com.padel.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import ar.com.padel.model.entity.Employee;
import ar.com.padel.model.entity.Player;
import ar.com.padel.security.context.SessionContext;

public class PlayerDAOImpl implements PlayerDAO {

    private final DataSource dataSource;

    public PlayerDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Player findById(Long id) throws SQLException {

        String sql = """
            SELECT *
            FROM Player
            WHERE IsRemoved = 0
              AND PlayerId = ?
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
    public List<Player> findAll() throws SQLException {

        String sql = """
            SELECT *
            FROM Player
            WHERE IsRemoved = 0
            ORDER BY LastName, Name
            """;

        List<Player> players = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                players.add(map(rs));
            }
        }

        return players;
    }

    @Override
    public void insert(Player player) throws SQLException {

        String sql = """
            INSERT INTO Player
                (Name, LastName, Birthdate, UserAdded, DateAdded)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, player.getName());
            ps.setString(2, player.getLastname());

            if (player.getBirthdate() != null) {
                ps.setDate(3, Date.valueOf(player.getBirthdate()));
            } else {
                ps.setNull(3, java.sql.Types.DATE);
            }

            Employee user = SessionContext.getLoggedUser();

            if (user != null) {
                ps.setObject(4, user.getSurrogate());
            } else {
                ps.setNull(4, java.sql.Types.BIGINT);
            }

            ps.setTimestamp(5, Timestamp.valueOf(player.getDateAdded()));

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    player.setSurrogate(keys.getLong(1));
                }
            }
        }
    }

    @Override
    public void update(Player player) throws SQLException {

        String sql = """
            UPDATE Player
            SET Name = ?,
                LastName = ?,
                Birthdate = ?,
                UserChanged = ?,
                DateChanged = CURRENT_TIMESTAMP
            WHERE PlayerId = ?
              AND IsRemoved = 0
            """;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, player.getName());
            ps.setString(2, player.getLastname());

            if (player.getBirthdate() != null) {
                ps.setDate(3, Date.valueOf(player.getBirthdate()));
            } else {
                ps.setNull(3, java.sql.Types.DATE);
            }

            Employee user = SessionContext.getLoggedUser();

            if (user != null) {
                ps.setObject(4, user.getSurrogate());
            } else {
                ps.setNull(4, java.sql.Types.BIGINT);
            }

            ps.setLong(5, player.getSurrogate());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Long playerId) throws SQLException {

        String sql = """
            UPDATE Player
            SET IsRemoved = 1,
                UserChanged = ?,
                DateChanged = CURRENT_TIMESTAMP
            WHERE PlayerId = ?
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

            ps.setLong(2, playerId);

            ps.executeUpdate();
        }
    }

    private Player map(ResultSet rs) throws SQLException {

        Player player = new Player();

        player.setSurrogate(rs.getLong("PlayerId"));
        player.setName(rs.getString("Name"));
        player.setLastname(rs.getString("LastName"));

        Date birthdate = rs.getDate("Birthdate");
        if (birthdate != null) {
            player.setBirthdate(birthdate.toLocalDate());
        }

        Timestamp dateAdded = rs.getTimestamp("DateAdded");
        if (dateAdded != null) {
            player.setDateAdded(dateAdded.toLocalDateTime());
        }

        Timestamp dateChanged = rs.getTimestamp("DateChanged");
        if (dateChanged != null) {
            player.setDateChanged(dateChanged.toLocalDateTime());
        }

        return player;
    }
}
