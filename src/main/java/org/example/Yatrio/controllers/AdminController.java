package org.example.Yatrio.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.Yatrio.dao.*;
import org.example.Yatrio.models.*;
import org.example.Yatrio.services.DataExportService;
import org.example.Yatrio.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AdminController implements Initializable {

    @FXML private ToggleButton analyticsTab;
    @FXML private ToggleButton touristTab;
    @FXML private ToggleButton guideTab;
    @FXML private ToggleButton attractionTab;
    @FXML private ToggleButton bookingTab;
    @FXML private ToggleButton emergencyTab;

    @FXML private VBox analyticsPane;
    @FXML private VBox touristPane;
    @FXML private VBox guidePane;
    @FXML private VBox attractionPane;
    @FXML private VBox bookingPane;
    @FXML private VBox emergencyPane;

    // Analytics Components
    @FXML private Button refreshAnalyticsButton;
    @FXML private Label lastUpdatedLabel;

    // Summary Cards
    @FXML private Label totalTouristsLabel;
    @FXML private Label touristGrowthLabel;
    @FXML private Label activeGuidesLabel;
    @FXML private Label guideStatusLabel;
    @FXML private Label totalBookingsLabel;
    @FXML private Label bookingStatusLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label revenueStatusLabel;

    // Charts
    @FXML private PieChart mainPieChart;
    @FXML private BarChart<String, Number> popularAttractionsChart;
    @FXML private CategoryAxis barChartXAxis;
    @FXML private NumberAxis barChartYAxis;
    @FXML private ComboBox<String> chartTypeComboBox;
    @FXML private ComboBox<String> barChartTypeComboBox;

    // Tourist Table
    @FXML private TableView<User> touristTable;
    @FXML private TableColumn<User, String> touristNameColumn;
    @FXML private TableColumn<User, String> touristNationalityColumn;
    @FXML private TableColumn<User, String> touristContactColumn;
    @FXML private TableColumn<User, String> touristEmergencyColumn;
    @FXML private TableColumn<User, String> touristRegDateColumn;
    @FXML private TableColumn<User, String> touristStatusColumn;
    @FXML private TableColumn<User, Void> touristActionColumn;
    @FXML private TextField touristSearchField;
    @FXML private ComboBox<String> touristNationalityFilter;

    // Guide Table
    @FXML private TableView<Guide> guideTable;
    @FXML private TableColumn<Guide, String> guideNameColumn;
    @FXML private TableColumn<Guide, String> guideLanguageColumn;
    @FXML private TableColumn<Guide, Integer> guideExperienceColumn;
    @FXML private TableColumn<Guide, String> guideSpecializationColumn;
    @FXML private TableColumn<Guide, Double> guideRatingColumn;
    @FXML private TableColumn<Guide, String> guideContactColumn;
    @FXML private TableColumn<Guide, String> guideStatusColumn;
    @FXML private TableColumn<Guide, Void> guideActionColumn;
    @FXML private TextField guideSearchField;
    @FXML private ComboBox<String> guideSpecializationFilter;

    // Attraction Table
    @FXML private TableView<Attraction> attractionTable;
    @FXML private TableColumn<Attraction, String> attractionNameColumn;
    @FXML private TableColumn<Attraction, String> attractionTypeColumn;
    @FXML private TableColumn<Attraction, String> attractionLocationColumn;
    @FXML private TableColumn<Attraction, String> attractionDifficultyColumn;
    @FXML private TableColumn<Attraction, String> attractionAltitudeColumn;
    @FXML private TableColumn<Attraction, String> attractionDurationColumn;
    @FXML private TableColumn<Attraction, String> attractionStatusColumn;
    @FXML private TableColumn<Attraction, Void> attractionActionColumn;
    @FXML private TextField attractionSearchField;
    @FXML private ComboBox<String> attractionTypeFilter;
    @FXML private ComboBox<String> attractionDifficultyFilter;

    // Booking Table
    @FXML private TableView<Booking> bookingTable;
    @FXML private TableColumn<Booking, String> bookingTouristColumn;
    @FXML private TableColumn<Booking, String> bookingAttractionColumn;
    @FXML private TableColumn<Booking, String> bookingDatesColumn;
    @FXML private TableColumn<Booking, Double> bookingCostColumn;
    @FXML private TableColumn<Booking, String> bookingStatusColumn;
    @FXML private TableColumn<Booking, String> bookingGuideColumn;
    @FXML private TableColumn<Booking, Void> bookingAssignColumn;
    @FXML private TableColumn<Booking, Void> bookingActionColumn;
    @FXML private TableColumn<Booking, Void> bookingRemoveColumn;
    @FXML private TextField bookingSearchField;
    @FXML private ComboBox<String> bookingStatusFilter;

    // Emergency Table
    @FXML private TableView<EmergencyContact> emergencyTable;
    @FXML private TableColumn<EmergencyContact, String> emergencyNameColumn;
    @FXML private TableColumn<EmergencyContact, String> emergencyOrgColumn;
    @FXML private TableColumn<EmergencyContact, String> emergencyPhoneColumn;
    @FXML private TableColumn<EmergencyContact, String> emergencyTypeColumn;
    @FXML private TableColumn<EmergencyContact, String> emergencyLocationColumn;
    @FXML private TableColumn<EmergencyContact, String> emergency24_7Column;
    @FXML private TableColumn<EmergencyContact, Void> emergencyActionColumn;

    // DAOs
    private UserDAO userDAO;
    private GuideDAO guideDAO;
    private AttractionDAO attractionDAO;
    private BookingDAO bookingDAO;
    private EmergencyContactDAO emergencyDAO;
    private DataExportService exportService;

    // Data lists
    private ObservableList<User> touristList;
    private ObservableList<Guide> guideList;
    private ObservableList<Attraction> attractionList;
    private ObservableList<Booking> bookingList;
    private ObservableList<Booking> filteredBookingList;
    private ObservableList<EmergencyContact> emergencyList;

    // Analytics data cache
    private Map<String, Object> analyticsCache = new HashMap<>();
    private LocalDateTime lastAnalyticsUpdate;

    // Color arrays for charts
    private final String[] PIE_COLORS = {
            "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7",
            "#DDA0DD", "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E9"
    };
    private final String[] BAR_COLORS = {
            "#FF9F43", "#10AC84", "#EE5A24", "#0984E3", "#A29BFE",
            "#FD79A8", "#FDCB6E", "#6C5CE7", "#00B894", "#E17055"
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeDAOs();
        initializeTables();
        setupTabNavigation();
        setupAnalyticsControls();
        setupChartStyling();
        setupSearchAndFilters();
        loadAllData();
        refreshAnalytics();
        showPane(analyticsPane);
        setActiveTab(analyticsTab);
    }

    private void initializeDAOs() {
        userDAO = new UserDAO();
        guideDAO = new GuideDAO();
        attractionDAO = new AttractionDAO();
        bookingDAO = new BookingDAO();
        emergencyDAO = new EmergencyContactDAO();
        exportService = new DataExportService();
    }

    private void setupAnalyticsControls() {
        if (chartTypeComboBox != null) {
            chartTypeComboBox.setValue("Tourists by Nationality");
        }
        if (barChartTypeComboBox != null) {
            barChartTypeComboBox.setValue("Top Attractions");
        }
    }

    private void setupChartStyling() {
        if (mainPieChart != null) {
            mainPieChart.setLegendVisible(true);
            mainPieChart.setLabelsVisible(true);
            mainPieChart.setStartAngle(90);
            mainPieChart.setClockwise(true);
            mainPieChart.setStyle("-fx-font-size: 14px;");
        }
        if (popularAttractionsChart != null) {
            popularAttractionsChart.setLegendVisible(true);
            popularAttractionsChart.setAnimated(true);
            popularAttractionsChart.setStyle("-fx-font-size: 14px;");
            if (barChartXAxis != null) {
                barChartXAxis.setTickLabelRotation(-45);
                barChartXAxis.setStyle("-fx-font-size: 12px;");
            }
            if (barChartYAxis != null) {
                barChartYAxis.setStyle("-fx-font-size: 12px;");
            }
        }
    }

    private void setupSearchAndFilters() {
        // Tourist search and filter
        if (touristSearchField != null) {
            touristSearchField.textProperty().addListener((observable, oldValue, newValue) -> filterTourists());
        }
        if (touristNationalityFilter != null) {
            touristNationalityFilter.setOnAction(e -> filterTourists());
        }

        // Guide search and filter
        if (guideSearchField != null) {
            guideSearchField.textProperty().addListener((observable, oldValue, newValue) -> filterGuides());
        }
        if (guideSpecializationFilter != null) {
            guideSpecializationFilter.setOnAction(e -> filterGuides());
        }

        // Attraction search and filter
        if (attractionSearchField != null) {
            attractionSearchField.textProperty().addListener((observable, oldValue, newValue) -> filterAttractions());
        }
        if (attractionTypeFilter != null) {
            attractionTypeFilter.setOnAction(e -> filterAttractions());
        }
        if (attractionDifficultyFilter != null) {
            attractionDifficultyFilter.setOnAction(e -> filterAttractions());
        }

        // Booking search and filter
        if (bookingSearchField != null) {
            bookingSearchField.textProperty().addListener((observable, oldValue, newValue) -> filterBookings());
        }
        if (bookingStatusFilter != null) {
            bookingStatusFilter.setOnAction(e -> filterBookings());
        }
    }

    private void setupTabNavigation() {
        analyticsTab.setOnAction(e -> {
            showPane(analyticsPane);
            setActiveTab(analyticsTab);
            refreshAnalytics();
        });

        touristTab.setOnAction(e -> {
            showPane(touristPane);
            setActiveTab(touristTab);
            loadTouristData();
        });

        guideTab.setOnAction(e -> {
            showPane(guidePane);
            setActiveTab(guideTab);
            loadGuideData();
        });

        attractionTab.setOnAction(e -> {
            showPane(attractionPane);
            setActiveTab(attractionTab);
            loadAttractionData();
        });

        bookingTab.setOnAction(e -> {
            showPane(bookingPane);
            setActiveTab(bookingTab);
            loadBookingData();
        });

        emergencyTab.setOnAction(e -> {
            showPane(emergencyPane);
            setActiveTab(emergencyTab);
            loadEmergencyData();
        });
    }

    private void initializeTables() {
        // Tourist Table
        if (touristTable != null) {
            touristNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
            touristNationalityColumn.setCellValueFactory(new PropertyValueFactory<>("nationality"));
            touristContactColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
            touristEmergencyColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getEmergencyContact() != null ?
                            cellData.getValue().getEmergencyContact() : "Not provided"));
            touristRegDateColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getRegistrationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
            touristStatusColumn.setCellValueFactory(cellData -> new SimpleStringProperty("Active"));

            // Setup Action Column with Remove Button
            touristActionColumn.setCellFactory(param -> new TableCell<User, Void>() {
                private final Button removeButton = new Button("Remove");
                {
                    removeButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10;");
                    removeButton.setOnAction(event -> {
                        User tourist = getTableView().getItems().get(getIndex());
                        removeTourist(tourist);
                    });
                }

                @Override
                public void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(removeButton);
                    }
                }
            });

            // Add row selection listener for tourist details
            touristTable.setRowFactory(tv -> {
                TableRow<User> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && (!row.isEmpty())) {
                        User selectedTourist = row.getItem();
                        showTouristDetails(selectedTourist);
                    }
                });
                return row;
            });
        }

        // Guide Table
        if (guideTable != null) {
            guideNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            guideLanguageColumn.setCellValueFactory(new PropertyValueFactory<>("languages"));
            guideExperienceColumn.setCellValueFactory(new PropertyValueFactory<>("experience"));
            guideSpecializationColumn.setCellValueFactory(new PropertyValueFactory<>("specialization"));
            guideRatingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
            guideContactColumn.setCellValueFactory(new PropertyValueFactory<>("contact"));
            guideStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

            // Setup Action Column with Remove and Update Buttons
            guideActionColumn.setCellFactory(param -> new TableCell<Guide, Void>() {
                private final Button updateButton = new Button("Update");
                private final Button removeButton = new Button("Remove");
                private final HBox buttonBox = new HBox(5);

                {
                    updateButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 8;");
                    removeButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 8;");
                    buttonBox.setAlignment(Pos.CENTER);
                    buttonBox.getChildren().addAll(updateButton, removeButton);

                    updateButton.setOnAction(event -> {
                        Guide guide = getTableView().getItems().get(getIndex());
                        updateGuide(guide);
                    });

                    removeButton.setOnAction(event -> {
                        Guide guide = getTableView().getItems().get(getIndex());
                        removeGuide(guide);
                    });
                }

                @Override
                public void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(buttonBox);
                    }
                }
            });

            // Add row selection listener for guide details
            guideTable.setRowFactory(tv -> {
                TableRow<Guide> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && (!row.isEmpty())) {
                        Guide selectedGuide = row.getItem();
                        showGuideDetails(selectedGuide);
                    }
                });
                return row;
            });
        }

        // Attraction Table
        if (attractionTable != null) {
            attractionNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            attractionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
            attractionLocationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
            attractionDifficultyColumn.setCellValueFactory(new PropertyValueFactory<>("difficulty"));
            attractionAltitudeColumn.setCellValueFactory(new PropertyValueFactory<>("altitude"));
            attractionDurationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
            attractionStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

            // Setup Action Column with Update and Remove Buttons for Attractions
            attractionActionColumn.setCellFactory(param -> new TableCell<Attraction, Void>() {
                private final Button updateButton = new Button("Update");
                private final Button removeButton = new Button("Remove");
                private final HBox buttonBox = new HBox(5);

                {
                    updateButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 8;");
                    removeButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 8;");
                    buttonBox.setAlignment(Pos.CENTER);
                    buttonBox.getChildren().addAll(updateButton, removeButton);

                    updateButton.setOnAction(event -> {
                        Attraction attraction = getTableView().getItems().get(getIndex());
                        updateAttraction(attraction);
                    });

                    removeButton.setOnAction(event -> {
                        Attraction attraction = getTableView().getItems().get(getIndex());
                        removeAttraction(attraction);
                    });
                }

                @Override
                public void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(buttonBox);
                    }
                }
            });

            // Add row selection listener for attraction details
            attractionTable.setRowFactory(tv -> {
                TableRow<Attraction> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && (!row.isEmpty())) {
                        Attraction selectedAttraction = row.getItem();
                        showAttractionDetails(selectedAttraction);
                    }
                });
                return row;
            });
        }

        // Booking Table - Enhanced with Assign Guide functionality
        if (bookingTable != null) {
            bookingTouristColumn.setCellValueFactory(new PropertyValueFactory<>("touristName"));
            bookingAttractionColumn.setCellValueFactory(new PropertyValueFactory<>("attractionName"));
            bookingDatesColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getStartDate() + " to " + cellData.getValue().getEndDate()));
            bookingCostColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
            bookingStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

            // Enhanced Assigned Guide Column with better formatting
            bookingGuideColumn.setCellValueFactory(cellData -> {
                Booking booking = cellData.getValue();
                String guideName = booking.getGuideName();
                if (guideName != null && !guideName.trim().isEmpty()) {
                    return new SimpleStringProperty("✅ " + guideName);
                } else {
                    return new SimpleStringProperty("❌ Not Assigned");
                }
            });

            // Setup Assign Guide Column with Button or "Guide Assigned" text
            bookingAssignColumn.setCellFactory(param -> new TableCell<Booking, Void>() {
                private final Button assignButton = new Button("Assign Guide");
                private final Label assignedLabel = new Label("Guide Assigned");

                {
                    assignButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10;");
                    assignedLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold; -fx-font-size: 12px;");
                    assignButton.setOnAction(event -> {
                        Booking booking = getTableView().getItems().get(getIndex());
                        if (booking != null) {
                            openGuideAssignmentDialog(booking);
                        }
                    });
                }

                @Override
                public void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Booking booking = getTableView().getItems().get(getIndex());
                        if (booking != null) {
                            // Show "Guide Assigned" text if guide is assigned and status is confirmed
                            if (booking.getGuideName() != null && !booking.getGuideName().trim().isEmpty() &&
                                    "Confirmed".equals(booking.getStatus())) {
                                setGraphic(assignedLabel);
                            }
                            // Show button for bookings without assigned guides or pending status
                            else if (booking.getGuideName() == null || booking.getGuideName().trim().isEmpty() ||
                                    "Pending".equals(booking.getStatus())) {
                                assignButton.setText(booking.getGuideName() == null || booking.getGuideName().trim().isEmpty() ?
                                        "Assign Guide" : "Change Guide");
                                setGraphic(assignButton);
                            } else {
                                setGraphic(null);
                            }
                        } else {
                            setGraphic(null);
                        }
                    }
                }
            });

            // Setup Details Action Column with Button
            bookingActionColumn.setCellFactory(param -> new TableCell<Booking, Void>() {
                private final Button detailsButton = new Button("Details");

                {
                    detailsButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10;");
                    detailsButton.setOnAction(event -> {
                        Booking booking = getTableView().getItems().get(getIndex());
                        if (booking != null) {
                            showBookingDetails(booking);
                        }
                    });
                }

                @Override
                public void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(detailsButton);
                    }
                }
            });

            // Setup Remove Action Column with Remove Button
            bookingRemoveColumn.setCellFactory(param -> new TableCell<Booking, Void>() {
                private final Button removeButton = new Button("Remove");

                {
                    removeButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10;");
                    removeButton.setOnAction(event -> {
                        Booking booking = getTableView().getItems().get(getIndex());
                        if (booking != null) {
                            removeBooking(booking);
                        }
                    });
                }

                @Override
                public void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(removeButton);
                    }
                }
            });

            // Setup context menu for right-click actions
            bookingTable.setRowFactory(tv -> {
                TableRow<Booking> row = new TableRow<>();

                // Context menu for right-click
                ContextMenu contextMenu = new ContextMenu();

                MenuItem assignGuideItem = new MenuItem("Assign Guide");
                assignGuideItem.setOnAction(event -> {
                    Booking booking = row.getItem();
                    if (booking != null) {
                        openGuideAssignmentDialog(booking);
                    }
                });

                MenuItem viewDetailsItem = new MenuItem("View Details");
                viewDetailsItem.setOnAction(event -> {
                    Booking booking = row.getItem();
                    if (booking != null) {
                        showBookingDetails(booking);
                    }
                });

                MenuItem changeStatusItem = new MenuItem("Change Status");
                changeStatusItem.setOnAction(event -> {
                    Booking booking = row.getItem();
                    if (booking != null) {
                        changeBookingStatus(booking);
                    }
                });

                MenuItem removeBookingItem = new MenuItem("Remove Booking");
                removeBookingItem.setOnAction(event -> {
                    Booking booking = row.getItem();
                    if (booking != null) {
                        removeBooking(booking);
                    }
                });

                contextMenu.getItems().addAll(assignGuideItem, viewDetailsItem, changeStatusItem,
                        new SeparatorMenuItem(), removeBookingItem);

                row.setContextMenu(contextMenu);

                // Double-click for details
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && (!row.isEmpty())) {
                        Booking selectedBooking = row.getItem();
                        showBookingDetails(selectedBooking);
                    }
                });

                return row;
            });
        }

        // Emergency Table
        if (emergencyTable != null) {
            emergencyNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            emergencyOrgColumn.setCellValueFactory(new PropertyValueFactory<>("organization"));
            emergencyPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
            emergencyTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
            emergencyLocationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
            emergency24_7Column.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().isAvailable24_7() ? "Yes" : "No"));

            // Setup Action Column with Update and Remove Buttons for Emergency Contacts
            emergencyActionColumn.setCellFactory(param -> new TableCell<EmergencyContact, Void>() {
                private final Button updateButton = new Button("Update");
                private final Button removeButton = new Button("Remove");
                private final HBox buttonBox = new HBox(5);

                {
                    updateButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 8;");
                    removeButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 8;");
                    buttonBox.setAlignment(Pos.CENTER);
                    buttonBox.getChildren().addAll(updateButton, removeButton);

                    updateButton.setOnAction(event -> {
                        EmergencyContact contact = getTableView().getItems().get(getIndex());
                        updateEmergencyContact(contact);
                    });

                    removeButton.setOnAction(event -> {
                        EmergencyContact contact = getTableView().getItems().get(getIndex());
                        removeEmergencyContact(contact);
                    });
                }

                @Override
                public void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(buttonBox);
                    }
                }
            });

            // Add row selection listener for emergency contact details
            emergencyTable.setRowFactory(tv -> {
                TableRow<EmergencyContact> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && (!row.isEmpty())) {
                        EmergencyContact selectedContact = row.getItem();
                        showEmergencyContactDetails(selectedContact);
                    }
                });
                return row;
            });
        }
    }

    private void removeBooking(Booking booking) {
        // Show confirmation dialog with warning
        Alert confirmAlert = new Alert(Alert.AlertType.WARNING);
        confirmAlert.setTitle("⚠️ Remove Booking - Warning");
        confirmAlert.setHeaderText("Are you sure you want to remove this booking?");

        StringBuilder message = new StringBuilder();
        message.append("📋 BOOKING DETAILS:\n");
        message.append("Tourist: ").append(booking.getTouristName()).append("\n");
        message.append("Email: ").append(booking.getTouristEmail()).append("\n");
        message.append("Attraction: ").append(booking.getAttractionName()).append("\n");
        message.append("Dates: ").append(booking.getStartDate()).append(" to ").append(booking.getEndDate()).append("\n");
        message.append("Total Cost: $").append(String.format("%.2f", booking.getTotalPrice())).append("\n");
        message.append("Status: ").append(booking.getStatus()).append("\n");
        message.append("Guide: ").append(booking.getGuideName() != null ? booking.getGuideName() : "Not Assigned").append("\n\n");

        message.append("⚠️ CRITICAL WARNING:\n");
        message.append("• This will PERMANENTLY delete the booking from the system\n");
        message.append("• The booking will be removed from the tourist's dashboard\n");
        message.append("• The tourist will lose access to this booking information\n");
        message.append("• Any assigned guide will be unassigned from this booking\n");
        message.append("• This action CANNOT be undone\n");
        message.append("• Consider changing status to 'Cancelled' instead of removing\n\n");

        message.append("💡 RECOMMENDATION:\n");
        message.append("Instead of removing, consider:\n");
        message.append("• Changing status to 'Cancelled' to keep records\n");
        message.append("• Contacting the tourist before removal\n");
        message.append("• Documenting the reason for removal\n\n");

        message.append("Are you absolutely sure you want to proceed with PERMANENT removal?");

        confirmAlert.setContentText(message.toString());

        // Customize buttons with clear warning
        ButtonType removeButton = new ButtonType("⚠️ Yes, PERMANENTLY Remove", ButtonBar.ButtonData.YES);
        ButtonType cancelButton = new ButtonType("❌ Cancel (Recommended)", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType changeStatusButton = new ButtonType("📝 Change Status Instead", ButtonBar.ButtonData.OTHER);

        confirmAlert.getButtonTypes().setAll(removeButton, changeStatusButton, cancelButton);

        // Set default button to Cancel for safety
        Button defaultButton = (Button) confirmAlert.getDialogPane().lookupButton(cancelButton);
        defaultButton.setDefaultButton(true);

        // Style the buttons
        Button removeBtn = (Button) confirmAlert.getDialogPane().lookupButton(removeButton);
        Button changeStatusBtn = (Button) confirmAlert.getDialogPane().lookupButton(changeStatusButton);
        Button cancelBtn = (Button) confirmAlert.getDialogPane().lookupButton(cancelButton);

        if (removeBtn != null) {
            removeBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold;");
        }
        if (changeStatusBtn != null) {
            changeStatusBtn.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; -fx-font-weight: bold;");
        }
        if (cancelBtn != null) {
            cancelBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");
        }

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == removeButton) {
                // Proceed with permanent removal
                try {
                    // Remove from database - this will automatically remove it from tourist dashboard
                    // since the tourist dashboard loads bookings from the same database
                    boolean deleted = bookingDAO.delete(booking.getId());

                    if (deleted) {
                        // Remove from table
                        bookingTable.getItems().remove(booking);
                        bookingList.remove(booking);

                        // Update filtered list if search/filter is active
                        filterBookings();

                        // Refresh analytics to reflect the change
                        refreshAnalytics();

                        // Show success message
                        showAlert("✅ Booking Removed Successfully",
                                "The booking has been permanently removed from the system.\n\n" +
                                        "📋 Removed Booking:\n" +
                                        "Tourist: " + booking.getTouristName() + "\n" +
                                        "Attraction: " + booking.getAttractionName() + "\n" +
                                        "Dates: " + booking.getStartDate() + " to " + booking.getEndDate() + "\n\n" +
                                        "✅ The booking has been removed from:\n" +
                                        "• Admin booking management system\n" +
                                        "• Tourist's personal dashboard\n" +
                                        "• All system records\n\n" +
                                        "📧 Consider notifying the tourist about this change.");

                        System.out.println("Booking removed successfully: " + booking.getId() + " for tourist: " + booking.getTouristName());
                    } else {
                        showAlert("❌ Error", "Failed to remove booking. Please try again.");
                    }
                } catch (Exception e) {
                    System.err.println("Error removing booking: " + e.getMessage());
                    e.printStackTrace();
                    showAlert("❌ Error", "An error occurred while removing the booking:\n" + e.getMessage());
                }
            } else if (result.get() == changeStatusButton) {
                // Open change status dialog instead
                changeBookingStatus(booking);
            }
            // If cancel button is clicked, do nothing (dialog closes automatically)
        }
    }

    private void openGuideAssignmentDialog(Booking booking) {
        try {
            // Create a dialog for guide assignment
            Dialog<Guide> dialog = new Dialog<>();
            dialog.setTitle("Assign Guide to Booking");
            dialog.setHeaderText("Select a guide for: " + booking.getTouristName() + " - " + booking.getAttractionName());

            // Set the button types
            ButtonType assignButtonType = new ButtonType("Assign Guide", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(assignButtonType, ButtonType.CANCEL);

            // Create the guide selection interface
            VBox content = new VBox(15);
            content.setPrefWidth(600);

            // Booking information
            VBox bookingInfo = new VBox(5);
            bookingInfo.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-border-radius: 5; -fx-background-radius: 5;");
            bookingInfo.getChildren().addAll(
                    new Label("📋 Booking Information"),
                    new Label("Tourist: " + booking.getTouristName()),
                    new Label("Attraction: " + booking.getAttractionName()),
                    new Label("Dates: " + booking.getStartDate() + " to " + booking.getEndDate()),
                    new Label("Status: " + booking.getStatus()),
                    new Label("Current Guide: " + (booking.getGuideName() != null ? booking.getGuideName() : "Not Assigned"))
            );

            // Guide selection
            Label guideLabel = new Label("👨‍🏫 Available Guides:");
            guideLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            TableView<Guide> guideSelectionTable = new TableView<>();
            guideSelectionTable.setPrefHeight(300);

            TableColumn<Guide, String> nameCol = new TableColumn<>("Name");
            nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            nameCol.setPrefWidth(120);

            TableColumn<Guide, String> languageCol = new TableColumn<>("Languages");
            languageCol.setCellValueFactory(new PropertyValueFactory<>("languages"));
            languageCol.setPrefWidth(120);

//            TableColumn<Guide, String> specializationCol = new PropertyValueFactory<>("Specialization");
            TableColumn<Guide, String> specializationCol = new TableColumn<>("Specialization");
            specializationCol.setCellValueFactory(new PropertyValueFactory<>("specialization"));
            specializationCol.setPrefWidth(140);

            TableColumn<Guide, Integer> experienceCol = new TableColumn<>("Experience");
            experienceCol.setCellValueFactory(new PropertyValueFactory<>("experience"));
            experienceCol.setPrefWidth(80);

            TableColumn<Guide, Double> ratingCol = new TableColumn<>("Rating");
            ratingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));
            ratingCol.setPrefWidth(60);

            TableColumn<Guide, String> contactCol = new TableColumn<>("Contact");
            contactCol.setCellValueFactory(new PropertyValueFactory<>("contact"));
            contactCol.setPrefWidth(140);

            guideSelectionTable.getColumns().addAll(nameCol, languageCol, specializationCol, experienceCol, ratingCol, contactCol);

            // Load available guides (Active status only)
            List<Guide> availableGuides = guideDAO.findAll().stream()
                    .filter(guide -> "Active".equals(guide.getStatus()))
                    .collect(Collectors.toList());
            guideSelectionTable.setItems(FXCollections.observableArrayList(availableGuides));

            // Selection mode
            guideSelectionTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

            content.getChildren().addAll(bookingInfo, guideLabel, guideSelectionTable);
            dialog.getDialogPane().setContent(content);

            // Enable/Disable assign button based on selection
            Button assignButton = (Button) dialog.getDialogPane().lookupButton(assignButtonType);
            assignButton.setDisable(true);

            guideSelectionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                assignButton.setDisable(newSelection == null);
            });

            // Convert the result when the assign button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == assignButtonType) {
                    return guideSelectionTable.getSelectionModel().getSelectedItem();
                }
                return null;
            });

            Optional<Guide> result = dialog.showAndWait();
            result.ifPresent(selectedGuide -> {
                try {
                    // Assign guide to booking
                    booking.assignGuide(selectedGuide.getId(), selectedGuide.getName());
                    booking.setStatus("Confirmed"); // Update status to confirmed when guide is assigned
                    bookingDAO.update(booking);

                    // Refresh the booking table to show the assigned guide
                    refreshBookingTable();

                    // Refresh analytics
                    refreshAnalytics();

                    // Show success message
                    showAlert("Success",
                            "Guide assigned successfully!\n\n" +
                                    "Guide: " + selectedGuide.getName() + "\n" +
                                    "Contact: " + selectedGuide.getContact() + "\n" +
                                    "Booking Status: Confirmed\n\n" +
                                    "The tourist will see the assigned guide information in their dashboard.");

                    System.out.println("Guide assigned: " + selectedGuide.getName() + " to booking: " + booking.getId());
                } catch (IOException e) {
                    System.err.println("Error assigning guide: " + e.getMessage());
                    e.printStackTrace();
                    showAlert("Error", "Failed to assign guide: " + e.getMessage());
                }
            });
        } catch (IOException e) {
            System.err.println("Error loading guides for assignment: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to load available guides: " + e.getMessage());
        }
    }

    private void refreshBookingTable() {
        try {
            // Reload booking data from database
            List<Booking> bookings = bookingDAO.findAll();
            bookingList = FXCollections.observableArrayList(bookings);

            // Update the table with fresh data
            if (bookingTable != null) {
                bookingTable.setItems(bookingList);
                bookingTable.refresh(); // Force table refresh
            }

            // Apply current filters
            filterBookings();

            System.out.println("Booking table refreshed with " + bookings.size() + " bookings");
        } catch (IOException e) {
            System.err.println("Error refreshing booking table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void changeBookingStatus(Booking booking) {
        // Create a dialog for changing booking status
        ChoiceDialog<String> dialog = new ChoiceDialog<>(booking.getStatus(),
                "Pending", "Confirmed", "Completed", "Cancelled");
        dialog.setTitle("Change Booking Status");
        dialog.setHeaderText("Change status for booking: " + booking.getTouristName());
        dialog.setContentText("Select new status:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newStatus -> {
            try {
                booking.setStatus(newStatus);
                bookingDAO.update(booking);
                refreshBookingTable();
                refreshAnalytics();
                showAlert("Success", "Booking status updated to: " + newStatus);
            } catch (IOException e) {
                showAlert("Error", "Failed to update booking status: " + e.getMessage());
            }
        });
    }

    // Method called by GuideSelectionController when a guide is assigned
    public void onGuideAssigned(Booking booking, Guide selectedGuide) {
        try {
            // Update the booking with guide information
            booking.assignGuide(selectedGuide.getId(), selectedGuide.getName());
            booking.setStatus("Confirmed");

            // Save the updated booking
            bookingDAO.update(booking);

            // Refresh the bookings table
            refreshBookingTable();

            // Refresh analytics
            refreshAnalytics();

            // Show success message
            showAlert("Success", String.format(
                    "Guide '%s' has been successfully assigned to booking for %s.\n" +
                            "Booking status updated to 'Confirmed'.",
                    selectedGuide.getName(),
                    booking.getTouristName()
            ));

            System.out.println("Guide assigned via callback: " + selectedGuide.getName() + " to booking: " + booking.getId());
        } catch (IOException e) {
            System.err.println("Error in onGuideAssigned: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to assign guide: " + e.getMessage());
        }
    }

    private void loadAllData() {
        loadTouristData();
        loadGuideData();
        loadAttractionData();
        loadBookingData();
        loadEmergencyData();
    }

    private void loadTouristData() {
        try {
            List<User> tourists = userDAO.findAll().stream()
                    .filter(user -> "Tourist".equals(user.getRole()))
                    .collect(Collectors.toList());
            touristList = FXCollections.observableArrayList(tourists);
            if (touristTable != null) {
                touristTable.setItems(touristList);
            }

            // Update nationality filter
            if (touristNationalityFilter != null) {
                List<String> nationalities = tourists.stream()
                        .map(User::getNationality)
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList());
                ObservableList<String> nationalityOptions = FXCollections.observableArrayList();
                nationalityOptions.add("All Nationalities");
                nationalityOptions.addAll(nationalities);
                touristNationalityFilter.setItems(nationalityOptions);
                touristNationalityFilter.setValue("All Nationalities");
            }

            System.out.println("Loaded " + tourists.size() + " tourists in admin panel");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load tourist data: " + e.getMessage());
        }
    }

    private void loadGuideData() {
        try {
            List<Guide> guides = guideDAO.findAll();
            guideList = FXCollections.observableArrayList(guides);
            if (guideTable != null) {
                guideTable.setItems(guideList);
            }

            // Update specialization filter
            if (guideSpecializationFilter != null) {
                List<String> specializations = guides.stream()
                        .map(Guide::getSpecialization)
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList());
                ObservableList<String> specializationOptions = FXCollections.observableArrayList();
                specializationOptions.add("All Specializations");
                specializationOptions.addAll(specializations);
                guideSpecializationFilter.setItems(specializationOptions);
                guideSpecializationFilter.setValue("All Specializations");
            }

            System.out.println("Loaded " + guides.size() + " guides in admin panel");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load guide data: " + e.getMessage());
        }
    }

    private void loadAttractionData() {
        try {
            List<Attraction> attractions = attractionDAO.findAll();
            attractionList = FXCollections.observableArrayList(attractions);
            if (attractionTable != null) {
                attractionTable.setItems(attractionList);
            }

            // Update type and difficulty filters
            if (attractionTypeFilter != null) {
                List<String> types = attractions.stream()
                        .map(Attraction::getType)
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList());
                ObservableList<String> typeOptions = FXCollections.observableArrayList();
                typeOptions.add("All Types");
                typeOptions.addAll(types);
                attractionTypeFilter.setItems(typeOptions);
                attractionTypeFilter.setValue("All Types");
            }

            if (attractionDifficultyFilter != null) {
                List<String> difficulties = attractions.stream()
                        .map(Attraction::getDifficulty)
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList());
                ObservableList<String> difficultyOptions = FXCollections.observableArrayList();
                difficultyOptions.add("All Difficulties");
                difficultyOptions.addAll(difficulties);
                attractionDifficultyFilter.setItems(difficultyOptions);
                attractionDifficultyFilter.setValue("All Difficulties");
            }

            System.out.println("Loaded " + attractions.size() + " attractions in admin panel");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load attraction data: " + e.getMessage());
        }
    }

    @FXML
    public void loadBookingData() {
        try {
            List<Booking> bookings = bookingDAO.findAll();
            bookingList = FXCollections.observableArrayList(bookings);
            if (bookingTable != null) {
                bookingTable.setItems(bookingList);
            }

            // Initialize filtered list
            filteredBookingList = FXCollections.observableArrayList(bookings);

            // Update booking status filter
            if (bookingStatusFilter != null) {
                List<String> statuses = bookings.stream()
                        .map(Booking::getStatus)
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList());
                ObservableList<String> statusOptions = FXCollections.observableArrayList();
                statusOptions.add("All Statuses");
                statusOptions.addAll(statuses);

                // Add guide assignment status options
                statusOptions.add("--- Guide Status ---");
                statusOptions.add("Guide Assigned");
                statusOptions.add("Guide Not Assigned");

                bookingStatusFilter.setItems(statusOptions);
                bookingStatusFilter.setValue("All Statuses");
            }

            System.out.println("Loaded " + bookings.size() + " bookings in admin panel");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load booking data: " + e.getMessage());
        }
    }

    private void loadEmergencyData() {
        try {
            List<EmergencyContact> contacts = emergencyDAO.findAll();
            emergencyList = FXCollections.observableArrayList(contacts);
            if (emergencyTable != null) {
                emergencyTable.setItems(emergencyList);
            }

            System.out.println("Loaded " + contacts.size() + " emergency contacts in admin panel");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load emergency contact data: " + e.getMessage());
        }
    }

    @FXML
    private void refreshAnalytics() {
        try {
            System.out.println("Refreshing analytics data with colorful charts...");
            // Load fresh data
            loadAllData();

            // Update summary cards
            updateSummaryCards();

            // Update charts with colorful styling
            updatePieChart();
            updateBarChart();

            // Update last updated timestamp
            lastAnalyticsUpdate = LocalDateTime.now();
            if (lastUpdatedLabel != null) {
                lastUpdatedLabel.setText("Last updated: " +
                        lastAnalyticsUpdate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
            }

            System.out.println("Colorful analytics refresh completed successfully");
        } catch (Exception e) {
            System.err.println("Error refreshing analytics: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to refresh analytics: " + e.getMessage());
        }
    }

    @FXML
    private void refreshBookingData() {
        try {
            System.out.println("Refreshing booking data...");
            refreshBookingTable();
            showAlert("Success", "Booking data refreshed successfully!");
        } catch (Exception e) {
            System.err.println("Error refreshing booking data: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to refresh booking data: " + e.getMessage());
        }
    }

    private void updateSummaryCards() {
        try {
            // Calculate real statistics
            List<User> tourists = userDAO.findAll().stream()
                    .filter(user -> "Tourist".equals(user.getRole()))
                    .collect(Collectors.toList());
            List<Guide> guides = guideDAO.findAll();
            List<Booking> bookings = bookingDAO.findAll();
            List<Attraction> attractions = attractionDAO.findAll();

            // Update tourist card
            if (totalTouristsLabel != null) {
                totalTouristsLabel.setText(String.valueOf(tourists.size()));
            }
            if (touristGrowthLabel != null) {
                long recentTourists = tourists.stream()
                        .filter(t -> t.getRegistrationDate().isAfter(LocalDateTime.now().minusDays(30)))
                        .count();
                touristGrowthLabel.setText("+" + recentTourists + " this month");
            }

            // Update guides card
            if (activeGuidesLabel != null) {
                long activeGuides = guides.stream()
                        .filter(g -> "Active".equals(g.getStatus()))
                        .count();
                activeGuidesLabel.setText(String.valueOf(activeGuides));
            }
            if (guideStatusLabel != null) {
                guideStatusLabel.setText(guides.size() + " total guides");
            }

            // Update bookings card
            if (totalBookingsLabel != null) {
                totalBookingsLabel.setText(String.valueOf(bookings.size()));
            }
            if (bookingStatusLabel != null) {
                long pendingBookings = bookings.stream()
                        .filter(b -> "Pending".equals(b.getStatus()))
                        .count();
                bookingStatusLabel.setText(pendingBookings + " pending");
            }

            // Update revenue card
            double totalRevenue = bookings.stream()
                    .filter(b -> !"Cancelled".equals(b.getStatus()))
                    .mapToDouble(Booking::getTotalPrice)
                    .sum();
            if (totalRevenueLabel != null) {
                totalRevenueLabel.setText("$" + String.format("%.2f", totalRevenue));
            }
            if (revenueStatusLabel != null) {
                double avgBookingValue = bookings.isEmpty() ? 0 : totalRevenue / bookings.size();
                revenueStatusLabel.setText("Avg: $" + String.format("%.2f", avgBookingValue));
            }

            // Cache the data
            analyticsCache.put("tourists", tourists);
            analyticsCache.put("guides", guides);
            analyticsCache.put("bookings", bookings);
            analyticsCache.put("attractions", attractions);
            analyticsCache.put("totalRevenue", totalRevenue);
        } catch (IOException e) {
            System.err.println("Error updating summary cards: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void changeChartType() {
        updatePieChart();
    }

    @FXML
    private void changeBarChartType() {
        updateBarChart();
    }

    private void updatePieChart() {
        try {
            if (mainPieChart == null || chartTypeComboBox == null) return;

            String chartType = chartTypeComboBox.getValue();
            if (chartType == null) chartType = "Tourists by Nationality";

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

            switch (chartType) {
                case "Tourists by Nationality":
                    @SuppressWarnings("unchecked")
                    List<User> tourists = (List<User>) analyticsCache.get("tourists");
                    if (tourists == null) {
                        tourists = userDAO.findAll().stream()
                                .filter(user -> "Tourist".equals(user.getRole()))
                                .collect(Collectors.toList());
                    }

                    // Normalize nationalities and group them
                    Map<String, Long> nationalityCount = tourists.stream()
                            .collect(Collectors.groupingBy(
                                    user -> normalizeNationality(user.getNationality()),
                                    Collectors.counting()
                            ));

                    for (Map.Entry<String, Long> entry : nationalityCount.entrySet()) {
                        pieChartData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
                    }
                    mainPieChart.setTitle("Tourist Distribution by Nationality");
                    break;

                case "Bookings by Status":
                    @SuppressWarnings("unchecked")
                    List<Booking> bookings = (List<Booking>) analyticsCache.get("bookings");
                    if (bookings == null) bookings = bookingDAO.findAll();
                    Map<String, Long> statusCount = bookings.stream()
                            .collect(Collectors.groupingBy(Booking::getStatus, Collectors.counting()));
                    for (Map.Entry<String, Long> entry : statusCount.entrySet()) {
                        pieChartData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
                    }
                    mainPieChart.setTitle("Booking Status Distribution");
                    break;

                case "Attractions by Type":
                    @SuppressWarnings("unchecked")
                    List<Attraction> attractions = (List<Attraction>) analyticsCache.get("attractions");
                    if (attractions == null) attractions = attractionDAO.findAll();
                    Map<String, Long> typeCount = attractions.stream()
                            .collect(Collectors.groupingBy(Attraction::getType, Collectors.counting()));
                    for (Map.Entry<String, Long> entry : typeCount.entrySet()) {
                        pieChartData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
                    }
                    mainPieChart.setTitle("Attraction Type Distribution");
                    break;
            }

            mainPieChart.setData(pieChartData);
            // Apply different colors to pie chart segments
            applyPieChartColors();
        } catch (IOException e) {
            System.err.println("Error updating pie chart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String normalizeNationality(String nationality) {
        if (nationality == null || nationality.trim().isEmpty()) {
            return "Unknown";
        }

        String normalized = nationality.trim().toLowerCase();

        // Create a map of common nationality variations to their standard form
        Map<String, String> nationalityMap = new HashMap<>();

        // Nepal variations
        nationalityMap.put("nepal", "Nepali");
        nationalityMap.put("nepali", "Nepali");
        nationalityMap.put("nepalese", "Nepali");

        // India variations
        nationalityMap.put("india", "Indian");
        nationalityMap.put("indian", "Indian");
        nationalityMap.put("bharat", "Indian");
        nationalityMap.put("bharatiya", "Indian");

        // China variations
        nationalityMap.put("china", "Chinese");
        nationalityMap.put("chinese", "Chinese");
        nationalityMap.put("prc", "Chinese");

        // USA variations
        nationalityMap.put("usa", "American");
        nationalityMap.put("us", "American");
        nationalityMap.put("america", "American");
        nationalityMap.put("american", "American");
        nationalityMap.put("united states", "American");

        // UK variations
        nationalityMap.put("uk", "British");
        nationalityMap.put("britain", "British");
        nationalityMap.put("british", "British");
        nationalityMap.put("england", "British");
        nationalityMap.put("english", "British");
        nationalityMap.put("united kingdom", "British");

        // Germany variations
        nationalityMap.put("germany", "German");
        nationalityMap.put("german", "German");
        nationalityMap.put("deutschland", "German");

        // France variations
        nationalityMap.put("france", "French");
        nationalityMap.put("french", "French");

        // Japan variations
        nationalityMap.put("japan", "Japanese");
        nationalityMap.put("japanese", "Japanese");
        nationalityMap.put("nippon", "Japanese");

        // Australia variations
        nationalityMap.put("australia", "Australian");
        nationalityMap.put("australian", "Australian");
        nationalityMap.put("aussie", "Australian");

        // Canada variations
        nationalityMap.put("canada", "Canadian");
        nationalityMap.put("canadian", "Canadian");

        // Check if we have a mapping for this nationality
        String standardForm = nationalityMap.get(normalized);
        if (standardForm != null) {
            return standardForm;
        }

        // If no specific mapping found, capitalize first letter of each word
        String[] words = normalized.split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1));
                }
            }
        }

        return result.toString();
    }

    private void applyPieChartColors() {
        // Apply colors after the chart is rendered
        javafx.application.Platform.runLater(() -> {
            for (int i = 0; i < mainPieChart.getData().size(); i++) {
                PieChart.Data data = mainPieChart.getData().get(i);
                if (data.getNode() != null) {
                    String color = PIE_COLORS[i % PIE_COLORS.length];
                    data.getNode().setStyle("-fx-pie-color: " + color + ";");
                }
            }
        });
    }

    private void updateBarChart() {
        try {
            if (popularAttractionsChart == null || barChartTypeComboBox == null) return;

            String chartType = barChartTypeComboBox.getValue();
            if (chartType == null) chartType = "Top Attractions";

            popularAttractionsChart.getData().clear();

            switch (chartType) {
                case "Top Attractions":
                    updateTopAttractionsChart();
                    break;
                case "Monthly Bookings":
                    updateMonthlyBookingsChart();
                    break;
                case "Guide Performance":
                    updateGuidePerformanceChart();
                    break;
            }

            // Enhanced styling for larger bar chart
            popularAttractionsChart.setBarGap(3);
            popularAttractionsChart.setCategoryGap(10);
            // Apply colors to bar chart
            applyBarChartColors();
        } catch (Exception e) {
            System.err.println("Error updating bar chart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void applyBarChartColors() {
        // Apply colors after the chart is rendered
        javafx.application.Platform.runLater(() -> {
            for (int seriesIndex = 0; seriesIndex < popularAttractionsChart.getData().size(); seriesIndex++) {
                XYChart.Series<String, Number> series = popularAttractionsChart.getData().get(seriesIndex);
                for (int dataIndex = 0; dataIndex < series.getData().size(); dataIndex++) {
                    XYChart.Data<String, Number> data = series.getData().get(dataIndex);
                    if (data.getNode() != null) {
                        String color = BAR_COLORS[dataIndex % BAR_COLORS.length];
                        data.getNode().setStyle("-fx-bar-fill: " + color + ";");
                    }
                }
            }
        });
    }

    private void updateTopAttractionsChart() throws IOException {
        @SuppressWarnings("unchecked")
        List<Booking> bookings = (List<Booking>) analyticsCache.get("bookings");
        if (bookings == null) bookings = bookingDAO.findAll();

        Map<String, Long> attractionBookings = bookings.stream()
                .collect(Collectors.groupingBy(Booking::getAttractionName, Collectors.counting()));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Number of Bookings");

        attractionBookings.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(15)
                .forEach(entry -> {
                    String shortName = entry.getKey().length() > 20 ?
                            entry.getKey().substring(0, 17) + "..." : entry.getKey();
                    series.getData().add(new XYChart.Data<>(shortName, entry.getValue()));
                });

        popularAttractionsChart.getData().add(series);
        barChartXAxis.setLabel("Tourist Attractions");
        barChartYAxis.setLabel("Number of Bookings");
        popularAttractionsChart.setTitle("Top 15 Most Popular Attractions");
    }

    private void updateMonthlyBookingsChart() throws IOException {
        @SuppressWarnings("unchecked")
        List<Booking> bookings = (List<Booking>) analyticsCache.get("bookings");
        if (bookings == null) bookings = bookingDAO.findAll();

        Map<String, Long> monthlyBookings = bookings.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getBookingDate().format(DateTimeFormatter.ofPattern("MMM yyyy")),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Monthly Bookings");

        monthlyBookings.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue())));

        popularAttractionsChart.getData().add(series);
        barChartXAxis.setLabel("Month");
        barChartYAxis.setLabel("Number of Bookings");
        popularAttractionsChart.setTitle("Monthly Booking Trends");
    }

    private void updateGuidePerformanceChart() throws IOException {
        @SuppressWarnings("unchecked")
        List<Booking> bookings = (List<Booking>) analyticsCache.get("bookings");
        if (bookings == null) bookings = bookingDAO.findAll();

        Map<String, Long> guideBookings = bookings.stream()
                .filter(b -> b.getGuideName() != null && !b.getGuideName().isEmpty())
                .collect(Collectors.groupingBy(Booking::getGuideName, Collectors.counting()));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Bookings Handled");

        guideBookings.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(12)
                .forEach(entry -> series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue())));

        popularAttractionsChart.getData().add(series);
        barChartXAxis.setLabel("Tour Guides");
        barChartYAxis.setLabel("Number of Bookings");
        popularAttractionsChart.setTitle("Top 12 Guide Performance");
    }

    // Filter methods
    private void filterTourists() {
        if (touristList == null) return;

        String searchText = touristSearchField.getText().toLowerCase().trim();
        String selectedNationality = touristNationalityFilter.getValue();

        ObservableList<User> filteredList = touristList.stream()
                .filter(tourist -> {
                    // Search filter
                    boolean matchesSearch = searchText.isEmpty() ||
                            tourist.getFullName().toLowerCase().contains(searchText) ||
                            tourist.getEmail().toLowerCase().contains(searchText) ||
                            (tourist.getEmergencyContact() != null &&
                                    tourist.getEmergencyContact().toLowerCase().contains(searchText));

                    // Nationality filter
                    boolean matchesNationality = selectedNationality == null ||
                            selectedNationality.equals("All Nationalities") ||
                            tourist.getNationality().equals(selectedNationality);

                    return matchesSearch && matchesNationality;
                })
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        touristTable.setItems(filteredList);
    }

    private void filterGuides() {
        if (guideList == null) return;

        String searchText = guideSearchField.getText().toLowerCase().trim();
        String selectedSpecialization = guideSpecializationFilter.getValue();

        ObservableList<Guide> filteredList = guideList.stream()
                .filter(guide -> {
                    // Search filter
                    boolean matchesSearch = searchText.isEmpty() ||
                            guide.getName().toLowerCase().contains(searchText) ||
                            guide.getLanguages().toLowerCase().contains(searchText) ||
                            guide.getContact().toLowerCase().contains(searchText) ||
                            guide.getSpecialization().toLowerCase().contains(searchText);

                    // Specialization filter
                    boolean matchesSpecialization = selectedSpecialization == null ||
                            selectedSpecialization.equals("All Specializations") ||
                            guide.getSpecialization().equals(selectedSpecialization);

                    return matchesSearch && matchesSpecialization;
                })
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        guideTable.setItems(filteredList);
    }

    private void filterAttractions() {
        if (attractionList == null) return;

        String searchText = attractionSearchField.getText().toLowerCase().trim();
        String selectedType = attractionTypeFilter.getValue();
        String selectedDifficulty = attractionDifficultyFilter.getValue();

        ObservableList<Attraction> filteredList = attractionList.stream()
                .filter(attraction -> {
                    // Search filter
                    boolean matchesSearch = searchText.isEmpty() ||
                            attraction.getName().toLowerCase().contains(searchText) ||
                            attraction.getLocation().toLowerCase().contains(searchText) ||
                            attraction.getType().toLowerCase().contains(searchText) ||
                            attraction.getDescription().toLowerCase().contains(searchText);

                    // Type filter
                    boolean matchesType = selectedType == null ||
                            selectedType.equals("All Types") ||
                            attraction.getType().equals(selectedType);

                    // Difficulty filter
                    boolean matchesDifficulty = selectedDifficulty == null ||
                            selectedDifficulty.equals("All Difficulties") ||
                            attraction.getDifficulty().equals(selectedDifficulty);

                    return matchesSearch && matchesType && matchesDifficulty;
                })
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        attractionTable.setItems(filteredList);
    }

    @FXML
    private void filterBookings() {
        if (bookingList == null) return;

        String searchText = bookingSearchField != null ? bookingSearchField.getText().toLowerCase().trim() : "";
        String selectedStatus = bookingStatusFilter != null ? bookingStatusFilter.getValue() : null;

        filteredBookingList = bookingList.stream()
                .filter(booking -> {
                    // Search filter
                    boolean matchesSearch = searchText.isEmpty() ||
                            booking.getTouristName().toLowerCase().contains(searchText) ||
                            booking.getAttractionName().toLowerCase().contains(searchText) ||
                            booking.getTouristEmail().toLowerCase().contains(searchText) ||
                            (booking.getGuideName() != null && booking.getGuideName().toLowerCase().contains(searchText));

                    // Status filter
                    boolean matchesStatus = true;
                    if (selectedStatus != null && !selectedStatus.equals("All Statuses") && !selectedStatus.equals("--- Guide Status ---")) {
                        if (selectedStatus.equals("Guide Assigned")) {
                            matchesStatus = booking.getGuideName() != null && !booking.getGuideName().trim().isEmpty();
                        } else if (selectedStatus.equals("Guide Not Assigned")) {
                            matchesStatus = booking.getGuideName() == null || booking.getGuideName().trim().isEmpty();
                        } else {
                            matchesStatus = booking.getStatus().equals(selectedStatus);
                        }
                    }

                    return matchesSearch && matchesStatus;
                })
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        if (bookingTable != null) {
            bookingTable.setItems(filteredBookingList);
        }

        System.out.println("Filtered bookings: " + filteredBookingList.size() + " out of " + bookingList.size() +
                " (Filter: " + selectedStatus + ", Search: '" + searchText + "')");
    }

    // Action methods for CRUD operations
    private void removeTourist(User tourist) {
        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Tourist Removal");
        confirmAlert.setHeaderText("Are you sure you want to remove this tourist?");
        StringBuilder message = new StringBuilder();
        message.append("Tourist: ").append(tourist.getFullName()).append("\n");
        message.append("Email: ").append(tourist.getEmail()).append("\n");
        message.append("Nationality: ").append(tourist.getNationality()).append("\n\n");
        message.append("⚠️ WARNING: This action will:\n");
        message.append("• Permanently delete the tourist's account\n");
        message.append("• Remove all associated data\n");
        message.append("• Affect any existing bookings\n");
        message.append("• Cannot be undone\n\n");
        message.append("Are you sure you want to proceed?");
        confirmAlert.setContentText(message.toString());

        // Customize buttons
        ButtonType yesButton = new ButtonType("Yes, Remove", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("Cancel", ButtonBar.ButtonData.NO);
        confirmAlert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            try {
                // Check if tourist is an admin (additional safety check)
                if ("Admin".equals(tourist.getRole())) {
                    showAlert("Error", "Cannot remove admin users!");
                    return;
                }

                // Remove from database
                boolean deleted = userDAO.delete(tourist.getId());
                if (deleted) {
                    // Remove from table
                    touristTable.getItems().remove(tourist);
                    touristList.remove(tourist);

                    // Update filtered list if search/filter is active
                    filterTourists();

                    // Refresh analytics to reflect the change
                    refreshAnalytics();

                    // Show success message
                    showAlert("Success", "Tourist '" + tourist.getFullName() + "' has been successfully removed from the system.");
                    System.out.println("Tourist removed successfully: " + tourist.getEmail());
                } else {
                    showAlert("Error", "Failed to remove tourist. Please try again.");
                }
            } catch (IllegalArgumentException e) {
                showAlert("Error", e.getMessage());
            } catch (IOException e) {
                System.err.println("Error removing tourist: " + e.getMessage());
                e.printStackTrace();
                showAlert("Error", "An error occurred while removing the tourist: " + e.getMessage());
            }
        }
    }

    private void showTouristDetails(User tourist) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Tourist Details");
        alert.setHeaderText("Tourist Information");

        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(tourist.getFullName()).append("\n");
        details.append("Email: ").append(tourist.getEmail()).append("\n");
        details.append("Nationality: ").append(tourist.getNationality()).append("\n");
        details.append("Registration Date: ").append(tourist.getRegistrationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n");
        details.append("Emergency Contact: ").append(tourist.getEmergencyContact() != null ? tourist.getEmergencyContact() : "Not provided").append("\n");
        details.append("Role: ").append(tourist.getRole()).append("\n");
        details.append("User ID: ").append(tourist.getId());

        alert.setContentText(details.toString());
        alert.showAndWait();
    }

    private void removeGuide(Guide guide) {
        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Guide Removal");
        confirmAlert.setHeaderText("Are you sure you want to remove this guide?");
        StringBuilder message = new StringBuilder();
        message.append("Guide: ").append(guide.getName()).append("\n");
        message.append("Contact: ").append(guide.getContact()).append("\n");
        message.append("Specialization: ").append(guide.getSpecialization()).append("\n");
        message.append("Experience: ").append(guide.getExperience()).append(" years\n");
        message.append("Rating: ").append(guide.getRating()).append("/5.0\n\n");
        message.append("⚠️ WARNING: This action will:\n");
        message.append("• Permanently delete the guide's profile\n");
        message.append("• Remove all associated data\n");
        message.append("• Affect any existing bookings assigned to this guide\n");
        message.append("• Cannot be undone\n\n");
        message.append("Are you sure you want to proceed?");
        confirmAlert.setContentText(message.toString());

        // Customize buttons
        ButtonType yesButton = new ButtonType("Yes, Remove", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("Cancel", ButtonBar.ButtonData.NO);
        confirmAlert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            try {
                // Remove from database
                guideDAO.delete(guide.getId());

                // Update any bookings that were assigned to this guide
                List<Booking> bookings = bookingDAO.findAll();
                for (Booking booking : bookings) {
                    if (guide.getName().equals(booking.getGuideName())) {
                        booking.setGuideName(null);
                        booking.setGuideId(null);
                        bookingDAO.update(booking);
                    }
                }

                // Remove from table
                guideTable.getItems().remove(guide);
                guideList.remove(guide);

                // Update filtered list if search/filter is active
                filterGuides();

                // Refresh analytics to reflect the change
                refreshAnalytics();

                // Show success message
                showAlert("Success", "Guide '" + guide.getName() + "' has been successfully removed from the system.\n" +
                        "All associated bookings have been updated.");
                System.out.println("Guide removed successfully: " + guide.getName());
            } catch (IOException e) {
                System.err.println("Error removing guide: " + e.getMessage());
                e.printStackTrace();
                showAlert("Error", "An error occurred while removing the guide: " + e.getMessage());
            }
        }
    }

    private void updateGuide(Guide guide) {
        try {
            // Create a dialog for updating guide information
            Dialog<Guide> dialog = new Dialog<>();
            dialog.setTitle("Update Guide Information");
            dialog.setHeaderText("Update information for: " + guide.getName());

            // Set the button types
            ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

            // Create the form
            VBox form = new VBox(10);
            form.setPrefWidth(400);

            TextField nameField = new TextField(guide.getName());
            nameField.setPromptText("Guide Name");

            TextField languagesField = new TextField(guide.getLanguages());
            languagesField.setPromptText("Languages (comma separated)");

            TextField experienceField = new TextField(String.valueOf(guide.getExperience()));
            experienceField.setPromptText("Years of Experience");

            TextField contactField = new TextField(guide.getContact());
            contactField.setPromptText("Contact Information");

            TextField specializationField = new TextField(guide.getSpecialization());
            specializationField.setPromptText("Specialization");

            TextField ratingField = new TextField(String.valueOf(guide.getRating()));
            ratingField.setPromptText("Rating (0.0 - 5.0)");

            ComboBox<String> statusComboBox = new ComboBox<>();
            statusComboBox.getItems().addAll("Active", "Inactive", "On Leave");
            statusComboBox.setValue(guide.getStatus());

            form.getChildren().addAll(
                    new Label("Name:"), nameField,
                    new Label("Languages:"), languagesField,
                    new Label("Experience (years):"), experienceField,
                    new Label("Contact:"), contactField,
                    new Label("Specialization:"), specializationField,
                    new Label("Rating:"), ratingField,
                    new Label("Status:"), statusComboBox
            );

            dialog.getDialogPane().setContent(form);

            // Enable/Disable update button depending on whether fields are filled
            Button updateButton = (Button) dialog.getDialogPane().lookupButton(updateButtonType);
            updateButton.addEventFilter(ActionEvent.ACTION, event -> {
                if (nameField.getText().trim().isEmpty() ||
                        languagesField.getText().trim().isEmpty() ||
                        contactField.getText().trim().isEmpty() ||
                        specializationField.getText().trim().isEmpty()) {
                    showAlert("Validation Error", "Please fill in all required fields.");
                    event.consume();
                    return;
                }

                try {
                    int experience = Integer.parseInt(experienceField.getText().trim());
                    double rating = Double.parseDouble(ratingField.getText().trim());
                    if (experience < 0) {
                        showAlert("Validation Error", "Experience must be a positive number.");
                        event.consume();
                        return;
                    }
                    if (rating < 0.0 || rating > 5.0) {
                        showAlert("Validation Error", "Rating must be between 0.0 and 5.0.");
                        event.consume();
                        return;
                    }
                } catch (NumberFormatException e) {
                    showAlert("Validation Error", "Please enter valid numbers for experience and rating.");
                    event.consume();
                    return;
                }
            });

            // Convert the result when the update button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == updateButtonType) {
                    try {
                        guide.setName(nameField.getText().trim());
                        guide.setLanguages(languagesField.getText().trim());
                        guide.setExperience(Integer.parseInt(experienceField.getText().trim()));
                        guide.setContact(contactField.getText().trim());
                        guide.setSpecialization(specializationField.getText().trim());
                        guide.setRating(Double.parseDouble(ratingField.getText().trim()));
                        guide.setStatus(statusComboBox.getValue());
                        return guide;
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
                return null;
            });

            Optional<Guide> result = dialog.showAndWait();
            result.ifPresent(updatedGuide -> {
                try {
                    // Update in database
                    guideDAO.update(updatedGuide);

                    // Update any bookings that reference this guide
                    List<Booking> bookings = bookingDAO.findAll();
                    for (Booking booking : bookings) {
                        if (updatedGuide.getId().equals(booking.getGuideId())) {
                            booking.setGuideName(updatedGuide.getName());
                            bookingDAO.update(booking);
                        }
                    }

                    // Refresh the table
                    loadGuideData();
                    filterGuides();

                    // Refresh analytics
                    refreshAnalytics();

                    showAlert("Success", "Guide information updated successfully!");
                    System.out.println("Guide updated successfully: " + updatedGuide.getName());
                } catch (IOException e) {
                    System.err.println("Error updating guide: " + e.getMessage());
                    e.printStackTrace();
                    showAlert("Error", "Failed to update guide: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("Error opening update dialog: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to open update dialog: " + e.getMessage());
        }
    }

    private void showGuideDetails(Guide guide) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Guide Details");
        alert.setHeaderText("Guide Information");

        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(guide.getName()).append("\n");
        details.append("Languages: ").append(guide.getLanguages()).append("\n");
        details.append("Experience: ").append(guide.getExperience()).append(" years\n");
        details.append("Contact: ").append(guide.getContact()).append("\n");
        details.append("Specialization: ").append(guide.getSpecialization()).append("\n");
        details.append("Rating: ").append(guide.getRating()).append("/5.0\n");
        details.append("Status: ").append(guide.getStatus()).append("\n");
        details.append("Guide ID: ").append(guide.getId());

        alert.setContentText(details.toString());
        alert.showAndWait();
    }

    private void removeAttraction(Attraction attraction) {
        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Attraction Removal");
        confirmAlert.setHeaderText("Are you sure you want to remove this attraction?");
        StringBuilder message = new StringBuilder();
        message.append("Attraction: ").append(attraction.getName()).append("\n");
        message.append("Type: ").append(attraction.getType()).append("\n");
        message.append("Location: ").append(attraction.getLocation()).append("\n");
        message.append("Difficulty: ").append(attraction.getDifficulty()).append("\n");
        message.append("Duration: ").append(attraction.getDuration()).append("\n");
        message.append("Price: $").append(String.format("%.2f", attraction.getPrice())).append("\n\n");
        message.append("⚠️ WARNING: This action will:\n");
        message.append("• Permanently delete the attraction from the system\n");
        message.append("• Remove all associated data\n");
        message.append("• Affect any existing bookings for this attraction\n");
        message.append("• Cannot be undone\n\n");
        message.append("Are you sure you want to proceed?");
        confirmAlert.setContentText(message.toString());

        // Customize buttons
        ButtonType yesButton = new ButtonType("Yes, Remove", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("Cancel", ButtonBar.ButtonData.NO);
        confirmAlert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            try {
                // Remove from database
                attractionDAO.delete(attraction.getId());

                // Update any bookings that were for this attraction
                List<Booking> bookings = bookingDAO.findAll();
                for (Booking booking : bookings) {
                    if (attraction.getName().equals(booking.getAttractionName())) {
                        // Cancel the booking or mark it as affected
                        booking.setStatus("Cancelled - Attraction Removed");
                        bookingDAO.update(booking);
                    }
                }

                // Remove from table
                attractionTable.getItems().remove(attraction);
                attractionList.remove(attraction);

                // Update filtered list if search/filter is active
                filterAttractions();

                // Refresh analytics to reflect the change
                refreshAnalytics();

                // Show success message
                showAlert("Success", "Attraction '" + attraction.getName() + "' has been successfully removed from the system.\n" +
                        "All associated bookings have been updated.");
                System.out.println("Attraction removed successfully: " + attraction.getName());
            } catch (IOException e) {
                System.err.println("Error removing attraction: " + e.getMessage());
                e.printStackTrace();
                showAlert("Error", "An error occurred while removing the attraction: " + e.getMessage());
            }
        }
    }

    private void updateAttraction(Attraction attraction) {
        try {
            // Create a dialog for updating attraction information
            Dialog<Attraction> dialog = new Dialog<>();
            dialog.setTitle("Update Attraction Information");
            dialog.setHeaderText("Update information for: " + attraction.getName());

            // Set the button types
            ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

            // Create the form
            VBox form = new VBox(10);
            form.setPrefWidth(500);

            TextField nameField = new TextField(attraction.getName());
            nameField.setPromptText("Attraction Name");

            ComboBox<String> typeComboBox = new ComboBox<>();
            typeComboBox.getItems().addAll("Trekking", "Cultural", "Wildlife", "Sightseeing", "Adventure", "Religious");
            typeComboBox.setValue(attraction.getType());

            TextField locationField = new TextField(attraction.getLocation());
            locationField.setPromptText("Location");

            ComboBox<String> difficultyComboBox = new ComboBox<>();
            difficultyComboBox.getItems().addAll("Easy", "Moderate", "Hard", "Extreme");
            difficultyComboBox.setValue(attraction.getDifficulty());

            TextField altitudeField = new TextField(attraction.getAltitude());
            altitudeField.setPromptText("Altitude (e.g., 1400m)");

            TextField durationField = new TextField(attraction.getDuration());
            durationField.setPromptText("Duration (e.g., 3 days)");

            TextField bestSeasonField = new TextField(attraction.getBestSeason());
            bestSeasonField.setPromptText("Best Season");

            TextArea descriptionArea = new TextArea(attraction.getDescription());
            descriptionArea.setPromptText("Description");
            descriptionArea.setPrefRowCount(3);

            TextField priceField = new TextField(String.valueOf(attraction.getPrice()));
            priceField.setPromptText("Price (USD)");

            ComboBox<String> statusComboBox = new ComboBox<>();
            statusComboBox.getItems().addAll("Active", "Inactive", "Maintenance", "Seasonal");
            statusComboBox.setValue(attraction.getStatus());

            form.getChildren().addAll(
                    new Label("Name:"), nameField,
                    new Label("Type:"), typeComboBox,
                    new Label("Location:"), locationField,
                    new Label("Difficulty:"), difficultyComboBox,
                    new Label("Altitude:"), altitudeField,
                    new Label("Duration:"), durationField,
                    new Label("Best Season:"), bestSeasonField,
                    new Label("Description:"), descriptionArea,
                    new Label("Price (USD):"), priceField,
                    new Label("Status:"), statusComboBox
            );

            dialog.getDialogPane().setContent(form);

            // Enable/Disable update button depending on whether fields are filled
            Button updateButton = (Button) dialog.getDialogPane().lookupButton(updateButtonType);
            updateButton.addEventFilter(ActionEvent.ACTION, event -> {
                if (nameField.getText().trim().isEmpty() ||
                        typeComboBox.getValue() == null ||
                        locationField.getText().trim().isEmpty() ||
                        difficultyComboBox.getValue() == null ||
                        altitudeField.getText().trim().isEmpty() ||
                        durationField.getText().trim().isEmpty() ||
                        bestSeasonField.getText().trim().isEmpty() ||
                        descriptionArea.getText().trim().isEmpty() ||
                        priceField.getText().trim().isEmpty()) {
                    showAlert("Validation Error", "Please fill in all required fields.");
                    event.consume();
                    return;
                }

                try {
                    double price = Double.parseDouble(priceField.getText().trim());
                    if (price < 0) {
                        showAlert("Validation Error", "Price must be a positive number.");
                        event.consume();
                        return;
                    }
                } catch (NumberFormatException e) {
                    showAlert("Validation Error", "Please enter a valid price.");
                    event.consume();
                    return;
                }
            });

            // Convert the result when the update button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == updateButtonType) {
                    try {
                        attraction.setName(nameField.getText().trim());
                        attraction.setType(typeComboBox.getValue());
                        attraction.setLocation(locationField.getText().trim());
                        attraction.setDifficulty(difficultyComboBox.getValue());
                        attraction.setAltitude(altitudeField.getText().trim());
                        attraction.setDuration(durationField.getText().trim());
                        attraction.setBestSeason(bestSeasonField.getText().trim());
                        attraction.setDescription(descriptionArea.getText().trim());
                        attraction.setPrice(Double.parseDouble(priceField.getText().trim()));
                        attraction.setStatus(statusComboBox.getValue());
                        return attraction;
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
                return null;
            });

            Optional<Attraction> result = dialog.showAndWait();
            result.ifPresent(updatedAttraction -> {
                try {
                    // Update in database
                    attractionDAO.update(updatedAttraction);

                    // Update any bookings that reference this attraction
                    List<Booking> bookings = bookingDAO.findAll();
                    for (Booking booking : bookings) {
                        if (updatedAttraction.getId().equals(booking.getAttractionId())) {
                            booking.setAttractionName(updatedAttraction.getName());
                            bookingDAO.update(booking);
                        }
                    }

                    // Refresh the table
                    loadAttractionData();
                    filterAttractions();

                    // Refresh analytics
                    refreshAnalytics();

                    showAlert("Success", "Attraction information updated successfully!");
                    System.out.println("Attraction updated successfully: " + updatedAttraction.getName());
                } catch (IOException e) {
                    System.err.println("Error updating attraction: " + e.getMessage());
                    e.printStackTrace();
                    showAlert("Error", "Failed to update attraction: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("Error opening update dialog: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to open update dialog: " + e.getMessage());
        }
    }

    private void showAttractionDetails(Attraction attraction) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Attraction Details");
        alert.setHeaderText("Attraction Information");

        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(attraction.getName()).append("\n");
        details.append("Type: ").append(attraction.getType()).append("\n");
        details.append("Location: ").append(attraction.getLocation()).append("\n");
        details.append("Difficulty: ").append(attraction.getDifficulty()).append("\n");
        details.append("Altitude: ").append(attraction.getAltitude()).append("\n");
        details.append("Duration: ").append(attraction.getDuration()).append("\n");
        details.append("Best Season: ").append(attraction.getBestSeason()).append("\n");
        details.append("Price: $").append(String.format("%.2f", attraction.getPrice())).append("\n");
        details.append("Status: ").append(attraction.getStatus()).append("\n");
        details.append("Description: ").append(attraction.getDescription()).append("\n");
        details.append("Attraction ID: ").append(attraction.getId());

        alert.setContentText(details.toString());
        alert.showAndWait();
    }

    private void showBookingDetails(Booking booking) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Booking Details");
        alert.setHeaderText("Complete Booking Information");

        StringBuilder details = new StringBuilder();
        details.append("Booking ID: ").append(booking.getId()).append("\n\n");
        details.append("TOURIST INFORMATION:\n");
        details.append("Name: ").append(booking.getTouristName()).append("\n");
        details.append("Email: ").append(booking.getTouristEmail()).append("\n");
        details.append("Contact: ").append(booking.getTouristContact()).append("\n\n");
        details.append("ATTRACTION INFORMATION:\n");
        details.append("Attraction: ").append(booking.getAttractionName()).append("\n");
        details.append("Travel Dates: ").append(booking.getStartDate()).append(" to ").append(booking.getEndDate()).append("\n");
        details.append("Total Cost: $").append(String.format("%.2f", booking.getTotalPrice())).append("\n\n");
        details.append("GUIDE INFORMATION:\n");
        details.append("Assigned Guide: ").append(booking.getGuideName() != null ? booking.getGuideName() : "Not Assigned").append("\n\n");
        details.append("BOOKING STATUS:\n");
        details.append("Status: ").append(booking.getStatus()).append("\n");
        details.append("Booking Date: ").append(booking.getBookingDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n\n");
        if (booking.getNotes() != null && !booking.getNotes().trim().isEmpty()) {
            details.append("NOTES:\n").append(booking.getNotes());
        }

        alert.setContentText(details.toString());
        alert.showAndWait();
    }

    // Export methods
    @FXML
    private void exportTouristData() {
        if (touristList != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Tourist Data");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
            Stage stage = (Stage) touristTable.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                try {
                    exportService.exportTouristData(touristList, file.getAbsolutePath());
                    showAlert("Success", "Tourist data exported successfully!");
                } catch (IOException e) {
                    showAlert("Error", "Failed to export data: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void exportGuideData() {
        if (guideList != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Guide Data");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
            Stage stage = (Stage) guideTable.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                try {
                    exportService.exportGuideData(guideList, file.getAbsolutePath());
                    showAlert("Success", "Guide data exported successfully!");
                } catch (IOException e) {
                    showAlert("Error", "Failed to export guide data: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void exportAttractionData() {
        if (attractionList != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Attraction Data");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
            Stage stage = (Stage) attractionTable.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                try {
                    exportService.exportAttractionData(attractionList, file.getAbsolutePath());
                    showAlert("Success", "Attraction data exported successfully!");
                } catch (IOException e) {
                    showAlert("Error", "Failed to export attraction data: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void exportBookingData() {
        if (bookingList != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Booking Data");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
            Stage stage = (Stage) bookingTable.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                try {
                    exportService.exportBookingData(bookingList, file.getAbsolutePath());
                    showAlert("Success", "Booking data exported successfully!");
                } catch (IOException e) {
                    showAlert("Error", "Failed to export booking data: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void exportEmergencyData() {
        if (emergencyList != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Emergency Contact Data");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
            Stage stage = (Stage) emergencyTable.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                try {
                    exportService.exportEmergencyContactData(emergencyList, file.getAbsolutePath());
                    showAlert("Success", "Emergency contact data exported successfully!");
                } catch (IOException e) {
                    showAlert("Error", "Failed to export emergency contact data: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void exportCurrentChart() {
        showAlert("Info", "Chart export functionality - Export current pie chart data");
    }

    @FXML
    private void exportBarChart() {
        showAlert("Info", "Bar chart export functionality - Export current bar chart data");
    }

    // Dialog opening methods
    public void openAddGuideForm(ActionEvent actionEvent) {
        openDialog("/org/example/Yatrio/views/AddNewGuideDialog.fxml", "Add New Guide", this::loadGuideData);
    }

    public void openAddAttractionDialog(ActionEvent actionEvent) {
        openDialog("/org/example/Yatrio/views/AddNewAttractionDialog.fxml", "Add New Attraction", this::loadAttractionData);
    }

    public void openAddContact(ActionEvent actionEvent) {
        openDialog("/org/example/Yatrio/views/AddEmergencyContactDialog.fxml", "Add Emergency Contact", this::loadEmergencyData);
    }

    private void openDialog(String fxmlPath, String title, Runnable onClose) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.setScene(new Scene(root));
            stage.setOnHidden(e -> {
                onClose.run();
                // Refresh analytics when data changes
                if (analyticsPane.isVisible()) {
                    refreshAnalytics();
                }
            });
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open dialog: " + e.getMessage());
        }
    }

    // Utility methods
    private void showPane(VBox paneToShow) {
        analyticsPane.setVisible(false);
        touristPane.setVisible(false);
        guidePane.setVisible(false);
        attractionPane.setVisible(false);
        bookingPane.setVisible(false);
        emergencyPane.setVisible(false);
        paneToShow.setVisible(true);
    }

    private void setActiveTab(ToggleButton activeTab) {
        ToggleButton[] allTabs = {analyticsTab, touristTab, guideTab, attractionTab, bookingTab, emergencyTab};
        for (ToggleButton tab : allTabs) {
            if (tab == activeTab) {
                tab.getStyleClass().remove("inactive-tab");
                if (!tab.getStyleClass().contains("active-tab")) {
                    tab.getStyleClass().add("active-tab");
                }
            } else {
                tab.getStyleClass().remove("active-tab");
                if (!tab.getStyleClass().contains("inactive-tab")) {
                    tab.getStyleClass().add("inactive-tab");
                }
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateEmergencyContact(EmergencyContact contact) {
        try {
            // Create a dialog for updating emergency contact information
            Dialog<EmergencyContact> dialog = new Dialog<>();
            dialog.setTitle("Update Emergency Contact");
            dialog.setHeaderText("Update information for: " + contact.getName());

            // Set the button types
            ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

            // Create the form
            VBox form = new VBox(10);
            form.setPrefWidth(500);

            TextField nameField = new TextField(contact.getName());
            nameField.setPromptText("Service Name");

            TextField organizationField = new TextField(contact.getOrganization());
            organizationField.setPromptText("Organization");

            TextField phoneField = new TextField(contact.getPhone());
            phoneField.setPromptText("Phone Number");

            TextField emailField = new TextField(contact.getEmail());
            emailField.setPromptText("Email Address");

            ComboBox<String> typeComboBox = new ComboBox<>();
            typeComboBox.getItems().addAll("Police", "Tourist Police", "Mountain Rescue", "Medical", "Fire Department", "Embassy", "Other");
            typeComboBox.setValue(contact.getType());

            TextField locationField = new TextField(contact.getLocation());
            locationField.setPromptText("Location/Coverage Area");

            CheckBox available24_7CheckBox = new CheckBox("Available 24/7");
            available24_7CheckBox.setSelected(contact.isAvailable24_7());

            form.getChildren().addAll(
                    new Label("Service Name:"), nameField,
                    new Label("Organization:"), organizationField,
                    new Label("Phone:"), phoneField,
                    new Label("Email:"), emailField,
                    new Label("Type:"), typeComboBox,
                    new Label("Location:"), locationField,
                    available24_7CheckBox
            );

            dialog.getDialogPane().setContent(form);

            // Enable/Disable update button depending on whether fields are filled
            Button updateButton = (Button) dialog.getDialogPane().lookupButton(updateButtonType);
            updateButton.addEventFilter(ActionEvent.ACTION, event -> {
                if (nameField.getText().trim().isEmpty() ||
                        organizationField.getText().trim().isEmpty() ||
                        phoneField.getText().trim().isEmpty() ||
                        typeComboBox.getValue() == null ||
                        locationField.getText().trim().isEmpty()) {
                    showAlert("Validation Error", "Please fill in all required fields.");
                    event.consume();
                    return;
                }
            });

            // Convert the result when the update button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == updateButtonType) {
                    contact.setName(nameField.getText().trim());
                    contact.setOrganization(organizationField.getText().trim());
                    contact.setPhone(phoneField.getText().trim());
                    contact.setEmail(emailField.getText().trim());
                    contact.setType(typeComboBox.getValue());
                    contact.setLocation(locationField.getText().trim());
                    contact.setAvailable24_7(available24_7CheckBox.isSelected());
                    return contact;
                }
                return null;
            });

            Optional<EmergencyContact> result = dialog.showAndWait();
            result.ifPresent(updatedContact -> {
                try {
                    // Update in CSV file by rewriting all contacts
                    List<EmergencyContact> allContacts = emergencyDAO.findAll();
                    for (int i = 0; i < allContacts.size(); i++) {
                        if (allContacts.get(i).getId().equals(updatedContact.getId())) {
                            allContacts.set(i, updatedContact);
                            break;
                        }
                    }

                    // Rewrite the CSV file with updated data
                    rewriteEmergencyContactsFile(allContacts);

                    // Refresh the table
                    loadEmergencyData();

                    showAlert("Success", "Emergency contact updated successfully!");
                    System.out.println("Emergency contact updated successfully: " + updatedContact.getName());
                } catch (IOException e) {
                    System.err.println("Error updating emergency contact: " + e.getMessage());
                    e.printStackTrace();
                    showAlert("Error", "Failed to update emergency contact: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("Error opening update dialog: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to open update dialog: " + e.getMessage());
        }
    }

    private void removeEmergencyContact(EmergencyContact contact) {
        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Emergency Contact Removal");
        confirmAlert.setHeaderText("Are you sure you want to remove this emergency contact?");
        StringBuilder message = new StringBuilder();
        message.append("Service: ").append(contact.getName()).append("\n");
        message.append("Organization: ").append(contact.getOrganization()).append("\n");
        message.append("Phone: ").append(contact.getPhone()).append("\n");
        message.append("Type: ").append(contact.getType()).append("\n");
        message.append("Location: ").append(contact.getLocation()).append("\n");
        message.append("24/7 Available: ").append(contact.isAvailable24_7() ? "Yes" : "No").append("\n\n");
        message.append("⚠️ WARNING: This action will:\n");
        message.append("• Permanently delete the emergency contact from the system\n");
        message.append("• Remove it from tourist emergency contact lists\n");
        message.append("• Cannot be undone\n\n");
        message.append("Are you sure you want to proceed?");
        confirmAlert.setContentText(message.toString());

        // Customize buttons
        ButtonType yesButton = new ButtonType("Yes, Remove", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("Cancel", ButtonBar.ButtonData.NO);
        confirmAlert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            try {
                // Remove from CSV file by rewriting without this contact
                List<EmergencyContact> allContacts = emergencyDAO.findAll();
                allContacts.removeIf(c -> c.getId().equals(contact.getId()));

                // Rewrite the CSV file without the removed contact
                rewriteEmergencyContactsFile(allContacts);

                // Remove from table
                emergencyTable.getItems().remove(contact);
                emergencyList.remove(contact);

                // Show success message
                showAlert("Success", "Emergency contact '" + contact.getName() + "' has been successfully removed from the system.");
                System.out.println("Emergency contact removed successfully: " + contact.getName());
            } catch (IOException e) {
                System.err.println("Error removing emergency contact: " + e.getMessage());
                e.printStackTrace();
                showAlert("Error", "An error occurred while removing the emergency contact: " + e.getMessage());
            }
        }
    }

    private void showEmergencyContactDetails(EmergencyContact contact) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Emergency Contact Details");
        alert.setHeaderText("Emergency Contact Information");

        StringBuilder details = new StringBuilder();
        details.append("Service: ").append(contact.getName()).append("\n");
        details.append("Organization: ").append(contact.getOrganization()).append("\n");
        details.append("Phone: ").append(contact.getPhone()).append("\n");
        details.append("Email: ").append(contact.getEmail()).append("\n");
        details.append("Type: ").append(contact.getType()).append("\n");
        details.append("Location: ").append(contact.getLocation()).append("\n");
        details.append("24/7 Available: ").append(contact.isAvailable24_7() ? "Yes" : "No").append("\n");
        details.append("Contact ID: ").append(contact.getId());

        alert.setContentText(details.toString());
        alert.showAndWait();
    }

    private void rewriteEmergencyContactsFile(List<EmergencyContact> contacts) throws IOException {
        try (java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.FileWriter("data/emergency_contacts.csv"))) {
            for (EmergencyContact contact : contacts) {
                bw.write(contact.toCSV());
                bw.newLine();
            }
        }
    }

    @FXML
    private void logout() {
        SessionManager.logout();
        try {
            Stage stage = (Stage) analyticsPane.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/Yatrio/views/login-dashboard.fxml"));
            Scene scene = new Scene(loader.load(), 1440, 850);
            String css = getClass().getResource("/org/example/Yatrio/styles/style.css").toExternalForm();
            scene.getStylesheets().add(css);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

