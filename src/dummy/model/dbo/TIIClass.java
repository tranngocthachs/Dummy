package dummy.model.dbo;

import java.util.ArrayList;
import java.util.List;

public class TIIClass {
	private String title;
	private TIIUser instructor;
	private List<TIIUser> students;
	
	public TIIClass(String title, TIIUser instructor) {
		this(title, instructor, new ArrayList<TIIUser>());
	}
	
	public TIIClass(String title, TIIUser instructor, List<TIIUser> students) {
		this.title = title;
		setInstructor(instructor);
		this.students = students;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public TIIUser getInstructor() {
		return instructor;
	}
	public void setInstructor(TIIUser instructor) {
		if (instructor.getUserType() == TIIUserType.INSTRUCTOR)
			this.instructor = instructor;
	}
	public List<TIIUser> getStudents() {
		return students;
	}
	public void setStudent(TIIUser user) {
		if (user.getUserType() == TIIUserType.STUDENT)
			students.add(user);
	}
	
	@Override
	public String toString() {
		return "Class: " + getTitle() + ". " + getInstructor() + ". Students: " + getStudents();
	}
}
