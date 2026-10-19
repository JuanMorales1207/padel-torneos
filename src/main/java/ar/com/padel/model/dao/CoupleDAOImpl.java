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

import ar.com.padel.model.entity.Couple;
import ar.com.padel.model.entity.Employee;
import ar.com.padel.model.entity.Player;
import ar.com.padel.security.context.SessionContext;

public class CoupleDAOImpl implements CoupleDAO {

    private final DataSource dataSource;

    public CoupleDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Couple findById(Long id) throws SQLException {

        String sql = """
            SELECT
                c.CoupleId,
                c.Name AS CoupleName,
                c.RegistrationDate,
                c.IsActive,
                c.IsRemoved,
                c.DateAdded,
                c.DateChanged,
                c.Player1Id,
                c.Player2Id,
                p1.Name AS Player1Name,
                p1.LastName AS Player1LastName,
                p1.Birthdate AS Player1Birthdate,
                p1.Category AS Player1Category,
                p2.Name AS Player2Name,
                p2.LastName AS Player2LastName,
                p2.Birthdate AS Player2Birthdate,
                p2.Category AS Player2Category
            FROM Couple c
            INNER JOIN Player p1 ON p1.PlayerId = c.Player1Id
            INNER JOIN Player p2 ON p2.PlayerId = c.Player2Id
            WHERE c.IsRemoved = 0
              AND c.CoupleId = ?
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
    public List<Couple> findAll() throws SQLException {

        String sql = """
            SELECT
                c.CoupleId,
                c.Name AS CoupleName,
                c.RegistrationDate,
                c.IsActive,
                c.IsRemoved,
                c.DateAdded,
                c.DateChanged,
                c.Player1Id,
                c.Player2Id,
                p1.Name AS Player1Name,
                p1.LastName AS Player1LastName,
                p1.Birthdate AS Player1Birthdate,
                p1.Category AS Player1Category,
                p2.Name AS Player2Name,
                p2.LastName AS Player2LastName,
                p2.Birthdate AS Player2Birthdate,
                p2.Category AS Player2Category
            FROM Couple c
            INNER JOIN Player p1 ON p1.PlayerId = c.Player1Id
            INNER JOIN Player p2 ON p2.PlayerId = c.Player2Id
            WHERE c.IsRemoved = 0
            ORDER BY c.Name
            """;

        List<Couple> couples = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                couples.add(map(rs));
            }
        }

        return couples;
    }

    @Override
    public void insert(Couple couple) throws SQLException {

        String sql = """
            INSERT INTO Couple
                (Player1Id, Player2Id, Name, RegistrationDate,
                 IsActive, UserAdded, DateAdded)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, couple.getPlayer1().getSurrogate());
            ps.setLong(2, couple.getPlayer2().getSurrogate());
            ps.setString(3, couple.getName());

            if (couple.getRegistrationDate() != null) {
                ps.setTimestamp(
                        4, Timestamp.valueOf(couple.getRegistrationDate()));
            } else {
                ps.setTimestamp(4, Timestamp.valueOf(
                        java.time.LocalDateTime.now()));
            }

            ps.setBoolean(5, couple.isActive());

            Employee user = SessionContext.getLoggedUser();

            if (user != null) {
                ps.setObject(6, user.getSurrogate());
            } else {
                ps.setNull(6, java.sql.Types.BIGINT);
            }

