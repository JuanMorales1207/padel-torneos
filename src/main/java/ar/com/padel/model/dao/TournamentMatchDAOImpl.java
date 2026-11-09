package ar.com.padel.model.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import ar.com.padel.model.entity.Category;
import ar.com.padel.model.entity.Couple;
import ar.com.padel.model.entity.Player;
import ar.com.padel.model.entity.Tournament;
import ar.com.padel.model.entity.TournamentMatch;
import ar.com.padel.model.entity.TournamentRound;
import ar.com.padel.security.context.SessionContext;
import ar.com.padel.model.entity.Employee;

public class TournamentMatchDAOImpl implements TournamentMatchDAO {

    private final DataSource dataSource;

    public TournamentMatchDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private static final String SELECT_BASE = """
        SELECT
            tm.MatchId,
            tm.RoundId,
            tm.CoupleId1,
            tm.CoupleId2,
            tm.MatchDate,
            tm.Description AS MatchDescription,
            tm.Court,
            tm.Stage,
            tm.MatchStatus,
            tm.IsActive AS MatchIsActive,
            tm.IsRemoved AS MatchIsRemoved,
            tm.DateAdded AS MatchDateAdded,
            tm.DateChanged AS MatchDateChanged,

            tr.RoundNumber,
            tr.Description AS RoundDescription,
            tr.StartDate AS RoundStartDate,
            tr.EndDate AS RoundEndDate,
            tr.RoundStatus,
            tr.IsActive AS RoundIsActive,
            tr.IsRemoved AS RoundIsRemoved,
            tr.DateAdded AS RoundDateAdded,
            tr.DateChanged AS RoundDateChanged,

            t.TournamentId,
            t.Name AS TournamentName,
            t.Category AS TournamentCategory,
            t.TournamentDate,
            t.EndDate AS TournamentEndDate,
            t.IsActive AS TournamentIsActive,
            t.IsRemoved AS TournamentIsRemoved,
            t.DateAdded AS TournamentDateAdded,
            t.DateChanged AS TournamentDateChanged,

            c1.Name AS Couple1Name,
            c1.IsActive AS Couple1IsActive,
            c1.IsRemoved AS Couple1IsRemoved,
            c1.Player1Id AS Couple1Player1Id,
            c1.Player2Id AS Couple1Player2Id,

            c2.Name AS Couple2Name,
            c2.IsActive AS Couple2IsActive,
            c2.IsRemoved AS Couple2IsRemoved,
            c2.Player1Id AS Couple2Player1Id,
            c2.Player2Id AS Couple2Player2Id,

            p1.Name AS P1Name,
            p1.LastName AS P1LastName,
            p1.Birthdate AS P1Birthdate,
            p1.Category AS P1Category,

            p2.Name AS P2Name,
            p2.LastName AS P2LastName,
            p2.Birthdate AS P2Birthdate,
            p2.Category AS P2Category,

            p3.Name AS P3Name,
            p3.LastName AS P3LastName,
            p3.Birthdate AS P3Birthdate,
            p3.Category AS P3Category,

            p4.Name AS P4Name,
            p4.LastName AS P4LastName,
            p4.Birthdate AS P4Birthdate,
            p4.Category AS P4Category

        FROM TournamentMatch tm
        INNER JOIN TournamentRound tr
            ON tr.RoundId = tm.RoundId
        INNER JOIN Tournament t
            ON t.TournamentId = tr.TournamentId

        INNER JOIN Couple c1
            ON c1.CoupleId = tm.CoupleId1
        INNER JOIN Couple c2
            ON c2.CoupleId = tm.CoupleId2

        INNER JOIN Player p1
            ON p1.PlayerId = c1.Player1Id
        INNER JOIN Player p2
            ON p2.PlayerId = c1.Player2Id
        INNER JOIN Player p3
            ON p3.PlayerId = c2.Player1Id
        INNER JOIN Player p4
            ON p4.PlayerId = c2.Player2Id

        WHERE tm.IsRemoved = 0
        """;

