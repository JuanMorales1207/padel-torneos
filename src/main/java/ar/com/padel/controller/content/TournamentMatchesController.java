package ar.com.padel.controller.content;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import ar.com.padel.config.DataSourceProvider;
import ar.com.padel.controller.TournamentDialogMode;
import ar.com.padel.controller.TournamentMatchDialogMode;
import ar.com.padel.controller.content.dialog.TournamentMatchDialogController;
import ar.com.padel.model.dao.CoupleDAO;
import ar.com.padel.model.dao.CoupleDAOImpl;
import ar.com.padel.model.dao.MatchSetDAO;
import ar.com.padel.model.dao.MatchSetDAOImpl;
import ar.com.padel.model.dao.TournamentDAO;
import ar.com.padel.model.dao.TournamentDAOImpl;
import ar.com.padel.model.dao.TournamentMatchDAO;
import ar.com.padel.model.dao.TournamentMatchDAOImpl;
import ar.com.padel.model.dao.TournamentRoundDAO;
import ar.com.padel.model.dao.TournamentRoundDAOImpl;
import ar.com.padel.model.entity.Couple;
import ar.com.padel.model.entity.MatchSet;
import ar.com.padel.model.entity.Tournament;
import ar.com.padel.model.entity.TournamentMatch;
import ar.com.padel.model.service.CoupleService;
import ar.com.padel.model.service.TournamentMatchService;
import ar.com.padel.model.service.TournamentService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class TournamentMatchesController {

    // ─────────────────────────────
    // Tabla
    // ─────────────────────────────

    @FXML private TableView<TournamentMatch> matchTable;

    @FXML private TableColumn<TournamentMatch, String> tournamentColumn;
    @FXML private TableColumn<TournamentMatch, Integer> roundColumn;
    @FXML private TableColumn<TournamentMatch, String> couple1Column;
    @FXML private TableColumn<TournamentMatch, String> couple2Column;
    @FXML private TableColumn<TournamentMatch, String> winnerColumn;
    @FXML private TableColumn<TournamentMatch, String> resultColumn;

    @FXML private ToolBar toolbar;

    // ─────────────────────────────
    // Services
    // ─────────────────────────────

    private TournamentMatchService matchService;
    private TournamentService tournamentService;
    private CoupleService coupleService;

    // ─────────────────────────────
    // Init
    // ─────────────────────────────

    @FXML
    public void initialize() {

        configureColumns();

        matchTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        DataSource dataSource = DataSourceProvider.getDataSource();

        TournamentDAO tournamentDAO =
                new TournamentDAOImpl(dataSource);

        TournamentRoundDAO roundDAO =
                new TournamentRoundDAOImpl(dataSource);

        TournamentMatchDAO matchDAO =
                new TournamentMatchDAOImpl(dataSource);

        MatchSetDAO matchSetDAO =
                new MatchSetDAOImpl(dataSource);

        CoupleDAO coupleDAO =
                new CoupleDAOImpl(dataSource);

        tournamentService = new TournamentService(
                tournamentDAO,
                roundDAO,
                dataSource
        );

        matchService = new TournamentMatchService(
                matchDAO,
                roundDAO,
                matchSetDAO,
                dataSource
        );

        coupleService = new CoupleService(coupleDAO);
        
        configureRowFactory();
        loadMatches();
    }

    // ─────────────────────────────
    // Columns
    // ─────────────────────────────

    private void configureColumns() {

        tournamentColumn.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(
                        cell.getValue().getRound()
                                .getTournament().getName()
                )
        );

        roundColumn.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleObjectProperty<>(
                        cell.getValue().getRound().getRoundNumber()
                )
        );

        couple1Column.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(
                        formatCouple(cell.getValue().getCouple1())
                )
        );

        couple2Column.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(
                        formatCouple(cell.getValue().getCouple2())
                )
        );

        winnerColumn.setCellValueFactory(cell -> {

            TournamentMatch match = cell.getValue();
            List<MatchSet> sets = setsByMatch.get(match.getSurrogate());

            Couple winner = match.getWinner(sets);

            return new javafx.beans.property.SimpleStringProperty(
                    winner == null
                            ? (sets == null || sets.isEmpty()
                                    ? "Sin resultado"
                                    : "Empate")
                            : formatCouple(winner)
            );
        });

        resultColumn.setCellValueFactory(cell -> {

            TournamentMatch match = cell.getValue();
            List<MatchSet> sets = setsByMatch.get(match.getSurrogate());

            return new javafx.beans.property.SimpleStringProperty(
                    sets == null || sets.isEmpty()
                            ? "-"
                            : match.getResult(sets)
            );
        });
    }

    // ─────────────────────────────
    // Data
    // ─────────────────────────────

    private final Map<Long, List<MatchSet>> setsByMatch =
            new HashMap<>();
    

    private void loadMatches() {

        try {

            setsByMatch.clear();

            List<TournamentMatch> matches = matchService.findAll();

            for (TournamentMatch match : matches) {

                List<MatchSet> sets =
                        matchService.findSetsByMatch(match.getSurrogate());

                setsByMatch.put(match.getSurrogate(), sets);
            }

            ObservableList<TournamentMatch> data =
                    FXCollections.observableArrayList(matches);

            matchTable.setItems(data);

        } catch (SQLException e) {

            e.printStackTrace();
            showError("Error cargando los partidos.");
        }
    }

    // ─────────────────────────────
    // Couple formatting
    // ─────────────────────────────

    private String formatCouple(Couple couple) {

        if (couple == null) {
            return "";
        }

        String player1 = couple.getPlayer1() != null
                ? couple.getPlayer1().getLastname()
                : "";

        String player2 = couple.getPlayer2() != null
                ? couple.getPlayer2().getLastname()
                : "";

        return player1 + " / " + player2;
    }
    
    private void configureRowFactory() {
        matchTable.setRowFactory(tv -> {
            TableRow<TournamentMatch> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openDialog(
                            TournamentDialogMode.VIEW,
                            row.getItem().getSurrogate()
                    );
                }
            });

            return row;
        });
    }

    // ─────────────────────────────
    // Dialog
    // ─────────────────────────────

    private void openDialog(TournamentDialogMode mode, Long id) {

        try {

            TournamentMatch match;

            if (mode == TournamentDialogMode.CREATE) {
                match = new TournamentMatch();
            } else {
                match = matchService.findById(id);
            }

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/dialog/TournamentMatchDialog.fxml"
                    )
            );

            Parent root = loader.load();

            TournamentMatchDialogController controller =
                    loader.getController();

            controller.init(
                    match,
                    matchService,
                    tournamentService,
                    coupleService
            );

            Stage stage = new Stage();
            controller.setStage(stage);
            stage.setTitle(
                    mode == TournamentDialogMode.CREATE
                            ? "Nuevo Partido"
                            : "Editar Partido"
            );

            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true);
            stage.sizeToScene();

            stage.showAndWait();

            loadMatches();

        } catch (Exception e) {

            e.printStackTrace();
            showError("Error abriendo el diálogo del partido.");
        }
    }

    // ─────────────────────────────
    // Toolbar
    // ─────────────────────────────

    @FXML
    private void onNuevo() {

        openDialog(
                TournamentDialogMode.CREATE,
                null
        );
    }

    @FXML
    private void onEditar() {

        TournamentMatch selected =
                matchTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Seleccioná un partido para editar.");
            return;
        }

        openDialog(
                TournamentDialogMode.EDIT,
                selected.getSurrogate()
        );
    }

    @FXML
    private void onBorrar() {

        TournamentMatch selected =
                matchTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Seleccioná un partido para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);

        confirm.setHeaderText("Eliminar partido");
        confirm.setContentText(
                "¿Está seguro que desea eliminar el partido seleccionado?"
        );

        if (confirm.showAndWait()
                .orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {

            matchService.delete(selected.getSurrogate());

            loadMatches();

        } catch (SQLException e) {

            e.printStackTrace();
            showError("Error eliminando el partido.");
        }
    }

    // ─────────────────────────────
    // Utils
    // ─────────────────────────────

    private void showError(String msg) {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
