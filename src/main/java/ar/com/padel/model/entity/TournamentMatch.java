
package ar.com.padel.model.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TournamentMatch extends AuditInformation {

    private Long surrogate;

    private TournamentRound round;

    private Couple couple1;

    private Couple couple2;

    private LocalDateTime matchDate;

    private String description;

    private String court;

    private String stage;

    private String matchStatus;

    private boolean active;

    private boolean removed;

    // Constructor con todos los datos del partido.
    public TournamentMatch(Long surrogate, TournamentRound round,
            Couple couple1, Couple couple2, LocalDateTime matchDate,
            String description, String court, String stage, String matchStatus,
            LocalDateTime dateAdded, LocalDateTime dateChanged,
            Employee userAdded, Employee userChanged,
            boolean active, boolean removed) {

        super(dateAdded, dateChanged, userAdded, userChanged);

        this.surrogate = surrogate;
        this.round = round;
        this.couple1 = couple1;
        this.couple2 = couple2;
        this.matchDate = matchDate;
        this.description = description;
        this.court = court;
        this.stage = stage;
        this.matchStatus = matchStatus;
        this.active = active;
        this.removed = removed;
    }

    // Constructor para nuevos partidos con datos de auditoría.
    public TournamentMatch(Employee userAdded, Employee userChanged) {
        super(userAdded, userChanged);
    }

    // Constructor por defecto.
    public TournamentMatch() {
        super();
    }

    public Long getSurrogate() {
        return surrogate;
    }

    public void setSurrogate(Long surrogate) {
        this.surrogate = surrogate;
    }

    public TournamentRound getRound() {
        return round;
    }

    public void setRound(TournamentRound round) {
        this.round = round;
    }

    public Couple getCouple1() {
        return couple1;
    }

    public void setCouple1(Couple couple1) {
        this.couple1 = couple1;
    }

    public Couple getCouple2() {
        return couple2;
    }

    public void setCouple2(Couple couple2) {
        this.couple2 = couple2;
    }

    public LocalDateTime getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(LocalDateTime matchDate) {
        this.matchDate = matchDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCourt() {
        return court;
    }

    public void setCourt(String court) {
        this.court = court;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getMatchStatus() {
        return matchStatus;
    }

    public void setMatchStatus(String matchStatus) {
        this.matchStatus = matchStatus;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }
    
    public Couple getWinner(List<MatchSet> sets) {

        if (sets == null || sets.isEmpty()) {
            return null;
        }

        int setsCouple1 = 0;
        int setsCouple2 = 0;
        int gamesCouple1 = 0;
        int gamesCouple2 = 0;

        for (MatchSet set : sets) {

            int winner = set.getWinner();

            if (winner == 1) {
                setsCouple1++;
            } else if (winner == 2) {
                setsCouple2++;
            }

            gamesCouple1 += set.getGamesCouple1();
            gamesCouple2 += set.getGamesCouple2();
        }

        if (setsCouple1 > setsCouple2) {
            return couple1;
        }

        if (setsCouple2 > setsCouple1) {
            return couple2;
        }

        if (gamesCouple1 > gamesCouple2) {
            return couple1;
        }

        if (gamesCouple2 > gamesCouple1) {
            return couple2;
        }

        return null;
    }
    
    public String getResult(List<MatchSet> sets) {

        if (sets == null || sets.isEmpty()) {
            return "Sin resultado";
        }

        List<MatchSet> orderedSets = new ArrayList<>(sets);

        orderedSets.sort(
            Comparator.comparingInt(MatchSet::getSetNumber)
        );

        StringBuilder result = new StringBuilder();

        for (MatchSet set : orderedSets) {

            if (result.length() > 0) {
                result.append(" / ");
            }

            result.append(set.getGamesCouple1())
                  .append("-")
                  .append(set.getGamesCouple2());

            if (set.getTieBreakLoserPoints() != null) {
                result.append(" (")
                      .append(set.getTieBreakLoserPoints())
                      .append(")");
            }
        }

        return result.toString();
    }

}
