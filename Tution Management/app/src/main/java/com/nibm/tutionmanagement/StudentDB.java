package com.nibm.tutionmanagement;

public class StudentDB {
    private int id;
    private String name;
    private String address;
    private String dob;
    private String phone;
    private String email;
    private String parentPhone;
    private String parentEmail;
    private String role;

    // Constructor without ID (for inserting new students)
    public StudentDB(String name, String address, String dob, String phone, String email, String parentPhone, String parentEmail) {
        this.name = name;
        this.address = address;
        this.dob = dob;
        this.phone = phone;
        this.email = email;
        this.parentPhone = parentPhone;
        this.parentEmail = parentEmail;
        this.role = "Student";  // default role
    }

    // Constructor with ID (for reading/updating)
    public StudentDB(int id, String name, String address, String dob, String phone, String email, String parentPhone, String parentEmail) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.dob = dob;
        this.phone = phone;
        this.email = email;
        this.parentPhone = parentPhone;
        this.parentEmail = parentEmail;
        this.role = "Student";
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getDob() { return dob; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getParentPhone() { return parentPhone; }
    public String getParentEmail() { return parentEmail; }
    public String getRole() { return role; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setDob(String dob) { this.dob = dob; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setParentPhone(String parentPhone) { this.parentPhone = parentPhone; }
    public void setParentEmail(String parentEmail) { this.parentEmail = parentEmail; }
    public void setRole(String role) { this.role = role; }
}
