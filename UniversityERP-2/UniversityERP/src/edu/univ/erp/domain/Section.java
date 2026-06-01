package edu.univ.erp.domain;

public class Section {
    private final int sectionId;
    private final String courseCode;
    private final Integer instructorId;
    private final DayOfWeek day;
    private final String time;
    private final String room;
    private final int capacity;
    private final Semester semester;
    private final int year;
    private int enrolledCount;
    
    public Section(int sectionId, String courseCode, Integer instructorId, DayOfWeek day, 
                   String time, String room, int capacity, Semester semester, int year, int enrolledCount) {
        this.sectionId = sectionId;
        this.courseCode = courseCode;
        this.instructorId = instructorId;
        this.day = day;
        this.time = time;
        this.room = room;
        this.capacity = capacity;
        this.semester = semester;
        this.year = year;
        this.enrolledCount = enrolledCount;
    }
    
    public int getSectionId() { return sectionId; }
    public String getCourseCode() { return courseCode; }
    public Integer getInstructorId() { return instructorId; }
    public DayOfWeek getDay() { return day; }
    public String getTime() { return time; }
    public String getRoom() { return room; }
    public int getCapacity() { return capacity; }
    public Semester getSemester() { return semester; }
    public int getYear() { return year; }
    public int getEnrolledCount() { return enrolledCount; }
    
    public boolean hasAvailableSeats() {
        return enrolledCount < capacity;
    }
    
    public int getAvailableSeats() {
        return capacity - enrolledCount;
    }
    
    public void setEnrolledCount(int enrolledCount) {
        this.enrolledCount = enrolledCount;
    }
    
    @Override
    public String toString() {
        return String.format("Section{sectionId=%d, courseCode='%s', instructorId=%s, day=%s, capacity=%d}", 
                           sectionId, courseCode, instructorId, day, capacity);
    }
}