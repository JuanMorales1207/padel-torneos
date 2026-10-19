package ar.com.padel.model.dao;

import ar.com.padel.dto.EmployeeRow;
import ar.com.padel.model.entity.Employee;
import ar.com.padel.model.entity.RoleType;
import ar.com.padel.security.context.SessionContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAOImpl implements EmployeeDAO {

    private final DataSource dataSource;

    public EmployeeDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Employee findById(Long id) throws SQLException {
        String sql = """
            SELECT e.*, r.LongDescription AS RoleDescription,
                   r.ShortDescription AS RoleShortDescription
            FROM Employee e
            LEFT JOIN RoleType r ON r.RoleTypeId = e.RoleTypeId
            WHERE e.EmployeeId = ?
              AND e.IsRemoved = 0
            """;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    @Override
    public Employee findByUsername(String username) throws SQLException {
        // Alias de búsqueda por UserId.
        return findByUserId(username);
    }

    @Override
    public Employee findByUserId(String username) throws SQLException {
        String sql = """
            SELECT e.*, r.LongDescription AS RoleDescription,
                   r.ShortDescription AS RoleShortDescription
            FROM Employee e
            LEFT JOIN RoleType r ON r.RoleTypeId = e.RoleTypeId
            WHERE e.UserId = ?
              AND e.IsRemoved = 0
            """;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    @Override
    public List<Employee> findAll() throws SQLException {
        String sql = """
            SELECT e.*, r.LongDescription AS RoleDescription,
                   r.ShortDescription AS RoleShortDescription
            FROM Employee e
            LEFT JOIN RoleType r ON r.RoleTypeId = e.RoleTypeId
            WHERE e.IsRemoved = 0
            ORDER BY e.LastName, e.Name
            """;

        List<Employee> employees = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                employees.add(map(rs));
            }
        }

        return employees;
    }

    @Override
    public List<EmployeeRow> findAllForGrid() throws SQLException {
        String sql = """
            SELECT e.EmployeeId, e.UserId, e.Name, e.LastName,
                   r.LongDescription AS Role,
                   e.StartDate, e.TerminationDate, e.Enabled
            FROM Employee e
            LEFT JOIN RoleType r ON r.RoleTypeId = e.RoleTypeId
            WHERE e.IsRemoved = 0
            ORDER BY e.LastName, e.Name
            """;

        List<EmployeeRow> employees = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                employees.add(new EmployeeRow(
                    rs.getLong("EmployeeId"),
                    rs.getString("UserId"),
                    rs.getString("Name"),
                    rs.getString("LastName"),
                    rs.getString("Role"),
                    toLocalDate(rs.getDate("StartDate")),
                    toLocalDate(rs.getDate("TerminationDate")),
                    rs.getBoolean("Enabled")
                ));
            }
        }

        return employees;
    }

    @Override
    public List<Employee> findOperators() throws SQLException {
        String sql = """
            SELECT e.*, r.LongDescription AS RoleDescription,
                   r.ShortDescription AS RoleShortDescription
            FROM Employee e
            LEFT JOIN RoleType r ON r.RoleTypeId = e.RoleTypeId
            WHERE e.IsRemoved = 0
              AND r.ShortDescription = 'OPR'
            ORDER BY e.LastName, e.Name
            """;

        List<Employee> employees = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                employees.add(map(rs));
            }
        }

        return employees;
    }

    @Override
    public void insert(Employee employee) throws SQLException {
        String sql = """
            INSERT INTO Employee (
                StartDate, TerminationDate, Birthday, Name, LastName,
                RoleTypeId, PasswordHash, Enabled, LastLogin,
                FailedAttempts, UserAdded, DateAdded, UserId,
                IsRemoved, UserChanged, DateChanged
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?)
            """;

        String userId = generateUserId(employee);

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setDate(1, toDate(employee.getStartDate()));
            ps.setDate(2, toDate(employee.getTerminationDate()));
            ps.setDate(3, toDate(employee.getBirthdate()));
            ps.setString(4, employee.getName());
            ps.setString(5, employee.getLastname());
            ps.setLong(6, employee.getRoleType().getSurrogate());
            ps.setString(7, employee.getPasswordHash());
            ps.setBoolean(8, employee.getEnabled());
            ps.setTimestamp(9, toTimestamp(employee.getLastLogin()));
            ps.setInt(10, employee.getFailedAttempts());

            Employee loggedUser = SessionContext.getLoggedUser();
            Long auditUserId = loggedUser != null ? loggedUser.getSurrogate() : null;

            setNullableLong(ps, 11, auditUserId);
            ps.setTimestamp(12, toTimestamp(employee.getDateAdded()));
            ps.setString(13, userId);
            setNullableLong(ps, 14, auditUserId);
            ps.setTimestamp(15, toTimestamp(employee.getDateChanged()));

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    employee.setSurrogate(keys.getLong(1));
                }
            }

            employee.setUserAdded(loggedUser);
            employee.setUserChanged(loggedUser);
        }
    }

    @Override
    public void update(Employee employee) throws SQLException {
        String sql = """
            UPDATE Employee
            SET StartDate = ?,
                TerminationDate = ?,
                Birthday = ?,
                Name = ?,
                LastName = ?,
                RoleTypeId = ?,
                PasswordHash = ?,
                Enabled = ?,
                UserChanged = ?,
                DateChanged = CURRENT_TIMESTAMP
            WHERE EmployeeId = ?
              AND IsRemoved = 0
            """;

        Employee loggedUser = SessionContext.getLoggedUser();
        Long auditUserId = loggedUser != null ? loggedUser.getSurrogate() : null;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, toDate(employee.getStartDate()));
            ps.setDate(2, toDate(employee.getTerminationDate()));
            ps.setDate(3, toDate(employee.getBirthdate()));
            ps.setString(4, employee.getName());
            ps.setString(5, employee.getLastname());
            ps.setLong(6, employee.getRoleType().getSurrogate());
            ps.setString(7, employee.getPasswordHash());
            ps.setBoolean(8, employee.getEnabled());
            setNullableLong(ps, 9, auditUserId);
            ps.setLong(10, employee.getSurrogate());

            ps.executeUpdate();
        }
    }

    @Override
    public void updateLastLogin(Long employeeId, LocalDateTime lastLogin)
            throws SQLException {

        String sql = """
            UPDATE Employee
            SET LastLogin = ?
            WHERE EmployeeId = ?
              AND IsRemoved = 0
            """;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, toTimestamp(lastLogin));
            ps.setLong(2, employeeId);
            ps.executeUpdate();
        }
    }

    @Override
    public void updateFailedAttempts(Long employeeId, int attempts) throws SQLException {

        String sql = """
            UPDATE Employee
            SET FailedAttempts = ?
            WHERE EmployeeId = ?
              AND IsRemoved = 0
            """;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, attempts);
            ps.setLong(2, employeeId);
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Long employeeId) throws SQLException {
        String sql = """
            UPDATE Employee
            SET IsRemoved = 1,
                UserChanged = ?,
                DateChanged = CURRENT_TIMESTAMP
            WHERE EmployeeId = ?
              AND IsRemoved = 0
            """;

        Employee loggedUser = SessionContext.getLoggedUser();
        Long auditUserId = loggedUser != null  ? loggedUser.getSurrogate() : null;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            setNullableLong(ps, 1, auditUserId);
            ps.setLong(2, employeeId);
            ps.executeUpdate();
        }
    }

    private Employee map(ResultSet rs) throws SQLException {
        Employee employee = new Employee();

        employee.setSurrogate(rs.getLong("EmployeeId"));
        employee.setStartDate(toLocalDate(rs.getDate("StartDate")));
        employee.setTerminationDate(toLocalDate(rs.getDate("TerminationDate")));
        employee.setBirthdate(toLocalDate(rs.getDate("Birthday")));
        employee.setName(rs.getString("Name"));
        employee.setLastname(rs.getString("LastName"));

        RoleType role = new RoleType();
        long roleId = rs.getLong("RoleTypeId");

        if (!rs.wasNull()) {
            role.setSurrogate(roleId);
        }

        role.setLongDescription(rs.getString("RoleDescription"));
        role.setShortDescription(rs.getString("RoleShortDescription"));
        employee.setRoleType(role);

        employee.setPasswordHash(rs.getString("PasswordHash"));
        employee.setEnabled(rs.getBoolean("Enabled"));
        employee.setLastLogin(toLocalDateTime(rs.getTimestamp("LastLogin")));
        employee.setFailedAttempts(rs.getInt("FailedAttempts"));

        employee.setDateAdded(toLocalDateTime(rs.getTimestamp("DateAdded")));
        employee.setDateChanged(toLocalDateTime(rs.getTimestamp("DateChanged")));

        long userAddedId = rs.getLong("UserAdded");
        if (!rs.wasNull()) {
            Employee userAdded = new Employee();
            userAdded.setSurrogate(userAddedId);
            employee.setUserAdded(userAdded);
        }

        long userChangedId = rs.getLong("UserChanged");
        if (!rs.wasNull()) {
            Employee userChanged = new Employee();
            userChanged.setSurrogate(userChangedId);
            employee.setUserChanged(userChanged);
        }

        return employee;
    }

    private String generateUserId(Employee employee) throws SQLException {
        if (employee.getRoleType() == null || employee.getRoleType().getSurrogate() == null) {
            throw new SQLException("El empleado debe tener un rol asignado.");
        }

        if (employee.getName() == null || employee.getName().isBlank()
                || employee.getLastname() == null
                || employee.getLastname().isBlank()) {
            throw new SQLException("El nombre y el apellido son necesarios para generar el usuario.");
        }

        Long roleId = employee.getRoleType().getSurrogate();

        String prefix;
        if (roleId.equals(1L)) {
            prefix = "SUPER";
        } else if (roleId.equals(2L)) {
            prefix = "ADM";
        } else if (roleId.equals(3L)) {
            prefix = "OPR";
        } else {
            throw new SQLException("El rol asignado no es válido.");
        }
        
        /*El userId está compuesto por el prefijo correspondiente al rol + Inicial de nombre + "Z" + Inicial de apellido*/
        String base = prefix + Character.toUpperCase(employee.getName().charAt(0)) + "Z" + Character.toUpperCase(employee.getLastname().charAt(0));
        int suffix = 1;
        String candidate;

        do {
            candidate = base + suffix++;
        } while (findByUserId(candidate) != null);

        return candidate;
    }

    private void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {

        if (value == null) {
            ps.setNull(index, java.sql.Types.BIGINT);
        } else {
            ps.setLong(index, value);
        }
    }

    private Date toDate(LocalDate date) {
        return date == null ? null : Date.valueOf(date);
    }

    private LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private Timestamp toTimestamp(LocalDateTime dateTime) {
        return dateTime == null ? null : Timestamp.valueOf(dateTime);
    }
}
