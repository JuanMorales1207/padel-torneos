package ar.com.padel.dto;

public class PlayerStatisticsRow {

    private Long playerId;
    private String playerName;
    private int matchesPlayed;
    private int matchesWon;
    private int matchesLost;
    private int gamesWon;
    private int gamesLost;
    private double matchesWonPercentage;
    private double gamesWonPercentage;

    // Getters y setters

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getMatchesPlayed() {
        return matchesPlayed;
    }

    public void setMatchesPlayed(int matchesPlayed) {
        this.matchesPlayed = matchesPlayed;
    }

    public int getMatchesWon() {
        return matchesWon;
    }

    public void setMatchesWon(int matchesWon) {
        this.matchesWon = matchesWon;
    }

    public int getMatchesLost() {
        return matchesLost;
    }

    public void setMatchesLost(int matchesLost) {
        this.matchesLost = matchesLost;
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
    }

    public int getGamesLost() {
        return gamesLost;
    }

    public void setGamesLost(int gamesLost) {
        this.gamesLost = gamesLost;
    }

    public double getMatchesWonPercentage() {
        return matchesWonPercentage;
    }

    public void setMatchesWonPercentage(double matchesWonPercentage) {
        this.matchesWonPercentage = matchesWonPercentage;
    }

    public double getGamesWonPercentage() {
        return gamesWonPercentage;
    }

    public void setGamesWonPercentage(double gamesWonPercentage) {
        this.gamesWonPercentage = gamesWonPercentage;
    }
}