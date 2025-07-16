package com.nibm.tutionmanagement;

import java.util.List;

public class Course1 {
    private int courseId;
    private String name;
    private String grade;
    private String batch;
    private List<Assignment> assignments;
    private boolean expanded = false;

    public Course1(int courseId, String name, String grade, String batch, List<Assignment> assignments) {
        this.courseId = courseId;
        this.name = name;
        this.grade = grade;
        this.batch = batch;
        this.assignments = assignments;
    }

    public int getCourseId() { return courseId; }
    public String getName() { return name; }
    public String getGrade() { return grade; }
    public String getBatch() { return batch; }
    public List<Assignment> getAssignments() { return assignments; }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    // Nested Assignment class
    public static class Assignment {
        private int assignmentId;
        private String description;

        public Assignment(int assignmentId, String description) {
            this.assignmentId = assignmentId;
            this.description = description;
        }

        public int getAssignmentId() { return assignmentId; }
        public String getDescription() { return description; }
    }
}
