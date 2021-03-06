package com.myardina.buckeyes.myardina;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.myardina.buckeyes.myardina.Common.Utility.CommonConstants;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener {

    private UserRegisterTask mRegTask = null;

    // UI references.
    private EditText mEmailView;
    private EditText mEmailConfirmView;
    private EditText mPasswordView;
    private EditText mPasswordConfirmView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Bundle extras = this.getIntent().getExtras();

        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);
        if (extras != null) {
            String email = (String) extras.get(CommonConstants.EXTRA_EMAIL);
            if (!TextUtils.isEmpty(email)) {
                mEmailView.setText(email);
            }
        }
        mEmailView.setOnFocusChangeListener(this);
        mEmailConfirmView = (EditText) findViewById(R.id.email_confirm);
        mEmailConfirmView.setOnFocusChangeListener(this);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnFocusChangeListener(this);
        mPasswordConfirmView = (EditText) findViewById(R.id.password_confirm);
        mPasswordConfirmView.setOnFocusChangeListener(this);

        Button mEmailRegisterButton = (Button) findViewById(R.id.email_register_button);
        mEmailRegisterButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id) {
            case R.id.email_register_button:
                attemptLoginOrRegister();
                break;
            default:
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        int id = v.getId();
        if (!hasFocus) {
            String original = "";
            String confirm = "";
            switch (id) {
                case R.id.email:
                    validateEmail(mEmailView.getText().toString());
                    break;
                case R.id.email_confirm:
                    original = mEmailView.getText().toString();
                    confirm = mEmailConfirmView.getText().toString();
                    validateConfirmField(original, confirm, mEmailConfirmView, getString(R.string.error_email_mismatch));
                    break;
                case R.id.password:
                    validatePassword(mPasswordView.getText().toString());
                    break;
                case R.id.password_confirm:
                    original = mPasswordView.getText().toString();
                    confirm = mPasswordConfirmView.getText().toString();
                    validateConfirmField(original, confirm, mPasswordConfirmView, getString(R.string.error_password_mismatch));
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     * int l_r is 0 if login 1 if register
     */
    private void attemptLoginOrRegister() {
        // Reset errors.
        mEmailView.setError(null);
        mEmailConfirmView.setError(null);
        mPasswordView.setError(null);
        mPasswordConfirmView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String emailConfirm = mEmailConfirmView.getText().toString();
        String password = mPasswordView.getText().toString();
        String passwordConfirm = mPasswordConfirmView.getText().toString();

        View focusView = null;

        boolean validPasswordConfirm = validateConfirmField(password, passwordConfirm,
                mPasswordConfirmView, getString(R.string.error_password_mismatch));
        focusView = validPasswordConfirm ? mPasswordConfirmView : focusView;

        boolean validPassword = validatePassword(password);
        focusView = validPassword ? mPasswordView : focusView;

        boolean validEmailConfirm = validateConfirmField(email, emailConfirm,
                mEmailConfirmView, getString(R.string.error_email_mismatch));
        focusView = validEmailConfirm ? mEmailConfirmView : focusView;

        boolean validEmail = validateEmail(email);
        focusView = validEmail ? mEmailView : focusView;

        if (focusView != null) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // perform the user login attempt.
            mRegTask = new UserRegisterTask(email, password, this);
            mRegTask.execute((Void) null);
            mRegTask = null;//important do not remove
            Intent registerActivity = new Intent(RegisterActivity.this, RegisterActivity.class);
            RegisterActivity.this.startActivity(registerActivity);
        }
    }

    private boolean validatePassword(String password) {
        boolean invalid = false;
        // Check for a valid password.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            invalid = true;
        } else {
            String error = isPasswordValid(password);
            if (!TextUtils.isEmpty(error)) {
                mPasswordView.setError(error);
                invalid = true;
            }
        }
        return invalid;
    }

    private boolean validateEmail(String email) {
        boolean invalid = false;
        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            invalid = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            invalid = true;
        }
        return invalid;
    }

    private boolean validateConfirmField(String original, String confirm, EditText field, String invalidFieldMessage) {
        boolean invalid = false;
        // Check for a valid password.
        if (TextUtils.isEmpty(confirm)) {
            field.setError(getString(R.string.error_field_required));
            invalid = true;
        } else if (!TextUtils.equals(original, confirm)) {
            field.setError(invalidFieldMessage);
            invalid = true;
        }
        return invalid;
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private String isPasswordValid(String password) {
        String error = "";
        if (password.length() <= 5) {
            error = "Password is too short";
        } else if (TextUtils.equals(password, password.toLowerCase())) {
            error = "Password must contain a capital letter";
        } else if (TextUtils.equals(password, password.toUpperCase())) {
            error = "Password must contain a lower case letter";
        }
        return error;
    }

    /**
     * Represents an asynchronous registration task used to authenticate
     * the user.
     */
    public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private final Context mContext;
        FirebaseAuth auth = FirebaseAuth.getInstance();

        UserRegisterTask(String email, String password, Context context) {
            mEmail = email;
            mPassword = password;
            mContext = context;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Boolean doInBackground(Void... params) {
            Task register_task = auth.createUserWithEmailAndPassword(mEmail, mPassword);
            OnSuccessListener login_success = new OnSuccessListener() {
                @Override
                public void onSuccess(Object o) {
                    Intent symptomsActivity = new Intent(RegisterActivity.this, SymptomsActivity.class);
                    RegisterActivity.this.startActivity(symptomsActivity);
                }
            };
            OnFailureListener login_failure = new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // there was an error
                    Toast created_user_toast = Toast.makeText(mContext, "User " + mEmail + " cannot be created, might exist already, try again!", Toast.LENGTH_SHORT);
                    created_user_toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
                    created_user_toast.show();
                }
            };
            register_task.addOnSuccessListener(login_success);
            register_task.addOnFailureListener(login_failure);

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mRegTask = null;
            //if (success){}
        }
    }

    @Override
    protected void onStart(){
        System.out.println("onStart method for RegisterActivity being called");
        super.onStart();
    }

    @Override
    protected void onRestart(){
        System.out.println("onRestart method for RegisterActivity being called");
        super.onRestart();
    }

    @Override
    protected void onPause(){
        System.out.println("onPause method for RegisterActivity being called");
        super.onPause();
    }

    @Override
    protected void onResume(){
        System.out.println("onResume method for RegisterActivity being called");
        super.onResume();
    }

    @Override
    protected void onStop()
    {
        System.out.println("onStop method for RegisterActivity being called");
        super.onStop();
    }

    @Override
    protected void onDestroy(){
        System.out.println("onDestroy method for RegisterActivity being called");
        super.onDestroy();
    }
}
