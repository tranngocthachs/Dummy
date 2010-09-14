package dummy.model.actions;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

//This test file demonstrates using the Turnitin Api to log into Turnitin.

public class Test {
	public static void main(String args[]) {

		//Create a Turnitin API object
		TurnitinAPI tii_api = new TurnitinAPI();

		//Set the remote host
		tii_api.remoteHost = "http://localhost:8080/Dummy/api";

		//Set the account information
		tii_api.aid = "12345";
		tii_api.shared_secret_key = "password";

		//Set the required information. MD5 and GMT are created for you.
		tii_api.diagnostic = "0";
		tii_api.encrypt = "0";
		tii_api.uem = "tch01@example.com";
		tii_api.ufn = "Teacher";
		tii_api.uln = "One";
		tii_api.utp = "2";
		
//		
//		tii_api.fid = "5";
//		tii_api.fcmd = "2";
//		tii_api.assignid = "quicksubmit";
//		tii_api.ptl = "Example";
//		tii_api.pfn = "Student";
//		tii_api.pln = "One";
//		tii_api.ptype = "2";
		
		
		tii_api.fid = "8";
		tii_api.fcmd = "2";
		tii_api.oid = "2916";
		
		try {
			/*
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(tii_api.remoteHost);
			MultipartEntity reqEntity = new MultipartEntity(
					HttpMultipartMode.BROWSER_COMPATIBLE);
			reqEntity.addPart("aid", new StringBody(tii_api.aid));
			reqEntity.addPart("assignid", new StringBody(tii_api.assignid));
			reqEntity.addPart("diagnostic", new StringBody(tii_api.diagnostic));
			reqEntity.addPart("encrypt", new StringBody(tii_api.encrypt));
			reqEntity.addPart("fcmd",
					new StringBody(tii_api.fcmd));
			reqEntity.addPart("fid",
					new StringBody(tii_api.fid));
			reqEntity.addPart("gmtime",
					new StringBody(tii_api.gmt));
			InputStreamBody bin = new InputStreamBody(
					new FileInputStream("/Users/tranngocthachs/Documents/workspace/Dummy/src/dummy/resources/SU2010-B3_Lab2.doc"), "application/msword", "SU2010-B3_Lab2.doc");
			reqEntity.addPart("pdata", bin );
			reqEntity.addPart("pfn", new StringBody(tii_api.pfn));
			reqEntity.addPart("pln", new StringBody(tii_api.pln));
			reqEntity.addPart("ptl", new StringBody(tii_api.ptl));
			reqEntity.addPart("ptype", new StringBody(tii_api.ptype));
			reqEntity.addPart("uem", new StringBody(tii_api.uem));
			reqEntity.addPart("ufn", new StringBody(tii_api.ufn));
			reqEntity.addPart("uln", new StringBody(tii_api.uln));
			reqEntity.addPart("utp", new StringBody(tii_api.utp));
			reqEntity.addPart("md5", new StringBody(tii_api.getMD5()));
			reqEntity.addPart("internet_check", new StringBody("1"));
			reqEntity.addPart("s_paper_check", new StringBody("1"));
			reqEntity.addPart("journal_check", new StringBody("1"));
			httppost.setEntity(reqEntity);
			System.out.println("executing request " + httppost.getRequestLine());

			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				long len = entity.getContentLength();
				if (len != -1 && len < 2048) {
					System.out.println(EntityUtils.toString(entity));
				} else {
					IOUtils.copy(entity.getContent(), System.out);
				}
			}
			*/
			
			
			String url = tii_api.getRedirectUrl();
			System.out.println(url);
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
			    long len = entity.getContentLength();
			    if (len != -1 && len < 2048) {
			        System.out.println(EntityUtils.toString(entity));
			    } else {
			        IOUtils.copy(entity.getContent(), System.out);
			    }
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
