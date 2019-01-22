package org.heath.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import net.sf.json.JSONObject;

public class HttpClient {

	public static JSONObject httpClient(JSONObject httpParameter)
			throws UnsupportedEncodingException, IOException, ClientProtocolException {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		String url = httpParameter.getString("url");
		JSONObject parameter = httpParameter.getJSONObject("parameter");
		Iterator<String> iterator = parameter.keys();
		HttpPost httpPost = new HttpPost(url);

		String appKey = "4e200e8e7932b84967bbf96a59a0dfc2";
		String appSecret = "127518b51856";
		String nonce = RandomCode.getRandomCode();
		String curTime = String.valueOf((new Date()).getTime() / 1000L);
		String checkSum = CheckSumBuilder.getCheckSum(appSecret, nonce, curTime);// 参考
																					// 计算CheckSum的java代码

		// 设置请求的header
		httpPost.addHeader("AppKey", appKey);
		httpPost.addHeader("Nonce", nonce);
		httpPost.addHeader("CurTime", curTime);
		httpPost.addHeader("CheckSum", checkSum);
		httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

		// 设置请求的参数
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			nvps.add(new BasicNameValuePair(key, parameter.getString(key)));

		}
		httpPost.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));
		// 执行请求
		HttpResponse IMResponse = httpClient.execute(httpPost);
		String temp = EntityUtils.toString(IMResponse.getEntity(), "utf-8");
		JSONObject returnObj = JSONObject.fromObject(temp);
		// 打印执行结果
		System.out.println(returnObj);
		return returnObj;
	}
}
