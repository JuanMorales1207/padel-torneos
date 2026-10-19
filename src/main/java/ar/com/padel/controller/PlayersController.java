package ar.com.padel.controller;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.IntStream;

import ar.com.padel.config.DataSourceProvider;
import ar.com.padel.model.dao.PlayerDAO;
import ar.com.padel.model.dao.PlayerDAOImpl;
import ar.com.padel.model.entity.Player;
import ar.com.padel.model.service.PlayerService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

public class PlayersController {

    @FXML private TableView<Player> playersTable;
    @FXML private TableColumn<Player, String> nombreColumn;
    @FXML private TableColumn<Player, String> apellidoColumn;
    @FXML private TableColumn<Player, Integer> categoriaColumn;
    @FXML private TableColumn<Player, LocalDate> fechaNacimientoColumn;

    private PlayerService playerService;

    @FXML
    public void initialize() {
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        apellidoColumn.setCellValueFactory(new PropertyValueFactory<>("lastname"));
        categoriaColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        fechaNacimientoColumn.setCellValueFactory(new PropertyValueFactory<>("birthdate"));
        fechaNacimientoColumn.setCellFactory(column ->
                new javafx.scene.control.TableCell<>() {
                    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                    @Override
                    protected void updateItem(LocalDate date, boolean empty) {
                        super.updateItem(date, empty);
                        setText(empty || date == null ? null : formatter.format(date));
                    }
                });

        PlayerDAO playerDAO = new PlayerDAOImpl(DataSourceProvider.getDataSource());
        playerService = new PlayerService(playerDAO);
        loadPlayers();
    }

    private void loadPlayers() {
        try {
            ObservableList<Player> data = FXCollections.observableArrayList(playerService.findAll());

            playersTable.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("No se pudieron cargar los jugadores.");
        }
    }

    @FXML
    private void onNuevo() {
        showPlayerDialog(null);
    }

    @FXML
    private void onEditar() {
        Player selected = playersTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Seleccioná un jugador para editar.");
            return;
        }

        showPlayerDialog(selected);
    }

    @FXML
    private void onBorrar() {
        Player selected = playersTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Seleccioná un jugador para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar jugador");
        confirm.setHeaderText("Eliminar jugador");
        confirm.setContentText("¿Deseás eliminar a " + selected.getName()
                + " " + selected.getLastname() + "?");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isEmpty() || result.get().getButtonData() != ButtonData.OK_DONE) {
            return;
        }
        try {
            playerService.delete(selected.getSurrogate());
            loadPlayers();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("No se pudo eliminar el jugador.");
        }
    }

    private void showPlayerDialog(Player existingPlayer) {
        boolean isNew = existingPlayer == null;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isNew ? "Nuevo jugador" : "Editar jugador");
        dialog.setHeaderText(isNew ? "Ingresá los datos del jugador" : "Modificá los datos del jugador");
        ButtonType saveButton = new ButtonType("Guardar", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);
        TextField txtName = new TextField();
        txtName.setPromptText("Nombre");
        TextField txtLastName = new TextField();
        txtLastName.setPromptText("Apellido");
        DatePicker dpBirthdate = new DatePicker();
        ComboBox<Integer> cmbCategory = new ComboBox<>();
        cmbCategory.setItems(FXCollections.observableArrayList(IntStream.rangeClosed(1, 8).boxed().toList()));
        cmbCategory.setPromptText("Seleccioná una categoría");
        cmbCategory.setMaxWidth(Double.MAX_VALUE);
        if (!isNew) {
            txtName.setText(existingPlayer.getName());
            txtLastName.setText(existingPlayer.getLastname());
            dpBirthdate.setValue(existingPlayer.getBirthdate());
            cmbCategory.setValue(existingPlayer.getCategory());
        } else {
            cmbCategory.getSelectionModel().selectFirst();
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new javafx.geometry.Insets(15));

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtName, 1, 0);

        grid.add(new Label("Apellido:"), 0, 1);
        grid.add(txtLastName, 1, 1);

        grid.add(new Label("Categoría:"), 0, 2);
        grid.add(cmbCategory, 1, 2);

        grid.add(new Label("Fecha de nacimiento:"), 0, 3);
        grid.add(dpBirthdate, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.getDialogPane().lookupButton(saveButton)
                .addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                    if (txtName.getText().isBlank() || txtLastName.getText().isBlank()) {
                        event.consume();
                        showError("El nombre y el apellido son obligatorios.");
                        return;
                    }

                    if (cmbCategory.getValue() == null) {
                        event.consume();
                        showError("Seleccioná una categoría.");
                    }
                });

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isEmpty() || result.get() != saveButton) {
            return;
        }

        String name = txtName.getText().trim();
        String lastName = txtLastName.getText().trim();
        LocalDate birthdate = dpBirthdate.getValue();
        int category = cmbCategory.getValue();

        try {
            Player player = isNew ? new Player() : existingPlayer;

            player.setName(name);
            player.setLastname(lastName);
            player.setBirthdate(birthdate);
            player.setCategory(category);

            if (isNew) {
                playerService.create(player);
            } else {
                playerService.update(player);
            }

            loadPlayers();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("No se pudo guardar el jugador.");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Jugadores");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
