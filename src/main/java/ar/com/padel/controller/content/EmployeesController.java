package ar.com.padel.controller.content;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import ar.com.padel.config.DataSourceProvider;
import ar.com.padel.model.dao.EmployeeDAO;
import ar.com.padel.model.dao.EmployeeDAOImpl;
import ar.com.padel.dto.EmployeeRow;
import ar.com.padel.model.entity.Employee;
import ar.com.padel.model.entity.RoleType;
import ar.com.padel.model.service.EmployeeService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

public class EmployeesController {

    @FXML private TableView<EmployeeRow> employeesTable;

    @FXML private TableColumn<EmployeeRow, String> nombreColumn;
    @FXML private TableColumn<EmployeeRow, String> apellidoColumn;
    @FXML private TableColumn<EmployeeRow, String> usuarioColumn;
    @FXML private TableColumn<EmployeeRow, String> rolColumn;
    @FXML private TableColumn<EmployeeRow, LocalDate> fechaIngresoColumn;
    @FXML private TableColumn<EmployeeRow, LocalDate> fechaFinalizacionColumn;
    @FXML private TableColumn<EmployeeRow, Boolean> activoColumn;

    private EmployeeService employeeService;

    @FXML
    public void initialize() {

        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        apellidoColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        usuarioColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        rolColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        fechaIngresoColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        fechaFinalizacionColumn.setCellValueFactory(
                new PropertyValueFactory<>("terminationDate"));
        activoColumn.setCellValueFactory(new PropertyValueFactory<>("enabled"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        fechaIngresoColumn.setCellFactory(column ->
            new TableCell<>() {
                @Override
                protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    setText(empty || date == null ? null : formatter.format(date));
                }
            });

        fechaFinalizacionColumn.setCellFactory(column ->
            new TableCell<>() {
                @Override
                protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    setText(empty || date == null ? null : formatter.format(date));
                }
            });

        activoColumn.setCellFactory(column ->
            new TableCell<>() {
                @Override
                protected void updateItem(Boolean enabled, boolean empty) {
                    super.updateItem(enabled, empty);
                    setText(empty || enabled == null ? null : enabled ? "Sí" : "No");
                }
            });

        EmployeeDAO employeeDAO =
                new EmployeeDAOImpl(DataSourceProvider.getDataSource());

        employeeService = new EmployeeService(employeeDAO);

        loadEmployees();
    }

    private void loadEmployees() {
        try {
            ObservableList<EmployeeRow> data =
                    FXCollections.observableArrayList(
                            employeeService.findAllForGrid());

            employeesTable.setItems(data);

        } catch (SQLException e) {
            e.printStackTrace();
            showError("No se pudieron cargar los empleados.");
        }
    }

    @FXML
    private void onNuevo() {
        showEmployeeDialog(null);
    }

    @FXML
    private void onEditar() {

        EmployeeRow selected =
                employeesTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Seleccioná un empleado para editar.");
            return;
        }

