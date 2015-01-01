package pt.isec.amov.petfinder.rest;

import android.content.Context;
import android.util.Base64;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import static pt.isec.amov.petfinder.rest.WebServiceTask.TaskType.POST;

/**
 *
 */
public class AuthenticateUserTask extends WebServiceTask {

    private final Credentials credentials;

    public AuthenticateUserTask(final Context ctx, final Credentials credentials, final Parameters params) {
        super(ctx, POST, params.getConnTimeout(), params.getSocketTimeout(), params.getParams());
        this.credentials = credentials;
    }

    @Override
    protected void configureRequest(final HttpPost post) {
        final String cred = credentials.getId() + ":" + credentials.getSecret();
        final String encoded = Base64.encodeToString(cred.getBytes(), Base64.NO_WRAP);
        post.setHeader(AUTH, BASIC + " " + encoded);
    }

    @Override
    protected void onPostExecute(final String response) {
        // TODO deserialize the response into the following variables
        String accessToken = "";
        String refreshToken = "";
        long expiresIn = 0;

        // call the task-specific overload
        this.onPostExecute(accessToken, refreshToken, expiresIn);
    }

    public void onPostExecute(final String accessToken, final String refreshToken, final long expiresIn) {
        // override to provide some meaningful behavior
    }

    public static class Parameters extends BaseParameters<Parameters> {

        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
        public static final String GRANT_TYPE = "grant_type";

        public Parameters(final String username, final String password) {
            params.add(new BasicNameValuePair(USERNAME, username));
            params.add(new BasicNameValuePair(PASSWORD, password));
            params.add(new BasicNameValuePair(GRANT_TYPE, PASSWORD));
        }

    }
}
