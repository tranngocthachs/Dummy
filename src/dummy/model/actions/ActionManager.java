package dummy.model.actions;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Predicate;

import dummy.model.dbo.TIIQuickSubmission;
import dummy.model.dbo.TIISubmission;
import dummy.model.dbo.TIIUser;
import dummy.model.dbo.TIIUserType;
import dummy.servlets.API;

public final class ActionManager {
	public static final String AID = "aid";
	public static final String ASSIGN = "assign";
	public static final String ASSIGNID = "assignid";
	public static final String CID = "cid";
	public static final String CPW = "cpw";
	public static final String CTL = "ctl";
	public static final String DIAGNOSTIC = "diagnostic";
	public static final String DIS = "dis";
	public static final String DTDUE = "dtdue";
	public static final String DTSTART = "dtstart";
	public static final String ENCRYPT = "encrypt";
	public static final String FCMD = "fcmd";
	public static final String FID = "fid";
	public static final String GMTIME = "gmtime";
	public static final String NEWASSIGN = "newassign";
	public static final String NEWUPW = "newupw";
	public static final String OID = "oid";
	public static final String PFN = "pfn";
	public static final String PLN = "pln";
	public static final String PTL = "ptl";
	public static final String PTYPE = "ptype";
	public static final String SAID = "said";
	public static final String TEM = "tem";
	public static final String UEM = "uem";
	public static final String UFN = "ufn";
	public static final String UID = "uid";
	public static final String ULN = "uln";
	public static final String UPW = "upw";
	public static final String USERNAME = "username";
	public static final String UTP = "utp";
	public static final String SHARED_SECRET_KEY = "password";
	public static final String AUTHORISED_AID = "12345";

	public static final String XML_RETURN_DATA = "returndata";
	public static final String XML_RETURN_CODE = "rcode";
	public static final String XML_RETURN_MSG = "rmessage";
	private static final Object IDSYNC = "idsync";
	public final static String DB4OFILENAME = System.getProperty("user.home") + "/dummyTII.db4o";


	public static final Document perform(Map<String, String> para) {
		Map<String, String> fullPara = getEmptyParas();
		for (String s : para.keySet()) {
			if (fullPara.containsKey(s)) {
				fullPara.put(s, para.get(s));
			}
		}
		// verify aid
		if (para.get(AID) == null || !para.get(AID).equals(AUTHORISED_AID))
			return generateDummyError("bad primary accound id");
		// verify md5
		String md5 = getMD5(fullPara);
		if (md5.equalsIgnoreCase(para.get("md5"))) {
			String fun = para.get(FID);
			if (fun != null) {
				int fid = -1;
				try {
					fid = Integer.parseInt(fun);
				} catch (NumberFormatException e) {
					return generateDummyError("bad fid");
				}
				switch (fid) {
				case 1:
					return performCreateUser(para);
				case 2:
					return performCreateClass(para);
				case 3:
					return performJoinClass(para);
				case 4:
					return performCreateAssignment(para);
				case 5:
					return performSubmitPaper(para);
				case 6:
					return performReturnReportFunction(para);
				case 7:
				case 8:
					return performDeleteSubmission(para);
				case 9:
				case 10:
				case 11:
				case 12:
				case 13:
					return generateDummyError("not implement yet");
				default:
					return generateError(116, "fid missing from URL, or does not reference existing function");
				}
			}
			else
				return generateError(116, "fid missing from URL, or does not reference existing function");
		}
		else {
			return generateError(302, "MD5 not authenticated - the MD5 in the URL does not match the MD5 calculated");
		}
	}

