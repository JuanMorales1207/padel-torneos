package ar.com.padel.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ar.com.padel.dto.PlayerStatisticsRow;
import ar.com.padel.dto.StatisticsSummary;
import ar.com.padel.dto.TournamentStatisticsRow;
import ar.com.padel.dto.RoundStatisticsRow;

import javax.sql.DataSource;


public class StatisticsDAOImpl implements StatisticsDAO {

    private final DataSource dataSource;

    public StatisticsDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public StatisticsSummary getGeneralSummary() throws SQLException {

        String sql = """
            SELECT
                (SELECT COUNT(*)
                 FROM Tournament t
                 WHERE t.IsRemoved = 0) AS TournamentsCount,

                (SELECT COUNT(*)
                 FROM TournamentRound tr
                 INNER JOIN Tournament t
                    ON t.TournamentId = tr.TournamentId
                 WHERE tr.IsRemoved = 0
                   AND t.IsRemoved = 0) AS RoundsCount,

                (SELECT COUNT(*)
                 FROM Player p
                 WHERE p.IsRemoved = 0) AS PlayersCount,

                (SELECT COUNT(*)
                 FROM Couple c
                 INNER JOIN Player p1
                    ON p1.PlayerId = c.Player1Id
                 INNER JOIN Player p2
                    ON p2.PlayerId = c.Player2Id
                 WHERE c.IsRemoved = 0
                   AND p1.IsRemoved = 0
                   AND p2.IsRemoved = 0) AS CouplesCount,

                (SELECT COUNT(*)
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
                   AND tr.IsRemoved = 0
                   AND t.IsRemoved = 0
                   AND c1.IsRemoved = 0
                   AND c2.IsRemoved = 0
                   AND p1.IsRemoved = 0
                   AND p2.IsRemoved = 0
                   AND p3.IsRemoved = 0
                   AND p4.IsRemoved = 0) AS MatchesCount
            """;

        StatisticsSummary summary = new StatisticsSummary();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                summary.setTournamentsCount(
                    rs.getInt("TournamentsCount"));

                summary.setRoundsCount(
                    rs.getInt("RoundsCount"));

                summary.setPlayersCount(
                    rs.getInt("PlayersCount"));

                summary.setCouplesCount(
                    rs.getInt("CouplesCount"));

                summary.setMatchesCount(
                    rs.getInt("MatchesCount"));
            }
        }

        return summary;
    }
    
    @Override
    public List<TournamentStatisticsRow> getTournamentsStatistics() throws SQLException {

        String sql = """
            SELECT
                t.TournamentId,
                t.Name,
                t.TournamentDate,
                t.EndDate
            FROM Tournament t
            WHERE t.IsRemoved = 0
            ORDER BY t.TournamentDate DESC, t.Name
            """;

        List<TournamentStatisticsRow> tournaments = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                TournamentStatisticsRow row = new TournamentStatisticsRow();

                row.setTournamentId(rs.getLong("TournamentId"));
                row.setTournamentName(rs.getString("Name"));

                row.setTournamentDate(
                    rs.getDate("TournamentDate") != null
                        ? rs.getDate("TournamentDate").toLocalDate()
                        : null
                );

                row.setEndDate(
                    rs.getDate("EndDate") != null
                        ? rs.getDate("EndDate").toLocalDate()
                        : null
                );

                tournaments.add(row);
            }
        }

        return tournaments;
    }
    
    @Override
    public List<RoundStatisticsRow> getRoundsStatistics(Long tournamentId) throws SQLException {

        String sql = """
            SELECT
                tr.RoundId,
                tr.RoundNumber,
                tr.Description,
                tr.StartDate,
                tr.EndDate,
                COUNT(tm.MatchId) AS MatchesCount
            FROM TournamentRound tr
            INNER JOIN Tournament t
                ON t.TournamentId = tr.TournamentId
            LEFT JOIN TournamentMatch tm
                ON tm.RoundId = tr.RoundId
                AND tm.IsRemoved = 0
                AND EXISTS (
                    SELECT 1
                    FROM Couple c1
                    INNER JOIN Player p1
                        ON p1.PlayerId = c1.Player1Id
                        AND p1.IsRemoved = 0
                    INNER JOIN Player p2
                        ON p2.PlayerId = c1.Player2Id
                        AND p2.IsRemoved = 0
                    WHERE c1.CoupleId = tm.CoupleId1
                      AND c1.IsRemoved = 0
                )
                AND EXISTS (
                    SELECT 1
                    FROM Couple c2
                    INNER JOIN Player p3
                        ON p3.PlayerId = c2.Player1Id
                        AND p3.IsRemoved = 0
                    INNER JOIN Player p4
                        ON p4.PlayerId = c2.Player2Id
                        AND p4.IsRemoved = 0
                    WHERE c2.CoupleId = tm.CoupleId2
                      AND c2.IsRemoved = 0
                )
            WHERE tr.TournamentId = ?
              AND tr.IsRemoved = 0
              AND t.IsRemoved = 0
            GROUP BY
                tr.RoundId,
                tr.RoundNumber,
                tr.Description,
                tr.StartDate,
                tr.EndDate
            ORDER BY tr.RoundNumber
            """;

        List<RoundStatisticsRow> rounds = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, tournamentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RoundStatisticsRow row = new RoundStatisticsRow();

                    row.setRoundId(rs.getLong("RoundId"));
                    row.setRoundNumber(rs.getInt("RoundNumber"));
                    row.setDescription(rs.getString("Description"));

                    row.setStartDate(
                        rs.getDate("StartDate") != null
                            ? rs.getDate("StartDate").toLocalDate()
                            : null
                    );

                    row.setEndDate(
                        rs.getDate("EndDate") != null
                            ? rs.getDate("EndDate").toLocalDate()
                            : null
                    );

                    row.setMatchesCount(rs.getInt("MatchesCount"));

                    rounds.add(row);
                }
            }
        }

        return rounds;
    }
    
    @Override
    public List<PlayerStatisticsRow> getPlayersStatistics(Long tournamentId)
            throws SQLException {

        String sql = """
            WITH ValidMatches AS (
                SELECT
                    tm.MatchId,
                    tm.CoupleId1,
                    tm.CoupleId2,
                    c1.Player1Id AS P1,
                    c1.Player2Id AS P2,
                    c2.Player1Id AS P3,
                    c2.Player2Id AS P4
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
                  AND tr.IsRemoved = 0
                  AND t.IsRemoved = 0
                  AND c1.IsRemoved = 0
                  AND c2.IsRemoved = 0
                  AND p1.IsRemoved = 0
                  AND p2.IsRemoved = 0
                  AND p3.IsRemoved = 0
                  AND p4.IsRemoved = 0
                  AND (? IS NULL OR t.TournamentId = ?)
            ),

            SetStatistics AS (
                SELECT
                    ms.MatchId,
                    SUM(CASE
                        WHEN ms.GamesCouple1 >= 6
                         AND ms.GamesCouple1 - ms.GamesCouple2 >= 2
                            THEN 1
                        WHEN ms.GamesCouple1 = 7
                         AND ms.GamesCouple2 = 6
                         AND ms.TieBreakLoserPoints IS NOT NULL
                            THEN 1
                        ELSE 0
                    END) AS SetsWon1,

                    SUM(CASE
                        WHEN ms.GamesCouple2 >= 6
                         AND ms.GamesCouple2 - ms.GamesCouple1 >= 2
                            THEN 1
                        WHEN ms.GamesCouple2 = 7
                         AND ms.GamesCouple1 = 6
                         AND ms.TieBreakLoserPoints IS NOT NULL
                            THEN 1
                        ELSE 0
                    END) AS SetsWon2,

                    SUM(ms.GamesCouple1) AS Games1,
                    SUM(ms.GamesCouple2) AS Games2

                FROM MatchSet ms
                WHERE ms.IsRemoved = 0
                GROUP BY ms.MatchId
            ),

            MatchResults AS (
                SELECT
                    vm.*,
                    ss.Games1,
                    ss.Games2,

                    CASE
                        WHEN ss.SetsWon1 > ss.SetsWon2
                            THEN vm.CoupleId1
                        WHEN ss.SetsWon2 > ss.SetsWon1
                            THEN vm.CoupleId2
                        WHEN ss.Games1 > ss.Games2
                            THEN vm.CoupleId1
                        WHEN ss.Games2 > ss.Games1
                            THEN vm.CoupleId2
                        ELSE NULL
                    END AS WinnerCoupleId

                FROM ValidMatches vm
                INNER JOIN SetStatistics ss
                    ON ss.MatchId = vm.MatchId
            ),

            PlayerMatches AS (
                SELECT
                    mr.P1 AS PlayerId,
                    mr.MatchId,
                    mr.CoupleId1 AS CoupleId,
                    mr.WinnerCoupleId,
                    mr.Games1 AS GamesWon,
                    mr.Games2 AS GamesLost
                FROM MatchResults mr

                UNION ALL

                SELECT
                    mr.P2,
                    mr.MatchId,
                    mr.CoupleId1,
                    mr.WinnerCoupleId,
                    mr.Games1,
                    mr.Games2
                FROM MatchResults mr

                UNION ALL

                SELECT
                    mr.P3,
                    mr.MatchId,
                    mr.CoupleId2,
                    mr.WinnerCoupleId,
                    mr.Games2,
                    mr.Games1
                FROM MatchResults mr

                UNION ALL

                SELECT
                    mr.P4,
                    mr.MatchId,
                    mr.CoupleId2,
                    mr.WinnerCoupleId,
                    mr.Games2,
                    mr.Games1
                FROM MatchResults mr
            )

            SELECT
                p.PlayerId,
                p.Name,
                p.LastName,

                COUNT(*) AS MatchesPlayed,

                SUM(CASE
                    WHEN pm.WinnerCoupleId = pm.CoupleId THEN 1
                    ELSE 0
                END) AS MatchesWon,

                SUM(CASE
                    WHEN pm.WinnerCoupleId IS NOT NULL
                     AND pm.WinnerCoupleId <> pm.CoupleId THEN 1
                    ELSE 0
                END) AS MatchesLost,

                SUM(pm.GamesWon) AS GamesWon,
                SUM(pm.GamesLost) AS GamesLost

            FROM PlayerMatches pm
            INNER JOIN Player p
                ON p.PlayerId = pm.PlayerId

            GROUP BY
                p.PlayerId,
                p.Name,
                p.LastName

            ORDER BY
                MatchesWon DESC,
                GamesWon DESC,
                p.LastName,
                p.Name
            """;

        List<PlayerStatisticsRow> players = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (tournamentId != null) {
                ps.setLong(1, tournamentId);
                ps.setLong(2, tournamentId);
            } else {
                ps.setNull(1, java.sql.Types.BIGINT);
                ps.setNull(2, java.sql.Types.BIGINT);
            }

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    PlayerStatisticsRow row = new PlayerStatisticsRow();

                    row.setPlayerId(rs.getLong("PlayerId"));
                    row.setPlayerName(
                            rs.getString("Name") + " "
                            + rs.getString("LastName")
                    );

                    row.setMatchesPlayed(rs.getInt("MatchesPlayed"));
                    row.setMatchesWon(rs.getInt("MatchesWon"));
                    row.setMatchesLost(rs.getInt("MatchesLost"));
                    row.setGamesWon(rs.getInt("GamesWon"));
                    row.setGamesLost(rs.getInt("GamesLost"));

                    int matchesWithResult =
                            row.getMatchesWon() + row.getMatchesLost();

                    if (matchesWithResult > 0) {
                        row.setMatchesWonPercentage(
                                row.getMatchesWon() * 100.0
                                / matchesWithResult
                        );
                    }

                    int totalGames =
                            row.getGamesWon() + row.getGamesLost();

                    if (totalGames > 0) {
                        row.setGamesWonPercentage(
                                row.getGamesWon() * 100.0
                                / totalGames
                        );
                    }

                    players.add(row);
                }
            }
        }

        return players;
    }
    
}
