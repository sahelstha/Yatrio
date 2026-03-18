package org.example.Yatrio.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.Yatrio.dao.AttractionDAO;
import org.example.Yatrio.dao.BookingDAO;
import org.example.Yatrio.dao.EmergencyContactDAO;
import org.example.Yatrio.dao.UserDAO;
import org.example.Yatrio.models.Attraction;
import org.example.Yatrio.models.Booking;
import org.example.Yatrio.models.User;
import org.example.Yatrio.utils.DataRefreshManager;
import org.example.Yatrio.utils.LocaleManager;
import org.example.Yatrio.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TourismController implements Initializable, DataRefreshManager.DataRefreshListener {

    // booking tab elements
    public Label trackBookings;
    public Label discoverNepal, findAndBook;

    // profile tab info
    @FXML
    public Label personalInfo;
    @FXML
    public Label accDetails;
    @FXML
    public Label yourName;
    @FXML
    public Label yourEmail;
    @FXML
    public Label yourNationality;
    @FXML
    public Label yourTotalBookings;
    @FXML
    public Label myBookingsLabel;
    @FXML
    public ToggleButton myBookingsTab;
    public Label profileEmergencyContact, profileImportant, profileEnum, redImmediate, emergencyTabContc;
    public Label impNums, npPolice, npPoliceNum, touristPolice, touristPoliceNum, hmRescue, hmRescueNum;
    public Label safetyTips, safetyTipsSub, tip01Title, tip01, tip02Title, tip02, tip03Title, tip03;


    // Header elements
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label touristName;

    // Tab buttons
    @FXML
    private ToggleButton exploreTab;
    @FXML
    private ToggleButton profileTab;
    @FXML
    private ToggleButton emergencyTab;

    // Content areas
    @FXML
    private VBox exploreScreen;
    @FXML
    private VBox myBookingsScreen;
    @FXML
    private VBox profileScreen;
    @FXML
    private VBox emergencyScreen;

    // Explore screen elements
    @FXML
    private TilePane destinationCards;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> typeFilter;

    // My Bookings screen elements
    @FXML
    private VBox bookingList;

    // Profile screen elements
    @FXML
    private Label profileName;
    @FXML
    private Label profileEmail;
    @FXML
    private Label profileNationality;
    @FXML
    private Label profileTotalBookings;
    @FXML
    private TextField emergencyContactField;
    @FXML
    private Button updateEmergencyButton;

    // DAOs
    private AttractionDAO attractionDAO;
    private BookingDAO bookingDAO;
    private EmergencyContactDAO emergencyContactDAO;
    private UserDAO userDAO;

    // Store all attractions for filtering
    private List<Attraction> allAttractions;

    @FXML
    ComboBox<String> languageComboBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeDAOs();
        setupDataRefreshListeners();
        setupTabNavigation();
        updateTouristName();
        loadDestinations();
        setupSearchAndFilter();
        loadBookings();

        // Load user profile only if profile elements exist
        if (profileName != null) {
            loadUserProfile();
        }

        // Setup emergency contact update button
        if (updateEmergencyButton != null) {
            updateEmergencyButton.setOnAction(e -> updateEmergencyContact());
        }

        // Setup language switch
        languageComboBox.getItems().addAll("English", "Nepali");
        languageComboBox.setValue("English");
        changeLanguage("en");

        languageComboBox.setOnAction(event -> {
            String selected = languageComboBox.getValue();
            if (selected.equalsIgnoreCase("English")) {
                changeLanguage("en");
            } else {
                changeLanguage("ne");
            }
        });

        // Show explore screen by default
        showScreen(exploreScreen);
        setActiveTab(exploreTab);
    }

    private String getFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "User";
        }

        String[] nameParts = fullName.trim().split("\\s+");
        String firstName = nameParts[0];
        return capitalizeFirstLetter(firstName);
    }

    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    private void setupTabNavigation() {
        exploreTab.setOnAction(e -> {
            showScreen(exploreScreen);
            setActiveTab(exploreTab);
            loadDestinations();
        });

        myBookingsTab.setOnAction(e -> {
            showScreen(myBookingsScreen);
            setActiveTab(myBookingsTab);
            loadBookings();
        });

        profileTab.setOnAction(e -> {
            showScreen(profileScreen);
            setActiveTab(profileTab);
            loadUserProfile();
        });

        emergencyTab.setOnAction(e -> {
            showScreen(emergencyScreen);
            setActiveTab(emergencyTab);
        });
    }

    private void showScreen(VBox screenToShow) {
        exploreScreen.setVisible(false);
        exploreScreen.setManaged(false);
        myBookingsScreen.setVisible(false);
        myBookingsScreen.setManaged(false);
        profileScreen.setVisible(false);
        profileScreen.setManaged(false);
        emergencyScreen.setVisible(false);
        emergencyScreen.setManaged(false);

        screenToShow.setVisible(true);
        screenToShow.setManaged(true);
    }

    private void setActiveTab(ToggleButton activeTab) {
        ToggleButton[] allTabs = {exploreTab, myBookingsTab, profileTab, emergencyTab};

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

    private void loadDestinations() {
        try {
            // Force fresh data load from DAO
            System.out.println("TourismController: Loading fresh attractions data...");
            allAttractions = attractionDAO.findAll();
            System.out.println("TourismController: Loaded " + allAttractions.size() + " attractions");

            // Display the attractions
            displayAttractions(allAttractions);

        } catch (IOException e) {
            System.err.println("Error loading destinations: " + e.getMessage());
            showAlert("Error", "Failed to load destinations: " + e.getMessage());
        }
    }

    private void initializeDAOs() {
        attractionDAO = new AttractionDAO();
        bookingDAO = new BookingDAO();
        emergencyContactDAO = new EmergencyContactDAO();
        userDAO = new UserDAO();
    }

    private void setupDataRefreshListeners() {
        // Register this controller to listen for attraction data changes
        DataRefreshManager.addAttractionListener(this);
        DataRefreshManager.addBookingListener(this);
        DataRefreshManager.addUserListener(this);

        System.out.println("TourismController registered for data refresh notifications");
    }

    @Override
    public void onDataRefresh() {
        // This method is called when data changes in other parts of the application
        Platform.runLater(() -> {
            System.out.println("TourismController: Refreshing data due to external changes");

            // Force reload attractions data
            loadDestinations();

            // Refresh bookings
            loadBookings();

            // Refresh profile if visible
            if (profileName != null) {
                loadUserProfile();
            }

            // Refresh search and filter options
            setupSearchAndFilter();

            System.out.println("TourismController: Data refresh completed");
        });
    }

    private void setupSearchAndFilter() {
        // Setup type filter ComboBox
        if (typeFilter != null) {
            String currentSelection = typeFilter.getValue();
            typeFilter.getItems().clear();
            typeFilter.getItems().add("All Types");

            // Add unique attraction types from data
            try {
                List<String> types = attractionDAO.findAll().stream()
                        .map(attraction -> {
                            String type = attraction.getType();
                            return type != null ? type.trim() : "Other";
                        })
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList());

                typeFilter.getItems().addAll(types);

                // Restore previous selection or set default
                if (currentSelection != null && typeFilter.getItems().contains(currentSelection)) {
                    typeFilter.setValue(currentSelection);
                } else {
                    typeFilter.setValue("All Types");
                }

                // Add listener for type filter changes
                typeFilter.setOnAction(e -> filterAttractions());

            } catch (IOException e) {
                System.err.println("Error loading attraction types: " + e.getMessage());
            }
        }

        // Setup search field
        if (searchField != null) {
            // Add listener for search text changes
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterAttractions();
            });

            // Add placeholder text using current locale
            String placeholder = LocaleManager.getString("searchField");
            searchField.setPromptText(placeholder);
        }
    }

    private void filterAttractions() {
        if (allAttractions == null || allAttractions.isEmpty()) {
            return;
        }

        String searchText = searchField != null ? searchField.getText().toLowerCase().trim() : "";
        String selectedType = typeFilter != null ? typeFilter.getValue() : "All Types";

        List<Attraction> filteredAttractions = allAttractions.stream()
                .filter(attraction -> {
                    // Filter by search text
                    boolean matchesSearch = searchText.isEmpty() ||
                            attraction.getName().toLowerCase().contains(searchText) ||
                            attraction.getLocation().toLowerCase().contains(searchText) ||
                            (attraction.getDescription() != null && attraction.getDescription().toLowerCase().contains(searchText)) ||
                            attraction.getDifficulty().toLowerCase().contains(searchText);

                    // Filter by type
                    boolean matchesType = "All Types".equals(selectedType) ||
                            (attraction.getType() != null && attraction.getType().equals(selectedType));

                    return matchesSearch && matchesType;
                })
                .collect(Collectors.toList());

        // Update the display
        displayAttractions(filteredAttractions);
    }

    private void displayAttractions(List<Attraction> attractions) {
        destinationCards.getChildren().clear();

        if (attractions.isEmpty()) {
            // Show "No results found" message
            Label noResultsLabel = new Label("No attractions found matching your criteria.");
            noResultsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666; -fx-padding: 40px;");

            VBox noResultsBox = new VBox(noResultsLabel);
            noResultsBox.setStyle("-fx-alignment: center; -fx-background-color: #f9f9f9; -fx-background-radius: 10; -fx-padding: 40px;");
            noResultsBox.setPrefWidth(350);

            destinationCards.getChildren().add(noResultsBox);
        } else {
            for (Attraction attraction : attractions) {
                VBox card = createDestinationCard(attraction);
                destinationCards.getChildren().add(card);
            }
        }
    }

    //nipun 29 jul
    private void updateTouristName() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null && touristName != null) {
            // Capitalize first letter of name and nationality
            String firstName = getFirstName(currentUser.getFullName());
            String nationality = capitalizeFirstLetter(currentUser.getNationality());

            String touristNameNationality = firstName + " (" + nationality + ")";
            touristName.setText(touristNameNationality);
        }
    }

    private VBox createDestinationCard(Attraction attraction) {
        VBox card = new VBox(10);
        card.getStyleClass().add("attraction-card");
        card.setPrefWidth(380);
        card.setMaxWidth(380);
        card.setPadding(new Insets(15));

        // Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(350);
        imageView.setFitHeight(220);
        imageView.setPreserveRatio(false);

        Image image = loadAttractionImage(attraction.getImagePath(), attraction.getName());
        imageView.setImage(image);

        // Title
        Label titleLabel = new Label(attraction.getName());
        titleLabel.getStyleClass().add("attraction-title");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Location and duration
        Label locationLabel = new Label("\uD83D\uDCCD " + attraction.getLocation() + " • ⏱️ " + attraction.getDuration());
        locationLabel.setStyle("-fx-font-size: 14px");

        // Difficulty
        Label difficultyLabel = new Label("Difficulty: " + attraction.getDifficulty());
        if ("Easy".equalsIgnoreCase(attraction.getDifficulty())) {
            difficultyLabel.getStyleClass().add("difficulty-easy");
        }
        if ("Moderate".equalsIgnoreCase(attraction.getDifficulty())) {
            difficultyLabel.getStyleClass().add("difficulty-moderate");
        }
        if ("Hard".equalsIgnoreCase(attraction.getDifficulty())) {
            difficultyLabel.getStyleClass().add("difficulty-hard");
        }
        if ("Extreme".equalsIgnoreCase(attraction.getDifficulty())) {
            difficultyLabel.getStyleClass().add("difficulty-extreme");
        }

        // Type
        Label typeLabel = new Label("Type: " + (attraction.getType() != null ? attraction.getType() : "Adventure"));
        typeLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        // Description
        Label descriptionLabel = new Label(attraction.getDescription());
        descriptionLabel.getStyleClass().add("attraction-description");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 14px");
        descriptionLabel.setMaxWidth(350);

        // Price and Book button
        VBox bottomSection = new VBox(10);
        Label priceLabel = new Label("$" + String.format("%.0f", attraction.getPrice()) + "/day");
        priceLabel.getStyleClass().add("price-label");
        VBox.setMargin(priceLabel, new Insets(15, 0, 0, 0));
        priceLabel.setStyle("-fx-text-fill: #50C878; -fx-font-weight: bold; -fx-font-size: 22px;");

        Button bookButton = new Button("Book Now");
        bookButton.getStyleClass().add("book-button");
        bookButton.setPrefWidth(350);
        bookButton.setMinHeight(50);
        bookButton.setStyle("-fx-font-size: 16px; -fx-font-weight: bold");
        bookButton.setOnAction(e -> openBookingDialog(attraction));

        VBox.setMargin(bookButton, new Insets(50, 0, 0, 0));
        bottomSection.getChildren().addAll(priceLabel, bookButton);

        card.getChildren().addAll(imageView, titleLabel, locationLabel, difficultyLabel,
                typeLabel, descriptionLabel, bottomSection);

        return card;
    }

    private Image loadAttractionImage(String imagePath, String attractionName) {
        try {
            // First, try to load from external images directory (admin-uploaded images)
            File externalImageFile = new File("images", imagePath);
            if (externalImageFile.exists()) {
                Image image = new Image(externalImageFile.toURI().toString());
                System.out.println("Loaded external image: " + externalImageFile.getAbsolutePath() + " for attraction: " + attractionName);
                return image;
            }

            // Second, try to load from resources (default images)
            String resourcePath = "/org/example/Yatrio/images/" + imagePath;
            try {
                Image image = new Image(getClass().getResourceAsStream(resourcePath));
                System.out.println("Loaded resource image: " + resourcePath + " for attraction: " + attractionName);
                return image;
            } catch (Exception e) {
                System.out.println("Resource image not found: " + resourcePath);
            }

            // Third, try default placeholder from resources
            try {
                Image defaultImage = new Image(getClass().getResourceAsStream("/org/example/Yatrio/images/new.jpg"));
                System.out.println("Using default placeholder image for: " + attractionName);
                return defaultImage;
            } catch (Exception e) {
                System.err.println("Failed to load default placeholder image");
            }

        } catch (Exception e) {
            System.err.println("Error loading image for " + attractionName + ": " + e.getMessage());
        }

        // Final fallback - create a simple placeholder
        System.err.println("Creating empty placeholder for: " + attractionName);
        return new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==");
    }

    private void openBookingDialog(Attraction attraction) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/Yatrio/views/BookingDialog.fxml"));
            Parent root = loader.load();

            BookingsDialogController controller = loader.getController();
            controller.setAttractionData(attraction);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Book Attraction - " + attraction.getName());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(destinationCards.getScene().getWindow());
            dialogStage.setScene(new Scene(root));

            // Refresh data when dialog closes
            dialogStage.setOnHidden(e -> {
                // Always refresh bookings and profile when dialog closes
                loadBookings();
                if (profileName != null) {
                    loadUserProfile();
                }

                // Show success message if booking was created
                if (controller.wasBookingCreated()) {
                    System.out.println("Booking created successfully - data refreshed");
                }
            });

            dialogStage.showAndWait();

        } catch (IOException e) {
            System.err.println("Error opening booking dialog: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to open booking dialog: " + e.getMessage());
        }
    }

    private void loadBookings() {
        try {
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser == null) {
                System.err.println("No current user found");
                return;
            }

            System.out.println("Loading bookings for user: " + currentUser.getId());
            List<Booking> userBookings = bookingDAO.findByTouristId(currentUser.getId());
            System.out.println("Found " + userBookings.size() + " bookings");

            if (bookingList != null) {
                bookingList.getChildren().clear();

                if (userBookings.isEmpty()) {
                    Label noBookingsLabel = new Label("No bookings found. Start exploring to make your first booking!");
                    noBookingsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666; -fx-padding: 20;");
                    bookingList.getChildren().add(noBookingsLabel);
                } else {
                    for (Booking booking : userBookings) {
                        VBox bookingCard = createBookingCard(booking);
                        bookingList.getChildren().add(bookingCard);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading bookings: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to load bookings: " + e.getMessage());
        }
    }

    private VBox createBookingCard(Booking booking) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-border-color: #e0e0e0; " +
                "-fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPrefWidth(1000);

        // Header with attraction name and status
        javafx.scene.layout.HBox headerBox = new javafx.scene.layout.HBox(10);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label attractionLabel = new Label(booking.getAttractionName());
        attractionLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label statusLabel = new Label("Status: " + booking.getStatus());
        String statusColor = getStatusColor(booking.getStatus());
        statusLabel.setStyle("-fx-text-fill: " + statusColor + "; -fx-font-weight: bold;");

        // Add spacer
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        headerBox.getChildren().addAll(attractionLabel, spacer, statusLabel);

        // Dates
        Label datesLabel = new Label("📅 " + booking.getStartDate().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")) +
                " to " + booking.getEndDate().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")));

        // Guide info
        Label guideLabel = new Label("👨‍🏫 Guide: " + (booking.getGuideName() != null ? booking.getGuideName() : "To be assigned"));

        // Price
        Label priceLabel = new Label("💰 Total: $" + String.format("%.2f", booking.getTotalPrice()));
        priceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");

        // Contact info
        Label contactLabel = new Label("📞 Contact: " + booking.getTouristContact());

        // Booking date
        Label bookingDateLabel = new Label("📝 Booked on: " + booking.getBookingDate().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
        bookingDateLabel.setStyle("-fx-text-fill: #666;");

        card.getChildren().addAll(headerBox, datesLabel, guideLabel, priceLabel, contactLabel, bookingDateLabel);

        // Notes if any
        if (booking.getNotes() != null && !booking.getNotes().trim().isEmpty()) {
            Label notesLabel = new Label("📝 Notes: " + booking.getNotes());
            notesLabel.setWrapText(true);
            notesLabel.setStyle("-fx-text-fill: #555;");
            card.getChildren().add(notesLabel);
        }

        // Action buttons section
        javafx.scene.layout.HBox actionBox = new javafx.scene.layout.HBox(10);
        actionBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        actionBox.setPadding(new javafx.geometry.Insets(10, 0, 0, 0));

        // Check booking status and add appropriate buttons/labels
        String status = booking.getStatus().toLowerCase();

        if (status.equals("pending") || status.equals("confirmed")) {
            // Add cancel button for bookings that can be cancelled
            Button cancelButton = new Button("Cancel Booking");
            cancelButton.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold; " +
                    "-fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8 16; -fx-cursor: hand;");

            cancelButton.setOnAction(e -> {
                // Show confirmation dialog
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Cancel Booking");
                confirmAlert.setHeaderText("Are you sure you want to cancel this booking?");
                confirmAlert.setContentText("Booking: " + booking.getAttractionName() + "\n" +
                        "Dates: " + booking.getStartDate() + " to " + booking.getEndDate() + "\n" +
                        "Total: $" + String.format("%.2f", booking.getTotalPrice()) + "\n\n" +
                        "This action cannot be undone.");

                confirmAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        cancelBooking(booking);
                    }
                });
            });

            actionBox.getChildren().add(cancelButton);

        } else if (status.equals("completed")) {
            // Add status indicator for completed bookings
            Label completedLabel = new Label("✅ Trip Completed");
            completedLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold; -fx-font-size: 14px;");
            actionBox.getChildren().add(completedLabel);

        } else if (status.equals("cancelled")) {
            // Add status indicator for cancelled bookings
            Label cancelledLabel = new Label("❌ Booking Cancelled");
            cancelledLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold; -fx-font-size: 14px;");
            actionBox.getChildren().add(cancelledLabel);
        }

        // Always add the action box to the card
        card.getChildren().add(actionBox);

        return card;
    }

    private String getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "confirmed":
                return "#2e7d32";
            case "pending":
                return "#f57c00";
            case "cancelled":
                return "#d32f2f";
            case "completed":
                return "#1976d2";
            default:
                return "#666";
        }
    }

    private void loadUserProfile() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            // Update basic profile information
            if (profileName != null) {
                profileName.setText(capitalizeFirstLetter(currentUser.getFullName()));
            }
            if (profileEmail != null) {
                profileEmail.setText(currentUser.getEmail());
            }
            if (profileNationality != null) {
                profileNationality.setText(capitalizeFirstLetter(currentUser.getNationality()));
            }

            // Count total bookings
            try {
                List<Booking> userBookings = bookingDAO.findByTouristId(currentUser.getId());
                if (profileTotalBookings != null) {
                    profileTotalBookings.setText(String.valueOf(userBookings.size()));
                }
            } catch (IOException e) {
                if (profileTotalBookings != null) {
                    profileTotalBookings.setText("0");
                }
            }

            // Load emergency contact if exists
            if (emergencyContactField != null) {
                String emergencyContact = currentUser.getEmergencyContact();
                if (emergencyContact != null && !emergencyContact.trim().isEmpty()) {
                    emergencyContactField.setText(emergencyContact);
                } else {
                    String placeholder = LocaleManager.getString("emergencyContactField");
                    emergencyContactField.setPromptText(placeholder);
                }
            }
        }
    }

    private void updateEmergencyContact() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            showAlert("Error", "No user session found. Please login again.");
            return;
        }

        if (emergencyContactField == null) {
            showAlert("Error", "Emergency contact field not found.");
            return;
        }

        String emergencyContact = emergencyContactField.getText().trim();

        // Validate emergency contact
        if (emergencyContact.isEmpty()) {
            showAlert("Validation Error", "Please enter an emergency contact number.");
            emergencyContactField.requestFocus();
            return;
        }

        // Validate phone number format
        if (!emergencyContact.matches("^[+]?[0-9\\s\\-()]{7,20}$")) {
            showAlert("Validation Error", "Please enter a valid phone number.\n\n" +
                    "Examples:\n" +
                    "• +977-1-4247041\n" +
                    "• 9841234567\n" +
                    "• +1-555-123-4567");
            emergencyContactField.requestFocus();
            return;
        }

        try {
            // Update user's emergency contact
            currentUser.setEmergencyContact(emergencyContact);

            // Save to database
            userDAO.update(currentUser);

            // Update session
            SessionManager.setCurrentUser(currentUser);

            // Refresh profile display
            loadUserProfile();

            // Show success message
            showAlert("Success", "✅ Emergency contact updated successfully!\n\n" +
                    "📞 Contact: " + emergencyContact + "\n\n" +
                    "This information will be used for safety purposes during your trips.");

        } catch (IOException e) {
            System.err.println("Error updating emergency contact: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to update emergency contact. Please try again.\n\nError: " + e.getMessage());
        }
    }

    @FXML
    private void logout() {
        try {
            // Remove this controller from data refresh listeners
            DataRefreshManager.removeAttractionListener(this);
            DataRefreshManager.removeBookingListener(this);
            DataRefreshManager.removeUserListener(this);

            SessionManager.logout();

            // Load login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/Yatrio/views/login-dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) exploreTab.getScene().getWindow();
            Scene scene = new Scene(root, 1440, 850);
            String css = getClass().getResource("/org/example/Yatrio/styles/style.css").toExternalForm();
            scene.getStylesheets().add(css);
            stage.setScene(scene);
            stage.setTitle("Nepal Tourism Management System");

        } catch (IOException e) {
            System.err.println("Error during logout: " + e.getMessage());
            showAlert("Error", "Failed to logout: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void cancelBooking(Booking booking) {
        try {
            // Update booking status to cancelled
            booking.setStatus("Cancelled");
            bookingDAO.update(booking);

            // Refresh the bookings display
            loadBookings();

            // Refresh profile data as well
            if (profileName != null) {
                loadUserProfile();
            }

            // Show success message
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Booking Cancelled");
            successAlert.setHeaderText("Your booking has been cancelled successfully");
            successAlert.setContentText("Booking for " + booking.getAttractionName() + " has been cancelled.\n" +
                    "If you paid any advance, please contact our support team for refund processing.");
            successAlert.showAndWait();

        } catch (IOException e) {
            System.err.println("Error cancelling booking: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to cancel booking. Please try again.\n\nError: " + e.getMessage());
        }
    }
//
//    private void changeLanguage(String langCode) {
//        try {
//            System.out.println("TourismController: Changing language to: " + langCode);
//            LocaleManager.setLanguage(langCode);
//            applyTranslations();
//            System.out.println("TourismController: Language changed successfully to: " + langCode);
//        } catch (Exception e) {
//            System.err.println("TourismController: Failed to load language: " + langCode);
//            e.printStackTrace();
//            showAlert("Language Error", "Failed to change language to " + langCode + ". Please try again.");
//        }
//    }
//
//    private void applyTranslations() {
//        try {
//            System.out.println("TourismController: Applying translations...");
//
//            // Update welcome message with user name
//            updateWelcomeMessage();
//
//            // Tab buttons
//            if (exploreTab != null) exploreTab.setText(LocaleManager.getString("Explore"));
//            if (myBookingsTab != null) myBookingsTab.setText(LocaleManager.getString("myBookingsTab"));
//            if (profileTab != null) profileTab.setText(LocaleManager.getString("Profile"));
//            if (emergencyTab != null) emergencyTab.setText(LocaleManager.getString("Emergency"));
//
//            // Explore Tab content
//            if (discoverNepal != null) discoverNepal.setText(LocaleManager.getString("DiscoverNepal"));
//            if (findAndBook != null) findAndBook.setText(LocaleManager.getString("FindAndBook"));
//            if (searchField != null) searchField.setPromptText(LocaleManager.getString("searchField"));
//
//            // My Bookings Tab content
//            if (myBookingsLabel != null) myBookingsLabel.setText(LocaleManager.getString("myBookingsLabel"));
//            if (trackBookings != null) trackBookings.setText(LocaleManager.getString("trackBookings"));
//
//            // Profile Tab content
//            if (personalInfo != null) personalInfo.setText(LocaleManager.getString("personalInfo"));
//            if (accDetails != null) accDetails.setText(LocaleManager.getString("accDetails"));
//            if (yourName != null) yourName.setText(LocaleManager.getString("yourName"));
//            if (yourEmail != null) yourEmail.setText(LocaleManager.getString("yourEmail"));
//            if (yourNationality != null) yourNationality.setText(LocaleManager.getString("yourNationality"));
//            if (yourTotalBookings != null) yourTotalBookings.setText(LocaleManager.getString("yourTotalBookings"));
//
//            if (profileEmergencyContact != null) profileEmergencyContact.setText(LocaleManager.getString("profileEmergencyContact"));
//            if (profileImportant != null) profileImportant.setText(LocaleManager.getString("profileImportant"));
//            if (profileEnum != null) profileEnum.setText(LocaleManager.getString("profileEnum"));
//            if (emergencyContactField != null) emergencyContactField.setPromptText(LocaleManager.getString("emergencyContactField"));
//            if (updateEmergencyButton != null) updateEmergencyButton.setText(LocaleManager.getString("updateEmergencyButton"));
//
//            // Emergency Tab content
//            if (redImmediate != null) redImmediate.setText(LocaleManager.getString("redImmediate"));
//            if (emergencyTabContc != null) emergencyTabContc.setText(LocaleManager.getString("emergencyTabContc"));
//            if (impNums != null) impNums.setText(LocaleManager.getString("impNums"));
//            if (npPolice != null) npPolice.setText(LocaleManager.getString("npPolice"));
//            if (npPoliceNum != null) npPoliceNum.setText(LocaleManager.getString("npPoliceNum"));
//            if (touristPolice != null) touristPolice.setText(LocaleManager.getString("touristPolice"));
//            if (touristPoliceNum != null) touristPoliceNum.setText(LocaleManager.getString("touristPoliceNum"));
//            if (hmRescue != null) hmRescue.setText(LocaleManager.getString("hmRescue"));
//            if (hmRescueNum != null) hmRescueNum.setText(LocaleManager.getString("hmRescueNum"));
//
//            // Safety tips
//            if (safetyTips != null) safetyTips.setText(LocaleManager.getString("safetyTips"));
//            if (safetyTipsSub != null) safetyTipsSub.setText(LocaleManager.getString("safetyTipsSub"));
//            if (tip01Title != null) tip01Title.setText(LocaleManager.getString("tip01Title"));
//            if (tip01 != null) tip01.setText(LocaleManager.getString("tip01"));
//            if (tip02Title != null) tip02Title.setText(LocaleManager.getString("tip02Title"));
//            if (tip02 != null) tip02.setText(LocaleManager.getString("tip02"));
//            if (tip03Title != null) tip03Title.setText(LocaleManager.getString("tip03Title"));
//            if (tip03 != null) tip03.setText(LocaleManager.getString("tip03"));
//
//            System.out.println("TourismController: Translations applied successfully");
//
//        } catch (Exception e) {
//            System.err.println("TourismController: Error applying translations: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }

    private void changeLanguage(String langCode) {
        try {
            LocaleManager.setLanguage(langCode);
            applyTranslations(LocaleManager.getBundle());
        } catch (Exception e) {
            System.err.println("Failed to load language: " + langCode);
            e.printStackTrace();
        }
    }

    private void applyTranslations(ResourceBundle resourceBundle) {
        // Explore Tab
        welcomeLabel.setText(resourceBundle.getString("Welcome"));
        exploreTab.setText(resourceBundle.getString("Explore"));
        myBookingsTab.setText(resourceBundle.getString("myBookingsTab"));
        profileTab.setText(resourceBundle.getString("Profile"));
        emergencyTab.setText(resourceBundle.getString("Emergency"));
        discoverNepal.setText(resourceBundle.getString("DiscoverNepal"));
        findAndBook.setText(resourceBundle.getString("FindAndBook"));
        searchField.setPromptText(resourceBundle.getString("searchField"));

        // My Bookings Tab
        myBookingsLabel.setText(resourceBundle.getString("myBookingsLabel"));
        trackBookings.setText(resourceBundle.getString("trackBookings"));

        // Profile Tab
        personalInfo.setText(resourceBundle.getString("personalInfo"));
        accDetails.setText(resourceBundle.getString("accDetails"));
        yourName.setText(resourceBundle.getString("yourName"));
        yourEmail.setText(resourceBundle.getString("yourEmail"));
        yourNationality.setText(resourceBundle.getString("yourNationality"));
        yourTotalBookings.setText(resourceBundle.getString("yourTotalBookings"));

        profileEmergencyContact.setText(resourceBundle.getString("profileEmergencyContact"));
        profileImportant.setText(resourceBundle.getString("profileImportant"));
        profileEnum.setText(resourceBundle.getString("profileEnum"));
        emergencyContactField.setPromptText(resourceBundle.getString("emergencyContactField"));
        updateEmergencyButton.setText(resourceBundle.getString("updateEmergencyButton"));

        // Emergency Tab
        redImmediate.setText(resourceBundle.getString("redImmediate"));
        emergencyTabContc.setText(resourceBundle.getString("emergencyTabContc"));
        impNums.setText(resourceBundle.getString("impNums"));
        npPolice.setText(resourceBundle.getString("npPolice"));
        npPoliceNum.setText(resourceBundle.getString("npPoliceNum"));
        touristPolice.setText(resourceBundle.getString("touristPolice"));
        hmRescue.setText(resourceBundle.getString("hmRescue"));
//        hmRescueNum.setText(resourceBundle.getString("hmRescueNum"));

        System.out.println("Safety tips 111");
        safetyTips.setText(resourceBundle.getString("safetyTips"));
        safetyTipsSub.setText(resourceBundle.getString("safetyTipsSub"));
        tip01Title.setText(resourceBundle.getString("tip01Title"));
        tip01.setText(resourceBundle.getString("tip01"));
        tip02Title.setText(resourceBundle.getString("tip02Title"));
        tip02.setText(resourceBundle.getString("tip02"));
        tip03Title.setText(resourceBundle.getString("tip03Title"));
        tip03.setText(resourceBundle.getString("tip03"));
        System.out.println("Safety tips 111");

    }
}