	private static Document performDeleteSubmission(Map<String, String> para) {
		if (para.get(FCMD) == null || !para.get(FCMD).equals("2"))
			return generateDummyError("bad fcmd");
		Document retval = null;
		try {
			retval = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		Element root = retval.createElement(XML_RETURN_DATA);
		retval.appendChild(root);
		Element rcode = retval.createElement(XML_RETURN_CODE);
		root.appendChild(rcode);
		Element rmessage = retval.createElement(XML_RETURN_MSG);
		root.appendChild(rmessage);
		// accessDb4o
		ObjectContainer db = Db4oEmbedded.openFile(Db4oEmbedded
				.newConfiguration(), DB4OFILENAME);
		try {
			String utp = para.get(UTP);
			final String uem = para.get(UEM);
			final String ufn = para.get(UFN);
			final String uln = para.get(ULN);
			String oid = para.get(OID);
			Long subId = null;
			if (utp == null || utp.isEmpty() || uem == null || uem.isEmpty() || ufn == null || ufn.isEmpty() || uln == null || uln.isEmpty())
				return generateDummyError("missing user para");
			if (oid == null || oid.isEmpty()) {
				return generateDummyError("oid missing");
			}
			else {
				try {
					subId = Long.valueOf(oid);
				} catch (NumberFormatException e) {
					return generateDummyError("malformed oid");
				}
			}
			List<TIIUser> usrs = db.query(new Predicate<TIIUser>() {
				public boolean match(TIIUser usr) {
					return usr.getEmail().equals(uem) && usr.getFirstName().equals(ufn) && usr.getLastName().equals(uln);
				}
			});
			if (usrs.size() == 1) {
				final TIIUser usr = usrs.get(0);
				TIIUserType usrType = usr.getUserType();
				TIISubmission sub = db.ext().getByID(subId);
				db.activate(sub, 10);
				if (sub == null)
					return generateDummyError("oid not found");
				if (utp.equals("1") && usrType == TIIUserType.STUDENT) {
					if (sub.getStudent() != usr)
						return generateDummyError("submission not belong to this user");
				}
				else if ((utp.equals("2") && usrType == TIIUserType.INSTRUCTOR) || (utp.equals("3") && usrType == TIIUserType.ADMIN)) {
					boolean isQuickSub = false;
					if (sub instanceof TIIQuickSubmission)
						isQuickSub = true;
					if (isQuickSub) {
						if (((TIIQuickSubmission)sub).getInstructor() != usr)
							return generateDummyError("submission not belong to this user");
					}
					else {
						// normal submission
						if (sub.getAssignment() != null && sub.getAssignment().getClazz() != null && sub.getAssignment().getClazz().getInstructor() != null) {
							if (sub.getAssignment().getClazz().getInstructor() != usr) {
								return generateDummyError("submission not belong to this user");
							}
						}
						else {
							return generateDummyError("inconsistent data");
						}
					}	
				}
				else {
					return generateDummyError("utp not match user type");
				}
				// seems to be ok now
				try {
					db.delete(sub);
				} catch (Exception e) {
					return generateError(418, "Error trying to delete submission");
				}
				
				// successfully delete
				Text rcodeTxt = retval.createTextNode("71");
				rcode.appendChild(rcodeTxt);
				Text rmsgTxt = retval.createTextNode("successful");
				rmessage.appendChild(rmsgTxt);				
			}
			else {
				return generateDummyError("user not found");
			}
		} finally {
			db.close();
		}
		return retval;
	}

	private static Document performReturnReportFunction(Map<String, String> para) {
		if (para.get(FCMD) == null || !para.get(FCMD).equals("2"))
			return generateDummyError("bad fcmd");
		Document retval = null;
		try {
			retval = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		Element root = retval.createElement(XML_RETURN_DATA);
		retval.appendChild(root);
		Element rcode = retval.createElement(XML_RETURN_CODE);
		root.appendChild(rcode);
		Element rmessage = retval.createElement(XML_RETURN_MSG);
		root.appendChild(rmessage);
		// accessDb4o
		ObjectContainer db = Db4oEmbedded.openFile(Db4oEmbedded
				.newConfiguration(), DB4OFILENAME);
		try {
			String utp = para.get(UTP);
			final String uem = para.get(UEM);
			final String ufn = para.get(UFN);
			final String uln = para.get(ULN);
			String oid = para.get(OID);
			Long subId = null;
			if (utp == null || utp.isEmpty() || uem == null || uem.isEmpty() || ufn == null || ufn.isEmpty() || uln == null || uln.isEmpty())
				return generateDummyError("missing user para");
			if (oid == null || oid.isEmpty()) {
				return generateDummyError("oid missing");
			}
			else {
				try {
					subId = Long.valueOf(oid);
				} catch (NumberFormatException e) {
					return generateDummyError("malformed oid");
				}
			}
			List<TIIUser> usrs = db.query(new Predicate<TIIUser>() {
				public boolean match(TIIUser usr) {
					return usr.getEmail().equals(uem) && usr.getFirstName().equals(ufn) && usr.getLastName().equals(uln);
				}
			});
			if (usrs.size() == 1) {
				final TIIUser usr = usrs.get(0);
				TIIUserType usrType = usr.getUserType();
				TIISubmission sub = db.ext().getByID(subId);
				db.activate(sub, 10);
				if (sub == null)
					return generateDummyError("oid not found");
				if (utp.equals("1") && usrType == TIIUserType.STUDENT) {
					if (sub.getStudent() != usr)
						return generateDummyError("submission not belong to this user");
				}
				else if ((utp.equals("2") && usrType == TIIUserType.INSTRUCTOR) || (utp.equals("3") && usrType == TIIUserType.ADMIN)) {
					boolean isQuickSub = false;
					if (sub instanceof TIIQuickSubmission)
						isQuickSub = true;
					if (isQuickSub) {
						if (((TIIQuickSubmission)sub).getInstructor() != usr)
							return generateDummyError("submission not belong to this user");
					}
					else {
						// normal submission
						if (sub.getAssignment() != null && sub.getAssignment().getClazz() != null && sub.getAssignment().getClazz().getInstructor() != null) {
							if (sub.getAssignment().getClazz().getInstructor() != usr) {
								return generateDummyError("submission not belong to this user");
							}
						}
						else {
							return generateDummyError("inconsistent data");
						}
					}	
				}
				else {
					return generateDummyError("utp not match user type");
				}
				// seems to be ok now
				if (sub.reportIsAvailable()) {
					Text rcodeTxt = retval.createTextNode("61");
					rcode.appendChild(rcodeTxt);
					Text rmsgTxt = retval.createTextNode("successful");
					rmessage.appendChild(rmsgTxt);
					Element oriScr = retval.createElement("originalityscore");
					root.appendChild(oriScr);
					Text oriScrTxt = retval.createTextNode(String.valueOf(sub.getReportScore()));
					oriScr.appendChild(oriScrTxt);
					Element webOvl = retval.createElement("web_overlap");
					root.appendChild(webOvl);
					Text webOvlTxt = retval.createTextNode(String.valueOf(sub.getReportScore()));
					webOvl.appendChild(webOvlTxt);
					Element pubOvl = retval.createElement("publication_overlap");
					root.appendChild(pubOvl);
					Text pubOvlTxt = retval.createTextNode(String.valueOf(sub.getReportScore()));
					pubOvl.appendChild(pubOvlTxt);
					Element stdOvl = retval.createElement("student_paper_overlap");
					root.appendChild(stdOvl);
					Text stdOvlTxt = retval.createTextNode(String.valueOf(sub.getReportScore()));
					stdOvl.appendChild(stdOvlTxt);
				}
				else {
					return generateError(415, "Originality score not available yet in fid 6, fcmd 2");
				}
			}
			else {
				return generateDummyError("user not found");
			}
		} finally {
			db.close();
		}
		return retval;
	}

	private static Document performSubmitPaper(Map<String, String> para) {
		if (para.get(FCMD) == null != !para.get(FCMD).equals("2"))
			return generateDummyError("bad fcmd");
		if (para.get(ASSIGNID) == null)
			return generateDummyError("no assignid specified");
		Document retval = null;
		try {
			retval = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		Element root = retval.createElement(XML_RETURN_DATA);
		retval.appendChild(root);
		Element rcode = retval.createElement(XML_RETURN_CODE);
		root.appendChild(rcode);
		Element rmessage = retval.createElement(XML_RETURN_MSG);
		root.appendChild(rmessage);
		// accessDb4o
		ObjectContainer db = Db4oEmbedded.openFile(Db4oEmbedded
				.newConfiguration(), DB4OFILENAME);
		try {
			String utp = para.get(UTP);
			final String uem = para.get(UEM);
			final String ufn = para.get(UFN);
			final String uln = para.get(ULN);
			if (para.get(ASSIGNID).equals("quicksubmit")) {
				// process quicksubmit
				if (utp == null || !utp.equals("2"))
					return generateDummyError("for quicksubmit, utp has to be 2");
				boolean internetCheck = para.get("internet_check") != null && para.get("internet_check").equals("1");
				boolean paperCheck = para.get("s_paper_check") != null && para.get("s_paper_check").equals("1");
				boolean journalCheck = para.get("journal_check") != null && para.get("journal_check").equals("1");
				if (!internetCheck && !paperCheck && !journalCheck) {
					return generateDummyError("at least one of the following checks must be set to 1: internet_check, s_paper_check, journal_check");
				}
				final String pfn = para.get(PFN);
				final String pln = para.get(PLN);
				String ptl = para.get(PTL);
				if (uem == null || uem.isEmpty() || ufn == null || ufn.isEmpty() || uln == null || uln.isEmpty())
					return generateDummyError("missing user para");
				if (ptl == null || ptl.isEmpty() || pfn == null || pfn.isEmpty() || pln == null || pln.isEmpty())
					return generateDummyError("missing paper para");

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
						Text rcodeTxt = retval.createTextNode("51");
						rcode.appendChild(rcodeTxt);
						Text rmsgTxt = retval.createTextNode("successful");
						rmessage.appendChild(rmsgTxt);
						Element objId = retval.createElement("objectid");
						root.appendChild(objId);
						Text objIdTxt = retval.createTextNode(String.valueOf(db.ext().getID(sub)));
						objId.appendChild(objIdTxt);
					}
					else
						return generateDummyError(uem + " is not an instructor");
				}
				else {
					return generateDummyError("instructor not found");
				}
			}
			else {
				// TODO: normal submission
			}
		} finally {
			db.close();
		}
		return retval;
	}

	private static Document performCreateAssignment(Map<String, String> para) {
		return null;
	}

	private static Document performJoinClass(Map<String, String> para) {
		return null;
	}

	private static Document performCreateClass(Map<String, String> para) {
		return null;
	}

	private static Document performCreateUser(Map<String, String> para) {
		if (para.get(FCMD) == null || !para.get(FCMD).equals("2"))
			return generateDummyError("bad fcmd");
		Document retval = null;
		try {
			retval = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		Element root = retval.createElement(XML_RETURN_DATA);
		retval.appendChild(root);
		Element rcode = retval.createElement(XML_RETURN_CODE);
		root.appendChild(rcode);
		Element rmessage = retval.createElement(XML_RETURN_MSG);
		root.appendChild(rmessage);
		// accessDb4o
		ObjectContainer db = Db4oEmbedded.openFile(Db4oEmbedded
				.newConfiguration(), DB4OFILENAME);
		try {
			String uem = para.get(UEM);
			String ufn = para.get(UFN);
			String uln = para.get(ULN);
			String utp = para.get(UTP);
			if (para.containsKey(UID) && !para.get(UID).isEmpty()) {
				// ammend user
				String uid = para.get(UID);
				TIIUser usr = db.ext().getByID(Long.valueOf(uid));
				db.activate(usr, 10);
				if (usr != null) {
					if (uem != null)
						usr.setEmail(uem);
					if (ufn != null)
						usr.setFirstName(ufn);
					if (uln != null)
						usr.setLastName(uln);
					db.store(usr);
					Text rcodeTxt = retval.createTextNode("11");
					rcode.appendChild(rcodeTxt);
					Text rmsgTxt = retval.createTextNode("successful, do not send to login");
					rmessage.appendChild(rmsgTxt);
					Element usrId = retval.createElement("userid");
					root.appendChild(usrId);
					Text usrIdTxt = retval.createTextNode(uid);
					usrId.appendChild(usrIdTxt);
				}
			}
			else if (para.containsKey(IDSYNC) && para.get(IDSYNC).equals("1") && uem != null && !uem.isEmpty()) {
				// sync user info by email
				TIIUser usr = new TIIUser(null, null, uem, null);
				ObjectSet objSet = db.queryByExample(usr);
				if (objSet.size() > 0) {
					TIIUser retrievedUsr = (TIIUser)objSet.next();
					if (ufn != null)
						retrievedUsr.setFirstName(ufn);
					if (uln != null)
						retrievedUsr.setLastName(uln);
					db.store(retrievedUsr);
					Text rcodeTxt = retval.createTextNode("11");
					rcode.appendChild(rcodeTxt);
					Text rmsgTxt = retval.createTextNode("successful, do not send to login");
					rmessage.appendChild(rmsgTxt);
					Element usrId = retval.createElement("userid");
					root.appendChild(usrId);
					Text usrIdTxt = retval.createTextNode(String.valueOf(db.ext().getID(retrievedUsr)));
					usrId.appendChild(usrIdTxt);
				}
				else {
					// create new user
					usr = new TIIUser(ufn, uln, uem, null);
					if (utp != null) {
						if (utp.equals("1"))
							usr.setUserType(TIIUserType.STUDENT);
						else if (utp.equals("2"))
							usr.setUserType(TIIUserType.INSTRUCTOR);
						else if (utp.equals("3"))
							usr.setUserType(TIIUserType.ADMIN);
						db.store(usr);
						Text rcodeTxt = retval.createTextNode("11");
						rcode.appendChild(rcodeTxt);
						Text rmsgTxt = retval.createTextNode("successful, do not send to login");
						rmessage.appendChild(rmsgTxt);
						Element usrId = retval.createElement("userid");
						root.appendChild(usrId);
						Text usrIdTxt = retval.createTextNode(String.valueOf(db.ext().getID(usr)));
						usrId.appendChild(usrIdTxt);
					}
				}
			} 
			else {
				// create new user
				TIIUser usr = new TIIUser(ufn, uln, uem, null);
				if (utp != null) {
					if (utp.equals("1"))
						usr.setUserType(TIIUserType.STUDENT);
					else if (utp.equals("2"))
						usr.setUserType(TIIUserType.INSTRUCTOR);
					else if (utp.equals("3"))
						usr.setUserType(TIIUserType.ADMIN);
					db.store(usr);
					Text rcodeTxt = retval.createTextNode("11");
					rcode.appendChild(rcodeTxt);
					Text rmsgTxt = retval.createTextNode("successful, do not send to login");
					rmessage.appendChild(rmsgTxt);
					Element usrId = retval.createElement("userid");
					root.appendChild(usrId);
					Text usrIdTxt = retval.createTextNode(String.valueOf(db.ext().getID(usr)));
					usrId.appendChild(usrIdTxt);
				}
			}
		} finally {
			db.close();
		}
		return retval;
	}

	private static String getMD5(Map<String, String> fullPara) {
		StringBuilder strBuilder = new StringBuilder();
		List<String> list = new LinkedList<String>(fullPara.keySet());
		Collections.sort(list);
		for (String s : list ) {
			strBuilder.append(fullPara.get(s));
		}
		String md5string = strBuilder.toString() + SHARED_SECRET_KEY;
		java.security.MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		md.update(md5string.getBytes());
		byte[] digest = md.digest();
		return byteArrayToHexString(digest);
	}

	private static String byteArrayToHexString(byte in[]) {
		byte ch = 0x00;
		int i = 0;

		if (in == null || in.length <= 0)
			return null;

		String pseudo[] = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

		StringBuffer out = new StringBuffer(in.length * 2);

		while (i < in.length) {
			ch = (byte) (in[i] & 0xF0);  // Strip off
			ch = (byte) (ch >>> 4);
			// shift the bits down

			ch = (byte) (ch & 0x0F);
			// must do this is high order bit is on

			out.append(pseudo[(int)ch]); 
			// convert the nibble to a String Character

			ch = (byte) (in[i] & 0x0F);  
			// Strip off low nibble

			out.append(pseudo[(int)ch]); 
			// conver the nibble to a String Charager
			i++;
		}

		String rslt = new String(out);
		return rslt;

	}
	public static Document generateError(int errorCode, String errorMsg) {
		Document retval = null;
		try {
			retval = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		Element root = retval.createElement(XML_RETURN_DATA);
		retval.appendChild(root);
		Element rcode = retval.createElement(XML_RETURN_CODE);
		root.appendChild(rcode);
		Element rmessage = retval.createElement(XML_RETURN_MSG);
		root.appendChild(rmessage);
		Text rcodeTxt = retval.createTextNode(String.valueOf(errorCode));
		rcode.appendChild(rcodeTxt);
		Text rmsgTxt = retval.createTextNode(errorMsg);
		rmessage.appendChild(rmsgTxt);
		return retval;
	}
	public static Document generateDummyError(String msg) {
		return generateError(9999, msg == null ? "unsuccessful, for some reasons" : msg);
	}

	public static final Map<String, String> getEmptyParas() {
		Map<String, String> retval = new HashMap<String, String>();
		retval.put(AID, new String());
		retval.put(ASSIGN, new String());
		retval.put(ASSIGNID, new String());
		retval.put(CID, new String());
		retval.put(CPW, new String());
		retval.put(CTL, new String());
		retval.put(DIAGNOSTIC, new String());
		retval.put(DIS, new String());
		retval.put(DTDUE, new String());
		retval.put(DTSTART, new String());
		retval.put(ENCRYPT, new String());
		retval.put(FCMD, new String());
		retval.put(FID, new String());
		retval.put(GMTIME, new String());
		retval.put(NEWASSIGN, new String());
		retval.put(NEWUPW, new String());
		retval.put(OID, new String());
		retval.put(PFN, new String());
		retval.put(PLN, new String());
		retval.put(PTL, new String());
		retval.put(PTYPE, new String());
		retval.put(SAID, new String());
		retval.put(PTYPE, new String());
		retval.put(TEM, new String());
		retval.put(UEM, new String());
		retval.put(UFN, new String());
		retval.put(UID, new String());
		retval.put(ULN, new String());
		retval.put(UPW, new String());
		retval.put(USERNAME, new String());
		retval.put(UTP, new String());
		return retval;
	}

	public static String getOriginalityReportURL(
			Map<String, String> para) {
		if (para.get(FID) == null || !para.get(FID).equals("6") || para.get(FCMD) == null || !para.get(FCMD).equals("1"))
			return getErrorCallbackURL(9999, "invalid fcmd or fid");
		// accessDb4o
		ObjectContainer db = Db4oEmbedded.openFile(Db4oEmbedded
				.newConfiguration(), DB4OFILENAME);
		try {
			String utp = para.get(UTP);
			final String uem = para.get(UEM);
			final String ufn = para.get(UFN);
			final String uln = para.get(ULN);
			String oid = para.get(OID);
			Long subId = null;
			if (utp == null || utp.isEmpty() || uem == null || uem.isEmpty() || ufn == null || ufn.isEmpty() || uln == null || uln.isEmpty())
				return getErrorCallbackURL(9999, "missing user para");
			if (oid == null || oid.isEmpty()) {
				return getErrorCallbackURL(9999, "oid missing");
			}
			else {
				try {
					subId = Long.valueOf(oid);
				} catch (NumberFormatException e) {
					return getErrorCallbackURL(9999, "malformed oid");
				}
			}
			List<TIIUser> usrs = db.query(new Predicate<TIIUser>() {
				public boolean match(TIIUser usr) {
					return usr.getEmail().equals(uem) && usr.getFirstName().equals(ufn) && usr.getLastName().equals(uln);
				}
			});
			if (usrs.size() == 1) {
				final TIIUser usr = usrs.get(0);
				TIIUserType usrType = usr.getUserType();
				TIISubmission sub = db.ext().getByID(subId);
				db.activate(sub, 10);
				if (sub == null)
					return getErrorCallbackURL(9999, "oid not found");
				if (utp.equals("1") && usrType == TIIUserType.STUDENT) {
					if (sub.getStudent() != usr)
						return getErrorCallbackURL(9999, "submission not belong to this user");
				}
				else if ((utp.equals("2") && usrType == TIIUserType.INSTRUCTOR) || (utp.equals("3") && usrType == TIIUserType.ADMIN)) {
					boolean isQuickSub = false;
					if (sub instanceof TIIQuickSubmission)
						isQuickSub = true;
					if (isQuickSub) {
						if (((TIIQuickSubmission)sub).getInstructor() != usr)
							return getErrorCallbackURL(9999, "submission not belong to this user");
					}
					else {
						// normal submission
						if (sub.getAssignment() != null && sub.getAssignment().getClazz() != null && sub.getAssignment().getClazz().getInstructor() != null) {
							if (sub.getAssignment().getClazz().getInstructor() != usr) {
								return getErrorCallbackURL(9999, "submission not belong to this user");
							}
						}
						else {
							return getErrorCallbackURL(9999, "inconsistent data");
						}
					}	
				}
				else {
					return getErrorCallbackURL(9999, "utp not match user type");
				}
				// seems to be ok now
				if (sub.reportIsAvailable()) {
					db.close();
					return API.contextPath + "/originality_report?oid=" + oid;
				}
				else {
					return getErrorCallbackURL(414, "Originality report not generated yet in fid 6, fcmd 1");
				}
			}
			else {
				return getErrorCallbackURL(9999, "user not found");
			}
		} finally {
			db.close();
		}
	}

	public static String getErrorCallbackURL(int rcode, String rmessage) {
		try {
			return callbackURL + "?ec=" + rcode + "&em=" + URLEncoder.encode(rmessage, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static final String callbackURL = "http://localhost:8080/BOSS2//PageDispatcherServlet/staff/turnitin_callback"; 
}
