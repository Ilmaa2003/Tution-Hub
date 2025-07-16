package com.nibm.tutionmanagement;

import java.util.ArrayList;
import java.util.List;

public class Course2 {
    private int assignmentId;
    private String name;
    private String grade;
    private String batch;
    private String description;
    private String scheduleType;

    private List<Assignment> assignments;
    private List<CourseMaterial> materials;

    public Course2(int assignmentId, String name, String grade, String batch,
                   String description, String scheduleType) {
        this.assignmentId = assignmentId;
        this.name = name;
        this.grade = grade;
        this.batch = batch;
        this.description = description;
        this.scheduleType = scheduleType;
        this.assignments = new ArrayList<>();
        this.materials = new ArrayList<>();
    }



    // Getters and setters

    public int getAssignmentId() {
        return assignmentId;
    }

    public String getName() {
        return name;
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

    public String getScheduleType() {
        return scheduleType;
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<Assignment> assignments) {
        this.assignments = assignments;
    }

    public List<CourseMaterial> getMaterials() {
        return materials;
    }

    public void setMaterials(List<CourseMaterial> materials) {
        this.materials = materials;
    }

    public void setName(String name) { this.name = name; }
    public void setGrade(String grade) { this.grade = grade; }
    public void setBatch(String batch) { this.batch = batch; }

}
