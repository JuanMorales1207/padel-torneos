package ar.com.padel.controller.content;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import ar.com.padel.config.DataSourceProvider;
import ar.com.padel.model.dao.CoupleDAO;
import ar.com.padel.model.dao.CoupleDAOImpl;
import ar.com.padel.model.dao.PlayerDAO;
import ar.com.padel.model.dao.PlayerDAOImpl;
import ar.com.padel.model.dao.TournamentDAO;
import ar.com.padel.model.dao.TournamentDAOImpl;
import ar.com.padel.model.dao.TournamentRegistrationDAO;
import ar.com.padel.model.dao.TournamentRegistrationDAOImpl;
import ar.com.padel.model.dao.TournamentRoundDAO;
import ar.com.padel.model.dao.TournamentRoundDAOImpl;
import ar.com.padel.model.entity.Couple;
import ar.com.padel.model.entity.Tournament;
import ar.com.padel.model.entity.TournamentRegistration;
import ar.com.padel.model.service.CoupleService;
import ar.com.padel.model.service.TournamentRegistrationService;
import ar.com.padel.model.service.TournamentService;
import ar.com.padel.controller.content.dialog.TournamentRegistrationDialogController;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.io.IOException;

public class TournamentRegistrationController {

    // -------------------------------------------------
    // Tabla
    // -------------------------------------------------

    @FXML private TableView<TournamentRegistration> registrationTable;

    @FXML private TableColumn<TournamentRegistration, Tournament> tournamentColumn;

    @FXML private TableColumn<TournamentRegistration, Couple> coupleColumn;

    @FXML private TableColumn<TournamentRegistration, LocalDateTime> dateColumn;

    @FXML private ComboBox<Tournament> tournamentFilter;

    @FXML private ComboBox<Couple> coupleFilter;

    @FXML private DatePicker dpDesde;

    @FXML private DatePicker dpHasta;

    @FXML private ToolBar toolbar;

    // -------------------------------------------------
    // Services
    // -------------------------------------------------

    private TournamentRegistrationService service;
    private TournamentService tournamentService;
    private CoupleService coupleService;

    // -------------------------------------------------
    // Init
    // -------------------------------------------------

    @FXML
    public void initialize() {

        configureColumns();

        registrationTable.setColumnResizePolicy(
            TableView.CONSTRAINED_RESIZE_POLICY
        );

        DataSource dataSource = DataSourceProvider.getDataSource();

        TournamentDAO tournamentDAO =
            new TournamentDAOImpl(dataSource);

        TournamentRoundDAO roundDAO =
            new TournamentRoundDAOImpl(dataSource);

        CoupleDAO coupleDAO =
            new CoupleDAOImpl(dataSource);

        PlayerDAO playerDAO =
            new PlayerDAOImpl(dataSource);

        TournamentRegistrationDAO registrationDAO =
            new TournamentRegistrationDAOImpl(dataSource);

        tournamentService =
            new TournamentService(tournamentDAO, roundDAO, dataSource);

        coupleService =
            new CoupleService(coupleDAO);

        service = new TournamentRegistrationService(
            registrationDAO,
            tournamentDAO,
            playerDAO,
            coupleService,
            dataSource
        );

        configureTournamentFilter();
        configureCoupleFilter();

        loadRegistrations();
        configureRowFactory();
    }

    // -------------------------------------------------
    // Columns
    // -------------------------------------------------

    private void configureColumns() {

        tournamentColumn.setCellValueFactory(
            new PropertyValueFactory<>("tournament")
        );

        tournamentColumn.setCellFactory(column -> new TableCell<>() {

            @Override
            protected void updateItem(Tournament tournament, boolean empty) {

                super.updateItem(tournament, empty);

                setText(empty || tournament == null
                    ? null
                    : tournament.getName());
            }
        });

        coupleColumn.setCellValueFactory(
            new PropertyValueFactory<>("couple")
        );

        dateColumn.setCellValueFactory(
            new PropertyValueFactory<>("registrationDate")
        );

        dateColumn.setCellFactory(column -> new TableCell<>() {

            private final DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {

                super.updateItem(date, empty);

                setText(empty || date == null
                    ? null
                    : date.format(formatter));
            }
        });
    }

    // -------------------------------------------------
    // Tournament Filter
    // -------------------------------------------------

    private void configureTournamentFilter() {

        try {

            ObservableList<Tournament> tournaments =
                FXCollections.observableArrayList(
                    tournamentService.findAll()
                );

            tournamentFilter.setItems(tournaments);

            tournamentFilter.setCellFactory(cb -> new ListCell<>() {

                @Override
                protected void updateItem(Tournament tournament, boolean empty) {

                    super.updateItem(tournament, empty);

                    setText(empty || tournament == null
                        ? null
                        : tournament.getName());
                }
            });

            tournamentFilter.setButtonCell(new ListCell<>() {

                @Override
                protected void updateItem(Tournament tournament, boolean empty) {

                    super.updateItem(tournament, empty);

                    setText(empty || tournament == null
                        ? "Todos los torneos"
                        : tournament.getName());
                }
            });

        } catch (SQLException e) {

            throw new RuntimeException("Error cargando torneos", e);
        }
    }

