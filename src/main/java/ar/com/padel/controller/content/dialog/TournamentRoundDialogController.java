package ar.com.padel.controller.content.dialog;

import java.time.LocalDate;

import ar.com.padel.model.entity.Tournament;
import ar.com.padel.model.entity.TournamentRound;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class TournamentRoundDialogController {

    @FXML
    private TextField roundNumberField;

    @FXML
    private TextField descriptionField;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private Button btnSave;

    private Stage stage;

    private TournamentRound round;

    private Tournament tournament;

    private boolean saved;

    // ─────────────────────────────
    // Init
    // ─────────────────────────────

    public void init(
            Tournament tournament,
            TournamentRound round,
            int roundNumber,
            boolean editMode) {

        this.tournament = tournament;
        this.round = round;
        this.saved = false;

        roundNumberField.setText(String.valueOf(roundNumber));

        if (editMode) {
            loadRound();
        }

    }

    // ─────────────────────────────
    // Load
    // ─────────────────────────────

    private void loadRound() {

        descriptionField.setText(round.getDescription());

        startDatePicker.setValue(round.getStartDate());

        endDatePicker.setValue(round.getEndDate());

    }

    // ─────────────────────────────
    // Save
    // ─────────────────────────────

    @FXML
    private void onGuardar() {

        try {

            validateFields();

            round.setTournament(tournament);

            round.setRoundNumber(
                    Integer.parseInt(roundNumberField.getText())
            );

            round.setDescription(
                    descriptionField.getText().trim()
            );

            round.setStartDate(startDatePicker.getValue());

            round.setEndDate(endDatePicker.getValue());

            saved = true;

            stage.close();

        } catch (IllegalArgumentException e) {

            showError(e.getMessage());

        }

    }

    // ─────────────────────────────
    // Validation
    // ─────────────────────────────

    private void validateFields() {

        String description = descriptionField.getText();

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Ingresá una descripción para la jornada."
            );
        }

        LocalDate startDate = startDatePicker.getValue();

        LocalDate endDate = endDatePicker.getValue();

        if (startDate != null && endDate != null
                && endDate.isBefore(startDate)) {

            throw new IllegalArgumentException(
                    "La fecha de finalización no puede ser anterior a la fecha de inicio."
            );

        }

        if (startDate != null
                && tournament.getTournamentDate() != null
                && startDate.isBefore(tournament.getTournamentDate())) {

            throw new IllegalArgumentException(
                    "La jornada no puede comenzar antes del torneo."
            );

        }

        if (endDate != null
                && tournament.getEndDate() != null
                && endDate.isAfter(tournament.getEndDate())) {

            throw new IllegalArgumentException(
                    "La jornada no puede finalizar después del torneo."
            );

        }

    }

    // ─────────────────────────────
    // Cancel
    // ─────────────────────────────

    @FXML
    private void onCancelar() {

        stage.close();

    }

    // ─────────────────────────────
    // Result
    // ─────────────────────────────

    public boolean isSaved() {

        return saved;

    }

    public TournamentRound getRound() {

        return round;

    }

    public void setStage(Stage stage) {

        this.stage = stage;

    }

    // ─────────────────────────────
    // Utils
    // ─────────────────────────────

    private void showError(String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();

    }

}