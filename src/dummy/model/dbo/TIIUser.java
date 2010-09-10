package dummy.model.dbo;

public class TIIUser {
	private String firstName;
	private String lastName;
	private String email;
	private TIIUserType userType;
	
	public TIIUser(String firstName, String lastName, String email, TIIUserType userType) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.userType = userType;
	}
	
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public TIIUserType getUserType() {
		return userType;
	}
	public void setUserType(TIIUserType userType) {
		this.userType = userType;
	}
	@Override
	public String toString() {
		return "User: " + firstName + " " + lastName + ". Email: " + email + ". Type: " + userType;
	}
}