    // -------------------------------------------------
    // Couple Filter
    // -------------------------------------------------

    private void configureCoupleFilter() {

        try {

            ObservableList<Couple> couples =
                FXCollections.observableArrayList(
                    coupleService.findAll()
                );

            coupleFilter.setItems(couples);

            coupleFilter.setCellFactory(cb -> new ListCell<>() {

                @Override
                protected void updateItem(Couple couple, boolean empty) {

                    super.updateItem(couple, empty);

                    setText(empty || couple == null
                        ? null
                        : couple.getName());
                }
            });

            coupleFilter.setButtonCell(new ListCell<>() {

                @Override
                protected void updateItem(Couple couple, boolean empty) {

                    super.updateItem(couple, empty);

                    setText(empty || couple == null
                        ? "Todas las parejas"
                        : couple.getName());
                }
            });

        } catch (SQLException e) {

            throw new RuntimeException("Error cargando parejas", e);
        }
    }

    // -------------------------------------------------
    // Row Factory
    // -------------------------------------------------

    private void configureRowFactory() {

        registrationTable.setRowFactory(tv -> {

            TableRow<TournamentRegistration> row = new TableRow<>();

            row.setOnMouseClicked(event -> {

                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    onEditar();
                }
            });

            return row;
        });
    }

    // -------------------------------------------------
    // Load
    // -------------------------------------------------

    private void loadRegistrations() {

        try {

            List<TournamentRegistration> registrations =
                service.findAll();

            Tournament selectedTournament =
                tournamentFilter.getValue();

            Couple selectedCouple =
                coupleFilter.getValue();

            LocalDate desde = dpDesde.getValue();
            LocalDate hasta = dpHasta.getValue();

            List<TournamentRegistration> filtered =
                registrations.stream()

                    .filter(r -> selectedTournament == null
                        || r.getTournament().getSurrogate()
                            .equals(selectedTournament.getSurrogate()))

                    .filter(r -> selectedCouple == null
                        || r.getCouple().getSurrogate()
                            .equals(selectedCouple.getSurrogate()))

                    .filter(r -> desde == null
                        || !r.getRegistrationDate().toLocalDate().isBefore(desde))

                    .filter(r -> hasta == null
                        || !r.getRegistrationDate().toLocalDate().isAfter(hasta))

                    .sorted(
                        Comparator.comparing(
                            TournamentRegistration::getRegistrationDate
                        ).reversed()
                    )

                    .collect(Collectors.toList());

            ObservableList<TournamentRegistration> data =
                FXCollections.observableArrayList(filtered);

            registrationTable.setItems(data);

        } catch (SQLException e) {

            throw new RuntimeException("Error cargando inscripciones", e);
        }
    }

    // -------------------------------------------------
    // Toolbar
    // -------------------------------------------------

    @FXML
    private void onNuevo() {
        openDialog(null);
    }

    @FXML
    private void onEditar() {

        TournamentRegistration selected =
            registrationTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Seleccioná una inscripción para editar.");
            return;
        }

        openDialog(selected);
    }

    @FXML
    private void onBorrar() {

        TournamentRegistration selected =
            registrationTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Seleccioná una inscripción para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);

        confirm.setHeaderText("Eliminar inscripción");
        confirm.setContentText(
            "¿Está seguro que desea eliminar la inscripción seleccionada?"
        );

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {

            service.delete(selected.getSurrogate());

            loadRegistrations();

        } catch (SQLException e) {

            e.printStackTrace();
            showError("Error eliminando inscripción.");
        }
    }

    // -------------------------------------------------
    // Filters
    // -------------------------------------------------

    @FXML
    private void onFiltrar() {

        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();

        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            showError("La fecha 'Desde' no puede ser posterior a la fecha 'Hasta'.");
            return;
        }

        loadRegistrations();
    }

    @FXML
    private void onLimpiarFiltro() {

        tournamentFilter.setValue(null);
        coupleFilter.setValue(null);
        dpDesde.setValue(null);
        dpHasta.setValue(null);

        loadRegistrations();
    }
    
    private void openDialog(TournamentRegistration registration) {

        try {

            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                    "/fxml/dialog/TournamentRegistrationDialog.fxml"
                )
            );

            Parent root = loader.load();

            TournamentRegistrationDialogController controller =
                loader.getController();

            Stage dialogStage = new Stage();

            dialogStage.setTitle(
                registration == null
                    ? "Nueva inscripción"
                    : "Editar inscripción"
            );

            dialogStage.initModality(Modality.APPLICATION_MODAL);

            Window owner = registrationTable.getScene().getWindow();

            if (owner != null) {
                dialogStage.initOwner(owner);
            }

            dialogStage.setScene(new Scene(root));

            controller.init(
                registration,
                service,
                tournamentService,
                coupleService,
                new PlayerDAOImpl(DataSourceProvider.getDataSource())
            );

            controller.setStage(dialogStage);

            dialogStage.showAndWait();

            if (controller.isSaved()) {
                loadRegistrations();
            }

        } catch (IOException e) {

            e.printStackTrace();
            showError("Error abriendo el diálogo de inscripción.");
        }
    }

    // -------------------------------------------------
    // Utils
    // -------------------------------------------------

    private void showError(String msg) {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showInfo(String msg) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
