package com.yukwangsik.teamproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private PhoneAuthProvider.ForceResendingToken forceResendingToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private ProgressDialog pd;

    EditText phoneEt, codeEt;
    Button phoneContinueBtn, codeSubmitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        findViewById(R.id.check).setOnClickListener(onClickListener);

        phoneEt = findViewById(R.id.phoneEt);
        codeEt = findViewById(R.id.codeEt);
        phoneContinueBtn = findViewById(R.id.phoneContinueBtn);
        codeSubmitBtn = findViewById(R.id.codeSubmitBtn);

        pd = new ProgressDialog(this);
        pd.setTitle("Please wait...");
        pd.setCanceledOnTouchOutside(false);

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                pd.dismiss();
                Toast.makeText(SignUpActivity.this, "인증실패", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(verificationId, forceResendingToken);
                mVerificationId = verificationId;
                forceResendingToken = token;
                pd.dismiss();

                Toast.makeText(SignUpActivity.this, "코드 발송", Toast.LENGTH_SHORT).show();

            }
        };

        phoneContinueBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String phone = "+82"+phoneEt.getText().toString().trim();
                if(TextUtils.isEmpty(phone)){
                    Toast.makeText(SignUpActivity.this, "번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                } else{
                    startPhoneNumberVerification(phone);
                }
            }
        });

        codeSubmitBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String code = codeEt.getText().toString().trim();
                if(TextUtils.isEmpty(code)){
                    Toast.makeText(SignUpActivity.this, "인증번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                } else{
                    verifyPhoneNumberWithCode(mVerificationId, code);
                }
            }
        });
    }

    private void startPhoneNumberVerification(String phone){
        pd.setMessage("전화번호 확인");
        pd.show();

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyPhoneNumberWithCode(String mVerificationId, String code) {
        pd.setMessage("인증번호 확인");
        pd.show();

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        pd.setMessage("확인중...");

        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        pd.dismiss();
                        Toast.makeText(SignUpActivity.this, "인증되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(SignUpActivity.this, "인증되지않았습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            switch (v.getId()){
                case  R.id.check:
                    signUp();
                    break;
            }
        }
    };

    private void signUp(){
        String id = ((EditText)findViewById(R.id.user_id)).getText().toString();
        String password = ((EditText)findViewById(R.id.user_pw)).getText().toString();
        String passwordck = ((EditText)findViewById(R.id.user_pwck)).getText().toString();

        if(id.length()>0 && password.length()>0 && passwordck.length()>0){
            if(password.equals(passwordck)){
                mAuth.createUserWithEmailAndPassword(id, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                            //Toast.makeText(SignUpActivity.this, "회원가입에 성공!" , Toast.LENGTH_SHORT).show();
                        } else{
                            if(task.getException().toString() != null){
                                //Toast.makeText(SignUpActivity.this, "회원가입에 실패!" , Toast.LENGTH_SHORT).show();
                                updateUI(null);
                            }
                        }
                    }

                    private void updateUI(FirebaseUser user) {
                        if(user != null){
                            Toast.makeText(SignUpActivity.this,"회원가입 성공",Toast.LENGTH_LONG).show();

                        }else {
                            Toast.makeText(SignUpActivity.this,"회원가입 실패",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else{
                Toast.makeText(SignUpActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(SignUpActivity.this, "아이디와 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
        }
    }
}