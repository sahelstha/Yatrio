package org.example.Yatrio.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.Yatrio.services.BookingService;
import org.example.Yatrio.models.Attraction;
import org.example.Yatrio.models.User;
import org.example.Yatrio.utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

public class BookingsDialogController implements Initializable {

    @FXML private Label touristName;
    @FXML private Label touristEmail;
    @FXML private Label touristDestination;
    @FXML private TextField touristContactField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextArea notesArea;
    @FXML private Label totalPriceLabel;
    @FXML private Label originalPriceLabel;
    @FXML private Label discountLabel;
    @FXML private Label festivalDiscountInfo;
    @FXML private Button closeButton;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;

    // High Altitude Warning Elements
    @FXML private VBox highAltitudeWarning;
    @FXML private Label altitudeInfoLabel;

    private Attraction selectedAttraction;
    private BookingService bookingService;
    private boolean bookingCreated = false;

    // Festival discount period (September 20 to October 5)
    private static final MonthDay FESTIVAL_START = MonthDay.of(9, 20); // September 20
    private static final MonthDay FESTIVAL_END = MonthDay.of(10, 5);   // October 5
    private static final double FESTIVAL_DISCOUNT_RATE = 0.20; // 20% discount

    // High altitude threshold (3000 meters)
    private static final double HIGH_ALTITUDE_THRESHOLD = 3000.0;

    public BookingsDialogController() {
        this.bookingService = new BookingService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize with safe defaults and null checks
        initializeUserData();
        initializeDatePickers();
        setupEventListeners();
        initializeFestivalDiscountInfo();
    }

