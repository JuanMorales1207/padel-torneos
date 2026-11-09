package ar.com.padel.model.service;

import ar.com.padel.dto.EmployeeRow;
import ar.com.padel.model.dao.EmployeeDAO;
import ar.com.padel.model.entity.Employee;
import ar.com.padel.model.entity.RoleType;
import ar.com.padel.security.PasswordUtil;
import ar.com.padel.security.context.SessionContext;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class EmployeeService {

    private final EmployeeDAO employeeDAO;

    public EmployeeService(EmployeeDAO employeeDAO) {
        this.employeeDAO = employeeDAO;
    }

    public void create(Employee employee, String plainPassword) throws SQLException {
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }

        employee.setPasswordHash(PasswordUtil.hash(plainPassword));
        employee.setFailedAttempts(0);

        Employee logged = SessionContext.getLoggedUser();
        employee.setUserAdded(logged);
        employee.setDateAdded(LocalDateTime.now());

        employeeDAO.insert(employee);
    }

    public void update(Employee employee, String plainPassword) throws SQLException {
        Employee current = employeeDAO.findById(employee.getSurrogate());

        if (current == null) {
            throw new IllegalArgumentException("El empleado no existe.");
        }

        if (plainPassword != null && !plainPassword.isBlank()) {
            employee.setPasswordHash(PasswordUtil.hash(plainPassword));
        } else {
            employee.setPasswordHash(current.getPasswordHash());
        }

        Employee logged = SessionContext.getLoggedUser();
        employee.setUserChanged(logged);
        employee.setDateChanged(LocalDateTime.now());

        employeeDAO.update(employee);
    }

    public Employee findById(Long id) throws SQLException {
        return employeeDAO.findById(id);
    }

    public Employee findByUsername(String username) throws SQLException {
        return employeeDAO.findByUsername(username);
    }

    public List<Employee> findAll() throws SQLException {
        return employeeDAO.findAll();
    }

    public List<EmployeeRow> findAllForGrid() throws SQLException {
        return employeeDAO.findAllForGrid();
    }

    public List<RoleType> findAllRoles() throws SQLException {
        return employeeDAO.findAllRoles();
    }

    public void delete(Long employeeId) throws SQLException {
        employeeDAO.delete(employeeId);
    }
    
    public void updateMyAccount(
            Employee employee,
            String currentPassword,
            String newPassword,
            String repeatPassword
    ) throws SQLException {

        Employee current = employeeDAO.findById(employee.getSurrogate());

        if (current == null) {
            throw new IllegalArgumentException("El empleado no existe.");
        }

        boolean changingPassword =
                (currentPassword != null && !currentPassword.isBlank())
                || (newPassword != null && !newPassword.isBlank())
                || (repeatPassword != null && !repeatPassword.isBlank());

        String passwordHash = null;

        if (changingPassword) {
            if (currentPassword == null || currentPassword.isBlank()
                    || newPassword == null || newPassword.isBlank()
                    || repeatPassword == null || repeatPassword.isBlank()) {
                throw new IllegalArgumentException(
                        "Para cambiar la contraseña, completá los tres campos."
                );
            }

            if (!PasswordUtil.matches(currentPassword, current.getPasswordHash())) {
                throw new IllegalArgumentException("La contraseña actual es incorrecta.");
            }

            if (!newPassword.equals(repeatPassword)) {
                throw new IllegalArgumentException("Las contraseñas nuevas no coinciden.");
            }

            passwordHash = PasswordUtil.hash(newPassword);
        }

        current.setName(employee.getName());
        current.setLastname(employee.getLastname());
        current.setBirthdate(employee.getBirthdate());
        current.setPasswordHash(passwordHash);

        Employee logged = SessionContext.getLoggedUser();
        current.setUserChanged(logged);
        current.setDateChanged(LocalDateTime.now());

        employeeDAO.updateMyAccount(current);

        SessionContext.setLoggedUser(current);
    }
    
    public String findUserIdByEmployeeId(Long employeeId) throws SQLException {
        return employeeDAO.findUserIdByEmployeeId(employeeId);
    }
    
}