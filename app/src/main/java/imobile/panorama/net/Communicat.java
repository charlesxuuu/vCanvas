package imobile.panorama.net;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.ParseException;
import android.util.Log;
import android.widget.Toast;

public class Communicat
{
	public static final String ip = "http://219.245.68.132/";
	
	//获取远方列表
	public List<String> getXmlList()
	{
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		InputStream is = null;
		StringBuilder sb = null;
		String result = null;
		JSONArray jArray;
		try
		{
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(URI.create(ip + "xmlList.php"));
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
		} catch (Exception e)
		{
			Log.e("myDebug", "Error in http connection" + e.toString());
		}
		// convert response to string
		try
		{
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(is, "iso-8859-1"), 8);
			sb = new StringBuilder();
			sb.append(reader.readLine() + "\n");

			String line = "0";
			while ((line = reader.readLine()) != null)
			{
				System.out.println("line"+line);
				sb.append(line + "\n");
			}
			is.close();
			result = sb.toString();
			System.out.println("result:\t"+result);
		} catch (Exception e)
		{
			Log.e("log_tag", "Error converting result " + e.toString());
		}
		// paring data
		List<String> xmlList = new ArrayList<String>();
		
		if(result!=null && !result.equals("")){
			try
			{
				jArray = new JSONArray(result);
				JSONObject json_data = null;
				for (int i = 0; i < jArray.length(); i++)
				{
					json_data = jArray.getJSONObject(i);
					// ct_id = json_data.getInt("id");
					//strXmlList += json_data.getString("name") + "\n";
					xmlList.add(json_data.getString("name"));
					//tv.append(ct_name + " \n");
				}
			} catch (JSONException e1)
			{
				// Toast.makeText(getBaseContext(), "No City Found"
				// ,Toast.LENGTH_LONG).show();
			} catch (ParseException e1)
			{
				e1.printStackTrace();
			}
		}
		
		return xmlList;
	}
	
	//上传xml文件
	// ip+
	public void uploadXmlFile(String uploadUrl,String filePath)
	  {
	    String end = "\r\n";
	    String twoHyphens = "--";
	    String boundary = "******";
	    try
	    {
	      URL url = new URL(uploadUrl);
	      HttpURLConnection httpURLConnection = (HttpURLConnection) url
	          .openConnection();
	      // 设置每次传输的流大小，可以有效防止手机因为内存不足崩溃
	      // 此方法用于在预先不知道内容长度时启用没有进行内部缓冲的 HTTP 请求正文的流。
	      httpURLConnection.setChunkedStreamingMode(128 * 1024);// 128K
	      // 允许输入输出流
	      httpURLConnection.setDoInput(true);
	      httpURLConnection.setDoOutput(true);
	      httpURLConnection.setUseCaches(false);
	      // 使用POST方法
	      httpURLConnection.setRequestMethod("POST");
	      httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
	      httpURLConnection.setRequestProperty("Charset", "UTF-8");
	      httpURLConnection.setRequestProperty("Content-Type",
	          "multipart/form-data;boundary=" + boundary);

	      DataOutputStream dos = new DataOutputStream(
	          httpURLConnection.getOutputStream());
	      dos.writeBytes(twoHyphens + boundary + end);
	      dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\"; filename=\""
	          + filePath.substring(filePath.lastIndexOf("/") + 1)
	          + "\""
	          + end);
	      dos.writeBytes(end);



	      FileInputStream fis = new FileInputStream(filePath);
	      byte[] buffer = new byte[1024]; // 8k
	      int count = 0;
	      // 读取文件
	      while ((count = fis.read(buffer)) != -1)
	      {
	        dos.write(buffer, 0, count);
	      }
	      fis.close();

	      dos.writeBytes(end);
	      dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
	      dos.flush();

	      InputStream is = httpURLConnection.getInputStream();
	      InputStreamReader isr = new InputStreamReader(is, "utf-8");
	      BufferedReader br = new BufferedReader(isr);
	      String result = br.readLine();

	//      Toast.makeText(this, result, Toast.LENGTH_LONG).show();
	      dos.close();
	      is.close();

	    } catch (Exception e)
	    {
	      e.printStackTrace();
	  //    setTitle(e.getMessage());
	    }
	  }
	  
	  ///////////////文件上传
	  public String uploadFiles(String actionUrl, //Map<String, String> params,  
	            Map<String, File> files) throws IOException {  
	        String BOUNDARY = java.util.UUID.randomUUID().toString();  
	        String PREFIX = "--", LINEND = "\r\n";  
	        String MULTIPART_FROM_DATA = "multipart/form-data";  
	        String CHARSET = "UTF-8";  
	        URL uri = new URL(actionUrl);  
	        HttpURLConnection conn = (HttpURLConnection) uri.openConnection();  
	        conn.setReadTimeout(5 * 1000);  
	        conn.setDoInput(true);// 允许输入  
	        conn.setDoOutput(true);// 允许输出  
	        conn.setUseCaches(false);  
	        conn.setRequestMethod("POST"); // Post方式  
	        conn.setRequestProperty("connection", "keep-alive");  
	        conn.setRequestProperty("Charsert", "UTF-8");  
	        conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA  
	                + ";boundary=" + BOUNDARY);  
	        // 首先组拼文本类型的参数  
