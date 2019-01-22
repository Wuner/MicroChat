package heath.com.microchat.utils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import heath.com.microchat.service.ServiceRulesException;

public class ClientUtils {
    public static final String client(JSONObject parameter, String url) throws Exception{
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        NameValuePair Phone = new BasicNameValuePair("parameterData",
                parameter.toString());
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(Phone);
        post.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));
        HttpResponse response = client.execute(post);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK)
            throw new ServiceRulesException(Common.MSG_SERVER_ERROR);

        String result = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
        return result;
    }
}
