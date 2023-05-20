package io.renren.modules.tokenatm.entity;

import javax.persistence.*;

@Entity
@Table(name = "token_request")
public class RequestEntity {
	private String studentId;
	private String studentName;
	private String assignmentId;
	private String assignmentName;
	private int tokenCount;
	private boolean isApproved;

	private String status;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	public RequestEntity(String studentId, String assignmentId, String assignmentName, int tokenCount, String status) {
		this.studentId = studentId;
		this.assignmentId = assignmentId;
		this.assignmentName = assignmentName;
		this.tokenCount = tokenCount;
		this.isApproved = false;
		this.status = status;
	}

	public RequestEntity() {

	}

	public String getStudentId() {
		return this.studentId;
	}

	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}

	public void setStudentName(String studentName) {
		this.studentName = studentName;
	}


	public String getStudentName() {
		return this.studentName;
	}
	public String getAssignmentId() {
		return this.assignmentId;
	}

	public void setAssignmentId(String assignmentId) {
		this.assignmentId = assignmentId;
	}

	public String getAssignmentName() {
		return this.assignmentName;
	}

	public void setAssignmentName(String assignmentName) {
		this.assignmentName = assignmentName;
	}

	public int getTokenCount() {
		return this.tokenCount;
	}

	public void setTokenCount(int tokenCount) {
		this.tokenCount = tokenCount;
	}

	public boolean isApproved() {
		return this.isApproved;
	}

	public void setApproved(boolean approved) {
		this.isApproved = approved;
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}


	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}
}

