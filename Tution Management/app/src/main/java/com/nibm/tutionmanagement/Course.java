package com.nibm.tutionmanagement;

import java.util.ArrayList;
import java.util.List;

public class Course {
    private int assignmentId;
    private String name;
    private String grade;
    private String batch;
    private String description;
    private String scheduleType;

    private List<CourseMaterial> materials = new ArrayList<>();

    public Course(int assignmentId, String name, String grade, String batch, String description, String scheduleType) {
        this.assignmentId = assignmentId;
        this.name = name;
        this.grade = grade;
        this.batch = batch;
        this.description = description;
        this.scheduleType = scheduleType;
    }

    public int getAssignmentId() { return assignmentId; }
    public String getName() { return name; }
    public String getGrade() { return grade; }
    public String getBatch() { return batch; }
    public String getDescription() { return description; }
    public String getScheduleType() { return scheduleType; }

    public List<CourseMaterial> getMaterials() { return materials; }

    public void setMaterials(List<CourseMaterial> materials) { this.materials = materials; }
}
