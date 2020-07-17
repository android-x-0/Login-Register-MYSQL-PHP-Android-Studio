package com.example.eshoperone.Main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.eshoperone.R;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.android.volley.VolleyLog.TAG;

public class EditprofileActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText edit_name, edit_email, edit_phonenumber, edit_age, edit_address;
    private Button save , back , btn_edit_photo;
    String getId = "";
    private ProgressDialog progressDialog;
    private Bitmap bitmap;
    private CircleImageView imageView;
    private Spinner spinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprofile);

        edit_name = findViewById(R.id.edit_name);
        edit_email = findViewById(R.id.edit_email);
        edit_phonenumber = findViewById(R.id.edit_phonenumber);
        edit_age = findViewById(R.id.edit_age);
        spinner = findViewById(R.id.edit_gender);
        edit_address = findViewById(R.id.edit_address);
        imageView = findViewById(R.id.edit_image);


        btn_edit_photo = findViewById(R.id.btn_edit_photo);
        save  = findViewById(R.id.save);
        back = findViewById(R.id.back);

        back.setOnClickListener(this);
        save.setOnClickListener(this);
        btn_edit_photo.setOnClickListener(this);


        progressDialog = new ProgressDialog(this);
        getId = String.valueOf(SharedPrefManager.getInstance(this).getUserId());

     //   getSupportActionBar().setTitle("Edit Profile");
     //   getSupportActionBar().hide();

        addListenerOnSpinnerItemSelection();
    }

    public void addListenerOnSpinnerItemSelection() {
        spinner = (Spinner) findViewById(R.id.edit_gender);
        spinner.setOnItemSelectedListener(new CustomOnItemSelectedListener());
    }

    @Override
    public void onClick(View v) {
        if(v == save){
            save();
            finish();
        }
        if (v == back){
           finish();
        }

        if (v == btn_edit_photo){
            chooseFile();
        }
    }

    private void save() {
        final String username = edit_name.getText().toString().trim();
        final String email = edit_email.getText().toString().trim();
        final String phonenumber = edit_phonenumber.getText().toString().trim();
        final String age = edit_age.getText().toString().trim();
        final String address = edit_address.getText().toString().trim();
        final String gender = String.valueOf(spinner.getSelectedItem());
        final String id = String.valueOf(SharedPrefManager.getInstance(this).getUserId());

        progressDialog.setMessage("Saving...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                Constants.UPDATE_INFO,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Toast.makeText(EditprofileActivity.this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.hide();
                        Toast.makeText(EditprofileActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("email", email);
                params.put("phonenumber", phonenumber);
                params.put("age", age);
                params.put("address", address);
                params.put("gender", gender);
                params.put("id", id);
                return params;
            }


        };


        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);


    }

    private void getUserDetail(){

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                Constants.URL_READ_DETAILS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        Log.i(TAG, response);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String success = jsonObject.getString("success");
                            JSONArray jsonArray = jsonObject.getJSONArray("read");

                            if (success.equals("1")){

                                for (int i =0; i < jsonArray.length(); i++){

                                    JSONObject object = jsonArray.getJSONObject(i);

                                    String strName = object.getString("username").trim();
                                    String strEmail = object.getString("email").trim();
                                    String strPhoneNumber = object.getString("phonenumber");
                                    String strAge = object.getString("age");
                                   String strGender = object.getString("gender");
                                    String strAddress = object.getString("address");
                                    //Add this for fetch image from json
                                    String strImage = object.getString("photo").trim();

                                    edit_name.setText(strName);
                                    edit_email.setText(strEmail);
                                    edit_phonenumber.setText(strPhoneNumber);
                                    edit_age.setText(strAge);
                                    edit_address.setText(strAddress);

                                    String compareValue = strGender;
                                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(EditprofileActivity.this, R.array.Gender, android.R.layout.simple_spinner_item);
                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    spinner.setAdapter(adapter);
                                    if (compareValue != null) {
                                        int spinnerPosition = adapter.getPosition(compareValue);
                                        spinner.setSelection(spinnerPosition);
                                    }
                                    String image = Constants.URL + strImage;
                                    //display image from string url
                                    Picasso.get().load(image).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(imageView);
                                }

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                            Toast.makeText(EditprofileActivity.this, "Error Reading Detail "+e.toString(), Toast.LENGTH_SHORT).show();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(EditprofileActivity.this, "Error Reading Detail "+error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String > params = new HashMap<>();
                params.put("id", getId);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    @Override
    public void onResume() {
        super.onResume();
        getUserDetail();
    }

    private void chooseFile(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select picture"), 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data !=null && data.getData() != null){
            Uri filepath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getBaseContext().getContentResolver(),filepath );
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            UploadPicture(String.valueOf(SharedPrefManager.getInstance(this).getUserId()), getStringImage(bitmap));
        }
    }

    private void UploadPicture(final String id, final String photo) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                Constants.URL_UPLOAD_IMAGE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Toast.makeText(EditprofileActivity.this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.hide();
                        Toast.makeText(EditprofileActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String , String> params = new HashMap<>();
                params.put("id", String.valueOf(SharedPrefManager.getInstance(getApplicationContext()).getUserId()));
                params.put("photo", photo);

                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public String getStringImage(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        byte[] imageByteArray = byteArrayOutputStream.toByteArray();
        String encodedImage = Base64.encodeToString(imageByteArray, Base64.DEFAULT);
        return encodedImage;
    }




}