        try {
            Employee employee =
                    employeeService.findById(selected.getEmployeeId());

            if (employee == null) {
                showError("No se encontró el empleado seleccionado.");
                return;
            }

            showEmployeeDialog(employee);

        } catch (SQLException e) {
            e.printStackTrace();
            showError("No se pudieron recuperar los datos del empleado.");
        }
    }

    @FXML
    private void onBorrar() {

        EmployeeRow selected =
                employeesTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Seleccioná un empleado para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar empleado");
        confirm.setHeaderText("Eliminar empleado");
        confirm.setContentText(
                "¿Deseás eliminar a " + selected.getName()
                + " " + selected.getLastName() + "?");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isEmpty()
                || result.get().getButtonData() != ButtonData.OK_DONE) {
            return;
        }

        try {
            employeeService.delete(selected.getEmployeeId());
            loadEmployees();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("No se pudo eliminar el empleado.");
        }
    }

    private void showEmployeeDialog(Employee existingEmployee) {

        boolean isNew = existingEmployee == null;

        Dialog<ButtonType> dialog = new Dialog<>();

        dialog.setTitle(isNew ? "Nuevo empleado" : "Editar empleado");
        dialog.setHeaderText(
                isNew ? "Ingresá los datos del empleado"
                      : "Modificá los datos del empleado");

        ButtonType saveButton =
                new ButtonType("Guardar", ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes()
                .addAll(saveButton, ButtonType.CANCEL);

        TextField txtName = new TextField();
        txtName.setPromptText("Nombre");

        TextField txtLastName = new TextField();
        txtLastName.setPromptText("Apellido");

        DatePicker dpBirthdate = new DatePicker();
        DatePicker dpStartDate = new DatePicker();
        DatePicker dpTerminationDate = new DatePicker();

        ComboBox<RoleType> cmbRole = new ComboBox<>();

        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText(
                isNew ? "Contraseña obligatoria"
                      : "Dejar vacío para conservar la contraseña");

        CheckBox chkEnabled = new CheckBox("Activo");
        chkEnabled.setSelected(true);

        try {
            List<RoleType> roles = employeeService.findAllRoles();

            cmbRole.setItems(FXCollections.observableArrayList(roles));
            cmbRole.setMaxWidth(Double.MAX_VALUE);
            cmbRole.setPromptText("Seleccioná un rol");

        } catch (SQLException e) {
            e.printStackTrace();
            showError("No se pudieron cargar los roles.");
            return;
        }

        if (!isNew) {
            txtName.setText(existingEmployee.getName());
            txtLastName.setText(existingEmployee.getLastname());
            dpBirthdate.setValue(existingEmployee.getBirthdate());
            dpStartDate.setValue(existingEmployee.getStartDate());
            dpTerminationDate.setValue(existingEmployee.getTerminationDate());
            cmbRole.setValue(existingEmployee.getRoleType());
            chkEnabled.setSelected(existingEmployee.getEnabled());
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new javafx.geometry.Insets(15));

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtName, 1, 0);

        grid.add(new Label("Apellido:"), 0, 1);
        grid.add(txtLastName, 1, 1);

        grid.add(new Label("Fecha de nacimiento:"), 0, 2);
        grid.add(dpBirthdate, 1, 2);

        grid.add(new Label("Fecha de ingreso:"), 0, 3);
        grid.add(dpStartDate, 1, 3);

        grid.add(new Label("Fecha de finalización:"), 0, 4);
        grid.add(dpTerminationDate, 1, 4);

        grid.add(new Label("Rol:"), 0, 5);
        grid.add(cmbRole, 1, 5);

        grid.add(new Label("Contraseña:"), 0, 6);
        grid.add(txtPassword, 1, 6);

        grid.add(new Label("Estado:"), 0, 7);
        grid.add(chkEnabled, 1, 7);

        dialog.getDialogPane().setContent(grid);

        dialog.getDialogPane().lookupButton(saveButton)
                .addEventFilter(javafx.event.ActionEvent.ACTION, event -> {

                    if (txtName.getText().isBlank()
                            || txtLastName.getText().isBlank()) {

                        event.consume();
                        showError("El nombre y el apellido son obligatorios.");
                        return;
                    }

                    if (cmbRole.getValue() == null) {
                        event.consume();
                        showError("Seleccioná un rol.");
                        return;
                    }

                    if (isNew && txtPassword.getText().isBlank()) {
                        event.consume();
                        showError("La contraseña es obligatoria.");
                    }
                });

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isEmpty() || result.get() != saveButton) {
            return;
        }

        try {
            Employee employee =
                    isNew ? new Employee() : existingEmployee;

            employee.setName(txtName.getText().trim());
            employee.setLastname(txtLastName.getText().trim());
            employee.setBirthdate(dpBirthdate.getValue());
            employee.setStartDate(dpStartDate.getValue());
            employee.setTerminationDate(dpTerminationDate.getValue());
            employee.setRoleType(cmbRole.getValue());
            employee.setEnabled(chkEnabled.isSelected());

            String password = txtPassword.getText();

            if (isNew) {
                employeeService.create(employee, password);
            } else {
                employeeService.update(employee, password);
            }

            loadEmployees();

        } catch (SQLException | IllegalArgumentException e) {
            e.printStackTrace();
            showError("No se pudo guardar el empleado: " + e.getMessage());
        }
    }

    private void showError(String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Empleados");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}