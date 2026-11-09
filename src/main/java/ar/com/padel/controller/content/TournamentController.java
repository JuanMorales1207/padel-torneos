package ar.com.padel.controller.content;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import ar.com.padel.controller.TournamentDialogMode;
import ar.com.padel.model.dao.TournamentDAO;
import ar.com.padel.model.dao.TournamentDAOImpl;
import ar.com.padel.model.dao.TournamentRoundDAO;
import ar.com.padel.model.dao.TournamentRoundDAOImpl;
import ar.com.padel.model.entity.Category;
import ar.com.padel.model.entity.Tournament;
import ar.com.padel.model.service.TournamentService;
import ar.com.padel.config.DataSourceProvider;
import ar.com.padel.controller.content.dialog.TournamentDialogController;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class TournamentController {

    // ─────────────────────────────
    // Tabla
    // ─────────────────────────────

    @FXML private TableView<Tournament> tournamentTable;

    @FXML private TableColumn<Tournament, Category> categoryColumn;
    @FXML private TableColumn<Tournament, String> nameColumn;
    @FXML private TableColumn<Tournament, LocalDate> dateColumn;

    @FXML private ComboBox<Category> categoryFilter;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;

    @FXML private ToolBar toolbar;

    // ─────────────────────────────
    // Services
    // ─────────────────────────────

    private TournamentService service;

    // ─────────────────────────────
    // Init
    // ─────────────────────────────

    @FXML
    public void initialize() {

        configureColumns();
        tournamentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        DataSource dataSource = DataSourceProvider.getDataSource();
        TournamentDAO tournamentDAO = new TournamentDAOImpl(dataSource);

        TournamentRoundDAO roundDAO = new TournamentRoundDAOImpl(dataSource);

        service = new TournamentService(tournamentDAO, roundDAO, dataSource);
        configureCategoryFilter();
        loadTournaments();
        configureRowFactory();
    }

    // ─────────────────────────────
    // Columns
    // ─────────────────────────────

    private void configureColumns() {

        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("tournamentDate"));
        dateColumn.setCellFactory(column -> new TableCell<>() {
                private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                @Override
                protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    setText(empty || date == null ? null : date.format(formatter));
                }
            }
        );
    }

    // ─────────────────────────────
    // Category Filter
    // ─────────────────────────────

    private void configureCategoryFilter() {

        ObservableList<Category> categories = FXCollections.observableArrayList(Category.values());
        categoryFilter.setItems(categories);
        categoryFilter.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Category category, boolean empty) {
                super.updateItem(category, empty);
                setText(empty || category == null ? null : category.toString());
            }
        });

        categoryFilter.setButtonCell(
            new ListCell<>() {
                @Override
                protected void updateItem(Category category, boolean empty) {
                    super.updateItem(category, empty);

                    setText(empty || category == null ? "Todas las categorías" : category.toString());
                }
            }
        );
    }

    // ─────────────────────────────
    // Row Factory
    // ─────────────────────────────

    private void configureRowFactory() {

        tournamentTable.setRowFactory(tv -> {
            TableRow<Tournament> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openDialog(TournamentDialogMode.VIEW, row.getItem().getSurrogate());
                }
            });

            return row;
        });
    }

    // ─────────────────────────────
    // Load
    // ─────────────────────────────

    private void loadTournaments() {

        try {

            List<Tournament> tournaments = service.findAll();

            Category category = categoryFilter.getValue();
            LocalDate desde = dpDesde.getValue();
            LocalDate hasta = dpHasta.getValue();

            List<Tournament> filtered = tournaments.stream().filter(t ->category == null || t.getCategory() == category)
                .filter(t ->desde == null ||!t.getTournamentDate().isBefore(desde))
                .filter(t ->hasta == null ||!t.getTournamentDate().isAfter(hasta))
                .sorted(Comparator.comparing(Tournament::getTournamentDate).reversed())
                .collect(Collectors.toList());
            ObservableList<Tournament> data = FXCollections.observableArrayList(filtered);
            tournamentTable.setItems(data);

        } catch (SQLException e) {
            throw new RuntimeException("Error cargando torneos", e);
        }
    }

    // ─────────────────────────────
    // Dialog
    // ─────────────────────────────

    private void openDialog(TournamentDialogMode mode, Long id) {

        try {
            Tournament tournament;
            if (mode == TournamentDialogMode.CREATE) {
                tournament = new Tournament();
            } else {
                tournament = service.findById(id);
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialog/TournamentDialog.fxml"));
            Parent root = loader.load();
            TournamentDialogController controller = loader.getController();
            controller.init(mode,tournament,service);
            Stage stage = new Stage();
            stage.setTitle(mode == TournamentDialogMode.CREATE ? "Nuevo Torneo" : "Torneo");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true);
            stage.sizeToScene();

            stage.showAndWait();

            loadTournaments();

        } catch (Exception e) {
            e.printStackTrace();

            showError("Error abriendo el diálogo de torneo");
        }
    }

    // ─────────────────────────────
    // Toolbar
    // ─────────────────────────────

    @FXML
    private void onNuevo() {
        openDialog(TournamentDialogMode.CREATE,null);
    }

    @FXML
    private void onEditar() {
        Tournament selected = tournamentTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Seleccioná un torneo para editar.");
            return;
        }

        openDialog(TournamentDialogMode.EDIT,selected.getSurrogate());
    }

    @FXML
    private void onBorrar() {

        Tournament selected = tournamentTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Seleccioná un torneo para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Eliminar torneo");
        confirm.setContentText("¿Está seguro que desea eliminar el torneo seleccionado?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL)!= ButtonType.OK) {
            return;
        }

        try {
            service.delete(selected.getSurrogate());
            loadTournaments();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error eliminando torneo");
        }
    }

    @FXML
    private void onFiltrar() {

        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();

        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            showError("La fecha 'Desde' no puede ser posterior a la fecha 'Hasta'.");
            return;
        }

        loadTournaments();
    }

    @FXML
    private void onLimpiarFiltro() {

        categoryFilter.setValue(null);
        dpDesde.setValue(null);
        dpHasta.setValue(null);

        loadTournaments();
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
