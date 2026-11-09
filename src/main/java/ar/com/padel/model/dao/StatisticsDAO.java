package ar.com.padel.model.dao;

import java.sql.SQLException;
import java.util.List;

import ar.com.padel.dto.TournamentStatisticsRow;
import ar.com.padel.dto.PlayerStatisticsRow;
import ar.com.padel.dto.RoundStatisticsRow;
import ar.com.padel.dto.StatisticsSummary;

public interface StatisticsDAO {

    StatisticsSummary getGeneralSummary() throws SQLException;
    List<TournamentStatisticsRow> getTournamentsStatistics() throws SQLException;
    List<RoundStatisticsRow> getRoundsStatistics(Long tournamentId) throws SQLException;
    List<PlayerStatisticsRow> getPlayersStatistics(Long tournamentId) throws SQLException;

}