    @Override
    public TournamentMatch findById(Long id) throws SQLException {

        String sql = SELECT_BASE + """
            AND tm.MatchId = ?
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
    public List<TournamentMatch> findAll() throws SQLException {

        String sql = SELECT_BASE + """
            ORDER BY tm.MatchDate DESC, tm.MatchId DESC
            """;

        List<TournamentMatch> matches = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                matches.add(map(rs));
            }
        }

        return matches;
    }

    @Override
    public List<TournamentMatch> findByRound(Long roundId) throws SQLException {

        String sql = SELECT_BASE + """
            AND tm.RoundId = ?
            ORDER BY tm.MatchDate DESC, tm.MatchId DESC
            """;

        List<TournamentMatch> matches = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, roundId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    matches.add(map(rs));
                }
            }
        }

        return matches;
    }

    @Override
    public void insert(TournamentMatch match) throws SQLException {

        try (Connection con = dataSource.getConnection()) {
            insert(con, match);
        }
    }

    @Override
    public void insert(Connection con, TournamentMatch match) throws SQLException {

        String sql = """
            INSERT INTO TournamentMatch
                (RoundId, CoupleId1, CoupleId2, MatchDate,
                 Description, Court, Stage, MatchStatus,
                 IsActive, UserAdded, DateAdded)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps = con.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, match.getRound().getSurrogate());
            ps.setLong(2, match.getCouple1().getSurrogate());
            ps.setLong(3, match.getCouple2().getSurrogate());

            if (match.getMatchDate() != null) {
                ps.setTimestamp(4, Timestamp.valueOf(match.getMatchDate()));
            } else {
                ps.setNull(4, java.sql.Types.TIMESTAMP);
            }

            ps.setString(5, match.getDescription());
            ps.setString(6, match.getCourt());
            ps.setString(7, match.getStage());
            ps.setString(8, match.getMatchStatus());
            ps.setBoolean(9, match.isActive());

            Employee user = SessionContext.getLoggedUser();

            if (user != null) {
                ps.setObject(10, user.getSurrogate());
            } else {
                ps.setNull(10, java.sql.Types.BIGINT);
            }

            LocalDateTime dateAdded = match.getDateAdded() != null
                    ? match.getDateAdded()
                    : java.time.LocalDateTime.now();

