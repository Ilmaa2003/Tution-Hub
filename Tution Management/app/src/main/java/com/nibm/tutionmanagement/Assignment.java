package com.nibm.tutionmanagement;

public class Assignment {
    private String title;
    private String description;
    private String deadline;  // format "yyyy-MM-dd HH:mm"
    private String fileUrl;   // new field to store file path/URL
    private String firebaseKey;  // Firebase key for this assignment (optional)

    public Assignment() {
        // Default constructor required for Firebase deserialization
    }

    public Assignment(String title, String description, String deadline, String fileUrl) {
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.fileUrl = fileUrl;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDeadline() { return deadline; }
    public String getFileUrl() { return fileUrl; }
    public String getFirebaseKey() { return firebaseKey; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public void setFirebaseKey(String firebaseKey) { this.firebaseKey = firebaseKey; }
}
