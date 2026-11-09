package ar.com.padel.controller.content.dialog;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import ar.com.padel.model.entity.Category;
import ar.com.padel.model.entity.Couple;
import ar.com.padel.model.entity.MatchSet;
import ar.com.padel.model.entity.Player;
import ar.com.padel.model.entity.Tournament;
import ar.com.padel.model.entity.TournamentMatch;
import ar.com.padel.model.entity.TournamentRound;
import ar.com.padel.model.service.CoupleService;
import ar.com.padel.model.service.TournamentMatchService;
import ar.com.padel.model.service.TournamentService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class TournamentMatchDialogController {

    @FXML private ComboBox<Tournament> tournamentCombo;

    @FXML private ComboBox<TournamentRound> roundCombo;

    @FXML private DatePicker matchDatePicker;

    @FXML private ComboBox<String> hourCombo;

    @FXML private ComboBox<String> minuteCombo;

    @FXML private TextField courtField;

    @FXML private TextField descriptionField;

    @FXML private TextField stageField;

    @FXML private ComboBox<String> statusCombo;

    @FXML private ComboBox<Couple> couple1Combo;

    @FXML private ComboBox<Couple> couple2Combo;

    @FXML private TableView<MatchSet> setsTable;

    @FXML private TableColumn<MatchSet, Integer> setNumberColumn;

    @FXML private TableColumn<MatchSet, Integer> gamesCouple1Column;

    @FXML private TableColumn<MatchSet, Integer> gamesCouple2Column;

    @FXML private TableColumn<MatchSet, Integer> tieBreakColumn;

    @FXML private Button btnEditSet;

    @FXML private Button btnDeleteSet;

    @FXML private Button btnSave;

    private TournamentMatchService matchService;
    private TournamentService tournamentService;
    private CoupleService coupleService;

    private TournamentMatch match;
    private Stage stage;
    private boolean saved;

    private final ObservableList<MatchSet> sets =
            FXCollections.observableArrayList();

    private final List<Long> removedSetIds = new ArrayList<>();

    private List<Couple> allCouples = new ArrayList<>();

    // -------------------------------------------------
    // Init
    // -------------------------------------------------

    public void init(
            TournamentMatch match,
            TournamentMatchService matchService,
            TournamentService tournamentService,
            CoupleService coupleService) {

        this.match = match != null ? match : new TournamentMatch();

        this.matchService = matchService;
        this.tournamentService = tournamentService;
        this.coupleService = coupleService;

        this.saved = false;

        configureSetsTable();
        configureTournamentCombo();
        configureRoundCombo();
        setsTable.setItems(sets);

        loadHoursAndMinutes();
        loadStatuses();
        loadData();

        tournamentCombo.setOnAction(event -> onTournamentSelected());

        if (this.match.getSurrogate() != null) {
            loadMatch();
        }
    }

    // -------------------------------------------------
    // Configuration
    // -------------------------------------------------

    private void configureSetsTable() {

        setNumberColumn.setCellValueFactory(
                new PropertyValueFactory<>("setNumber"));

        gamesCouple1Column.setCellValueFactory(
                new PropertyValueFactory<>("gamesCouple1"));

        gamesCouple2Column.setCellValueFactory(
                new PropertyValueFactory<>("gamesCouple2"));

        tieBreakColumn.setCellValueFactory(
                new PropertyValueFactory<>("tieBreakLoserPoints"));

        setsTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    private void configureTournamentCombo() {
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
    }

    private void configureRoundCombo() {
        roundCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(TournamentRound round) {
                if (round == null) {
                    return "";
                }

                return "Jornada " + round.getRoundNumber()
                        + " - " + round.getDescription();
            }

            @Override
            public TournamentRound fromString(String string) {
                return null;
            }
        });
    }

    private void loadHoursAndMinutes() {

        for (int i = 0; i < 24; i++) {
            hourCombo.getItems().add(String.format("%02d", i));
        }

        for (int i = 0; i < 60; i++) {
            minuteCombo.getItems().add(String.format("%02d", i));
        }
    }

    private void loadStatuses() {

        statusCombo.getItems().setAll(
                "Planificado",
                "En juego",
                "Finalizado",
                "Aplazado",
                "Suspendido"
        );
    }

    // -------------------------------------------------
    // Load data
    // -------------------------------------------------

    private void loadData() {

        try {

            List<Tournament> tournaments = tournamentService.findAll();

            tournaments.sort(
                    Comparator.comparing(
                            Tournament::getTournamentDate,
                            Comparator.nullsLast(Comparator.reverseOrder())
                    )
            );

            tournamentCombo.setItems(
                    FXCollections.observableArrayList(tournaments));

            allCouples = coupleService.findAll();

        } catch (SQLException e) {

            e.printStackTrace();
            showError("Error cargando los datos del partido.");
        }
    }

    private void onTournamentSelected() {

        Tournament tournament = tournamentCombo.getValue();

        roundCombo.getItems().clear();
        couple1Combo.getItems().clear();
        couple2Combo.getItems().clear();

        if (tournament == null) {
            return;
        }

        try {

            List<TournamentRound> rounds =
                    tournamentService.findRoundsByTournament(
                            tournament.getSurrogate());

            rounds.sort(
                    Comparator.comparingInt(
                            TournamentRound::getRoundNumber
                    ).reversed()
            );

            roundCombo.setItems(
                    FXCollections.observableArrayList(rounds));

            loadCouplesByCategory(tournament);

        } catch (SQLException e) {

            e.printStackTrace();
            showError("Error cargando las jornadas del torneo.");
        }
    }

    private void loadCouplesByCategory(Tournament tournament) {

        int category = tournament.getCategory().getValue();

        List<Couple> filtered = allCouples.stream()
                .filter(couple -> category == Category.LIBRE.getValue()
                        || (couple.getPlayer1().getCategory() == category
                        && couple.getPlayer2().getCategory() == category))
                .toList();

        couple1Combo.setItems(
                FXCollections.observableArrayList(filtered));

        couple2Combo.setItems(
                FXCollections.observableArrayList(filtered));
    }

    // -------------------------------------------------
    // Load existing match
    // -------------------------------------------------

    private void loadMatch() {

        try {

            Tournament tournament = match.getRound().getTournament();

            tournamentCombo.setValue(tournament);

            onTournamentSelected();

            roundCombo.setValue(match.getRound());

            couple1Combo.setValue(match.getCouple1());
            couple2Combo.setValue(match.getCouple2());

            matchDatePicker.setValue(
                    match.getMatchDate() != null
                            ? match.getMatchDate().toLocalDate()
                            : null);

            if (match.getMatchDate() != null) {

                hourCombo.setValue(
                        String.format("%02d",
                                match.getMatchDate().getHour()));

                minuteCombo.setValue(
                        String.format("%02d",
                                match.getMatchDate().getMinute()));
            }

            courtField.setText(match.getCourt());
            descriptionField.setText(match.getDescription());
            stageField.setText(match.getStage());

            statusCombo.setValue(statusToLabel(match.getMatchStatus()));

            sets.setAll(matchService.findSetsByMatch(
                    match.getSurrogate()));

        } catch (SQLException e) {

            e.printStackTrace();
            showError("Error cargando los datos del partido.");
        }
    }

    // -------------------------------------------------
    // Set management
    // -------------------------------------------------

    @FXML
    private void onAgregarSet() {

        if (sets.size() >= 5) {
            showError("Un partido no puede tener más de 5 sets.");
            return;
        }

        MatchSet set = new MatchSet();

        if (showSetDialog(set, false, null)) {
            sets.add(set);
            sortSets();
        }
    }

    @FXML
    private void onEditarSet() {

        MatchSet selected =
                setsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Seleccioná un set para editar.");
            return;
        }

        MatchSet copy = copySet(selected);

        if (showSetDialog(copy, true, selected)) {

            int index = sets.indexOf(selected);
            sets.set(index, copy);
            sortSets();
        }
    }

    @FXML
    private void onEliminarSet() {

        MatchSet selected =
                setsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Seleccioná un set para eliminar.");
            return;
        }

        if (selected.getSurrogate() != null) {
            removedSetIds.add(selected.getSurrogate());
        }

        sets.remove(selected);
    }
    
    private int getNextAvailableSetNumber() {

        for (int number = 1; number <= 5; number++) {

            final int candidateNumber = number;

            boolean exists = sets.stream()
                    .anyMatch(set -> set.getSetNumber() == candidateNumber);

            if (!exists) {
                return number;
            }
        }

        return 0;
    }

    private boolean showSetDialog(
            MatchSet set,
            boolean editMode,
            MatchSet originalSet) {

        Dialog<Boolean> dialog = new Dialog<>();

        dialog.setTitle(editMode ? "Editar set" : "Nuevo set");

        Button saveButton = new Button("Guardar");
        Button cancelButton = new Button("Cancelar");

        ComboBox<Integer> numberCombo = new ComboBox<>();
        numberCombo.getItems().addAll(1, 2, 3, 4, 5);

        numberCombo.setValue(
                editMode
                        ? set.getSetNumber()
                        : getNextAvailableSetNumber()
        );

        TextField games1Field = new TextField();
        TextField games2Field = new TextField();
        TextField tieBreakField = new TextField();

        if (editMode) {
            games1Field.setText(String.valueOf(set.getGamesCouple1()));
            games2Field.setText(String.valueOf(set.getGamesCouple2()));

            if (set.getTieBreakLoserPoints() != null) {
                tieBreakField.setText(
                        String.valueOf(set.getTieBreakLoserPoints()));
            }
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Número:"), 0, 0);
        grid.add(numberCombo, 1, 0);

        grid.add(new Label("Games pareja 1:"), 0, 1);
        grid.add(games1Field, 1, 1);

        grid.add(new Label("Games pareja 2:"), 0, 2);
        grid.add(games2Field, 1, 2);

        grid.add(new Label("Puntos tie-break perdedor:"), 0, 3);
        grid.add(tieBreakField, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(
                javafx.scene.control.ButtonType.OK,
                javafx.scene.control.ButtonType.CANCEL);

        dialog.setResultConverter(button ->
                button == javafx.scene.control.ButtonType.OK);

        var result = dialog.showAndWait();

        if (result.isEmpty() || !result.get()) {
            return false;
        }

        try {

            if (numberCombo.getValue() == null) {
                throw new IllegalArgumentException(
                        "Seleccioná el número de set.");
            }

            int games1 = Integer.parseInt(games1Field.getText().trim());
            int games2 = Integer.parseInt(games2Field.getText().trim());

            if (games1 < 0 || games2 < 0) {
                throw new IllegalArgumentException(
                        "Los games no pueden ser negativos.");
            }
            
            if (games1 > 7 || games2 > 7) {
                throw new IllegalArgumentException(
                        "Los games no pueden superar 7.");
            }
            
            if (games1 < 0 || games2 < 0) {
                throw new IllegalArgumentException(
                        "Los games no pueden ser negativos.");
            }

            int number = numberCombo.getValue();

            boolean duplicate = sets.stream()
                    .anyMatch(existing ->
                            existing.getSetNumber() == number
                            && existing != originalSet);

            if (duplicate) {
                throw new IllegalArgumentException(
                        "Ya existe un set con ese número.");
            }

            set.setSetNumber(number);
            set.setGamesCouple1(games1);
            set.setGamesCouple2(games2);

            String tieBreak = tieBreakField.getText().trim();

            if (tieBreak.isEmpty()) {
                set.setTieBreakLoserPoints(null);
            } else {
                int points = Integer.parseInt(tieBreak);

                if (points < 0) {
                    throw new IllegalArgumentException(
                            "Los puntos del tie-break no pueden ser negativos.");
                }
                
                if (Math.abs(games2-games1) > 1) {
                    throw new IllegalArgumentException(
                            "Los puntos del tie-break sólo son válidos para definiciones de set.");
                }

                set.setTieBreakLoserPoints(points);
            }

            return true;

        } catch (NumberFormatException e) {

            showError("Ingresá valores numéricos válidos.");
            return false;

        } catch (IllegalArgumentException e) {

            showError(e.getMessage());
            return false;
        }
    }

    private MatchSet copySet(MatchSet original) {

        MatchSet copy = new MatchSet();

        copy.setSurrogate(original.getSurrogate());
        copy.setMatch(original.getMatch());
        copy.setSetNumber(original.getSetNumber());
        copy.setGamesCouple1(original.getGamesCouple1());
        copy.setGamesCouple2(original.getGamesCouple2());
        copy.setTieBreakLoserPoints(
                original.getTieBreakLoserPoints());
        copy.setRemoved(original.isRemoved());

        return copy;
    }

    private void sortSets() {

        sets.sort(
                Comparator.comparingInt(MatchSet::getSetNumber));
    }

    // -------------------------------------------------
    // Save
    // -------------------------------------------------

    @FXML
    private void onGuardar() {

        try {

            validateFields();

            match.setRound(roundCombo.getValue());
            match.setCouple1(couple1Combo.getValue());
            match.setCouple2(couple2Combo.getValue());

            if (matchDatePicker.getValue() != null) {

                match.setMatchDate(
                        LocalDateTime.of(
                                matchDatePicker.getValue(),
                                java.time.LocalTime.of(
                                        Integer.parseInt(hourCombo.getValue()),
                                        Integer.parseInt(minuteCombo.getValue())
                                )
                        )
                );
            } else {
                match.setMatchDate(null);
            }

            match.setCourt(courtField.getText().trim());
            match.setDescription(descriptionField.getText().trim());
            match.setStage(stageField.getText().trim());
            match.setMatchStatus(labelToStatus(statusCombo.getValue()));

            matchService.saveWithSets(
                    match,
                    new ArrayList<>(sets),
                    new ArrayList<>(removedSetIds));

            saved = true;
            stage.close();

        } catch (IllegalArgumentException e) {

            showError(e.getMessage());

        } catch (SQLException e) {

            e.printStackTrace();
            showError("Error guardando el partido.");
        }
    }

    private void validateFields() {

        if (tournamentCombo.getValue() == null) {
            throw new IllegalArgumentException(
                    "Seleccioná un torneo.");
        }

        if (roundCombo.getValue() == null) {
            throw new IllegalArgumentException(
                    "Seleccioná una jornada.");
        }

        if (couple1Combo.getValue() == null
                || couple2Combo.getValue() == null) {
            throw new IllegalArgumentException(
                    "Seleccioná ambas parejas.");
        }

        if (matchDatePicker.getValue() != null
                && (hourCombo.getValue() == null
                || minuteCombo.getValue() == null)) {
            throw new IllegalArgumentException(
                    "Completá la hora del partido.");
        }

        if (statusCombo.getValue() == null) {
            throw new IllegalArgumentException(
                    "Seleccioná el estado del partido.");
        }
    }

    // -------------------------------------------------
    // Status conversion
    // -------------------------------------------------

    private String labelToStatus(String label) {

        return switch (label) {
            case "Planificado" -> "SCHEDULED";
            case "En juego" -> "IN_PROGRESS";
            case "Finalizado" -> "FINISHED";
            case "Aplazado" -> "POSTPONED";
            case "Suspendido" -> "SUSPENDED";
            default -> null;
        };
    }

    private String statusToLabel(String status) {

        if (status == null) {
            return null;
        }

        return switch (status) {
            case "SCHEDULED" -> "Planificado";
            case "IN_PROGRESS" -> "En juego";
            case "FINISHED" -> "Finalizado";
            case "POSTPONED" -> "Aplazado";
            case "SUSPENDED" -> "Suspendido";
            default -> null;
        };
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

    public TournamentMatch getMatch() {
        return match;
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