            ps.setTimestamp(7, Timestamp.valueOf(couple.getDateAdded()));

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    couple.setSurrogate(keys.getLong(1));
                }
            }
        }
    }

    @Override
    public void update(Couple couple) throws SQLException {

        String sql = """
            UPDATE Couple
            SET Player1Id = ?,
                Player2Id = ?,
                Name = ?,
                RegistrationDate = ?,
                IsActive = ?,
                UserChanged = ?,
                DateChanged = CURRENT_TIMESTAMP
            WHERE CoupleId = ?
              AND IsRemoved = 0
            """;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, couple.getPlayer1().getSurrogate());
            ps.setLong(2, couple.getPlayer2().getSurrogate());
            ps.setString(3, couple.getName());

            if (couple.getRegistrationDate() != null) {
                ps.setTimestamp(
                        4, Timestamp.valueOf(couple.getRegistrationDate()));
            } else {
                ps.setNull(4, java.sql.Types.TIMESTAMP);
            }

            ps.setBoolean(5, couple.isActive());

            Employee user = SessionContext.getLoggedUser();

            if (user != null) {
                ps.setObject(6, user.getSurrogate());
            } else {
                ps.setNull(6, java.sql.Types.BIGINT);
            }

            ps.setLong(7, couple.getSurrogate());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Long coupleId) throws SQLException {

        String sql = """
            UPDATE Couple
            SET IsRemoved = 1,
                UserChanged = ?,
                DateChanged = CURRENT_TIMESTAMP
            WHERE CoupleId = ?
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

            ps.setLong(2, coupleId);

            ps.executeUpdate();
        }
    }

    private Couple map(ResultSet rs) throws SQLException {

        Couple couple = new Couple();

        couple.setSurrogate(rs.getLong("CoupleId"));
        couple.setName(rs.getString("CoupleName"));
        couple.setActive(rs.getBoolean("IsActive"));
        couple.setRemoved(rs.getBoolean("IsRemoved"));

        Timestamp registrationDate = rs.getTimestamp("RegistrationDate");
        if (registrationDate != null) {
            couple.setRegistrationDate(registrationDate.toLocalDateTime());
        }

        Timestamp dateAdded = rs.getTimestamp("DateAdded");
        if (dateAdded != null) {
            couple.setDateAdded(dateAdded.toLocalDateTime());
        }

        Timestamp dateChanged = rs.getTimestamp("DateChanged");
        if (dateChanged != null) {
            couple.setDateChanged(dateChanged.toLocalDateTime());
        }

        Player player1 = new Player();
        player1.setSurrogate(rs.getLong("Player1Id"));
        player1.setName(rs.getString("Player1Name"));
        player1.setLastname(rs.getString("Player1LastName"));
        player1.setCategory(rs.getInt("Player1Category"));

        Date birthdate1 = rs.getDate("Player1Birthdate");
        if (birthdate1 != null) {
            player1.setBirthdate(birthdate1.toLocalDate());
        }

        Player player2 = new Player();
        player2.setSurrogate(rs.getLong("Player2Id"));
        player2.setName(rs.getString("Player2Name"));
        player2.setLastname(rs.getString("Player2LastName"));
        player2.setCategory(rs.getInt("Player2Category"));

        Date birthdate2 = rs.getDate("Player2Birthdate");
        if (birthdate2 != null) {
            player2.setBirthdate(birthdate2.toLocalDate());
        }

        couple.setPlayer1(player1);
        couple.setPlayer2(player2);

        return couple;
    }
    

	@Override
	public boolean existsByPlayers(Long player1Id, Long player2Id, Long excludeCoupleId) throws SQLException {
	
	    String sql = """
	        SELECT COUNT(*)
	        FROM Couple
	        WHERE IsRemoved = 0
	          AND (
	                (Player1Id = ? AND Player2Id = ?)
	                OR
	                (Player1Id = ? AND Player2Id = ?)
	              )
	          AND (? IS NULL OR CoupleId <> ?)
	        """;
	
	    try (Connection con = dataSource.getConnection();
	         PreparedStatement ps = con.prepareStatement(sql)) {
	
	        ps.setLong(1, player1Id);
	        ps.setLong(2, player2Id);
	
	        ps.setLong(3, player2Id);
	        ps.setLong(4, player1Id);
	
	        if (excludeCoupleId != null) {
	            ps.setLong(5, excludeCoupleId);
	            ps.setLong(6, excludeCoupleId);
	        } else {
	            ps.setNull(5, java.sql.Types.BIGINT);
	            ps.setNull(6, java.sql.Types.BIGINT);
	        }
	
	        try (ResultSet rs = ps.executeQuery()) {
	            return rs.next() && rs.getLong(1) > 0;
	        }
	    }
	}
    
}
