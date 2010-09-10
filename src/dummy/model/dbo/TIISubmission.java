package dummy.model.dbo;

import java.util.Date;
import java.util.Random;

public class TIISubmission {
	protected TIIAssignment assignment;
	protected String title;
	protected TIIUser student;
	protected Date subTime;
	protected int reportGenTime; // in seconds

	public TIISubmission(TIIAssignment assignment, String title, TIIUser student, Date subTime) {
		this.assignment = assignment;
		this.title = title;
		if (student != null && student.getUserType() == TIIUserType.STUDENT)
			this.student = student;
		this.subTime = subTime;
		reportGenTime = 10 + (new Random()).nextInt(20);
	}
	
	public TIIAssignment getAssignment() {
		return assignment;
	}
	public void setAssignment(TIIAssignment assignment) {
		this.assignment = assignment;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public TIIUser getStudent() {
		return student;
	}

	public void setStudent(TIIUser student) {
		this.student = student;
	}
	
	@Override
	public String toString() {
		return "Submission: " + title + ". " + student + ". " + assignment;
	}
	
	public Date getSubTime() {
		return subTime;
	}

	public void setSubTime(Date subTime) {
		this.subTime = subTime;
	}
	
	public boolean reportIsAvailable() {
		return ((new Date()).getTime() - getSubTime().getTime()) >= (reportGenTime * 1000); 
	}
	
	public int getReportScore() {
		if (reportIsAvailable()) {
			int p = (new Random()).nextInt(5);
			if (p == 0)
				return new Random().nextInt(40);
			else
				return 0;
		}
		else
			return -1;
	}
}
