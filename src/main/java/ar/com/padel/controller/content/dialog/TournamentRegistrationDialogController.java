package ar.com.padel.controller.content.dialog;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

import ar.com.padel.model.dao.PlayerDAO;
import ar.com.padel.model.entity.Couple;
import ar.com.padel.model.entity.Player;
import ar.com.padel.model.entity.Tournament;
import ar.com.padel.model.entity.TournamentRegistration;
import ar.com.padel.model.service.CoupleService;
import ar.com.padel.model.service.TournamentRegistrationService;
import ar.com.padel.model.service.TournamentService;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class TournamentRegistrationDialogController {

    @FXML
    private ComboBox<Tournament> tournamentCombo;

    @FXML
    private RadioButton existingCoupleRadio;

    @FXML
    private RadioButton newCoupleRadio;

    @FXML
    private ComboBox<Couple> coupleCombo;

    @FXML
    private ComboBox<Player> player1Combo;

    @FXML
    private ComboBox<Player> player2Combo;

    @FXML
    private Button btnSave;

    private TournamentRegistration registration;

    private TournamentRegistrationService registrationService;
    private TournamentService tournamentService;
    private CoupleService coupleService;
    private PlayerDAO playerDAO;

    private Stage stage;
    private boolean saved;

    private boolean editMode;

    // -------------------------------------------------
    // Init
    // -------------------------------------------------

    public void init(
            TournamentRegistration registration,
            TournamentRegistrationService registrationService,
            TournamentService tournamentService,
            CoupleService coupleService,
            PlayerDAO playerDAO) {

        this.registration = registration != null
                ? registration
                : new TournamentRegistration();

        this.registrationService = registrationService;
        this.tournamentService = tournamentService;
        this.coupleService = coupleService;
        this.playerDAO = playerDAO;

        this.saved = false;
        this.editMode = this.registration.getSurrogate() != null;

        configureRadioButtons();
        configureCombos();
        loadData();

        if (editMode) {
            loadRegistration();
        } else {
            existingCoupleRadio.setSelected(true);
        }

        updateSelectionMode();
    }

    // -------------------------------------------------
    // Configuration
    // -------------------------------------------------

    private void configureRadioButtons() {

        ToggleGroup group = new ToggleGroup();

        existingCoupleRadio.setToggleGroup(group);
        newCoupleRadio.setToggleGroup(group);

        existingCoupleRadio.setOnAction(event -> updateSelectionMode());
        newCoupleRadio.setOnAction(event -> updateSelectionMode());

        if (editMode) {
            newCoupleRadio.setDisable(true);
        }
    }

    private void configureCombos() {

        tournamentCombo.setConverter(new StringConverter<>() {

            @Override
            public String toString(Tournament tournament) {
                return tournament == null ? "" : tournament.getName();
            }

            @Override
            public Tournament fromString(String string) {
                return null;
            }
        });

        coupleCombo.setConverter(new StringConverter<>() {

            @Override
            public String toString(Couple couple) {
                return couple == null ? "" : couple.getName();
            }

            @Override
            public Couple fromString(String string) {
                return null;
            }
        });

        player1Combo.setConverter(new StringConverter<>() {

            @Override
            public String toString(Player player) {
                return player == null
                        ? ""
                        : player.getName() + " " + player.getLastname();
            }

            @Override
            public Player fromString(String string) {
                return null;
            }
        });

        player2Combo.setConverter(new StringConverter<>() {

            @Override
            public String toString(Player player) {
                return player == null
                        ? ""
                        : player.getName() + " " + player.getLastname();
            }

            @Override
            public Player fromString(String string) {
                return null;
            }
        });
    }

    private void updateSelectionMode() {

        boolean existing = existingCoupleRadio.isSelected();

        coupleCombo.setDisable(!existing);
        player1Combo.setDisable(existing);
        player2Combo.setDisable(existing);
    }

    // -------------------------------------------------
    // Load data
    // -------------------------------------------------

    private void loadData() {

        try {

            List<Tournament> tournaments = tournamentService.findAll();

            tournaments.removeIf(tournament ->
                    !tournament.isActive() || tournament.isRemoved());

            tournaments.sort(
                    Comparator.comparing(
                            Tournament::getTournamentDate,
                            Comparator.nullsLast(Comparator.reverseOrder())
                    )
            );

            tournamentCombo.setItems(
                    FXCollections.observableArrayList(tournaments)
            );

            List<Couple> couples = coupleService.findAll();

            couples.removeIf(couple ->
                    !couple.isActive() || couple.isRemoved());

            coupleCombo.setItems(
                    FXCollections.observableArrayList(couples)
            );

            List<Player> players = playerDAO.findAll();

            player1Combo.setItems(
                    FXCollections.observableArrayList(players)
            );

            player2Combo.setItems(
                    FXCollections.observableArrayList(players)
            );

        } catch (SQLException e) {

            e.printStackTrace();
            showError("Error cargando los datos de la inscripción.");
        }
    }

    // -------------------------------------------------
    // Load existing registration
    // -------------------------------------------------

    private void loadRegistration() {

        tournamentCombo.setValue(registration.getTournament());

        coupleCombo.setValue(registration.getCouple());

        existingCoupleRadio.setSelected(true);
    }

    // -------------------------------------------------
    // Save
    // -------------------------------------------------

    @FXML
    private void onGuardar() {

        try {

            validateFields();

            registration.setTournament(tournamentCombo.getValue());

            if (existingCoupleRadio.isSelected()) {

                registration.setCouple(coupleCombo.getValue());

                if (editMode) {
                    registrationService.update(registration);
                } else {
                    registrationService.create(registration);
                }

            } else {

                registrationService.createWithPlayers(
                        registration,
                        player1Combo.getValue(),
                        player2Combo.getValue()
                );
            }

            saved = true;
            stage.close();

        } catch (IllegalArgumentException e) {

            showError(e.getMessage());

        } catch (SQLException e) {

            e.printStackTrace();
            showError("Error guardando la inscripción.");
        }
    }

    private void validateFields() {

        if (tournamentCombo.getValue() == null) {
            throw new IllegalArgumentException(
                    "Seleccioná un torneo.");
        }

        if (existingCoupleRadio.isSelected()) {

            if (coupleCombo.getValue() == null) {
                throw new IllegalArgumentException(
                        "Seleccioná una pareja.");
            }

        } else {

            if (player1Combo.getValue() == null
                    || player2Combo.getValue() == null) {

                throw new IllegalArgumentException(
                        "Seleccioná ambos jugadores.");
            }

            if (player1Combo.getValue().getSurrogate().equals(
                    player2Combo.getValue().getSurrogate())) {

                throw new IllegalArgumentException(
                        "Una pareja debe estar formada por dos jugadores distintos.");
            }
        }
    }

    // -------------------------------------------------
    // Cancel and result
    // -------------------------------------------------

    @FXML
    private void onCancelar() {
        stage.close();
    }

    public boolean isSaved() {
        return saved;
    }

    public TournamentRegistration getRegistration() {
        return registration;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // -------------------------------------------------
    // Utils
    // -------------------------------------------------

    private void showError(String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
