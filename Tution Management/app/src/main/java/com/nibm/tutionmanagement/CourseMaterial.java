package com.nibm.tutionmanagement;

public class CourseMaterial {
    private String title;
    private String description;
    private String fileUrl; // URL from Firebase Storage
    private String firebaseKey; // Firebase DB key for this material

    // No-argument constructor required for Firebase
    public CourseMaterial() {}

    public CourseMaterial(String title, String description, String fileUrl) {
        this.title = title;
        this.description = description;
        this.fileUrl = fileUrl;
    }

    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getFirebaseKey() { return firebaseKey; }
    public void setFirebaseKey(String firebaseKey) { this.firebaseKey = firebaseKey; }
}
