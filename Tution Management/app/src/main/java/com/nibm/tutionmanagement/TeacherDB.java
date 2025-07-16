package com.nibm.tutionmanagement;

public class TeacherDB {
    private int id;
    private String name;
    private String address;
    private String dob;
    private String phone;
    private String email;
    private String role;

    // ✅ Updated constructor to accept all 7 fields including role
    public TeacherDB(int id, String name, String address, String dob, String phone, String email, String role) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.dob = dob;
        this.phone = phone;
        this.email = email;
        this.role = role;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getDob() { return dob; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getRole() { return role; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setDob(String dob) { this.dob = dob; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
}
