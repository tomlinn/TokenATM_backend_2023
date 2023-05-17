package io.renren.modules.tokenatm.service.Request;

public class UseTokenBody {

    private String assignmentId;
    private String assignmentName;
    private Integer tokenCount;
    private String studentId;

    public UseTokenBody() {
        // Default constructor
    }

    public String getAssignmentId() {
        return assignmentId;
    }
    public void setAssignmentId (String assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getAssignmentName() {
        return assignmentName;
    }
    public void setAssignmentName (String assignmentName) {
        this.assignmentName = assignmentName;
    }


    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public Integer getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(Integer tokenCount) {
        this.tokenCount = tokenCount;
    }


}
