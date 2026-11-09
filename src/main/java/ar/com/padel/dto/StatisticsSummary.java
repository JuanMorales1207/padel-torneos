package ar.com.padel.dto;

public class StatisticsSummary {

    private int tournamentsCount;
    private int finishedTournamentsCount;
    private int roundsCount;
    private int playersCount;
    private int couplesCount;
    private int matchesCount;

    public int getTournamentsCount() {
        return tournamentsCount;
    }

    public void setTournamentsCount(int tournamentsCount) {
        this.tournamentsCount = tournamentsCount;
    }

    public int getFinishedTournamentsCount() {
        return finishedTournamentsCount;
    }

    public void setFinishedTournamentsCount(int finishedTournamentsCount) {
        this.finishedTournamentsCount = finishedTournamentsCount;
    }

    public int getRoundsCount() {
        return roundsCount;
    }

    public void setRoundsCount(int roundsCount) {
        this.roundsCount = roundsCount;
    }

    public int getPlayersCount() {
        return playersCount;
    }

    public void setPlayersCount(int playersCount) {
        this.playersCount = playersCount;
    }

    public int getCouplesCount() {
        return couplesCount;
    }

    public void setCouplesCount(int couplesCount) {
        this.couplesCount = couplesCount;
    }

    public int getMatchesCount() {
        return matchesCount;
    }

    public void setMatchesCount(int matchesCount) {
        this.matchesCount = matchesCount;
    }
}