    private void initializeUserData() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            // Use null checks for FXML injected fields
            if (touristName != null) {
                touristName.setText(currentUser.getFullName());
            }
            if (touristEmail != null) {
                touristEmail.setText(currentUser.getEmail());
            }
        } else {
            // Set default values if user is null
            if (touristName != null) {
                touristName.setText("No user logged in");
            }
            if (touristEmail != null) {
                touristEmail.setText("No email available");
            }
        }
    }

    private void initializeDatePickers() {
        if (startDatePicker != null && endDatePicker != null) {
            // Set minimum date to tomorrow
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            startDatePicker.setValue(tomorrow);
            endDatePicker.setValue(tomorrow); // Changed: same day as start date

            // Set date picker constraints
            startDatePicker.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (date.isBefore(LocalDate.now().plusDays(1))) { // Changed: tomorrow is minimum
                        setDisable(true);
                        setStyle("-fx-background-color: #ffc0cb;");
                    } else if (isDateInFestivalPeriod(date)) {
                        // Highlight festival dates
                        setStyle("-fx-background-color: #e8f5e8; -fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                        setTooltip(new Tooltip("🎉 Festival Discount Available! 20% OFF"));
                    }
                }
            });

            endDatePicker.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    LocalDate startDate = startDatePicker.getValue();
                    if (startDate != null && date.isBefore(startDate)) { // Changed: allow same day
                        setDisable(true);
                        setStyle("-fx-background-color: #ffc0cb;");
                    } else if (isDateInFestivalPeriod(date)) {
                        // Highlight festival dates
                        setStyle("-fx-background-color: #e8f5e8; -fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                        setTooltip(new Tooltip("🎉 Festival Discount Available! 20% OFF"));
                    }
                }
            });
        }
    }

    private void initializeFestivalDiscountInfo() {
        if (festivalDiscountInfo != null) {
            // Show festival discount information
            LocalDate currentDate = LocalDate.now();
            int currentYear = currentDate.getYear();

            // Check if we're in festival season this year or next year
            LocalDate festivalStart = LocalDate.of(currentYear, 9, 20);
            LocalDate festivalEnd = LocalDate.of(currentYear, 10, 5);

            if (currentDate.isAfter(festivalEnd)) {
                // If current festival period has passed, show next year's dates
                festivalStart = LocalDate.of(currentYear + 1, 9, 20);
                festivalEnd = LocalDate.of(currentYear + 1, 10, 5);
            }

            festivalDiscountInfo.setText("🎉 Festival Season Discount: 20% OFF from " +
                    festivalStart.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd")) +
                    " to " +
                    festivalEnd.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            festivalDiscountInfo.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold; -fx-background-color: #e8f5e8; " +
                    "-fx-padding: 10; -fx-border-radius: 5; -fx-background-radius: 5;");
        }
    }

    private void setupEventListeners() {
        // Add listeners for date changes to update price
        if (startDatePicker != null) {
            startDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
                updateTotalPrice();
                // Update end date picker constraints
                if (endDatePicker != null && newDate != null) {
                    LocalDate currentEndDate = endDatePicker.getValue();
                    if (currentEndDate == null || currentEndDate.isBefore(newDate)) { // Changed: allow same day
                        endDatePicker.setValue(newDate); // Changed: set to same day
                    }
                }
            });
        }

        if (endDatePicker != null) {
            endDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> updateTotalPrice());
        }
    }

    public void setAttractionData(Attraction attraction) {
        this.selectedAttraction = attraction;
        if (attraction != null && touristDestination != null) {
            touristDestination.setText(attraction.getName());
            updateTotalPrice();

            // Check and display high altitude warning
            checkAndDisplayHighAltitudeWarning(attraction);
        }
    }

    private void checkAndDisplayHighAltitudeWarning(Attraction attraction) {
        if (highAltitudeWarning == null || altitudeInfoLabel == null) {
            return;
        }

        try {
            // Parse altitude from attraction data
            String altitudeStr = attraction.getAltitude();
            if (altitudeStr != null && !altitudeStr.trim().isEmpty()) {
                // Extract numeric value from altitude string (e.g., "3500m" -> 3500)
                String numericPart = altitudeStr.replaceAll("[^0-9.]", "");
                if (!numericPart.isEmpty()) {
                    double altitude = Double.parseDouble(numericPart);

                    if (altitude >= HIGH_ALTITUDE_THRESHOLD) {
                        // Show high altitude warning
                        altitudeInfoLabel.setText(String.format(
                                "This attraction is located at high altitude (%.0fm above sea level)", altitude));

                        highAltitudeWarning.setVisible(true);
                        highAltitudeWarning.setManaged(true);

                        System.out.println("High altitude warning displayed for: " + attraction.getName() +
                                " at " + altitude + "m");
                    } else {
                        // Hide warning for low altitude attractions
                        highAltitudeWarning.setVisible(false);
                        highAltitudeWarning.setManaged(false);
                    }
                } else {
                    // Hide warning if altitude cannot be parsed
                    highAltitudeWarning.setVisible(false);
                    highAltitudeWarning.setManaged(false);
                }
            } else {
                // Hide warning if no altitude data
                highAltitudeWarning.setVisible(false);
                highAltitudeWarning.setManaged(false);
            }
        } catch (NumberFormatException e) {
            // Hide warning if altitude parsing fails
            System.err.println("Error parsing altitude for attraction: " + attraction.getName() +
                    " - " + e.getMessage());
            highAltitudeWarning.setVisible(false);
            highAltitudeWarning.setManaged(false);
        }
    }

    private void updateTotalPrice() {
        if (selectedAttraction != null && startDatePicker != null && endDatePicker != null && totalPriceLabel != null) {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            if (startDate != null && endDate != null && !endDate.isBefore(startDate)) { // Changed: allow same day
                long days = ChronoUnit.DAYS.between(startDate, endDate) + 1; // This will be 1 for same day
                double originalPrice = selectedAttraction.getPrice() * days;

                // Check if dates fall within festival period
                boolean hasFestivalDiscount = hasDateRangeInFestivalPeriod(startDate, endDate);

                if (hasFestivalDiscount) {
                    double discountAmount = originalPrice * FESTIVAL_DISCOUNT_RATE;
                    double finalPrice = originalPrice - discountAmount;

                    // Show original price
                    if (originalPriceLabel != null) {
                        originalPriceLabel.setText("Original: $" + String.format("%.2f", originalPrice));
                        originalPriceLabel.setVisible(true);
                        originalPriceLabel.setStyle("-fx-text-decoration: line-through; -fx-text-fill: black;");
                    }

                    // Show discount
                    if (discountLabel != null) {
                        discountLabel.setText("🎉 Festival Discount (20%): -$" + String.format("%.2f", discountAmount));
                        discountLabel.setVisible(true);
                        discountLabel.setStyle("-fx-text-fill: black; -fx-font-weight: bold; " +
                                "-fx-background-color: #e8f5e8; -fx-padding: 5; " +
                                "-fx-border-radius: 3; -fx-background-radius: 3;");
                    }

                    // Show final price
                    totalPriceLabel.setText("$" + String.format("%.2f", finalPrice));
                    totalPriceLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");

                } else {
                    // No discount
                    if (originalPriceLabel != null) {
                        originalPriceLabel.setVisible(false);
                    }
                    if (discountLabel != null) {
                        discountLabel.setVisible(false);
                    }

                    totalPriceLabel.setText("$" + String.format("%.2f", originalPrice));
                    totalPriceLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");
                }
            } else {
                // Invalid date range
                if (originalPriceLabel != null) originalPriceLabel.setVisible(false);
                if (discountLabel != null) discountLabel.setVisible(false);
                totalPriceLabel.setText("$0.00");
                totalPriceLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");
            }
        }
    }

    private boolean isDateInFestivalPeriod(LocalDate date) {
        if (date == null) return false;

        MonthDay dateMonthDay = MonthDay.from(date);

        // Handle the case where festival period crosses year boundary
        // September 20 to October 5
        return !dateMonthDay.isBefore(FESTIVAL_START) && !dateMonthDay.isAfter(FESTIVAL_END);
    }

    private boolean hasDateRangeInFestivalPeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) return false;

        // Check if any date in the range falls within festival period
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (isDateInFestivalPeriod(current)) {
                return true;
            }
            current = current.plusDays(1);
        }
        return false;
    }

    private double calculateFinalPrice(LocalDate startDate, LocalDate endDate) {
        if (selectedAttraction == null || startDate == null || endDate == null) {
            return 0.0;
        }

        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        double originalPrice = selectedAttraction.getPrice() * days;

        if (hasDateRangeInFestivalPeriod(startDate, endDate)) {
            return originalPrice * (1 - FESTIVAL_DISCOUNT_RATE);
        }

        return originalPrice;
    }

    @FXML
    private void handleClose() {
        closeDialog();
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        try {
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser == null) {
                showAlert("Error", "User session expired. Please login again.");
                return;
            }

            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            String contact = touristContactField.getText().trim();
            String notes = notesArea != null ? notesArea.getText().trim() : "";

            // Calculate final price with discount if applicable
            double totalPrice = calculateFinalPrice(startDate, endDate);
            boolean hasDiscount = hasDateRangeInFestivalPeriod(startDate, endDate);

            // Create booking using BookingService
            bookingService.createBooking(
                    currentUser.getId(),
                    currentUser.getFullName(),
                    currentUser.getEmail(),
                    contact,
                    selectedAttraction.getId(),
                    selectedAttraction.getName(),
                    startDate,
                    endDate,
                    notes,
                    totalPrice
            );

            bookingCreated = true;

            // Enhanced success message with discount info
            String successMessage = "🎉 Booking Created Successfully!\n\n" +
                    "📍 Attraction: " + selectedAttraction.getName() + "\n" +
                    "📅 Dates: " + startDate + " to " + endDate + "\n";

            if (hasDiscount) {
                long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
                double originalPrice = selectedAttraction.getPrice() * days;
                double savedAmount = originalPrice - totalPrice;

                successMessage += "💰 Original Cost: $" + String.format("%.2f", originalPrice) + "\n" +
                        "🎊 Festival Discount (20%): -$" + String.format("%.2f", savedAmount) + "\n" +
                        "💵 Final Cost: $" + String.format("%.2f", totalPrice) + "\n";
            } else {
                successMessage += "💰 Total Cost: $" + String.format("%.2f", totalPrice) + "\n";
            }

            successMessage += "👤 Contact: " + contact + "\n\n" +
                    "✅ Your booking is now pending.\n" +
                    "🔔 You will be notified once a guide is assigned.\n" +
                    "📱 Check 'My Bookings' section for updates.";

            if (hasDiscount) {
                successMessage += "\n\n🎉 Congratulations! You saved $" +
                        String.format("%.2f", (selectedAttraction.getPrice() * ChronoUnit.DAYS.between(startDate, endDate) + selectedAttraction.getPrice()) - totalPrice) +
                        " with our Festival Season discount!";
            }

            // Add high altitude safety reminder if applicable
            if (selectedAttraction != null && isHighAltitudeAttraction(selectedAttraction)) {
                successMessage += "\n\n⚠️ SAFETY REMINDER: This is a high-altitude trek. " +
                        "Please follow all safety guidelines and consult with your guide about acclimatization.";
            }

            showAlert("Success", successMessage);
            closeDialog();

        } catch (IOException e) {
            System.err.println("Error creating booking: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to create booking. Please try again.\n\nError: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "An unexpected error occurred. Please try again.\n\nError: " + e.getMessage());
        }
    }

    private boolean isHighAltitudeAttraction(Attraction attraction) {
        try {
            String altitudeStr = attraction.getAltitude();
            if (altitudeStr != null && !altitudeStr.trim().isEmpty()) {
                String numericPart = altitudeStr.replaceAll("[^0-9.]", "");
                if (!numericPart.isEmpty()) {
                    double altitude = Double.parseDouble(numericPart);
                    return altitude >= HIGH_ALTITUDE_THRESHOLD;
                }
            }
        } catch (NumberFormatException e) {
            // Ignore parsing errors
        }
        return false;
    }

    private boolean validateForm() {
        // Check if required fields exist and are filled
        if (touristContactField == null || touristContactField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "📞 Contact number is required!");
            if (touristContactField != null) touristContactField.requestFocus();
            return false;
        }

        if (startDatePicker == null || startDatePicker.getValue() == null) {
            showAlert("Validation Error", "📅 Start date is required!");
            if (startDatePicker != null) startDatePicker.requestFocus();
            return false;
        }

        if (endDatePicker == null || endDatePicker.getValue() == null) {
            showAlert("Validation Error", "📅 End date is required!");
            if (endDatePicker != null) endDatePicker.requestFocus();
            return false;
        }

        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (endDate.isBefore(startDate)) { // Changed: removed the +1 day requirement
            showAlert("Validation Error", "❌ End date cannot be before start date!");
            endDatePicker.requestFocus();
            return false;
        }

        if (startDate.isBefore(LocalDate.now().plusDays(1))) { // Changed: tomorrow is minimum
            showAlert("Validation Error", "❌ Start date must be tomorrow or later!");
            startDatePicker.requestFocus();
            return false;
        }

        // Validate contact number format
        String contact = touristContactField.getText().trim();
        if (!contact.matches("^[+]?[0-9\\s\\-()]{7,20}$")) {
            showAlert("Validation Error", "📞 Please enter a valid contact number!\n\n" +
                    "Examples:\n" +
                    "• +977-1-4247041\n" +
                    "• 9841234567\n" +
                    "• +1-555-123-4567");
            touristContactField.requestFocus();
            return false;
        }

        // Check if booking duration is reasonable (not more than 30 days)
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (days > 30) {
            showAlert("Validation Error", "⏰ Booking duration cannot exceed 30 days!\n\n" +
                    "Current duration: " + days + " days\n" +
                    "Maximum allowed: 30 days");
            endDatePicker.requestFocus();
            return false;
        }

        if (days < 1) {
            showAlert("Validation Error", "⏰ Booking must be at least 1 day!");
            endDatePicker.requestFocus();
            return false;
        }

        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(title.equals("Success") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Make success alerts larger for better readability
        if (title.equals("Success")) {
            alert.setResizable(true);
            alert.getDialogPane().setPrefWidth(600);
            alert.getDialogPane().setPrefHeight(400);
        }

        alert.showAndWait();
    }

    private void closeDialog() {
        if (closeButton != null) {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        }
    }

    // Method to check if booking was created (for parent controller to refresh data)
    public boolean wasBookingCreated() {
        return bookingCreated;
    }
}
