package pt.isec.amov.petfinder.rest;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 *
 */
public class WebServiceTask extends AsyncTask<String, Integer, String> {

    private static final String TAG = "WebServiceTask";

    // connection timeout, in milliseconds (waiting to connect)
    public static final int CONN_TIMEOUT = 3000;

    // socket timeout, in milliseconds (waiting for data)
    public static final int SOCKET_TIMEOUT = 5000;

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String MIME_JSON = "application/json";
    public static final String AUTH = "Authorization";
    public static final String BEARER = "Bearer";
    public static final String BASIC = "Basic";

    private final TaskType taskType;
    private final int connTimeout;
    private final int socketTimeout;
    private final Context ctx;

    private List<? extends NameValuePair> params;

    protected WebServiceTask(final Context ctx, final TaskType taskType, final int connTimeout, final int socketTimeout, final List<? extends NameValuePair> params) {
        // TODO add null checks
        this.ctx = ctx;
        this.taskType = taskType;
        this.connTimeout = connTimeout;
        this.socketTimeout = socketTimeout;
        this.params = params;
    }

    @Override
    protected String doInBackground(final String... urls) {
        String url = urls[0];
        String result = "";

        HttpResponse response = doResponse(url);

        if (response == null) {
            return result;
        } else {

            try {

                result = inputStreamToString(response.getEntity().getContent());

            } catch (IllegalStateException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);

            } catch (IOException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            }

        }

        return result;
    }

    private String inputStreamToString(final InputStream is) {

        String line = "";
        StringBuilder total = new StringBuilder();

        // Wrap a BufferedReader around the InputStream
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

        try {
            // Read response until the end
            while ((line = rd.readLine()) != null) {
                total.append(line);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }

        // Return full string
        return total.toString();
    }

    private HttpParams getHttpParams() {

        HttpParams http = new BasicHttpParams();

        HttpConnectionParams.setConnectionTimeout(http, connTimeout);
        HttpConnectionParams.setSoTimeout(http, socketTimeout);

        return http;
    }

    private HttpResponse doResponse(String url) {

        // Use our connection and data timeouts as parameters for our
        // DefaultHttpClient
        HttpClient httpclient = new DefaultHttpClient(getHttpParams());
        HttpResponse response = null;

        try {
            switch (taskType) {

                case POST:
                    HttpPost httpPost = new HttpPost(url);
                    httpPost.addHeader(CONTENT_TYPE, MIME_JSON);
                    configureRequest(httpPost);

                    // Add parameters
                    httpPost.setEntity(new UrlEncodedFormEntity(params));

                    response = httpclient.execute(httpPost);
                    break;
                case GET:
                    HttpGet httpGet = new HttpGet(url);
                    httpGet.addHeader(CONTENT_TYPE, MIME_JSON);
                    configureRequest(httpGet);

                    response = httpclient.execute(httpGet);
                    break;
            }
        } catch (Exception e) {

            Log.e(TAG, e.getLocalizedMessage(), e);

        }

        return response;
    }

    protected void configureRequest(final HttpPost post) { }

    protected void configureRequest(final HttpGet get) { }

    public static enum TaskType {

        POST(1),
        GET(2);

        public final int type;

        private TaskType(final int type) {
            this.type = type;
        }

    }

}
