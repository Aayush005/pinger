package tech.aayush.pinger;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class OtpActivity extends AppCompatActivity {

    String verificationId;
    Intent intent;
    EditText et_otp;
    Button verify_btn;
    String otp;
    ProgressBar pb_bar;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);



        et_otp = findViewById(R.id.et_otp);
        verify_btn = findViewById(R.id.verify_btn);
        pb_bar = findViewById(R.id.pb_bar);
        pb_bar.setVisibility(View.GONE);
        mAuth = FirebaseAuth.getInstance();

        intent = getIntent();
        verificationId = intent.getStringExtra("verificationId");

        verify_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                otp = et_otp.getText().toString().trim();

                if(!otp.isEmpty()){
                    pb_bar.setVisibility(View.VISIBLE);
                    verifyOtp(verificationId , otp);
                }else {
                    et_otp.setError("Invalid otp");
                }


            }
        });
    }

    private void verifyOtp(String verificationId, String otp) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId,otp);

        //sign in user
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){
                            pb_bar.setVisibility(View.INVISIBLE);
                            Intent intent = new Intent(OtpActivity.this , MainActivity.class);
                            startActivity(intent);
                            finish();
                        }else {
                            pb_bar.setVisibility(View.INVISIBLE);
                            String message = "Verification failed , Please try again later.";

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                message = "Invalid code entered...";
                            }

                            Toast.makeText(OtpActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    }

                });
    }
}
