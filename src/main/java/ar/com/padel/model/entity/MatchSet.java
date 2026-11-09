package ar.com.padel.model.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MatchSet extends AuditInformation {

    private Long surrogate;
    private TournamentMatch match;
    private int setNumber;
    private int gamesCouple1;
    private int gamesCouple2;
    private Integer tieBreakLoserPoints;
    private boolean removed;

    // Constructor con todos los datos del set.
    public MatchSet(Long surrogate, TournamentMatch match, int setNumber,
            int gamesCouple1, int gamesCouple2, Integer tieBreakLoserPoints,
            java.time.LocalDateTime dateAdded, java.time.LocalDateTime dateChanged,
            Employee userAdded, Employee userChanged, boolean removed) {

        super(dateAdded, dateChanged, userAdded, userChanged);

        this.surrogate = surrogate;
        this.match = match;
        this.setNumber = setNumber;
        this.gamesCouple1 = gamesCouple1;
        this.gamesCouple2 = gamesCouple2;
        this.tieBreakLoserPoints = tieBreakLoserPoints;
        this.removed = removed;
    }

    // Constructor para nuevos sets con datos de auditoría.
    public MatchSet(Employee userAdded, Employee userChanged) {
        super(userAdded, userChanged);
    }

    // Constructor por defecto.
    public MatchSet() {
        super();
    }

    public Long getSurrogate() {
        return surrogate;
    }

    public void setSurrogate(Long surrogate) {
        this.surrogate = surrogate;
    }

    public TournamentMatch getMatch() {
        return match;
    }

    public void setMatch(TournamentMatch match) {
        this.match = match;
    }

    public int getSetNumber() {
        return setNumber;
    }

    public void setSetNumber(int setNumber) {
        this.setNumber = setNumber;
    }

    public int getGamesCouple1() {
        return gamesCouple1;
    }

    public void setGamesCouple1(int gamesCouple1) {
        this.gamesCouple1 = gamesCouple1;
    }

    public int getGamesCouple2() {
        return gamesCouple2;
    }

    public void setGamesCouple2(int gamesCouple2) {
        this.gamesCouple2 = gamesCouple2;
    }

    public Integer getTieBreakLoserPoints() {
        return tieBreakLoserPoints;
    }

    public void setTieBreakLoserPoints(Integer tieBreakLoserPoints) {
        this.tieBreakLoserPoints = tieBreakLoserPoints;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }
    

	public int getWinner() {
	
	    if (gamesCouple1 == gamesCouple2) {
	        return 0;
	    }
	
	    // Victoria por diferencia de dos games.
	    if (gamesCouple1 >= 6 && gamesCouple1 - gamesCouple2 >= 2) {
	        return 1;
	    }
	
	    if (gamesCouple2 >= 6 && gamesCouple2 - gamesCouple1 >= 2) {
	        return 2;
	    }
	
	    // Victoria por tie-break (7-6).
	    if (gamesCouple1 == 7 && gamesCouple2 == 6
	            && tieBreakLoserPoints != null) {
	        return 1;
	    }
	
	    if (gamesCouple2 == 7 && gamesCouple1 == 6
	            && tieBreakLoserPoints != null) {
	        return 2;
	    }	
	    return 0;
	}
	
	public String getResult(List<MatchSet> sets) {

	    if (sets == null || sets.isEmpty()) {
	        return "Sin resultado";
	    }

	    List<MatchSet> orderedSets = new ArrayList<>(sets);
	    orderedSets.sort(Comparator.comparingInt(MatchSet::getSetNumber));
	    StringBuilder result = new StringBuilder();
	    for (MatchSet set : orderedSets) {

	        if (result.length() > 0) {
	            result.append(" / ");
	        }

	        result.append(set.getGamesCouple1()).append("-").append(set.getGamesCouple2());

	        if (set.getTieBreakLoserPoints() != null) {
	            result.append(" (").append(set.getTieBreakLoserPoints()).append(")");
	        }
	    }
	    return result.toString();
	}

}