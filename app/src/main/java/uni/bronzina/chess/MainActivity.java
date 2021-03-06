package uni.bronzina.chess;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {

    private SharedPreferences preferenceManager;
    private SharedPreferences.Editor editor;
    private int theme;
    private boolean useBiometrics;
    private FragmentActivity activity;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private FirebaseUser user;

    FirebaseDatabase database;
    DatabaseReference playerRef;
    String playerName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferenceManager = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferenceManager.edit();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) theme = preferenceManager.getInt("Theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        else theme = preferenceManager.getInt("Theme", AppCompatDelegate.MODE_NIGHT_NO);
        AppCompatDelegate.setDefaultNightMode(theme);

        setContentView(R.layout.activity_main);

        //Check current user
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.addAuthStateListener(authStateListener);
        user = firebaseAuth.getCurrentUser();

        database = FirebaseDatabase.getInstance();
        playerRef = database.getReference("message");
        playerRef.setValue("Hello, World!");
        SharedPreferences preferences = getSharedPreferences("PREFS", 0);
        playerName = preferences.getString("playerName", "");
        /*if(!playerName.equals("")){
            playerRef = database.getReference("players/" + playerName);
            addEventListener();
            playerRef.setValue("");
        }*/

        useBiometrics = preferenceManager.getBoolean("UseBiometrics", false);

        activity = this;
        executor = Executors.newSingleThreadExecutor();

        biometricPrompt = new BiometricPrompt(activity, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    // user clicked negative button
                    FirebaseAuth.getInstance().signOut();
                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                FirebaseUser userFromFirebase = FirebaseAuth.getInstance().getCurrentUser();
                /*RestLocalMethods.initRetrofit(getApplicationContext());
                RestLocalMethods.getUserByEmail(userFromFirebase, new UserCallback() {
                    @Override
                    public void onSuccess(@NonNull User value) {

                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();

                    }
                });*/
                Intent intent = new Intent(MainActivity.this, IntroActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                biometricPrompt.authenticate(promptInfo);
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric authentication.")
                .setDescription("Use fingerprint for authentication")
                .setNegativeButtonText("Cancel")
                .build();
    }

    FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

            if (firebaseUser == null) {
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
            else  { // (firebaseUser != null)
                if (useBiometrics) {
                    biometricPrompt.authenticate(promptInfo);
                }
                /*else {

                    RestLocalMethods.initRetrofit(getApplicationContext());
                    RestLocalMethods.getUserByEmail(firebaseUser, new UserCallback() {

                        @Override
                        public void onSuccess( User value) {

                            User myNewUser = new User(firebaseUser.getDisplayName(),
                                 "",firebaseUser.getPhoneNumber(),firebaseUser.getEmail(), firebaseUser.getUid());
                            if(value == null) {
                                RestLocalMethods.createUser(myNewUser, new UserCallback() {
                                    @Override
                                    public void onSuccess(User value) {
                                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            }
                            else{
                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            }




                        }
                    });
                }*/
                Intent intent = new Intent(MainActivity.this, IntroActivity.class);
                startActivity(intent);
                finish();
            }
        }
    };

    /*private void userLogout() {
        AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>(){

            @Override
            public void onComplete(@NonNull Task<Void> task) {
                editor.putBoolean("UseBiometrics" , false);
                editor.apply();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finishAffinity();
            }
        });
    }*/

    private void addEventListener(){
        playerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!playerName.equals("")){
                    SharedPreferences preferences = getSharedPreferences("PREFS", 0);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("playerName", playerName);
                    editor.apply();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
