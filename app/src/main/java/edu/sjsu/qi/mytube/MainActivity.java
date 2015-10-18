package edu.sjsu.qi.mytube;

import android.content.Intent;
import android.app.Activity;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.*;
import com.google.android.gms.common.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistListResponse;

import java.io.IOException;
import java.util.List;


public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener
{
    private boolean mIsResolving = false;
    private boolean mShouldResolve = false;
    private static final int RC_SIGN_IN = 0;
    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = MainActivity.class.getSimpleName();

    private SignInButton mSignInButton;

    private static final String SAVED_PROGRESS = "sign_in_progress";
    private static final int REQUEST_AUTHORIZATION = 89898;
    private int mSignInProgress;

    private String accessCode = "";
    private String playlistID = "";
    private String PLAYLIST_TITLE="SJSU-CMPE-277";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Build GoogleApiClient with access to basic profile
        mSignInButton=(SignInButton)findViewById(R.id.button_Login);
        mSignInButton.setOnClickListener(this);
        if(savedInstanceState!=null){
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.EMAIL))
                .build();
    }
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    //Save state
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_PROGRESS, mSignInProgress);
    }

    @Override
    public void onClick(View v) {
        mGoogleApiClient.connect();
        if (v.getId() == R.id.button_Login) {
            onSignInClicked();
        }
    }

    private void onSignInClicked() {
        // User clicked the sign-in button, so begin the sign-in process and automatically
        // attempt to resolve any errors that occur.
        mShouldResolve = true;
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        // onConnected indicates that an account was selected on the device, that the selected
        // account has granted any requested permissions to our app and that we were able to
        // establish a service connection to Google Play services.
        Log.d(TAG, "onConnected:" + bundle);
        mShouldResolve = false;

        //Get User Access Token
        new GetIdTokenTask().execute();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible error codes.

        Log.d(TAG, "onConnectionFailed:" + connectionResult);

        if (!mIsResolving && mShouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(this, RC_SIGN_IN);
                    mIsResolving = true;
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Could not resolve ConnectionResult.", e);
                    mIsResolving = false;
                    mGoogleApiClient.connect();
                }
            } else {
                // Could not resolve the connection result, show the user an
                // error dialog.
                Log.v(TAG, connectionResult.toString());

                Toast.makeText(this, "should resolve but cannot resolve connection result!",
                        Toast.LENGTH_LONG).show();
            }
        } else {
            // Show the signed-out UI
            showSignedOutUI();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        if (requestCode == RC_SIGN_IN) {
            // If the error resolution was not successful we should not resolve further.
            if (resultCode != RESULT_OK) {
                mShouldResolve = false;
            }
            mIsResolving = false;
            mGoogleApiClient.connect();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    private void showSignedOutUI(){
        mIsResolving=false;
    }


    //Get Access Token
    private class GetIdTokenTask extends AsyncTask<Void, Void, String> {
        private String token ="";

        @Override
        protected String doInBackground(Void... params) {
            String accountName = Plus.AccountApi.getAccountName(mGoogleApiClient);

            String scope = "oauth2:" + Scopes.PROFILE + "  https://www.googleapis.com/auth/youtube";
            //String scope = "oauth2: https://www.googleapis.com/auth/youtube";

            Log.d(TAG, "accountName " + accountName);

            try {
                token =  GoogleAuthUtil.getToken(getApplicationContext(), accountName, scope, new Bundle());
                Log.d(TAG, "token " + token);

            } catch (UserRecoverableAuthException e) {
                Log.w(TAG, "Error retrieving the token: " + e.getMessage());
                Log.d(TAG, "Trying to solve the problem...");
                startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
            } catch (IOException e) {
                Log.e(TAG, "Error retrieving ID token.", e);
            } catch (GoogleAuthException e) {
                Log.e(TAG, "Error retrieving ID token.", e);
            }
            return token;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                accessCode =result;
                //Get user's playlist Id which title is "SJSU-CMPE-277"
                new GetPlaylistIDTask().execute();

            } else {
                // There was some error getting the ID Token
            }
        }
    }


    //Retrieve Playlist ID for title= "SJSU-CMPE-277"
    private class GetPlaylistIDTask extends AsyncTask<Void, Void, String> {
        private String listID="";

        @Override
        protected String doInBackground(Void... params) {
            try{

                GoogleCredential credential = new GoogleCredential.Builder()
                        .setTransport(new NetHttpTransport())
                        .setJsonFactory(new JacksonFactory())
                        .build();
                credential.setAccessToken(accessCode);

                // This object is used to make YouTube Data API requests.
                YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), credential)
                        .setApplicationName("MyTube")
                        .build();

                // Define the API request for retrieving search results.
                PlaylistListResponse playlistListResponse = youtube.playlists()
                        .list("id, snippet")
                        .setMine(true)
                        .execute();

                List<Playlist> playlistResultList = playlistListResponse.getItems();

                if(playlistResultList != null ){

                    for(int i=0; i< playlistResultList.size(); i++){
                        Log.d(TAG, "playlist title " + playlistResultList.get(i).getSnippet().getTitle());

                        if(playlistResultList.get(i).getSnippet().getTitle().equals(PLAYLIST_TITLE) ){
                            listID = playlistResultList.get(i).getId();
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Error retrieving Playlist ID.", e);
            }
            return listID;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                playlistID =result;
                Log.d(TAG,"playlistId " + playlistID);

                Intent intent = new Intent(MainActivity.this, MyTubeActivity.class);
                intent.putExtra("AccessToken", accessCode);
                intent.putExtra("PlaylistId", playlistID);
                startActivity(intent);

            } else {
                // There was some error getting the playlistID
            }
        }
    }

}