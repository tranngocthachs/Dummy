package dummy.servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import dummy.model.actions.ActionManager;

/**
 * Servlet implementation class API
 */
public class API extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static String contextPath = null;
	public static Logger logger = Logger.getLogger(API.class);
	
	@Override
	public void init() throws ServletException {
		super.init();
		contextPath = getServletContext().getContextPath();
		logger.setLevel(Level.INFO);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map paraMap = request.getParameterMap();
		Map<String, String> para = new HashMap<String, String>();
		for (Object key : paraMap.keySet()) {
			String[] vals = (String[]) paraMap.get(key);
			if (vals.length > 0)
				para.put((String) key, vals[0]);
			else
				para.put((String) key, "");
			logger.debug(key + " => " + para.get(key));
		}
		Document doc = null;
		if (para.containsKey(ActionManager.AID) && para.containsKey(ActionManager.FID) && para.containsKey(ActionManager.FCMD)) {
			int fid = -1;
			int fcmd = -1;
			try {
				fid = Integer.parseInt(para.get(ActionManager.FID));
				fcmd = Integer.parseInt(para.get(ActionManager.FCMD));
				if (fid >= 1 && fid <= 18 && fcmd >=1 && fcmd <= 5) {
					if ((fid == 4 && fcmd >= 2 && fcmd <= 4) || (fid == 5 && fcmd == 2))
						doc = ActionManager.generateError(409, "Function requires POST request");
					else {
						if (fcmd == 2)
							doc = ActionManager.perform(para);
						else if (fid == 6 && fcmd == 1)
							response.sendRedirect(ActionManager.getOriginalityReportURL(para));
						else if (fcmd == 1)
							response.sendRedirect(ActionManager.getErrorCallbackURL(9999, "not implement yet"));
						else
							doc = ActionManager.generateDummyError("not implement yet");
					}
				}
				else {
					doc = ActionManager.generateDummyError("undefined fid and/or fcmd");
				}
			} catch (NumberFormatException e) {
				doc = ActionManager.generateDummyError("bad fid and/or fcmd");
			}
		}
		else {
			doc = ActionManager.generateDummyError("aid or fid is missing");
		}
		if (doc != null) {
			response.setContentType("text/xml");
			PrintWriter writer = response.getWriter();
			Transformer transformer = null;
			try {
				transformer = TransformerFactory.newInstance().newTransformer();
			} catch (TransformerConfigurationException e) {
				e.printStackTrace();
			} catch (TransformerFactoryConfigurationError e) {
				e.printStackTrace();
			}
		    Source source = new DOMSource(doc);
		    Result output = new StreamResult(writer);
		    try {
				transformer.transform(source, output);
			} catch (TransformerException e) {
				e.printStackTrace();
			}
			writer.close();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!ServletFileUpload.isMultipartContent(request)) {
			doGet(request, response);
		}
		else {
			response.setContentType("text/xml");
			Map<String, String> para = new HashMap<String, String>();
			// Create a factory for disk-based file items
			FileItemFactory factory = new DiskFileItemFactory();

			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);

			// Parse the request
			List<FileItem> items = null;
			try {
				items = upload.parseRequest(request);
			} catch (FileUploadException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			for (FileItem item : items) {
				if (item.isFormField()) {
					String name = item.getFieldName();
					String value = item.getString();
					para.put(name, value);
					logger.debug(name + " => " + value);
				}
				else {
					File temp = File.createTempFile("dummy", item.getName());
					String name = item.getFieldName();
					if (name.equals("pdata")) {
						try {
							item.write(temp);
						} catch (Exception e) {
							e.printStackTrace();
						}
						para.put(name, temp.getAbsolutePath());
						logger.debug("File uploaded: " + name + " => " + temp.getAbsolutePath());
					}
				}
			}
			Document doc = null;
			if (para.containsKey(ActionManager.AID) && para.containsKey(ActionManager.FID) && para.get(ActionManager.FID).equals("5")) {
				doc = ActionManager.perform(para);
			}
			if (doc != null) {
				PrintWriter writer = response.getWriter();
				Transformer transformer = null;
				try {
					transformer = TransformerFactory.newInstance().newTransformer();
				} catch (TransformerConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TransformerFactoryConfigurationError e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    Source source = new DOMSource(doc);
			    Result output = new StreamResult(writer);
			    try {
					transformer.transform(source, output);
				} catch (TransformerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				writer.close();
			}
		}
	}

}
