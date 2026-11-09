
package ar.com.padel.controller.content.dialog;

import java.sql.SQLException;

import ar.com.padel.config.DataSourceProvider;
import ar.com.padel.model.dao.EmployeeDAO;
import ar.com.padel.model.dao.EmployeeDAOImpl;
import ar.com.padel.model.entity.Employee;
import ar.com.padel.model.service.EmployeeService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ModifyUserDialogController {

    @FXML private TextField txtName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtUserId;
    @FXML private DatePicker dpBirthday;

    @FXML private Button btnEdit;
    @FXML private Button btnCancel;
    @FXML private Button btnSave;
    @FXML private Button btnEnablePasswordChange;

    @FXML private VBox passwordSection;

    @FXML private PasswordField txtCurrentPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtRepeatPassword;

    private EmployeeService employeeService;
    private Employee employee;

    private boolean editing = false;
    private boolean changingPassword = false;

    @FXML
    public void initialize() {
        EmployeeDAO employeeDAO =
                new EmployeeDAOImpl(DataSourceProvider.getDataSource());

        employeeService = new EmployeeService(employeeDAO);

        setEditing(false);
        setPasswordSectionVisible(false);
    }

    public void setEmployee(Employee employee) {
        if (employee == null) {
            showError("No se encontró un usuario en la sesión actual.");
            return;
        }

        try {
            this.employee = employeeService.findById(employee.getSurrogate());

            if (this.employee == null) {
                showError("No se pudo encontrar el empleado en la base de datos.");
                return;
            }

            loadEmployeeData();

            String userId = employeeService.findUserIdByEmployeeId(
                    this.employee.getSurrogate()
            );

            txtUserId.setText(userId != null ? userId : "");

        } catch (SQLException e) {
            e.printStackTrace();
            showError("No se pudieron recuperar los datos de la cuenta.");
        }
    }

    private void loadEmployeeData() {
        txtName.setText(employee.getName());
        txtLastName.setText(employee.getLastname());
        dpBirthday.setValue(employee.getBirthdate());

        txtCurrentPassword.clear();
        txtNewPassword.clear();
        txtRepeatPassword.clear();
    }

    @FXML
    private void onEdit() {
        setEditing(true);
    }

    @FXML
    private void onEnablePasswordChange() {
        changingPassword = true;
        setPasswordSectionVisible(true);
        setEditing(true);

        btnEnablePasswordChange.setDisable(true);
    }

    private void setEditing(boolean editing) {
        this.editing = editing;

        txtName.setDisable(!editing);
        txtLastName.setDisable(!editing);
        txtUserId.setDisable(!editing);
        dpBirthday.setDisable(!editing);

        btnEdit.setVisible(!editing);
        btnEdit.setManaged(!editing);

        btnCancel.setVisible(editing);
        btnCancel.setManaged(editing);

        btnSave.setVisible(editing);
        btnSave.setManaged(editing);
    }

    private void setPasswordSectionVisible(boolean visible) {
        passwordSection.setVisible(visible);
        passwordSection.setManaged(visible);
    }

    @FXML
    private void onCancel() {
        if (employee == null) {
            closeWindow();
            return;
        }

        loadEmployeeData();

        changingPassword = false;
        setPasswordSectionVisible(false);
        btnEnablePasswordChange.setDisable(false);

        setEditing(false);
    }

    @FXML
    private void onSave() {
        if (txtName.getText() == null || txtName.getText().isBlank()
                || txtLastName.getText() == null
                || txtLastName.getText().isBlank()) {

            showError("El nombre y el apellido son obligatorios.");
            return;
        }

        employee.setName(txtName.getText().trim());
        employee.setLastname(txtLastName.getText().trim());
        employee.setBirthdate(dpBirthday.getValue());

        try {
            employeeService.updateMyAccount(
                    employee,
                    changingPassword ? txtCurrentPassword.getText() : null,
                    changingPassword ? txtNewPassword.getText() : null,
                    changingPassword ? txtRepeatPassword.getText() : null
            );

            changingPassword = false;
            setPasswordSectionVisible(false);
            btnEnablePasswordChange.setDisable(false);

            // Actualizamos los datos mostrados.
         // Actualizamos los datos mostrados.
            loadEmployeeData();

            setEditing(false);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Mi cuenta");
            alert.setHeaderText(null);
            alert.setContentText("¡Los cambios se guardaron correctamente!");
            alert.showAndWait();

        } catch (SQLException | IllegalArgumentException e) {
            showError("No se pudieron guardar los cambios: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Mi cuenta");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) txtName.getScene().getWindow();
        stage.close();
    }
}