            ps.setTimestamp(11, Timestamp.valueOf(dateAdded));

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    match.setSurrogate(keys.getLong(1));
                }
            }
        }
    }

    @Override
    public void update(TournamentMatch match) throws SQLException {

        try (Connection con = dataSource.getConnection()) {
            update(con, match);
        }
    }

    @Override
    public void update(Connection con, TournamentMatch match) throws SQLException {

        String sql = """
            UPDATE TournamentMatch
            SET RoundId = ?,
                CoupleId1 = ?,
                CoupleId2 = ?,
                MatchDate = ?,
                Description = ?,
                Court = ?,
                Stage = ?,
                MatchStatus = ?,
                IsActive = ?,
                UserChanged = ?,
                DateChanged = CURRENT_TIMESTAMP
            WHERE MatchId = ?
              AND IsRemoved = 0
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, match.getRound().getSurrogate());
            ps.setLong(2, match.getCouple1().getSurrogate());
            ps.setLong(3, match.getCouple2().getSurrogate());

            if (match.getMatchDate() != null) {
                ps.setTimestamp(4, Timestamp.valueOf(match.getMatchDate()));
            } else {
                ps.setNull(4, java.sql.Types.TIMESTAMP);
            }

            ps.setString(5, match.getDescription());
            ps.setString(6, match.getCourt());
            ps.setString(7, match.getStage());
            ps.setString(8, match.getMatchStatus());
            ps.setBoolean(9, match.isActive());

            Employee user = SessionContext.getLoggedUser();

            if (user != null) {
                ps.setObject(10, user.getSurrogate());
            } else {
                ps.setNull(10, java.sql.Types.BIGINT);
            }

            ps.setLong(11, match.getSurrogate());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Long matchId) throws SQLException {

        try (Connection con = dataSource.getConnection()) {
            delete(con, matchId);
        }
    }

    @Override
    public void delete(Connection con, Long matchId) throws SQLException {

        String sql = """
            UPDATE TournamentMatch
            SET IsRemoved = 1,
                UserChanged = ?,
                DateChanged = CURRENT_TIMESTAMP
            WHERE MatchId = ?
              AND IsRemoved = 0
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            Employee user = SessionContext.getLoggedUser();

            if (user != null) {
                ps.setObject(1, user.getSurrogate());
            } else {
                ps.setNull(1, java.sql.Types.BIGINT);
            }

            ps.setLong(2, matchId);

            ps.executeUpdate();
        }
    }

    private TournamentMatch map(ResultSet rs) throws SQLException {

        TournamentMatch match = new TournamentMatch();

        match.setSurrogate(rs.getLong("MatchId"));
        match.setMatchDate(
                rs.getTimestamp("MatchDate") != null
                        ? rs.getTimestamp("MatchDate").toLocalDateTime()
                        : null
        );
        match.setDescription(rs.getString("MatchDescription"));
        match.setCourt(rs.getString("Court"));
        match.setStage(rs.getString("Stage"));
        match.setMatchStatus(rs.getString("MatchStatus"));
        match.setActive(rs.getBoolean("MatchIsActive"));
        match.setRemoved(rs.getBoolean("MatchIsRemoved"));

        Timestamp matchDateAdded = rs.getTimestamp("MatchDateAdded");
        if (matchDateAdded != null) {
            match.setDateAdded(matchDateAdded.toLocalDateTime());
        }

        Timestamp matchDateChanged = rs.getTimestamp("MatchDateChanged");
        if (matchDateChanged != null) {
            match.setDateChanged(matchDateChanged.toLocalDateTime());
        }

        // Torneo
        Tournament tournament = new Tournament();

        tournament.setSurrogate(rs.getLong("TournamentId"));
        tournament.setName(rs.getString("TournamentName"));
        tournament.setCategory(Category.fromValue(
                rs.getInt("TournamentCategory")));
        tournament.setActive(rs.getBoolean("TournamentIsActive"));
        tournament.setRemoved(rs.getBoolean("TournamentIsRemoved"));

        Date tournamentDate = rs.getDate("TournamentDate");
        if (tournamentDate != null) {
            tournament.setTournamentDate(tournamentDate.toLocalDate());
        }

        Date tournamentEndDate = rs.getDate("TournamentEndDate");
        if (tournamentEndDate != null) {
            tournament.setEndDate(tournamentEndDate.toLocalDate());
        }

        Timestamp tournamentDateAdded = rs.getTimestamp("TournamentDateAdded");
        if (tournamentDateAdded != null) {
            tournament.setDateAdded(tournamentDateAdded.toLocalDateTime());
        }

        Timestamp tournamentDateChanged = rs.getTimestamp("TournamentDateChanged");
        if (tournamentDateChanged != null) {
            tournament.setDateChanged(tournamentDateChanged.toLocalDateTime());
        }

        // Jornada
        TournamentRound round = new TournamentRound();

        round.setSurrogate(rs.getLong("RoundId"));
        round.setTournament(tournament);
        round.setRoundNumber(rs.getInt("RoundNumber"));
        round.setDescription(rs.getString("RoundDescription"));
        round.setRoundStatus(rs.getString("RoundStatus"));
        round.setActive(rs.getBoolean("RoundIsActive"));
        round.setRemoved(rs.getBoolean("RoundIsRemoved"));

        Date roundStartDate = rs.getDate("RoundStartDate");
        if (roundStartDate != null) {
            round.setStartDate(roundStartDate.toLocalDate());
        }

        Date roundEndDate = rs.getDate("RoundEndDate");
        if (roundEndDate != null) {
            round.setEndDate(roundEndDate.toLocalDate());
        }

        Timestamp roundDateAdded = rs.getTimestamp("RoundDateAdded");
        if (roundDateAdded != null) {
            round.setDateAdded(roundDateAdded.toLocalDateTime());
        }

        Timestamp roundDateChanged = rs.getTimestamp("RoundDateChanged");
        if (roundDateChanged != null) {
            round.setDateChanged(roundDateChanged.toLocalDateTime());
        }

        match.setRound(round);

        // Pareja 1
        Couple couple1 = new Couple();

        couple1.setSurrogate(rs.getLong("CoupleId1"));
        couple1.setName(rs.getString("Couple1Name"));
        couple1.setActive(rs.getBoolean("Couple1IsActive"));
        couple1.setRemoved(rs.getBoolean("Couple1IsRemoved"));

        Player player1 = mapPlayer(rs, "P1", "Couple1Player1Id");
        Player player2 = mapPlayer(rs, "P2", "Couple1Player2Id");

        couple1.setPlayer1(player1);
        couple1.setPlayer2(player2);

        // Pareja 2
        Couple couple2 = new Couple();

        couple2.setSurrogate(rs.getLong("CoupleId2"));
        couple2.setName(rs.getString("Couple2Name"));
        couple2.setActive(rs.getBoolean("Couple2IsActive"));
        couple2.setRemoved(rs.getBoolean("Couple2IsRemoved"));

        Player player3 = mapPlayer(rs, "P3", "Couple2Player1Id");
        Player player4 = mapPlayer(rs, "P4", "Couple2Player2Id");

        couple2.setPlayer1(player3);
        couple2.setPlayer2(player4);

        match.setCouple1(couple1);
        match.setCouple2(couple2);

        return match;
    }

    private Player mapPlayer(
            ResultSet rs,
            String prefix,
            String idColumn) throws SQLException {

        Player player = new Player();

        player.setSurrogate(rs.getLong(idColumn));
        player.setName(rs.getString(prefix + "Name"));
        player.setLastname(rs.getString(prefix + "LastName"));
        player.setCategory(rs.getInt(prefix + "Category"));

        Date birthdate = rs.getDate(prefix + "Birthdate");
        if (birthdate != null) {
            player.setBirthdate(birthdate.toLocalDate());
        }

        return player;
    }
}