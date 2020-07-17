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
import android.widget.Button;
import android.widget.TextView;
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

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener{

    TextView username_view, email_view , title;
    TextView edit, save , btn_edit_photo;
    TextView age, address,phonenumber,gender , Location;
    CircleImageView profile_view;
    String getId = "";
    Button back_mn;

    private ProgressDialog progressDialog;
    private static final int RESULT_LOAD_IMAGE = 1;
    private Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        username_view = (TextView) findViewById(R.id.username_view);
        title = (TextView) findViewById(R.id.title);
        email_view = (TextView) findViewById(R.id.email_view);
        back_mn = (Button) findViewById (R.id.back_mn);
        back_mn.setOnClickListener(this);
        Location = (TextView) findViewById(R.id.Location);
        edit = (TextView) findViewById(R.id.edit);
        profile_view = (CircleImageView) findViewById(R.id.profile_view);
        btn_edit_photo = (Button) findViewById(R.id.btn_edit_photo);
        age = (TextView) findViewById(R.id.age);
        address = (TextView) findViewById(R.id.address);
        phonenumber = (TextView) findViewById(R.id.phonenumber);
        gender = (TextView) findViewById(R.id.gender);



        getId = String.valueOf(SharedPrefManager.getInstance(this).getUserId());

        edit.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);

    }

    public void onClick(View v) {
        if (v == edit) {
            Intent intent = new Intent(this, EditprofileActivity.class);
            startActivity(intent);
        }

        if (v == back_mn){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
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
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),filepath );
                profile_view.setImageBitmap(bitmap);
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
                            Toast.makeText(ProfileActivity.this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.hide();
                        Toast.makeText(ProfileActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String , String> params = new HashMap<>();
                params.put("id", String.valueOf(SharedPrefManager.getInstance(ProfileActivity.this).getUserId()));
                params.put("photo", photo);

                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    public String getStringImage(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        byte[] imageByteArray = byteArrayOutputStream.toByteArray();
        String encodedImage = Base64.encodeToString(imageByteArray, Base64.DEFAULT);
        return encodedImage;
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
                                    String strAge = object.getString("age").trim();
                                    String strAddress = object.getString("address").trim();
                                    String strPhoneNumber = object.getString("phonenumber").trim();
                                    String strGender = object.getString("gender").trim();
                                    String strLocation = object.getString("address").trim();
                                    //Add this for fetch image from json
                                    String strImage = object.getString("photo").trim();

                                    username_view.setText(strName);
                                    email_view.setText(strEmail);
                                    age.setText(strAge);
                                    address.setText(strAddress);
                                    phonenumber.setText(strPhoneNumber);
                                    gender.setText(strGender);
                                    Location.setText(strLocation);
                                    title.setText("Hello , "+strName);

                                    String image = Constants.URL + strImage;
                                    //display image from string url
                                    Picasso.get().load(image).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(profile_view);
                                }

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                            Toast.makeText(ProfileActivity.this, "Error Reading Detail "+e.toString(), Toast.LENGTH_SHORT).show();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this, "Error Reading Detail "+error.toString(), Toast.LENGTH_SHORT).show();
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

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);

    }

    @Override
    public void onResume() {
        super.onResume();
        getUserDetail();
    }



}//end
