package io.renren.modules.tokenatm.service.Beans;

public class AssignmentStatus {

    //Assignment name
    private String name;

    //Assignment Id
    private String assignment_id;

    //Resubmission Id
    private String resubmission_id;

    //Assignment due date
    private String deadline;

    //Assignment grade
    private double grade;

    //Maximum possible grade for this assignment
    private double maxGrade;

    private String status;

    public String getStatus() {
        return status;
    }

    public int getToken_required() {
        return token_required;
    }

    private int token_required;

    public AssignmentStatus(String name, String assignment_id, String resubmission_id, String deadline, double grade, double maxGrade, String status, int token_required) {
        this.name = name;
        this.assignment_id = assignment_id;
        this.resubmission_id = resubmission_id;
        this.deadline = deadline;
        this.grade = grade;
        this.maxGrade = maxGrade;
        this.status = status;
        this.token_required = token_required;
    }

    public String getResubmission_id() {
        return resubmission_id;
    }

    public void setResubmission_id(String resubmission_id) {
        this.resubmission_id = resubmission_id;
    }

    public String getName() {
        return name;
    }

    public String getDeadline() {
        return deadline;
    }

    public double getGrade() {
        return grade;
    }

    public double getMaxGrade() {
        return maxGrade;
    }

    public String getAssignment_id() {
        return assignment_id;
    }
}
