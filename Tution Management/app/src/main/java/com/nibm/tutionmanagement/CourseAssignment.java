package com.nibm.tutionmanagement;

public class CourseAssignment {
    private int assignmentId;
    private String courseName;
    private String grade;
    private String batch;
    private String description;
    private String scheduleType;
    private String dayOfWeek;
    private String startTime;
    private String endTime;

    public CourseAssignment(int assignmentId, String courseName, String grade, String batch,
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

    // Getters and setters

    public int getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getGrade() {
        return grade;
    }

    public String getBatch() {
        return batch;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
