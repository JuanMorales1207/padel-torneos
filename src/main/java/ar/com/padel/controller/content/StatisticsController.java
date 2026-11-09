package ar.com.padel.controller.content;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.sql.DataSource;

import ar.com.padel.config.DataSourceProvider;
import ar.com.padel.dto.StatisticsSummary;
import ar.com.padel.model.dao.StatisticsDAO;
import ar.com.padel.model.dao.StatisticsDAOImpl;
import ar.com.padel.model.service.StatisticsService;
import ar.com.padel.dto.TournamentStatisticsRow;
import ar.com.padel.dto.RoundStatisticsRow;
import ar.com.padel.dto.PlayerStatisticsRow;
import javafx.scene.control.ComboBox;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

public class StatisticsController {

    // -------------------------------------------------
    // Resumen general
    // -------------------------------------------------

    @FXML private Label lblTournamentsCount;
    @FXML private Label lblRoundsCount;
    @FXML private Label lblPlayersCount;
    @FXML private Label lblCouplesCount;
    @FXML private Label lblMatchesCount;    
    @FXML private TableView<TournamentStatisticsRow> tblTournaments;
    @FXML private TableColumn<TournamentStatisticsRow, String> colTournamentName;
    @FXML private TableColumn<TournamentStatisticsRow, String> colTournamentDate;
    @FXML private TableColumn<TournamentStatisticsRow, String> colTournamentEndDate;
    @FXML private TableView<RoundStatisticsRow> tblRounds;
    @FXML private TableColumn<RoundStatisticsRow, Integer> colRoundNumber;
    @FXML private TableColumn<RoundStatisticsRow, String> colRoundDescription;
    @FXML private TableColumn<RoundStatisticsRow, String> colRoundStartDate;
    @FXML private TableColumn<RoundStatisticsRow, String> colRoundEndDate;
    @FXML private TableColumn<RoundStatisticsRow, Integer> colRoundMatches;
    
	// -------------------------------------------------
	// Ranking de jugadores
	// -------------------------------------------------
	
	@FXML private ComboBox<TournamentStatisticsRow> cmbPlayerTournament;
	
	@FXML private TableView<PlayerStatisticsRow> tblPlayers;
	
	@FXML private TableColumn<PlayerStatisticsRow, String> colPlayerName;
	@FXML private TableColumn<PlayerStatisticsRow, Integer> colPlayerMatchesPlayed;
	@FXML private TableColumn<PlayerStatisticsRow, Integer> colPlayerMatchesWon;
	@FXML private TableColumn<PlayerStatisticsRow, Integer> colPlayerMatchesLost;
	@FXML private TableColumn<PlayerStatisticsRow, String> colPlayerMatchesWonPercentage;
	@FXML private TableColumn<PlayerStatisticsRow, Integer> colPlayerGamesWon;
	@FXML private TableColumn<PlayerStatisticsRow, Integer> colPlayerGamesLost;
	@FXML private TableColumn<PlayerStatisticsRow, String> colPlayerGamesWonPercentage;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // -------------------------------------------------
    // Services
    // -------------------------------------------------

    private StatisticsService statisticsService;

    // -------------------------------------------------
    // Init
    // -------------------------------------------------

    @FXML
    public void initialize() {

        DataSource dataSource = DataSourceProvider.getDataSource();

        StatisticsDAO statisticsDAO =
                new StatisticsDAOImpl(dataSource);

        statisticsService = new StatisticsService(statisticsDAO);

        configureTournamentTables();
        configurePlayerTable();
        configurePlayerTournamentFilter();

        loadTournamentsStatistics();
        loadPlayerTournaments();
        loadPlayersStatistics(null);

        loadGeneralSummary();
    }

    // -------------------------------------------------
    // General summary
    // -------------------------------------------------

    private void loadGeneralSummary() {

        try {

            StatisticsSummary summary =
                    statisticsService.getGeneralSummary();

            lblTournamentsCount.setText(
                    String.valueOf(summary.getTournamentsCount())
            );

            lblRoundsCount.setText(
                    String.valueOf(summary.getRoundsCount())
            );

            lblPlayersCount.setText(
                    String.valueOf(summary.getPlayersCount())
            );

            lblCouplesCount.setText(
                    String.valueOf(summary.getCouplesCount())
            );

            lblMatchesCount.setText(
                    String.valueOf(summary.getMatchesCount())
            );

        } catch (SQLException e) {

            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudieron cargar las estadísticas");
            alert.setContentText(
                    "Ocurrió un error al obtener el resumen general."
            );
            alert.showAndWait();
        }
    }
    
    private void configureTournamentTables() {

        colTournamentName.setCellValueFactory(
                new PropertyValueFactory<>("tournamentName"));

        colTournamentDate.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(
                        formatDate(cellData.getValue().getTournamentDate())));

