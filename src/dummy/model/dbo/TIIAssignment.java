package dummy.model.dbo;


public class TIIAssignment {
	private TIIClass clazz;
	private String title;
	
	public TIIAssignment(TIIClass clazz, String title) {
		this.clazz = clazz;
		this.title = title; 
	}
	
	public TIIClass getClazz() {
		return clazz;
	}
	public void setClazz(TIIClass clazz) {
		this.clazz = clazz;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	@Override
	public String toString() {
		return "Assignment: " + getTitle() + ". " + getClazz();
	}
}
