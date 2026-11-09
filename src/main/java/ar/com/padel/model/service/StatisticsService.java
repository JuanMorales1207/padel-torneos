package ar.com.padel.model.service;

import java.sql.SQLException;
import java.util.List;

import ar.com.padel.dto.TournamentStatisticsRow;
import ar.com.padel.dto.RoundStatisticsRow;
import ar.com.padel.model.dao.StatisticsDAO;
import ar.com.padel.dto.StatisticsSummary;
import ar.com.padel.dto.PlayerStatisticsRow;

public class StatisticsService {

    private final StatisticsDAO statisticsDAO;

    public StatisticsService(StatisticsDAO statisticsDAO) {
        this.statisticsDAO = statisticsDAO;
    }

    public StatisticsSummary getGeneralSummary() throws SQLException {
        return statisticsDAO.getGeneralSummary();
    }
    
    public List<TournamentStatisticsRow> getTournamentsStatistics() throws SQLException {
        return statisticsDAO.getTournamentsStatistics();
    }

    public List<RoundStatisticsRow> getRoundsStatistics(Long tournamentId) throws SQLException {
        return statisticsDAO.getRoundsStatistics(tournamentId);
    }
    
    public List<PlayerStatisticsRow> getPlayersStatistics(Long tournamentId)
            throws SQLException {

        return statisticsDAO.getPlayersStatistics(tournamentId);
    }
}
