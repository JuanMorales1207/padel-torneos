package ar.com.padel.model.dao;

import ar.com.padel.dto.EmployeeRow;
import ar.com.padel.model.entity.Employee;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public interface EmployeeDAO {

    Employee findById(Long id) throws SQLException;

    Employee findByUsername(String username) throws SQLException;

    Employee findByUserId(String username) throws SQLException;

    List<Employee> findAll() throws SQLException;

    void insert(Employee employee) throws SQLException;

    void update(Employee employee) throws SQLException;

    void updateLastLogin(Long employeeId, LocalDateTime lastLogin)
            throws SQLException;

    void updateFailedAttempts(Long employeeId, int attempts)
            throws SQLException;

    List<Employee> findOperators() throws SQLException;

    List<EmployeeRow> findAllForGrid() throws SQLException;

    void delete(Long employeeId) throws SQLException;
}
