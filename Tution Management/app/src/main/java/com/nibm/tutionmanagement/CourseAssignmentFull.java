package com.nibm.tutionmanagement;

public class CourseAssignmentFull {
    private int assignmentId;
    private String courseName;
    private String grade;
    private String batch;
    private String description;
    private String scheduleType;

    private String dayOfWeek;
    private String startTime;
    private String endTime;

    public CourseAssignmentFull(int assignmentId, String courseName, String grade, String batch,
                                String description, String scheduleType,
                                String dayOfWeek, String startTime, String endTime) {
        this.assignmentId = assignmentId;
        this.courseName = courseName;
        this.grade = grade;
        this.batch = batch;
        this.description = description;
        this.scheduleType = scheduleType;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters and setters here (omitted for brevity)
    public int getAssignmentId() { return assignmentId; }
    public String getCourseName() { return courseName; }
    public String getGrade() { return grade; }
    public String getBatch() { return batch; }
    public String getDescription() { return description; }
    public String getScheduleType() { return scheduleType; }
    public String getDayOfWeek() { return dayOfWeek; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }

    public void setDescription(String description) { this.description = description; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
}
