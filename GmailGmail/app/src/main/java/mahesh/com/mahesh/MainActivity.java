package mahesh.com.mahesh;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 007;

    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;

    private SignInButton btnSignIn;
    private Button btnSignOut, btnRevokeAccess;
    private LinearLayout llProfileLayout;
    private ImageView imgProfilePic;
    private TextView txtName, txtEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSignIn = (SignInButton) findViewById(R.id.btn_sign_in);
        btnSignOut = (Button) findViewById(R.id.btn_sign_out);
        btnRevokeAccess = (Button) findViewById(R.id.btn_revoke_access);
        llProfileLayout = (LinearLayout) findViewById(R.id.llProfile);
        imgProfilePic = (ImageView) findViewById(R.id.imgProfilePic);
        txtName = (TextView) findViewById(R.id.txtName);
        txtEmail = (TextView) findViewById(R.id.txtEmail);
        mProgressDialog = new ProgressDialog(this);
        btnSignIn.setOnClickListener(this);
        btnSignOut.setOnClickListener(this);
        btnRevokeAccess.setOnClickListener(this);
       // if (isDeviceOnline()) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestProfile()
                    .build();

            mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                    .enableAutoManage(this, this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();

        //}else{
         //   Toast.makeText(this, "No internet Connection", Toast.LENGTH_SHORT).show();
        //}



            // Customizing G+ button
            btnSignIn.setSize(SignInButton.SIZE_STANDARD);
           // btnSignIn.setScopes(gso.getScopeArray());
    }


    private void signIn() {
        if (!isGooglePlayServicesAvailable()){
            Toast.makeText(this, "Google Play Services are not Available", Toast.LENGTH_SHORT).show();
        }else if (isDeviceOnline()){
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.show();
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }
//        mProgressDialog.setMessage("Loading...");
//        mProgressDialog.show();
//        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
//        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        updateUI(false);
                    }
                });
    }

    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        updateUI(false);
                    }
                });
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();

            Log.e("mahesh","mahesh");
            Intent i = new Intent(getApplicationContext(), Success.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finishAffinity();
            startActivity(i);
            //updateUI(true);
        } else {
            // Signed out, show unauthenticated UI.
            updateUI(false);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.btn_sign_in:
                signIn();
                break;

            case R.id.btn_sign_out:
                signOut();
                break;

            case R.id.btn_revoke_access:
                revokeAccess();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.e("on result","on result");
           // handleSignInResult(result);
            if (result.isSuccess()) {
                mProgressDialog.dismiss();
                // Signed in successfully, show authenticated UI.
                GoogleSignInAccount acct = result.getSignInAccount();

                String personName = acct.getDisplayName();

                String email = acct.getEmail();
                Log.e("on result","mahesh"+email);
                Intent i = new Intent(getApplicationContext(), Success.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finishAffinity();
                startActivity(i);
                //updateUI(true);
            } else {
                // Signed out, show unauthenticated UI.
                updateUI(false);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
if (isDeviceOnline()){
    OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
    if (opr.isDone()) {
        // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
        // and the GoogleSignInResult will be available instantly.
        Log.d("if onstart", "Got cached sign-in");
        GoogleSignInResult result = opr.get();
        handleSignInResult(result);
    } else {
        // If the user has not previously signed in on this device or the sign-in has expired,
        // this asynchronous branch will attempt to sign in the user silently.  Cross-device
        // single sign-on will occur in this branch.

        opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
            @Override
            public void onResult(GoogleSignInResult googleSignInResult) {

                Log.e("else onstart","onstart"+googleSignInResult);

                handleSignInResult(googleSignInResult);
            }
        });
    }
}else {
    Toast.makeText(this, "no internet connection", Toast.LENGTH_SHORT).show();
}

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }


    private void updateUI(boolean isSignedIn) {
        if (isSignedIn) {
            btnSignIn.setVisibility(View.GONE);
            btnSignOut.setVisibility(View.VISIBLE);
            btnRevokeAccess.setVisibility(View.VISIBLE);
            llProfileLayout.setVisibility(View.VISIBLE);
        } else {
            btnSignIn.setVisibility(View.VISIBLE);
            btnSignOut.setVisibility(View.GONE);
            btnRevokeAccess.setVisibility(View.GONE);
            llProfileLayout.setVisibility(View.GONE);
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }




}