package uni.bronzina.chess;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignInActivity extends AppCompatActivity {

    private EditText emailSignIn, passwordSignIn;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private FirebaseDatabase database;
    private Button signInButton;
    private TextView notRegisteredButton, resetPasswordButton;
    private com.google.android.gms.common.SignInButton googleSignInButton;
    private LoginButton facebookSignInButton;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager;
    private CheckBox rememberMe;
    private SharedPreferences preferenceManager;
    private SharedPreferences.Editor editor;
    private static final int RC_SIGN_IN = 9001;
    private static final int STORAGE_PERMISSION_CODE = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        // Permission
        /*if (ContextCompat.checkSelfPermission(SignInActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)  == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(SignInActivity.this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, STORAGE_PERMISSION_CODE);
        }*/

        emailSignIn = findViewById(R.id.emailSignInEditText);
        passwordSignIn = findViewById(R.id.passwordSignInEditText);
        signInButton = findViewById(R.id.signInButton);
        googleSignInButton = findViewById(R.id.googleSignInButton);
        notRegisteredButton = findViewById(R.id.notRegisteredButton);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);
        rememberMe = findViewById(R.id.rememberMeCheckBox);

        preferenceManager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = preferenceManager.edit();

        databaseReference = FirebaseDatabase.getInstance("https://chess-32d01-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        database = FirebaseDatabase.getInstance("https://chess-32d01-default-rtdb.europe-west1.firebasedatabase.app/");

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();


        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Configure Facebook Sign In
        mCallbackManager = CallbackManager.Factory.create();
        /*facebookSignInButton = findViewById(R.id.facebookSignInButton);
        facebookSignInButton.setReadPermissions("email", "public_profile");
        facebookSignInButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                facebookSignInButton.setVisibility(View.GONE);
                handleAccessToken(loginResult.getAccessToken());
            }
            @Override
            public void onCancel() {
                Log.d("FBLogin", "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("FBLogin", "facebook:onError", error);
                // ...
            }
        });*/

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailSignIn.getText().toString().trim();
                final String password = passwordSignIn.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter your mail address", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter your password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.length() < 8) {
                    Toast.makeText(getApplicationContext(),"Password must be more than 8 digit",Toast.LENGTH_SHORT).show();
                    return;
                }
                //authenticate user
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            // there was an error
                            try {
                                throw task.getException();
                            }
                            // if user enters wrong email.
                            catch (FirebaseAuthInvalidUserException invalidEmail) {
                                Toast.makeText(getApplicationContext(),"Wrong email.",Toast.LENGTH_SHORT).show();
                            }
                            // if user enters wrong password.
                            catch (FirebaseAuthInvalidCredentialsException wrongPassword) {
                                Toast.makeText(getApplicationContext(),"Wrong password.",Toast.LENGTH_SHORT).show();
                            }
                            catch (Exception e) {
                                Log.d("SignInActivity", "onComplete: " + e.getMessage());
                            }
                        } else {
                            final FirebaseUser user = auth.getCurrentUser();
                            //Check if user exist yet  in rails and if not it creates him
                            Intent intent = new Intent(SignInActivity.this, IntroActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
            }
        });

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();
            }
        });

        notRegisteredButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateSignUp();
            }
        });

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateResetPassword();
            }
        });

        rememberMe.setChecked(preferenceManager.getBoolean("RememberMe", false));
        rememberMe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    editor.putBoolean("RememberMe", true);
                    editor.apply();
                }
                else {
                    editor.putBoolean("RememberMe", false);
                    editor.apply();
                }
            }
        });
    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(SignInActivity.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(SignInActivity.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }*/

    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    //GOOGLE
    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    final FirebaseUser user = auth.getCurrentUser();

                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.hasChild(user.getUid())) {
                                // run some code
                            }
                            else{
                                //databaseReference.child("users").child(user.getUid()).setValue(new Userinformation("","",""));
                                DatabaseReference userRef = database.getReference("users/" + user.getUid());
                                userRef.setValue(new Userinformation(user.getDisplayName(),user.getEmail(),user.getPhoneNumber()));
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    Intent intent = new Intent(SignInActivity.this, IntroActivity.class);
                    startActivity(intent);
                    finish();

                }
                else {
                    Toast.makeText(getApplicationContext(),"Authentication with Google failed!" ,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
            }
        }

        else mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    //FACEBOOK
    /*private void handleAccessToken(AccessToken accessToken) {
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        auth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    final FirebaseUser user = auth.getCurrentUser();

                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.hasChild(user.getUid())) {
                                //Run some code
                            }
                            else{
                                DatabaseReference userRef = database.getReference("users/" + user.getUid());
                                userRef.setValue(new Userinformation(user.getDisplayName(),user.getEmail(),user.getPhoneNumber()));
                                        //databaseReference.child("users").child(user.getUid()).setValue(new Userinformation("","",""));
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    Intent intent = new Intent(SignInActivity.this, IntroActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(SignInActivity.this, "Authentication with Facebook failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }*/

    private void navigateSignUp() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateResetPassword() {
        Intent intent = new Intent(this, ResetPasswordActivity.class);
        startActivity(intent);
        finish();
    }



}
