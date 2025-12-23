package com.aidsync.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

public class Beneficiary {
    private int id;
    private String beneficiaryId;
    private String fullName;
    private LocalDate birthDate;
    private Gender gender;
    private CivilStatus civilStatus;
    private String contactNumber;
    private String email;
    private int barangayId;
    private String barangayName;
    private int purokId;
    private String purokName;
    private String streetSitio;
    private boolean isHouseholdHead;
    private int familySize;
    private IncomeRange monthlyIncomeRange;
    private EmploymentStatus employmentStatus;
    private int priorityLevel;
    private BeneficiaryStatus status;
    private String photoPath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Vulnerability flags
    private boolean isPWD;
    private boolean isSeniorCitizen;
    private boolean isPregnantLactating;
    private boolean isSoloParent;
    private boolean hasChronicIllness;
    private boolean isOrphan;
    private boolean isIndigenous;
    private boolean isHomeless;
    private String disabilityType;
    
    public enum Gender {
        MALE("Male"),
        FEMALE("Female"),
        PREFER_NOT_TO_SAY("Prefer not to say");
        
        private final String displayName;
        Gender(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }
    
    public enum CivilStatus {
        SINGLE("Single"),
        MARRIED("Married"),
        WIDOWED("Widowed"),
        SEPARATED("Separated"),
        DIVORCED("Divorced");
        
        private final String displayName;
        CivilStatus(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }
    
    public enum IncomeRange {
        BELOW_5000("Below ₱5,000"),
        RANGE_5000_10000("₱5,000 - ₱10,000"),
        RANGE_10000_15000("₱10,000 - ₱15,000"),
        RANGE_15000_25000("₱15,000 - ₱25,000"),
        ABOVE_25000("Above ₱25,000");
        
        private final String displayName;
        IncomeRange(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }
    
    public enum EmploymentStatus {
        EMPLOYED("Employed"),
        UNEMPLOYED("Unemployed"),
        SELF_EMPLOYED("Self-employed"),
        STUDENT("Student"),
        RETIRED("Retired");
        
        private final String displayName;
        EmploymentStatus(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }
    
    public enum BeneficiaryStatus {
        ACTIVE("Active"),
        INACTIVE("Inactive"),
        TRANSFERRED("Transferred"),
        DECEASED("Deceased");
        
        private final String displayName;
        BeneficiaryStatus(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }
    
    // Constructors
    public Beneficiary() {}
    
    public Beneficiary(String fullName, LocalDate birthDate, Gender gender) {
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.status = BeneficiaryStatus.ACTIVE;
        this.priorityLevel = 3; // Default priority
        this.familySize = 1;
        this.isHouseholdHead = false;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getBeneficiaryId() { return beneficiaryId; }
    public void setBeneficiaryId(String beneficiaryId) { this.beneficiaryId = beneficiaryId; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    
    public CivilStatus getCivilStatus() { return civilStatus; }
    public void setCivilStatus(CivilStatus civilStatus) { this.civilStatus = civilStatus; }
    
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public int getBarangayId() { return barangayId; }
    public void setBarangayId(int barangayId) { this.barangayId = barangayId; }
    
    public String getBarangayName() { return barangayName; }
    public void setBarangayName(String barangayName) { this.barangayName = barangayName; }
    
    public int getPurokId() { return purokId; }
    public void setPurokId(int purokId) { this.purokId = purokId; }
    
    public String getPurokName() { return purokName; }
    public void setPurokName(String purokName) { this.purokName = purokName; }
    
    public String getStreetSitio() { return streetSitio; }
    public void setStreetSitio(String streetSitio) { this.streetSitio = streetSitio; }
    
    public boolean isHouseholdHead() { return isHouseholdHead; }
    public void setHouseholdHead(boolean householdHead) { isHouseholdHead = householdHead; }
    
    public int getFamilySize() { return familySize; }
    public void setFamilySize(int familySize) { this.familySize = familySize; }
    
    public IncomeRange getMonthlyIncomeRange() { return monthlyIncomeRange; }
    public void setMonthlyIncomeRange(IncomeRange monthlyIncomeRange) { this.monthlyIncomeRange = monthlyIncomeRange; }
    
    public EmploymentStatus getEmploymentStatus() { return employmentStatus; }
    public void setEmploymentStatus(EmploymentStatus employmentStatus) { this.employmentStatus = employmentStatus; }
    
    public int getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(int priorityLevel) { this.priorityLevel = priorityLevel; }
    
    public BeneficiaryStatus getStatus() { return status; }
    public void setStatus(BeneficiaryStatus status) { this.status = status; }
    
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Vulnerability getters and setters
    public boolean isPWD() { return isPWD; }
    public void setPWD(boolean PWD) { isPWD = PWD; }
    
    public boolean isSeniorCitizen() { return isSeniorCitizen; }
    public void setSeniorCitizen(boolean seniorCitizen) { isSeniorCitizen = seniorCitizen; }
    
    public boolean isPregnantLactating() { return isPregnantLactating; }
    public void setPregnantLactating(boolean pregnantLactating) { isPregnantLactating = pregnantLactating; }
    
    public boolean isSoloParent() { return isSoloParent; }
    public void setSoloParent(boolean soloParent) { isSoloParent = soloParent; }
    
    public boolean isHasChronicIllness() { return hasChronicIllness; }
    public void setHasChronicIllness(boolean hasChronicIllness) { this.hasChronicIllness = hasChronicIllness; }
    
    public boolean isOrphan() { return isOrphan; }
    public void setOrphan(boolean orphan) { isOrphan = orphan; }
    
    public boolean isIndigenous() { return isIndigenous; }
    public void setIndigenous(boolean indigenous) { isIndigenous = indigenous; }
    
    public boolean isHomeless() { return isHomeless; }
    public void setHomeless(boolean homeless) { isHomeless = homeless; }
    
    public String getDisabilityType() { return disabilityType; }
    public void setDisabilityType(String disabilityType) { this.disabilityType = disabilityType; }
    
    // Utility methods
    public int getAge() {
        if (birthDate == null) return 0;
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
    
    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        if (streetSitio != null && !streetSitio.trim().isEmpty()) {
            address.append(streetSitio).append(", ");
        }
        if (purokName != null) {
            address.append(purokName).append(", ");
        }
        if (barangayName != null) {
            address.append("Barangay ").append(barangayName);
        }
        return address.toString();
    }
    
    public boolean isVulnerable() {
        return isPWD || isSeniorCitizen || isPregnantLactating || isSoloParent || 
               hasChronicIllness || isOrphan || isIndigenous || isHomeless;
    }
    
    public String getPriorityDescription() {
        switch (priorityLevel) {
            case 1: return "Highest Priority";
            case 2: return "High Priority";
            case 3: return "Medium Priority";
            case 4: return "Low Priority";
            case 5: return "Lowest Priority";
            default: return "Unknown Priority";
        }
    }
    
    @Override
    public String toString() {
        return fullName + " (" + beneficiaryId + ")";
    }
}