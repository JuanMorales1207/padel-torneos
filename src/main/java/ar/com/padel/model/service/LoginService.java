
package ar.com.padel.model.service;

import java.sql.SQLException;
import java.time.LocalDateTime;

import ar.com.padel.model.dao.EmployeeDAO;
import ar.com.padel.model.entity.Employee;
import ar.com.padel.security.PasswordUtil;
import ar.com.padel.security.context.SessionContext;

public class LoginService {

    private final EmployeeDAO employeeDAO;

    public LoginService(EmployeeDAO employeeDAO) {
        this.employeeDAO = employeeDAO;
    }

    public Employee login(String username, String plainPassword) throws SQLException {

        Employee employee = employeeDAO.findByUserId(username);

        if (employee == null) {
            throw new RuntimeException("Usuario inexistente");
        }

        if (!employee.getEnabled()) {
            throw new RuntimeException("Usuario deshabilitado");
        }

        if (!PasswordUtil.matches(plainPassword, employee.getPasswordHash())) {
            employeeDAO.updateFailedAttempts(employee.getSurrogate(), employee.getFailedAttempts() + 1);
            throw new RuntimeException("Contraseña incorrecta");
        }

        // Login exitoso
        employeeDAO.updateLastLogin(employee.getSurrogate(), LocalDateTime.now());
        employeeDAO.updateFailedAttempts(employee.getSurrogate(), 0);
        SessionContext.setLoggedUser(employee);

        return employee;
    }

    public void logout() {
        SessionContext.clear();
    }
}
