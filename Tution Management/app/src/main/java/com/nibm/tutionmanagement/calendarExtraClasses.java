package com.nibm.tutionmanagement;

public class calendarExtraClasses {
    private int id;               // Unique ID (for database)
    private int courseId;         // Which course this extra class belongs to
    private long dateMillis;      // Date of the extra class (milliseconds)
    private int startHour;        // Start time hour (24h)
    private int startMinute;      // Start time minute
    private int endHour;          // End time hour (24h)
    private int endMinute;        // End time minute
    private String description;   // Description or notes about the extra class

    // Constructors
    public calendarExtraClasses() {
    }

    public calendarExtraClasses(int courseId, long dateMillis, int startHour, int startMinute, int endHour, int endMinute, String description) {
        this.courseId = courseId;
        this.dateMillis = dateMillis;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
        this.description = description;
    }

    public calendarExtraClasses(int id, int courseId, long dateMillis, int startHour, int startMinute, int endHour, int endMinute, String description) {
        this.id = id;
        this.courseId = courseId;
        this.dateMillis = dateMillis;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
        this.description = description;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public long getDateMillis() { return dateMillis; }
    public void setDateMillis(long dateMillis) { this.dateMillis = dateMillis; }

    public int getStartHour() { return startHour; }
    public void setStartHour(int startHour) { this.startHour = startHour; }

    public int getStartMinute() { return startMinute; }
    public void setStartMinute(int startMinute) { this.startMinute = startMinute; }

    public int getEndHour() { return endHour; }
    public void setEndHour(int endHour) { this.endHour = endHour; }

    public int getEndMinute() { return endMinute; }
    public void setEndMinute(int endMinute) { this.endMinute = endMinute; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // Optional convenience method to get formatted time strings
    public String getFormattedStartTime() {
        return String.format("%02d:%02d", startHour, startMinute);
    }

    public String getFormattedEndTime() {
        return String.format("%02d:%02d", endHour, endMinute);
    }


}
