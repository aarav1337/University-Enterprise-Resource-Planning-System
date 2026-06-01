package edu.univ.erp.auth;

import edu.univ.erp.data.InstructorDAO;
import edu.univ.erp.data.StudentDAO;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.User;
import edu.univ.erp.domain.UserRole;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private Student currentStudent;
    private Instructor currentInstructor;
    
    private SessionManager() {}
    
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    public void login(User user) {
        this.currentUser = user;
        loadUserProfile();
    }
    
    public void logout() {
        this.currentUser = null;
        this.currentStudent = null;
        this.currentInstructor = null;
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public boolean hasRole(UserRole role) {
        return isLoggedIn() && currentUser.getRole() == role;
    }
    
    public int getCurrentUserId() {
        return isLoggedIn() ? currentUser.getUserId() : -1;
    }
    
    public UserRole getCurrentUserRole() {
        return isLoggedIn() ? currentUser.getRole() : null;
    }
    
    public String getCurrentUsername() {
        return isLoggedIn() ? currentUser.getUsername() : null;
    }
    
    public Student getCurrentStudent() {
        return currentStudent;
    }
    
    public Instructor getCurrentInstructor() {
        return currentInstructor;
    }
    
    private void loadUserProfile() {
        if (currentUser == null) return;
        
        try {
            if (currentUser.getRole() == UserRole.STUDENT) {
                StudentDAO studentDAO = new StudentDAO();
                this.currentStudent = studentDAO.getStudentByUserId(currentUser.getUserId());
                if (this.currentStudent != null) {
                    this.currentStudent.setUser(currentUser);
                
                }
            } 
                else if (currentUser.getRole() == UserRole.INSTRUCTOR) {
                InstructorDAO instructorDAO = new InstructorDAO();
                this.currentInstructor = instructorDAO.getInstructorByUserId(currentUser.getUserId());
                if (this.currentInstructor != null) {
                    this.currentInstructor.setUser(currentUser);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading user profile: " + e.getMessage());
            e.printStackTrace();
        }
    }
}