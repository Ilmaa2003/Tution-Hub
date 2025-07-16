package com.nibm.tutionmanagement;

public class Submission {
    private String assignmentId;      // Reference to the assignment
    private String studentId;         // Who submitted
    private String submissionUri;     // Local file URI or path
    private long submittedAt;         // Timestamp of submission

    public Submission() {
        // Required empty constructor for Firebase or serialization
    }

    public Submission(String assignmentId, String studentId, String submissionUri, long submittedAt) {
        this.assignmentId = assignmentId;
        this.studentId = studentId;
        this.submissionUri = submissionUri;
        this.submittedAt = submittedAt;
    }

    // Getters and setters

    public String getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getSubmissionUri() {
        return submissionUri;
    }

    public void setSubmissionUri(String submissionUri) {
        this.submissionUri = submissionUri;
    }

    public long getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(long submittedAt) {
        this.submittedAt = submittedAt;
    }
}
