package in.safety.safety;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;

public class OnboardingActivity extends AppCompatActivity {
    private FrameLayout frameLayout;
    private FragmentManager fragmentManager;
    private WelcomeFragment welcomeFragment;
    private LoginFragment loginFragment;
    private PermissionsFragment permissionsFragment;
    private SharedPreferences permissionManager, sharedPreferences;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("AppStats", MODE_PRIVATE);
        if(sharedPreferences.getBoolean("firstRun", true))
        {
            startActivity(new Intent(getApplicationContext(), IntroActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_welcome);
        initializeViews();
        mAuth = FirebaseAuth.getInstance();
        welcomeFragment = new WelcomeFragment();
        permissionsFragment = new PermissionsFragment();
        loginFragment = new LoginFragment();
        permissionManager = getSharedPreferences("permissionManager", Context.MODE_PRIVATE);
        if(mAuth.getCurrentUser() != null)
        {
            if(permissionManager.getBoolean("askedForPermissions", false))
            {
                startActivity(new Intent(OnboardingActivity.this, MainActivity.class));
                finish();
            }
            else
            {
                fragmentManager.beginTransaction().replace(frameLayout.getId(), permissionsFragment).commit();
            }
        }
//        if(getSharedPreferences("login_status", MODE_PRIVATE).getBoolean("isLoggedIn", false))
//        {
//            startActivity(new Intent(getApplicationContext(), MainActivity.class));
//            finish();
//        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 111);
        }
        permissionsFragment.setOnPermissionGrantedListener(new PermissionsFragment.onPermissionsGrantedListener() {
            @Override
            public void granted() {
                fragmentManager.beginTransaction().replace(frameLayout.getId(), new AddEmergencyContactsFragment()).commit();
            }
        });
        welcomeFragment.setOnButtonClickedListener(new WelcomeFragment.buttonAction() {
            @Override
            public void login() {
                fragmentManager.beginTransaction().replace(frameLayout.getId(), loginFragment).commit();
                loginFragment.setOnLoginSuccessfulListener(new LoginFragment.OnLoginSuccessfulListener() {
                    @Override
                    public void loginSuccess() {
                        if(permissionManager.getBoolean("askedForPermissions", false))
                        {
                            startActivity(new Intent(OnboardingActivity.this, MainActivity.class));
                            finish();
                        }
                        else
                        {
                            fragmentManager.beginTransaction().replace(frameLayout.getId(), permissionsFragment).commit();
                        }
                    }
                });
            }

            @Override
            public void signup() {

            }

            @Override
            public void skip() {
                mAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            if(getSharedPreferences("permissionManager", MODE_PRIVATE).getBoolean("askedForPermission", false))
                            {
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            }
                            else
                            {
                                fragmentManager.beginTransaction().replace(frameLayout.getId(), permissionsFragment).commit();
                            }
                        }
                        else
                        {
                            if(task.getException() != null)
                                Toast.makeText(OnboardingActivity.this, "Error: "+task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        if(getActionBar() != null)
            getActionBar().hide();
        else
            getSupportActionBar().hide();
        fragmentManager.beginTransaction().replace(frameLayout.getId(), welcomeFragment).commit();
    }

    private void initializeViews() {
        frameLayout = findViewById(R.id.frame_layout);
        fragmentManager = getSupportFragmentManager();
    }
}