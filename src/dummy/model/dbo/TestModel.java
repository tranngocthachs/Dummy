package dummy.model.dbo;

import java.io.File;
import java.util.Date;
import java.util.List;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Predicate;

import dummy.model.actions.ActionManager;

public class TestModel {
	
	public static void main(String[] args) {
		new File(ActionManager.DB4OFILENAME).delete();
		ObjectContainer db = Db4oEmbedded.openFile(Db4oEmbedded
                .newConfiguration(), ActionManager.DB4OFILENAME);
        try {
            // create an instructor
        	TIIUser inst = new TIIUser("Teacher", "One", "tch01@example.com", TIIUserType.INSTRUCTOR);
        	db.store(inst);
        	
        	// create a student
        	TIIUser std1 = new TIIUser("Student", "One", "std01@example.com", TIIUserType.STUDENT);
        	TIIUser std2 = new TIIUser("Student", "Two", "std02@example.com", TIIUserType.STUDENT);
        	TIIUser std3 = new TIIUser("Student", "Three", "std03@example.com", TIIUserType.STUDENT);
        	db.store(std1);
        	db.store(std2);
        	db.store(std3);
        	
        	// create a class
        	TIIClass clazz = new TIIClass("Intro to Programming 10", inst);
        	clazz.getStudents().add(std1);
        	clazz.getStudents().add(std2);
        	clazz.getStudents().add(std3);
        	db.store(clazz);
        	
        	// create an assignment
        	TIIAssignment ass = new TIIAssignment(clazz, "Hello World");
        	db.store(ass);
        	
        } finally {
            db.close();
        }
        
        db = Db4oEmbedded.openFile(Db4oEmbedded
                .newConfiguration(), ActionManager.DB4OFILENAME);
        try {
        	// implementing fid = 5
        	final String uem = "tch01@example.com";
        	final String ufn = "Teacher";
        	final String uln = "One";
        	final String pfn = "Student";
        	final String pln = "Two";
        	String ptl = "My Hello World";
        	final String ctl = "Intro to Programming 10";
        	final String assign = "Hello World";
        	List<TIIUser> insts = db.query(new Predicate<TIIUser>() {
				public boolean match(TIIUser usr) {
					return usr.getEmail().equals(uem) && usr.getFirstName().equals(ufn) && usr.getLastName().equals(uln);
				}
        	});
        	if (insts.size() == 1) {
        		final TIIUser inst = insts.get(0);
        		List<TIIClass> clazzs = db.query(new Predicate<TIIClass>() {
					public boolean match(TIIClass clazz) {
						return clazz.getTitle().equals(ctl) && clazz.getInstructor().equals(inst);
					}
				});
        		if (clazzs.size() == 1) {
        			final TIIClass clazz = clazzs.get(0);
        			List<TIIAssignment> asss = db.query(new Predicate<TIIAssignment>() {
						public boolean match(TIIAssignment ass) {
							return ass.getTitle().equals(assign) && ass.getClazz().equals(clazz);
						}
        			});
        			if (asss.size() == 1) {
        				final TIIAssignment ass = asss.get(0);
        				List<TIIUser> usrs = db.query(new Predicate<TIIUser>() {
        					public boolean match(TIIUser usr) {
        						return usr.getFirstName().equals(pfn) && usr.getLastName().equals(pln);
        					}
        				});
        				if (usrs.size() == 1) {
        					final TIIUser usr = usrs.get(0);
        					if (usr.getUserType() == TIIUserType.STUDENT && clazz.getStudents().contains(usr)) {
        						TIISubmission sub = new TIISubmission(ass, ptl, usr, new Date());
        						db.store(sub);
        						listResult(db.queryByExample(TIISubmission.class));
        					}
        				}
        			}
        		}
        	}
        } finally {
            db.close();
        }
        
        db = Db4oEmbedded.openFile(Db4oEmbedded
                .newConfiguration(), ActionManager.DB4OFILENAME);
        try {
			// implementing fid = 5, assignid = 'quicksubmit'
			final String uem = "tch01@example.com";
			final String ufn = "Teacher";
			final String uln = "One";
			final String pfn = "Student";
			final String pln = "Two";
			String ptl = "My Hello World";
			
			// verify if this is user is really an instructor
			List<TIIUser> insts = db.query(new Predicate<TIIUser>() {
				public boolean match(TIIUser usr) {
					return usr.getEmail().equals(uem) && usr.getFirstName().equals(ufn) && usr.getLastName().equals(uln);
				}
			});
			if (insts.size() == 1) {
				final TIIUser inst = insts.get(0);
				if (inst.getUserType() == TIIUserType.INSTRUCTOR) {
					TIIQuickSubmission sub = new TIIQuickSubmission(ptl, new Date(), pfn, pln, inst);
					db.store(sub);
					listResult(db.queryByExample(TIISubmission.class));
				}
			}
		} finally {
			db.close();
		} 
		
	}
	public static void listResult(ObjectSet result) {
	    System.out.println(result.size());
	    while(result.hasNext()) {
	        System.out.println(result.next());
	    }
	}
}
