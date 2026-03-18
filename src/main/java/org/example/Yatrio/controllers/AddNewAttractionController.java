package org.example.Yatrio.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.Yatrio.dao.AttractionDAO;
import org.example.Yatrio.models.Attraction;
import org.example.Yatrio.utils.DataRefreshManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;

public class AddNewAttractionController implements Initializable {

    @FXML private TextField nameField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField locationField;
    @FXML private ComboBox<String> difficultyComboBox;
    @FXML private TextField altitudeField;
    @FXML private TextField durationField;
    @FXML private TextField bestSeasonField;
    @FXML private TextField priceField;
    @FXML private TextArea descriptionArea;
    @FXML private Button closeButton;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;

    // Image related controls
    @FXML private ImageView imagePreview;
    @FXML private Label imageStatusLabel;
    @FXML private Button selectImageButton;
    @FXML private Button removeImageButton;

    private AttractionDAO attractionDAO;
    private File selectedImageFile;
    private String savedImagePath;

    public AddNewAttractionController() {
        this.attractionDAO = new AttractionDAO();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBoxes();
        setupImagePreview();
        setupValidation();
    }

    private void setupComboBoxes() {
        typeComboBox.getItems().addAll(
                "Trekking",
                "Cultural",
                "Wildlife",
                "Sightseeing",
                "Adventure",
                "Religious",
                "Historical",
                "Natural Wonder",
                "Mountain Peak",
                "National Park"
        );

        difficultyComboBox.getItems().addAll(
                "Easy",
                "Moderate",
                "Hard",
                "Extreme"
        );
    }

    private void setupImagePreview() {
        // Set default placeholder image
        try {
            Image placeholderImage = new Image(getClass().getResourceAsStream("/org/example/Yatrio/images/new.jpg"));
            imagePreview.setImage(placeholderImage);
            imageStatusLabel.setText("Default placeholder image");
        } catch (Exception e) {
            imageStatusLabel.setText("No image selected");
        }
    }

