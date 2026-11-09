package ar.com.padel.model.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import ar.com.padel.model.entity.MatchSet;

public interface MatchSetDAO {

    MatchSet findById(Long id) throws SQLException;

    List<MatchSet> findByMatch(Long matchId) throws SQLException;

    void insert(MatchSet matchSet) throws SQLException;

    void update(MatchSet matchSet) throws SQLException;

    void delete(Long matchSetId) throws SQLException;

    void insert(Connection con, MatchSet matchSet) throws SQLException;

    void update(Connection con, MatchSet matchSet) throws SQLException;

    void delete(Connection con, Long matchSetId) throws SQLException;
}