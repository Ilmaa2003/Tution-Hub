
package com.nibm.tutionmanagement;

public class StudentGrade {
    public String courseName;
    public String examName;
    public String studentEmail;
    public String grade;

    // Default constructor required for calls to DataSnapshot.getValue(StudentGrade.class)
    public StudentGrade() {}

    public StudentGrade(String courseName, String examName, String studentEmail, String grade) {
        this.courseName = courseName;
        this.examName = examName;
        this.studentEmail = studentEmail;
        this.grade = grade;
    }
}
