package com.aidsync.controller;

import com.aidsync.model.Beneficiary;
import com.aidsync.service.BeneficiaryService;
import com.aidsync.util.AlertUtil;
import com.aidsync.util.SceneManager;
import com.aidsync.util.SecurityUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class BeneficiaryController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(BeneficiaryController.class);
    
    @FXML private TextField searchField;
    @FXML private ComboBox<String> barangayFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TableView<Beneficiary> beneficiaryTable;
    @FXML private TableColumn<Beneficiary, String> idColumn;
    @FXML private TableColumn<Beneficiary, String> nameColumn;
    @FXML private TableColumn<Beneficiary, Integer> ageColumn;
    @FXML private TableColumn<Beneficiary, String> barangayColumn;
    @FXML private TableColumn<Beneficiary, String> statusColumn;
    @FXML private TableColumn<Beneficiary, Integer> priorityColumn;
    
    // Form fields
    @FXML private TextField fullNameField;
    @FXML private DatePicker birthDatePicker;
    @FXML private ComboBox<Beneficiary.Gender> genderComboBox;
    @FXML private ComboBox<Beneficiary.CivilStatus> civilStatusComboBox;
    @FXML private TextField contactNumberField;
    @FXML private ComboBox<String> barangayComboBox;
    @FXML private TextField streetSitioField;
    @FXML private CheckBox householdHeadCheckBox;
    @FXML private Spinner<Integer> familySizeSpinner;
    @FXML private ComboBox<Beneficiary.IncomeRange> incomeRangeComboBox;
    @FXML private ComboBox<Beneficiary.EmploymentStatus> employmentStatusComboBox;
    @FXML private Slider prioritySlider;
    @FXML private Label priorityLabel;
    
    // Vulnerability checkboxes
    @FXML private CheckBox pwdCheckBox;
    @FXML private CheckBox seniorCitizenCheckBox;
    @FXML private CheckBox pregnantLactatingCheckBox;
    @FXML private CheckBox soloParentCheckBox;
    @FXML private CheckBox chronicIllnessCheckBox;
    @FXML private CheckBox orphanCheckBox;
    @FXML private CheckBox indigenousCheckBox;
    @FXML private CheckBox homelessCheckBox;
    
    @FXML private Button saveButton;
    @FXML private Button clearButton;
    @FXML private Button deleteButton;
    @FXML private Button backButton;
    
    private BeneficiaryService beneficiaryService;
    private ObservableList<Beneficiary> beneficiaryList;
    private Beneficiary selectedBeneficiary;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        beneficiaryService = new BeneficiaryService();
        beneficiaryList = FXCollections.observableArrayList();
        
        setupTable();
        setupForm();
        loadBeneficiaries();
        setupFilters();
    }
    
    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("beneficiaryId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        ageColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getAge()).asObject());
        barangayColumn.setCellValueFactory(new PropertyValueFactory<>("barangayName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priorityLevel"));
        
        beneficiaryTable.setItems(beneficiaryList);
        beneficiaryTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectedBeneficiary = newSelection;
                    populateForm(newSelection);
                }
            });
    }
    
    private void setupForm() {
        // Initialize combo boxes
        genderComboBox.setItems(FXCollections.observableArrayList(Beneficiary.Gender.values()));
        civilStatusComboBox.setItems(FXCollections.observableArrayList(Beneficiary.CivilStatus.values()));
        incomeRangeComboBox.setItems(FXCollections.observableArrayList(Beneficiary.IncomeRange.values()));
        employmentStatusComboBox.setItems(FXCollections.observableArrayList(Beneficiary.EmploymentStatus.values()));
        
        // Setup family size spinner
        familySizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1));
        
        // Setup priority slider
        prioritySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int priority = newVal.intValue();
            priorityLabel.setText(getPriorityDescription(priority));
        });
        
        // Load barangays
        loadBarangays();
    }
    
    private void loadBarangays() {
        try {
            List<String> barangays = beneficiaryService.getAllBarangays();
            barangayComboBox.setItems(FXCollections.observableArrayList(barangays));
            barangayFilter.setItems(FXCollections.observableArrayList(barangays));
        } catch (Exception e) {
            logger.error("Error loading barangays", e);
            AlertUtil.showError("Error", "Failed to load barangays: " + e.getMessage());
        }
    }
    
    private void setupFilters() {
        statusFilter.setItems(FXCollections.observableArrayList("All", "ACTIVE", "INACTIVE", "TRANSFERRED", "DECEASED"));
        statusFilter.setValue("All");
        barangayFilter.setValue("All Barangays");
        
        // Add filter listeners
        searchField.textProperty().addListener((obs, oldText, newText) -> filterBeneficiaries());
        barangayFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterBeneficiaries());
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterBeneficiaries());
    }
    
    private void loadBeneficiaries() {
        try {
            List<Beneficiary> beneficiaries = beneficiaryService.getAllBeneficiaries();
            beneficiaryList.setAll(beneficiaries);
        } catch (Exception e) {
            logger.error("Error loading beneficiaries", e);
            AlertUtil.showError("Error", "Failed to load beneficiaries: " + e.getMessage());
        }
    }
    
    private void filterBeneficiaries() {
        try {
            String searchText = searchField.getText();
            String selectedBarangay = this.barangayFilter.getValue();
            String selectedStatus = this.statusFilter.getValue();
            
            // Validate search input
            if (searchText != null && !SecurityUtil.isInputSafe(searchText)) {
                logger.warn("Suspicious search input detected: {}", SecurityUtil.sanitizeForLogging(searchText));
                AlertUtil.showWarning("Invalid Input", "Search contains invalid characters.");
                return;
            }
            
            List<Beneficiary> filtered = beneficiaryService.searchBeneficiaries(searchText, selectedBarangay, selectedStatus);
            if (filtered != null) {
                beneficiaryList.setAll(filtered);
            }
        } catch (Exception e) {
            logger.error("Error filtering beneficiaries", e);
            AlertUtil.showError("Search Error", "Failed to filter beneficiaries. Please try again.");
        }
    }
    
    private void populateForm(Beneficiary beneficiary) {
        fullNameField.setText(beneficiary.getFullName());
        birthDatePicker.setValue(beneficiary.getBirthDate());
        genderComboBox.setValue(beneficiary.getGender());
        civilStatusComboBox.setValue(beneficiary.getCivilStatus());
        contactNumberField.setText(beneficiary.getContactNumber());
        barangayComboBox.setValue(beneficiary.getBarangayName());
        streetSitioField.setText(beneficiary.getStreetSitio());
        householdHeadCheckBox.setSelected(beneficiary.isHouseholdHead());
        familySizeSpinner.getValueFactory().setValue(beneficiary.getFamilySize());
        incomeRangeComboBox.setValue(beneficiary.getMonthlyIncomeRange());
        employmentStatusComboBox.setValue(beneficiary.getEmploymentStatus());
        prioritySlider.setValue(beneficiary.getPriorityLevel());
        
        // Vulnerability flags
        pwdCheckBox.setSelected(beneficiary.isPWD());
        seniorCitizenCheckBox.setSelected(beneficiary.isSeniorCitizen());
        pregnantLactatingCheckBox.setSelected(beneficiary.isPregnantLactating());
        soloParentCheckBox.setSelected(beneficiary.isSoloParent());
        chronicIllnessCheckBox.setSelected(beneficiary.isHasChronicIllness());
        orphanCheckBox.setSelected(beneficiary.isOrphan());
        indigenousCheckBox.setSelected(beneficiary.isIndigenous());
        homelessCheckBox.setSelected(beneficiary.isHomeless());
        
        deleteButton.setDisable(false);
    }
    
    private void clearForm() {
        fullNameField.clear();
        birthDatePicker.setValue(null);
        genderComboBox.setValue(null);
        civilStatusComboBox.setValue(null);
        contactNumberField.clear();
        barangayComboBox.setValue(null);
        streetSitioField.clear();
        householdHeadCheckBox.setSelected(false);
        familySizeSpinner.getValueFactory().setValue(1);
        incomeRangeComboBox.setValue(null);
        employmentStatusComboBox.setValue(null);
        prioritySlider.setValue(3);
        
        // Clear vulnerability checkboxes
        pwdCheckBox.setSelected(false);
        seniorCitizenCheckBox.setSelected(false);
        pregnantLactatingCheckBox.setSelected(false);
        soloParentCheckBox.setSelected(false);
        chronicIllnessCheckBox.setSelected(false);
        orphanCheckBox.setSelected(false);
        indigenousCheckBox.setSelected(false);
        homelessCheckBox.setSelected(false);
        
        selectedBeneficiary = null;
        deleteButton.setDisable(true);
    }
    
    @FXML
    private void handleSave() {
        try {
            if (!validateForm()) return;
            
            Beneficiary beneficiary = selectedBeneficiary != null ? selectedBeneficiary : new Beneficiary();
            
            // Set basic info
            beneficiary.setFullName(fullNameField.getText().trim());
            beneficiary.setBirthDate(birthDatePicker.getValue());
            beneficiary.setGender(genderComboBox.getValue());
            beneficiary.setCivilStatus(civilStatusComboBox.getValue());
            beneficiary.setContactNumber(contactNumberField.getText().trim());
            beneficiary.setBarangayName(barangayComboBox.getValue());
            beneficiary.setStreetSitio(streetSitioField.getText().trim());
            beneficiary.setHouseholdHead(householdHeadCheckBox.isSelected());
            beneficiary.setFamilySize(familySizeSpinner.getValue());
            beneficiary.setMonthlyIncomeRange(incomeRangeComboBox.getValue());
            beneficiary.setEmploymentStatus(employmentStatusComboBox.getValue());
            beneficiary.setPriorityLevel((int) prioritySlider.getValue());
            
            // Set vulnerability flags
            beneficiary.setPWD(pwdCheckBox.isSelected());
            beneficiary.setSeniorCitizen(seniorCitizenCheckBox.isSelected());
            beneficiary.setPregnantLactating(pregnantLactatingCheckBox.isSelected());
            beneficiary.setSoloParent(soloParentCheckBox.isSelected());
            beneficiary.setHasChronicIllness(chronicIllnessCheckBox.isSelected());
            beneficiary.setOrphan(orphanCheckBox.isSelected());
            beneficiary.setIndigenous(indigenousCheckBox.isSelected());
            beneficiary.setHomeless(homelessCheckBox.isSelected());
            
            if (selectedBeneficiary == null) {
                beneficiaryService.createBeneficiary(beneficiary);
                AlertUtil.showInfo("Success", "Beneficiary created successfully!");
            } else {
                beneficiaryService.updateBeneficiary(beneficiary);
                AlertUtil.showInfo("Success", "Beneficiary updated successfully!");
            }
            
            loadBeneficiaries();
            clearForm();
            
        } catch (Exception e) {
            logger.error("Error saving beneficiary", e);
            AlertUtil.showError("Error", "Failed to save beneficiary: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleClear() {
        clearForm();
    }
    
    @FXML
    private void handleDelete() {
        if (selectedBeneficiary == null) return;
        
        if (AlertUtil.showConfirmation("Delete Beneficiary", 
            "Are you sure you want to delete " + selectedBeneficiary.getFullName() + "?")) {
            try {
                beneficiaryService.deleteBeneficiary(selectedBeneficiary.getId());
                AlertUtil.showInfo("Success", "Beneficiary deleted successfully!");
                loadBeneficiaries();
                clearForm();
            } catch (Exception e) {
                logger.error("Error deleting beneficiary", e);
                AlertUtil.showError("Error", "Failed to delete beneficiary: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleBack() {
        SceneManager.switchToDashboard();
    }
    
    private boolean validateForm() {
        String fullName = fullNameField.getText();
        if (fullName == null || fullName.trim().isEmpty()) {
            AlertUtil.showWarning("Validation Error", "Full name is required.");
            return false;
        }
        
        if (!SecurityUtil.isInputSafe(fullName)) {
            AlertUtil.showWarning("Validation Error", "Full name contains invalid characters.");
            logger.warn("Suspicious input detected in full name field: {}", SecurityUtil.sanitizeForLogging(fullName));
            return false;
        }
        
        if (birthDatePicker.getValue() == null) {
            AlertUtil.showWarning("Validation Error", "Birth date is required.");
            return false;
        }
        
        if (birthDatePicker.getValue().isAfter(LocalDate.now())) {
            AlertUtil.showWarning("Validation Error", "Birth date cannot be in the future.");
            return false;
        }
        
        if (genderComboBox.getValue() == null) {
            AlertUtil.showWarning("Validation Error", "Gender is required.");
            return false;
        }
        
        if (barangayComboBox.getValue() == null) {
            AlertUtil.showWarning("Validation Error", "Barangay is required.");
            return false;
        }
        
        // Validate contact number if provided
        String contactNumber = contactNumberField.getText();
        if (contactNumber != null && !contactNumber.trim().isEmpty()) {
            if (!SecurityUtil.isInputSafe(contactNumber)) {
                AlertUtil.showWarning("Validation Error", "Contact number contains invalid characters.");
                return false;
            }
        }
        
        return true;
    }
    
    private String getPriorityDescription(int priority) {
        switch (priority) {
            case 1: return "Highest Priority";
            case 2: return "High Priority";
            case 3: return "Medium Priority";
            case 4: return "Low Priority";
            case 5: return "Lowest Priority";
            default: return "Unknown Priority";
        }
    }
}