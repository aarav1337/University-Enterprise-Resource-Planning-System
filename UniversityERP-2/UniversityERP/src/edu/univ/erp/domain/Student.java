package edu.univ.erp.domain;

public class Student {
    private final int userId;
    private final String rollNo;
    private final String program;
    private final int year;
    private User user;
    
    public Student(int userId, String rollNo, String program, int year) {
        this.userId = userId;
        this.rollNo = rollNo;
        this.program = program;
        this.year = year;
    }
    
    public int getUserId() { return userId; }
    public String getRollNo() { return rollNo; }
    public String getProgram() { return program; }
    public int getYear() { return year; }
    public User getUser()
    {
    	return user;
    }
    
    @Override
    public String toString() {
        return String.format("Student{userId=%d, rollNo='%s', program='%s', year=%d}", 
                           userId, rollNo, program, year);
    }

	public void setUser(User user) {
		this.user = user;
	}
}