//	        StringBuilder sb = new StringBuilder();  
//	        for (Map.Entry<String, String> entry : params.entrySet()) {  
//	            sb.append(PREFIX);  
//	            sb.append(BOUNDARY);  
//	            sb.append(LINEND);  
//	            sb.append("Content-Disposition: form-data; name=\""  
//	                    + entry.getKey() + "\"" + LINEND);  
//	            sb.append("Content-Type: text/plain; charset=" + CHARSET + LINEND);  
//	            sb.append("Content-Transfer-Encoding: 8bit" + LINEND);  
//	            sb.append(LINEND);  
//	            sb.append(entry.getValue());  
//	            sb.append(LINEND);  
//	        }  
	        DataOutputStream outStream = new DataOutputStream(  
	                conn.getOutputStream());  
	       // outStream.write(sb.toString().getBytes());  
	        // 发送文件数据  
	        if (files != null)  
	            for (Map.Entry<String, File> file : files.entrySet()) {  
	                StringBuilder sb1 = new StringBuilder();  
	                sb1.append(PREFIX);  
	                sb1.append(BOUNDARY);  
	                sb1.append(LINEND);  
	                sb1.append("Content-Disposition: form-data; name=\"uploadedfile[]\"; filename=\""  
	                        + file.getKey() + "\"" + LINEND);  
	                sb1.append("Content-Type: application/octet-stream; charset="  
	                        + CHARSET + LINEND);  
	                sb1.append(LINEND);  
	                outStream.write(sb1.toString().getBytes()); //System.out.println(sb1);
	                InputStream is = new FileInputStream(file.getValue());  
	                byte[] buffer = new byte[1024];  
	                int len = 0;  
	                while ((len = is.read(buffer)) != -1) {  
	                    outStream.write(buffer, 0, len);  
	                }  
	                is.close();  
	                outStream.write(LINEND.getBytes());  
	            }  
	        // 请求结束标志  
	        byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();  
	        outStream.write(end_data);  
	        outStream.flush();  
	        // 得到响应码  
	        int res = conn.getResponseCode();  
	        InputStream in = conn.getInputStream();  
	        InputStreamReader isReader = new InputStreamReader(in);  
	        BufferedReader bufReader = new BufferedReader(isReader);  
	        String line = null;  
	        String data = "OK";  
	        while ((line = bufReader.readLine()) == null)  
	            data += line;  
	        if (res == 200) {  
	            int ch;  
	            StringBuilder sb2 = new StringBuilder();  
	            while ((ch = in.read()) != -1) {  
	                sb2.append((char) ch);  
	            }  
	        }  
	        outStream.close();  
	        conn.disconnect();  
	        return in.toString();  
	     
	}

	public void downFile(String url, String path, String fileName)
			throws IOException {
		if (fileName == null || fileName == "")
			fileName = url.substring(url.lastIndexOf("/") + 1);
		// else
		// this.FileName = fileName; // 取得文件名，如果输入新文件名，则使用新文件名
		URL Url = new URL(url);
		URLConnection conn = Url.openConnection();
		conn.connect();
		InputStream is = conn.getInputStream();
		int fileSize = conn.getContentLength();// 根据响应获取文件大小
		if (fileSize <= 0) {
			// 获取内容长度为0
			throw new RuntimeException("无法获知文件大小 ");
		}
		if (is == null) {
			// 没有下载流
			// sendEmptyMessage(Down_ERROR);
			throw new RuntimeException("无法获取文件");
		}
		File file = new File(path + fileName);
		if(!file.exists()){
			file.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(file);
		// 创建写入文件内存流，通过此流向目标写文件
		byte buf[] = new byte[1024];

		int numread;
		while ((numread = is.read(buf)) != -1) {
			fos.write(buf, 0, numread);
			System.out.println("A");
			// downLoadFilePosition += numread;
		}
		try {
			is.close();
		} catch (Exception ex) {

		}
	}
	  
	
}
