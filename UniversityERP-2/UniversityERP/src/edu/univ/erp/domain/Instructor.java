package edu.univ.erp.domain;

public class Instructor {
    private final int userId;
    private final String department;
    private User user;
    
    public Instructor(int userId, String department) {
        this.userId = userId;
        this.department = department;
    }
    
    public int getUserId() { return userId; }
    public String getDepartment() { return department; }
    
    @Override
    public String toString() {
        return String.format("Instructor{userId=%d, department='%s'}", userId, department);
    }

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}