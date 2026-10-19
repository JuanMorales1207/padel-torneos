package ar.com.padel.controller;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import ar.com.padel.config.DataSourceProvider;
import ar.com.padel.model.dao.CoupleDAO;
import ar.com.padel.model.dao.CoupleDAOImpl;
import ar.com.padel.model.dao.PlayerDAO;
import ar.com.padel.model.dao.PlayerDAOImpl;
import ar.com.padel.model.entity.Couple;
import ar.com.padel.model.entity.Player;
import ar.com.padel.model.service.CoupleService;
import ar.com.padel.model.service.PlayerService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.control.CheckBox;

public class CouplesController {

    @FXML private TableView<Couple> couplesTable;
    @FXML private TableColumn<Couple, String> nombreColumn;
    @FXML private TableColumn<Couple, String> jugador1Column;
    @FXML private TableColumn<Couple, String> jugador2Column;
    @FXML private TableColumn<Couple, LocalDateTime> fechaRegistroColumn;
    @FXML private TableColumn<Couple, String> activaColumn;

    private CoupleService coupleService;
    private PlayerService playerService;

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        jugador1Column.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(playerName(cell.getValue().getPlayer1())));

        jugador2Column.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(playerName(cell.getValue().getPlayer2())));        
        activaColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().isActive() ? "Sí" : "No"));

        fechaRegistroColumn.setCellValueFactory(new PropertyValueFactory<>("registrationDate"));

        fechaRegistroColumn.setCellFactory(column ->
                new javafx.scene.control.TableCell<>() {
                    @Override
                    protected void updateItem(LocalDateTime date, boolean empty) {
                        super.updateItem(date, empty);
                        setText(empty || date == null ? null : dateFormatter.format(date));
                    }
                });

        CoupleDAO coupleDAO = new CoupleDAOImpl(DataSourceProvider.getDataSource());

        PlayerDAO playerDAO = new PlayerDAOImpl(DataSourceProvider.getDataSource());

        coupleService = new CoupleService(coupleDAO);
        playerService = new PlayerService(playerDAO);

        loadCouples();
    }

    private String playerName(Player player) {
        if (player == null) {
            return "";
        }

        return player.getName() + " " + player.getLastname();
    }

    private void loadCouples() {
        try {
            ObservableList<Couple> data =
                    FXCollections.observableArrayList(coupleService.findAll());

            couplesTable.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("No se pudieron cargar las parejas.");
        }
    }

    @FXML
    private void onNuevo() {
        showCoupleDialog(null);
    }

    @FXML
    private void onEditar() {
        Couple selected =
                couplesTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Seleccioná una pareja para editar.");
            return;
        }

        showCoupleDialog(selected);
    }

    @FXML
    private void onBorrar() {
        Couple selected =
                couplesTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Seleccioná una pareja para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar pareja");
        confirm.setHeaderText("Eliminar pareja");
        confirm.setContentText("¿Deseás eliminar la pareja \"" + selected.getName() + "\"?");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isEmpty()
                || result.get().getButtonData() != ButtonData.OK_DONE) {
            return;
        }

        try {
            coupleService.delete(selected.getSurrogate());
            loadCouples();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("No se pudo eliminar la pareja.");
        }
    }

    private void showCoupleDialog(Couple existingCouple) {
        boolean isNew = existingCouple == null;
        CheckBox chkActive = new CheckBox("Pareja activa");

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isNew ? "Nueva pareja" : "Editar pareja");
        dialog.setHeaderText(isNew
                ? "Ingresá los datos de la pareja"
                : "Modificá los datos de la pareja");

        ButtonType saveButton =
                new ButtonType("Guardar", ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes()
                .addAll(saveButton, ButtonType.CANCEL);

        TextField txtName = new TextField();
        txtName.setPromptText("Nombre de la pareja");

        ComboBox<Player> cmbPlayer1 = new ComboBox<>();
        ComboBox<Player> cmbPlayer2 = new ComboBox<>();

        cmbPlayer1.setMaxWidth(Double.MAX_VALUE);
        cmbPlayer2.setMaxWidth(Double.MAX_VALUE);

        try {
            List<Player> players = playerService.findAll();

            cmbPlayer1.setItems(
                    FXCollections.observableArrayList(players));

            cmbPlayer2.setItems(
                    FXCollections.observableArrayList(players));

        } catch (SQLException e) {
            e.printStackTrace();
            showError("No se pudieron cargar los jugadores.");
            return;
        }

        cmbPlayer1.setPromptText("Seleccioná el jugador 1");
        cmbPlayer2.setPromptText("Seleccioná el jugador 2");

        // Mostrar nombre y apellido en ambos desplegables.
        cmbPlayer1.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Player player) {
                return player == null ? "" : playerName(player);
            }

            @Override
            public Player fromString(String string) {
                return null;
            }
        });

        cmbPlayer2.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Player player) {
                return player == null ? "" : playerName(player);
            }

            @Override
            public Player fromString(String string) {
                return null;
            }
        });

        // Evitar que un jugador ocupe las dos posiciones.
        cmbPlayer1.valueProperty().addListener((obs, oldPlayer, newPlayer) -> {
            Player selectedPlayer2 = cmbPlayer2.getValue();

            ObservableList<Player> availablePlayers =
                    FXCollections.observableArrayList(
                            cmbPlayer1.getItems().stream()
                                    .filter(player -> newPlayer == null
                                            || !player.getSurrogate()
                                                    .equals(newPlayer.getSurrogate()))
                                    .toList());

            cmbPlayer2.setItems(availablePlayers);

            if (selectedPlayer2 != null
                    && newPlayer != null
                    && selectedPlayer2.getSurrogate()
                            .equals(newPlayer.getSurrogate())) {
                cmbPlayer2.getSelectionModel().clearSelection();
            }
        });

        if (!isNew) {
            txtName.setText(existingCouple.getName());
            cmbPlayer1.setValue(existingCouple.getPlayer1());
            cmbPlayer2.setValue(existingCouple.getPlayer2());
            chkActive.setSelected(existingCouple.isActive());
        } else {
            chkActive.setSelected(true);
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new javafx.geometry.Insets(15));

        grid.add(new Label("Nombre de pareja:"), 0, 0);
        grid.add(txtName, 1, 0);

        grid.add(new Label("Jugador 1:"), 0, 1);
        grid.add(cmbPlayer1, 1, 1);

        grid.add(new Label("Jugador 2:"), 0, 2);
        grid.add(cmbPlayer2, 1, 2);
        
        grid.add(new Label("Estado:"), 0, 3);
        grid.add(chkActive, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.getDialogPane().lookupButton(saveButton)
                .addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                    if (txtName.getText().isBlank()) {
                        event.consume();
                        showError("El nombre de la pareja es obligatorio.");
                        return;
                    }

                    if (cmbPlayer1.getValue() == null
                            || cmbPlayer2.getValue() == null) {
                        event.consume();
                        showError("Seleccioná ambos jugadores.");
                    }
                });

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isEmpty() || result.get() != saveButton) {
            return;
        }

        try {
            Couple couple = isNew ? new Couple() : existingCouple;

            couple.setName(txtName.getText().trim());
            couple.setPlayer1(cmbPlayer1.getValue());
            couple.setPlayer2(cmbPlayer2.getValue());
            couple.setActive(chkActive.isSelected());

            if (isNew) {
                couple.setRegistrationDate(LocalDateTime.now());
                coupleService.create(couple);
            } else {
                coupleService.update(couple);
            }

            loadCouples();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("No se pudo guardar la pareja.");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Parejas");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
