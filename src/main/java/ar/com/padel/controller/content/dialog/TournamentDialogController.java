package ar.com.padel.controller.content.dialog;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import ar.com.padel.controller.TournamentDialogMode;
import ar.com.padel.model.entity.Category;
import ar.com.padel.model.entity.Tournament;
import ar.com.padel.model.entity.TournamentRound;
import ar.com.padel.model.service.TournamentService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class TournamentDialogController {

    // ─────────────────────────────
    // Tournament fields
    // ─────────────────────────────

    @FXML
    private TextField nameField;

    @FXML
    private ComboBox<Category> categoryCombo;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    // ─────────────────────────────
    // Rounds table
    // ─────────────────────────────

    @FXML
    private TableView<TournamentRound> roundsTable;

    @FXML
    private TableColumn<TournamentRound, Integer> roundNumberColumn;

    @FXML
    private TableColumn<TournamentRound, String> roundDescriptionColumn;

    @FXML
    private TableColumn<TournamentRound, LocalDate> roundStartDateColumn;

    @FXML
    private TableColumn<TournamentRound, LocalDate> roundEndDateColumn;

    // ─────────────────────────────
    // Buttons
    // ─────────────────────────────

    @FXML
    private Button btnAddRound;

    @FXML
    private Button btnEditRound;

    @FXML
    private Button btnDeleteRound;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnSave;

    // ─────────────────────────────
    // Services and data
    // ─────────────────────────────

    private TournamentService tournamentService;

    private Tournament tournament;

    private TournamentDialogMode mode;

    private final ObservableList<TournamentRound> rounds =
            FXCollections.observableArrayList();

    private final List<Long> removedRoundIds = new ArrayList<>();

    private int nextRoundNumber = 1;

    // ─────────────────────────────
    // Init
    // ─────────────────────────────

    public void init(
            TournamentDialogMode mode,
            Tournament tournament,
            TournamentService tournamentService) {

    	this.mode = mode;
    	this.tournament = tournament != null ? tournament : new Tournament();
    	this.tournamentService = tournamentService;

        configureColumns();

        roundsTable.setItems(rounds);

        loadCategories();

        if (mode != TournamentDialogMode.CREATE) {
            loadTournament();
            loadRounds();
        }

        applyMode();
    }

    // ─────────────────────────────
    // Columns
    // ─────────────────────────────

    private void configureColumns() {

        roundNumberColumn.setCellValueFactory(
                new PropertyValueFactory<>("roundNumber"));

        roundDescriptionColumn.setCellValueFactory(
                new PropertyValueFactory<>("description"));

        roundStartDateColumn.setCellValueFactory(
                new PropertyValueFactory<>("startDate"));

        roundEndDateColumn.setCellValueFactory(
                new PropertyValueFactory<>("endDate"));

        configureDateColumn(roundStartDateColumn);
        configureDateColumn(roundEndDateColumn);

        roundsTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);

        roundsTable.setRowFactory(tv -> {

            TableRow<TournamentRound> row = new TableRow<>();

            row.setOnMouseClicked(event -> {

                if (event.getClickCount() == 2 && !row.isEmpty()
                        && mode != TournamentDialogMode.VIEW) {

                    openRoundDialog(
                            row.getItem(),
                            true
                    );
                }
            });

            return row;
        });
    }

    private void configureDateColumn(
            TableColumn<TournamentRound, LocalDate> column) {

        column.setCellFactory(col -> new TableCell<>() {

            private final DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            protected void updateItem(LocalDate date, boolean empty) {

                super.updateItem(date, empty);

                setText(
                        empty || date == null
                                ? null
                                : date.format(formatter)
                );
            }
        });
    }

    // ─────────────────────────────
    // Categories
    // ─────────────────────────────

    private void loadCategories() {

        categoryCombo.setItems(
                FXCollections.observableArrayList(Category.values()));
    }

    // ─────────────────────────────
    // Load tournament
    // ─────────────────────────────

    private void loadTournament() {

        nameField.setText(tournament.getName());

        categoryCombo.setValue(tournament.getCategory());

        startDatePicker.setValue(tournament.getTournamentDate());

        endDatePicker.setValue(tournament.getEndDate());
    }

    // ─────────────────────────────
    // Load rounds
    // ─────────────────────────────

    private void loadRounds() {

        try {

            List<TournamentRound> tournamentRounds =
                    tournamentService.findRoundsByTournament(
                            tournament.getSurrogate());

            rounds.setAll(tournamentRounds);

            nextRoundNumber = rounds.stream()
                    .mapToInt(TournamentRound::getRoundNumber)
                    .max()
                    .orElse(0) + 1;

        } catch (SQLException e) {

            e.printStackTrace();

            showError("Error cargando las jornadas del torneo.");
        }
    }

    // ─────────────────────────────
    // Mode
    // ─────────────────────────────

    private void applyMode() {

        boolean viewMode = mode == TournamentDialogMode.VIEW;

        boolean createMode = mode == TournamentDialogMode.CREATE;

        nameField.setEditable(!viewMode);
        categoryCombo.setDisable(viewMode);
        startDatePicker.setDisable(viewMode);
        endDatePicker.setDisable(viewMode);

        btnAddRound.setVisible(!viewMode);
        btnAddRound.setManaged(!viewMode);

        btnEditRound.setVisible(!viewMode);
        btnEditRound.setManaged(!viewMode);

        btnDeleteRound.setVisible(!viewMode);
        btnDeleteRound.setManaged(!viewMode);

        btnEdit.setVisible(viewMode);
        btnEdit.setManaged(viewMode);

        btnDelete.setVisible(viewMode);
        btnDelete.setManaged(viewMode);

        btnSave.setVisible(!viewMode);
        btnSave.setManaged(!viewMode);
    }

    // ─────────────────────────────
    // Add round
    // ─────────────────────────────

    @FXML
    private void onAgregarJornada() {

        openRoundDialog(null, false);
    }

    // ─────────────────────────────
    // Edit round
    // ─────────────────────────────

    @FXML
    private void onEditarJornada() {

        TournamentRound selected =
                roundsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Seleccioná una jornada para editar.");
            return;
        }

        openRoundDialog(selected, true);
    }

    // ─────────────────────────────
    // Delete round
    // ─────────────────────────────

    @FXML
    private void onEliminarJornada() {

        TournamentRound selected =
                roundsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Seleccioná una jornada para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);

        confirm.setHeaderText("Eliminar jornada");

        confirm.setContentText(
                "¿Está seguro que desea eliminar la jornada seleccionada?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL)
                != ButtonType.OK) {
            return;
        }

        if (selected.getSurrogate() != null) {
            removedRoundIds.add(selected.getSurrogate());
        }

        rounds.remove(selected);
    }

    // ─────────────────────────────
    // Open round dialog
    // ─────────────────────────────

    private void openRoundDialog(
            TournamentRound selected,
            boolean editMode) {

        try {

            TournamentRound round;

            if (editMode) {
                round = copyRound(selected);
            } else {
                round = new TournamentRound();
            }

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/dialog/TournamentRoundDialog.fxml"));

            Parent root = loader.load();

            TournamentRoundDialogController controller =
                    loader.getController();

            Stage stage = new Stage();

            controller.setStage(stage);

            controller.init(
                    tournament,
                    round,
                    editMode
                            ? selected.getRoundNumber()
                            : nextRoundNumber,
                    editMode
            );

            stage.setTitle(
                    editMode ? "Editar jornada" : "Nueva jornada");

            stage.setScene(new Scene(root));

            stage.initModality(Modality.APPLICATION_MODAL);

            stage.setResizable(false);

            stage.showAndWait();

            if (controller.isSaved()) {

                TournamentRound savedRound = controller.getRound();

                if (editMode) {

                    int index = rounds.indexOf(selected);

                    rounds.set(index, savedRound);

                } else {

                    rounds.add(savedRound);

                    nextRoundNumber++;
                }

                sortRounds();
            }

        } catch (Exception e) {

            e.printStackTrace();

            showError("Error abriendo el diálogo de jornada.");
        }
    }

    // ─────────────────────────────
    // Copy round
    // ─────────────────────────────

    private TournamentRound copyRound(TournamentRound original) {

        TournamentRound copy = new TournamentRound();

        copy.setSurrogate(original.getSurrogate());
        copy.setTournament(original.getTournament());
        copy.setDescription(original.getDescription());
        copy.setRoundNumber(original.getRoundNumber());
        copy.setStartDate(original.getStartDate());
        copy.setEndDate(original.getEndDate());
        copy.setRoundStatus(original.getRoundStatus());
        copy.setActive(original.isActive());
        copy.setRemoved(original.isRemoved());

        return copy;
    }

    // ─────────────────────────────
    // Sort
    // ─────────────────────────────

    private void sortRounds() {

        rounds.sort(
                Comparator.comparingInt(TournamentRound::getRoundNumber));
    }

    // ─────────────────────────────
    // Edit tournament
    // ─────────────────────────────

    @FXML
    private void onEditar() {

        mode = TournamentDialogMode.EDIT;

        applyMode();
    }

    // ─────────────────────────────
    // Delete tournament
    // ─────────────────────────────

    @FXML
    private void onEliminar() {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);

        confirm.setHeaderText("Eliminar torneo");

        confirm.setContentText(
                "¿Está seguro que desea eliminar este torneo?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL)
                != ButtonType.OK) {
            return;
        }

        try {

            tournamentService.delete(tournament.getSurrogate());

            closeDialog();

        } catch (SQLException e) {

            e.printStackTrace();

            showError("Error eliminando el torneo.");
        }
    }

    // ─────────────────────────────
    // Save tournament
    // ─────────────────────────────

    @FXML
    private void onGuardar() {

        try {

            tournament.setName(nameField.getText().trim());

            tournament.setCategory(categoryCombo.getValue());

            tournament.setTournamentDate(startDatePicker.getValue());

            tournament.setEndDate(endDatePicker.getValue());

            if (mode == TournamentDialogMode.CREATE) {
                tournament.setActive(true);
            }

            tournamentService.saveWithRounds(
                    tournament,
                    new ArrayList<>(rounds),
                    new ArrayList<>(removedRoundIds)
            );

            closeDialog();

        } catch (IllegalArgumentException e) {

            showError(e.getMessage());

        } catch (SQLException e) {

            e.printStackTrace();

            showError("Error guardando el torneo y sus jornadas.");
        }
    }

    // ─────────────────────────────
    // Cancel
    // ─────────────────────────────

    @FXML
    private void onCancelar() {

        closeDialog();
    }

    // ─────────────────────────────
    // Utils
    // ─────────────────────────────

    private void closeDialog() {

        Stage stage = (Stage) nameField.getScene().getWindow();

        stage.close();
    }

    private void showError(String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }
}