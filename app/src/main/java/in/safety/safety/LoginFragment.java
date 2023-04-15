package in.safety.safety;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private View view;
    private LinearLayout illustrationLayout;
    private EditText phoneInput, otpInput;
    private FloatingActionButton next;
    private TextView instructionText;
    private ProgressBar progressBar;
    private String mVerificationId;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    public void setOnLoginSuccessfulListener(OnLoginSuccessfulListener onLoginSuccessfulListener) {
        this.onLoginSuccessfulListener = onLoginSuccessfulListener;
    }

    public interface OnLoginSuccessfulListener {
        void loginSuccess();
    }

    private OnLoginSuccessfulListener onLoginSuccessfulListener;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_login, container, false);
        mAuth = FirebaseAuth.getInstance();
        illustrationLayout = view.findViewById(R.id.illustration_linear_layout);
        phoneInput = view.findViewById(R.id.phone_input);
        next = view.findViewById(R.id.next_btn);
        instructionText = view.findViewById(R.id.instruction_text);
        otpInput = view.findViewById(R.id.otp_input);
        progressBar = view.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);
        otpInput.setVisibility(View.GONE);
        YoYo.with(Techniques.FadeIn)
                .duration(600)
                .playOn(illustrationLayout);
        YoYo.with(Techniques.SlideInRight)
                .duration(600)
                .playOn(phoneInput);
        YoYo.with(Techniques.SlideInLeft)
                .duration(600)
                .playOn(next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Patterns.PHONE.matcher(phoneInput.getText().toString().trim()).matches())
                {
                    YoYo.with(Techniques.SlideOutLeft)
                            .duration(600)
                            .playOn(phoneInput);
                    YoYo.with(Techniques.FadeOut)
                            .duration(600)
                            .playOn(instructionText);
                    YoYo.with(Techniques.SlideOutRight)
                            .duration(600)
                            .playOn(next);
                    //sendSMS("12345676890");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            instructionText.setText("Please wait, verifying your phone number");
                            YoYo.with(Techniques.FadeIn)
                                    .duration(600)
                                    .playOn(instructionText);
                            progressBar.setVisibility(View.VISIBLE);
                            login();
                        }
                    },600);
                }
            }
        });
        return view;
    }

    private void sendSMS(String phoneNumber)
    {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, "Please verify this number #14828", null, null);
    }

    private void login()
    {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                loginWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.e("FIREX", e.toString());
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;
                YoYo.with(Techniques.FadeOut)
                        .duration(600)
                        .playOn(progressBar);
                YoYo.with(Techniques.FadeOut)
                        .duration(600)
                        .playOn(instructionText);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        instructionText.setText("An OTP has been sent to your number. Please enter the OTP to verify");
                        otpInput.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.SlideInLeft)
                                .duration(600)
                                .playOn(otpInput);
                        YoYo.with(Techniques.FadeIn)
                                .duration(600)
                                .playOn(next);
                    }
                },600);
                YoYo.with(Techniques.FadeIn)
                        .duration(600)
                        .delay(600)
                        .playOn(instructionText);
                next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        YoYo.with(Techniques.FadeIn)
                                .duration(600)
                                .playOn(progressBar);
                        String OTP = otpInput.getText().toString().trim();
                        otpInput.setEnabled(false);
                        next.setEnabled(false);
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, OTP);
                        loginWithPhoneAuthCredential(credential);
                    }
                });
            }
        };
        String phoneNumber = phoneInput.getText().toString().trim();
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+91"+phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(getActivity())                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void loginWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    verified(task.getResult().getUser());
                }
                else
                {
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        otpInput.setError("Invalid OTP");
                        YoYo.with(Techniques.FadeOut)
                                .duration(600)
                                .playOn(progressBar);
                        otpInput.setEnabled(true);
                        next.setEnabled(true);
                    }
                }
            }
        });
    }

    private void verified(FirebaseUser user) {
        YoYo.with(Techniques.FadeOut)
                .duration(600)
                .playOn(progressBar);
        instructionText.setText("Welcome back".concat(user.getDisplayName()!=null?", "+user.getDisplayName():"!"));
        YoYo.with(Techniques.FadeIn)
                .duration(600)
                .playOn(instructionText);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onLoginSuccessfulListener.loginSuccess();
            }
        }, 1000);
    }
}