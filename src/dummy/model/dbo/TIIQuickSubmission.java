package dummy.model.dbo;

import java.util.Date;

public class TIIQuickSubmission extends TIISubmission {
	private String authorFN;
	private String authorLN;
	private TIIUser instructor;
	public TIIQuickSubmission(String title, Date subDate, String authorFN, String authorLN, TIIUser instructor) {
		super(null, title, null, subDate);
		this.authorFN = authorFN;
		this.authorLN = authorLN;
		if (instructor.getUserType() == TIIUserType.INSTRUCTOR)
			this.instructor = instructor;
	}
	public String getAuthorFN() {
		return authorFN;
	}
	public void setAuthorFN(String authorFN) {
		this.authorFN = authorFN;
	}
	public String getAuthorLN() {
		return authorLN;
	}
	public void setAuthorLN(String authorLN) {
		this.authorLN = authorLN;
	}
	public TIIUser getInstructor() {
		return instructor;
	}
	public void setInstructor(TIIUser instructor) {
		this.instructor = instructor;
	}
	@Override
	public void setAssignment(TIIAssignment assignment) {
	}
	@Override
	public void setStudent(TIIUser student) {
	}
	@Override
	public String toString() {
		return "Quick Submission: " + title + ". Author: " + authorFN + " " + authorLN + ". " +getInstructor();
	}
	
}
