package com.nibm.tutionmanagement;

public class Payment {
    public String course;
    public String grade;
    public String batch;
    public String teacher;
    public String month;
    public String year;
    public String description;
    public String paymentSlipUrl;

    public Payment() {}

    public Payment(String course, String grade, String batch, String teacher,
                   String month, String year, String description,
                   String paymentSlipUrl) {
        this.course = course;
        this.grade = grade;
        this.batch = batch;
        this.teacher = teacher;
        this.month = month;
        this.year = year;
        this.description = description;
        this.paymentSlipUrl = paymentSlipUrl;
    }
}
