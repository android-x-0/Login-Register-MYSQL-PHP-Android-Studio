package com.example.eshoperone.Main;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.example.eshoperone.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {


    private EditText editTextUsername, editTextEmail, editTextPassword;
    private Button buttonRegister;
    private ProgressDialog progressDialog;
    private TextView textViewLogin;


    AwesomeValidation awesomeValidation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

     //   if (getSupportActionBar() != null) {
      //      getSupportActionBar().hide();
     //   }


        if(SharedPrefManager.getInstance(this).isLoggedIn()){
            finish();
            startActivity(new Intent(this, MainActivity.class));
            return;
        }

        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);


        textViewLogin = (TextView) findViewById(R.id.textViewLogin);

        buttonRegister = (Button) findViewById(R.id.buttonRegister);

        progressDialog = new ProgressDialog(this);

        buttonRegister.setOnClickListener(this);
        textViewLogin.setOnClickListener(this);

        editTextUsername.addTextChangedListener(loginTextWatcher);
        editTextEmail.addTextChangedListener(loginTextWatcher);
        editTextPassword.addTextChangedListener(loginTextWatcher);

        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);



    }
    private TextWatcher loginTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String usernameInput = editTextUsername.getText().toString().trim();
            String passwordInput = editTextPassword.getText().toString().trim();
            String emailInput = editTextEmail.getText().toString().trim();

            buttonRegister.setEnabled(!usernameInput.isEmpty() && !passwordInput.isEmpty() && !emailInput.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(emailInput).matches());
            validateEmail();

            if (editTextPassword.getText().toString().trim().length() < 6) {
                editTextPassword.setError("Your passwords must be atleast 6 characters in length");
                buttonRegister.setEnabled(false);
            }



        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };


    private void registerUser() {
        final String email = editTextEmail.getText().toString().trim();
        final String username = editTextUsername.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();


        progressDialog.setMessage("Registering user...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                Constants.URL_REGISTER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.hide();
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };


        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);


    }

    @Override
    public void onClick(View view) {
        if (view == buttonRegister){
        registerUser();
        editTextEmail.getText().clear();
        editTextPassword.getText().clear();
        editTextUsername.getText().clear();
            hideSoftKeyboard(RegisterActivity.this);
            editTextEmail.clearFocus();
            editTextPassword.clearFocus();
            editTextUsername.clearFocus();
        }

        if(view == textViewLogin)
            startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(0, 0);
    }

    public boolean validateEmail(){
        String emailInput = editTextEmail.getText().toString().trim();

        if (emailInput.isEmpty()){
            editTextEmail.setError("Field cannot be Empty");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()){
            editTextEmail.setError("Please enter valid email");
            return false;
        } else {
            return true;
        }
    }

    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }


    public static void hideSoftKeyboard(RegisterActivity activity) {
        if (activity == null) return;
        if (activity.getCurrentFocus() == null) return;

        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

}


