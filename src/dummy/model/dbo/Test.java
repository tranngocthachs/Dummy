package dummy.model.dbo;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;

import dummy.model.actions.ActionManager;

public class Test {
	public static void main(String[] args) {
		ObjectContainer db = Db4oEmbedded.openFile(Db4oEmbedded
	            .newConfiguration(), ActionManager.DB4OFILENAME);
		try {
			ObjectSet result = db.queryByExample(TIISubmission.class);
			while(result.hasNext()) {
				Object obj = result.next();
				System.out.println(db.ext().getID(obj) + "; " + obj);
			}
	    } finally {
	        db.close();
	    }
	}
}