        colTournamentEndDate.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(
                        formatDate(cellData.getValue().getEndDate())));

        colRoundNumber.setCellValueFactory(
                new PropertyValueFactory<>("roundNumber"));

        colRoundDescription.setCellValueFactory(
                new PropertyValueFactory<>("description"));

        colRoundStartDate.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(
                        formatDate(cellData.getValue().getStartDate())));

        colRoundEndDate.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(
                        formatDate(cellData.getValue().getEndDate())));

        colRoundMatches.setCellValueFactory(
                new PropertyValueFactory<>("matchesCount"));

        tblTournaments.getSelectionModel().setSelectionMode(
                SelectionMode.SINGLE);

        tblTournaments.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {

                    if (newValue != null) {
                        loadRoundsStatistics(newValue.getTournamentId());
                    } else {
                        tblRounds.getItems().clear();
                    }
                });
    }
    
    private void loadTournamentsStatistics() {

        try {
            List<TournamentStatisticsRow> tournaments =
                    statisticsService.getTournamentsStatistics();

            tblTournaments.setItems(
                    FXCollections.observableArrayList(tournaments));

            tblRounds.getItems().clear();

        } catch (SQLException e) {
            e.printStackTrace();

            showStatisticsError(
                    "No se pudieron cargar los torneos.");
        }
    }
    
    private void loadRoundsStatistics(Long tournamentId) {

        try {
            List<RoundStatisticsRow> rounds =
                    statisticsService.getRoundsStatistics(tournamentId);

            tblRounds.setItems(
                    FXCollections.observableArrayList(rounds));

        } catch (SQLException e) {
            e.printStackTrace();

            showStatisticsError(
                    "No se pudieron cargar las jornadas.");
        }
    }
    
    private void configurePlayerTable() {

        colPlayerName.setCellValueFactory(
                new PropertyValueFactory<>("playerName"));

        colPlayerMatchesPlayed.setCellValueFactory(
                new PropertyValueFactory<>("matchesPlayed"));

        colPlayerMatchesWon.setCellValueFactory(
                new PropertyValueFactory<>("matchesWon"));

        colPlayerMatchesLost.setCellValueFactory(
                new PropertyValueFactory<>("matchesLost"));

        colPlayerMatchesWonPercentage.setCellValueFactory(
                cellData -> {
                    PlayerStatisticsRow player = cellData.getValue();

                    int total = player.getMatchesWon() + player.getMatchesLost();

                    String value = total > 0
                            ? String.format("%.1f%%", player.getMatchesWonPercentage())
                            : "-";

                    return new javafx.beans.property.SimpleStringProperty(value);
                });

        colPlayerGamesWon.setCellValueFactory(
                new PropertyValueFactory<>("gamesWon"));

        colPlayerGamesLost.setCellValueFactory(
                new PropertyValueFactory<>("gamesLost"));

        colPlayerGamesWonPercentage.setCellValueFactory(
                cellData -> {
                    PlayerStatisticsRow player = cellData.getValue();

                    int total = player.getGamesWon() + player.getGamesLost();

                    String value = total > 0
                            ? String.format("%.1f%%", player.getGamesWonPercentage())
                            : "-";

                    return new javafx.beans.property.SimpleStringProperty(value);
                });
    }
    
    private void loadPlayerTournaments() {

        try {
            List<TournamentStatisticsRow> tournaments =
                    statisticsService.getTournamentsStatistics();

            cmbPlayerTournament.getItems().clear();

            // Primera opción: histórico general (null)
            cmbPlayerTournament.getItems().add(null);

            // Luego, los torneos disponibles
            cmbPlayerTournament.getItems().addAll(tournaments);

            // Seleccionar inicialmente el histórico general
            cmbPlayerTournament.getSelectionModel().selectFirst();

        } catch (SQLException e) {
            e.printStackTrace();
            showStatisticsError("No se pudieron cargar los torneos para el ranking.");
        }
    }
    
    private void loadPlayersStatistics(Long tournamentId) {

        try {
            List<PlayerStatisticsRow> players =
                    statisticsService.getPlayersStatistics(tournamentId);

            tblPlayers.setItems(
                    FXCollections.observableArrayList(players));

        } catch (SQLException e) {
            e.printStackTrace();
            showStatisticsError("No se pudieron cargar las estadísticas de jugadores.");
        }
    }
    
    private void configurePlayerTournamentFilter() {

        cmbPlayerTournament.setConverter(
                new javafx.util.StringConverter<TournamentStatisticsRow>() {

                    @Override
                    public String toString(TournamentStatisticsRow tournament) {
                        return tournament != null
                                ? tournament.getTournamentName()
                                : "Histórico general";
                    }

                    @Override
                    public TournamentStatisticsRow fromString(String string) {
                        return null;
                    }
                });

        cmbPlayerTournament.valueProperty().addListener(
                (observable, oldValue, newValue) -> {

                    if (newValue == null) {
                        loadPlayersStatistics(null);
                    } else {
                        loadPlayersStatistics(newValue.getTournamentId());
                    }
                });
    }
    
    //Utils
    
    private void showStatisticsError(String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error al cargar estadísticas");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private String formatDate(java.time.LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "-";
    }
    
}