    private void setupValidation() {
        // Add real-time validation for numeric fields
        altitudeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                altitudeField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                priceField.setText(oldValue);
            }
        });
    }

    @FXML
    private void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Attraction Image");

        // Set extension filters
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
                "Image Files", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp");
        fileChooser.getExtensionFilters().add(imageFilter);

        // Set initial directory
        File initialDir = new File(System.getProperty("user.home"));
        if (initialDir.exists()) {
            fileChooser.setInitialDirectory(initialDir);
        }

        // Show file chooser
        Stage stage = (Stage) selectImageButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            // Validate file size (max 5MB)
            long fileSizeInMB = selectedFile.length() / (1024 * 1024);
            if (fileSizeInMB > 5) {
                showAlert("File Too Large", "Please select an image smaller than 5MB.\nSelected file size: " + fileSizeInMB + "MB");
                return;
            }

            try {
                // Load and display the image
                Image image = new Image(selectedFile.toURI().toString());
                imagePreview.setImage(image);

                // Update UI
                selectedImageFile = selectedFile;
                imageStatusLabel.setText("Selected: " + selectedFile.getName());
                removeImageButton.setVisible(true);

                System.out.println("Image selected: " + selectedFile.getAbsolutePath());

            } catch (Exception e) {
                showAlert("Invalid Image", "Failed to load the selected image. Please choose a valid image file.");
                System.err.println("Error loading image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRemoveImage() {
        // Reset to default placeholder
        try {
            Image placeholderImage = new Image(getClass().getResourceAsStream("/org/example/Yatrio/images/new.jpg"));
            imagePreview.setImage(placeholderImage);
        } catch (Exception e) {
            imagePreview.setImage(null);
        }

        selectedImageFile = null;
        savedImagePath = null;
        imageStatusLabel.setText("Default placeholder image");
        removeImageButton.setVisible(false);
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
            // Get form data
            String name = nameField.getText().trim();
            String type = typeComboBox.getValue();
            String location = locationField.getText().trim();
            String difficulty = difficultyComboBox.getValue();
            String altitude = altitudeField.getText().trim() + "m";
            String duration = durationField.getText().trim();
            String bestSeason = bestSeasonField.getText().trim();
            String description = descriptionArea.getText().trim();
            double price = Double.parseDouble(priceField.getText().trim());

            // Handle image saving - FIXED: Save to accessible directory
            String imagePath = "new.jpg"; // default image
            if (selectedImageFile != null) {
                imagePath = saveImageFile(selectedImageFile, name);
            }

            // Create attraction object
            Attraction attraction = new Attraction(name, type, location, difficulty,
                    altitude, duration, bestSeason, description, price, imagePath);

            // Save to database
            attractionDAO.save(attraction);

            // Notify all controllers about data change
            DataRefreshManager.notifyAttractionDataChanged();

            showAlert("Success", "✅ Attraction added successfully!\n\n" +
                    "📍 Name: " + name + "\n" +
                    "🏔️ Location: " + location + "\n" +
                    "💰 Price: $" + String.format("%.2f", price) + "/day\n" +
                    "📸 Image: " + (selectedImageFile != null ? "Custom image uploaded" : "Default placeholder"));

            closeDialog();

        } catch (NumberFormatException e) {
            showAlert("Invalid Price", "Please enter a valid price amount.");
            priceField.requestFocus();
        } catch (IOException e) {
            showAlert("Error", "Failed to save attraction: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String saveImageFile(File sourceFile, String attractionName) throws IOException {
        // Create images directory in the project root (accessible at runtime)
        Path imagesDir = Paths.get("images");
        if (!Files.exists(imagesDir)) {
            Files.createDirectories(imagesDir);
            System.out.println("Created images directory: " + imagesDir.toAbsolutePath());
        }

        // Generate unique filename
        String fileExtension = getFileExtension(sourceFile.getName());
        String sanitizedName = attractionName.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
        String fileName = sanitizedName + "_" + System.currentTimeMillis() + "." + fileExtension;

        // Copy file to images directory
        Path targetPath = imagesDir.resolve(fileName);
        Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        System.out.println("Image saved to: " + targetPath.toAbsolutePath());
        return fileName;
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "jpg"; // default extension
    }

    private boolean validateForm() {
        // Name validation
        if (nameField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "📝 Attraction name is required!");
            nameField.requestFocus();
            return false;
        }

        // Type validation
        if (typeComboBox.getValue() == null) {
            showAlert("Validation Error", "🏷️ Attraction type is required!");
            typeComboBox.requestFocus();
            return false;
        }

        // Location validation
        if (locationField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "📍 Location is required!");
            locationField.requestFocus();
            return false;
        }

        // Difficulty validation
        if (difficultyComboBox.getValue() == null) {
            showAlert("Validation Error", "⚡ Difficulty level is required!");
            difficultyComboBox.requestFocus();
            return false;
        }

        // Altitude validation
        if (altitudeField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "🏔️ Altitude is required!");
            altitudeField.requestFocus();
            return false;
        }

        try {
            int altitude = Integer.parseInt(altitudeField.getText().trim());
            if (altitude < 0 || altitude > 9000) {
                showAlert("Validation Error", "🏔️ Altitude must be between 0 and 9000 meters!");
                altitudeField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "🏔️ Please enter a valid altitude in meters!");
            altitudeField.requestFocus();
            return false;
        }

        // Duration validation
        if (durationField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "⏱️ Duration is required!");
            durationField.requestFocus();
            return false;
        }

        // Best season validation
        if (bestSeasonField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "🌤️ Best season information is required!");
            bestSeasonField.requestFocus();
            return false;
        }

        // Price validation
        if (priceField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "💰 Price is required!");
            priceField.requestFocus();
            return false;
        }

        try {
            double price = Double.parseDouble(priceField.getText().trim());
            if (price <= 0) {
                showAlert("Validation Error", "💰 Price must be greater than 0!");
                priceField.requestFocus();
                return false;
            }
            if (price > 10000) {
                showAlert("Validation Error", "💰 Price seems too high! Please enter a reasonable amount.");
                priceField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "💰 Please enter a valid price amount!");
            priceField.requestFocus();
            return false;
        }

        // Description validation
        if (descriptionArea.getText().trim().isEmpty()) {
            showAlert("Validation Error", "📝 Description is required!");
            descriptionArea.requestFocus();
            return false;
        }

        if (descriptionArea.getText().trim().length() < 20) {
            showAlert("Validation Error", "📝 Description must be at least 20 characters long!");
            descriptionArea.requestFocus();
            return false;
        }

        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(title.equals("Success") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Make the dialog resizable for longer messages
        alert.setResizable(true);
        alert.getDialogPane().setPrefWidth(400);

        alert.showAndWait();
    }

    private void closeDialog() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
