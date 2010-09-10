package dummy.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;

import dummy.model.actions.ActionManager;
import dummy.model.dbo.TIISubmission;

/**
 * Servlet implementation class OriginalityReportServlet
 */
public class OriginalityReportServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String oidStr = request.getParameter("oid");
		if (oidStr == null || oidStr.trim().isEmpty())
			throw new ServletException("unexpected request");
		Long oid = Long.valueOf(request.getParameter("oid").trim());
		// accessDb4o
		ObjectContainer db = Db4oEmbedded.openFile(Db4oEmbedded
				.newConfiguration(), ActionManager.DB4OFILENAME);
		try {
			TIISubmission sub = db.ext().getByID(oid);
			db.activate(sub, 10);
			if (sub == null)
				response.sendRedirect(ActionManager.getErrorCallbackURL(9999, "oid not found"));
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.write(
					"<HTML>" +
					"<HEAD>" +
					"<TITLE>Dummy Originality Report</TITLE>" +
					"</HEAD>" +
					"<BODY>" +
					"<h2>Originality Report of OID: " + oid + "</h2>" +
					"<p>Score: " + sub.getReportScore() + "</p>" +
					"</BODY>" +
					"</HTML>");
			out.close();
		} finally {
			db.close();
		}
	}

}
