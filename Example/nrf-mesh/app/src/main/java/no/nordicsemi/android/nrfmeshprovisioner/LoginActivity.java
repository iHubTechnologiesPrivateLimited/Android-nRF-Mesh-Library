package no.nordicsemi.android.nrfmeshprovisioner;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final String URL_FOR_LOGIN = "http://192.168.1.102:5000/userlogin/";
    ProgressDialog progressDialog;
    private EditText loginInputEmail, loginInputPassword;
    private Button btnlogin;
    private Button btnLinkSignup;
    SharedPreferences sharedpreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginInputEmail = (EditText) findViewById(R.id.login_input_email);
        loginInputPassword = (EditText) findViewById(R.id.login_input_password);
        btnlogin = (Button) findViewById(R.id.btn_login);
        btnLinkSignup = (Button) findViewById(R.id.btn_link_signup);
        // Progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("click", "in onclick");
               // Log.d(TAG, "onClick: in onclick");
              //  progressDialog.setMessage("Register Request email: " + loginInputEmail.getText().toString()+"Register Request password: " + loginInputPassword.getText().toString());
              //  showDialog();

               loginUser(loginInputEmail.getText().toString(),
                       loginInputPassword.getText().toString());
            }
        });

        btnLinkSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(i);
            }
        });
    }

    private void loginUser( final String email, final String password) {
        // Tag used to cancel the request
        Log.d(TAG, "Register Request email: " + email);
        Log.d(TAG, "Register Request password: " + password);
        String cancel_req_tag = "login";
        progressDialog.setMessage("Logging you in...");
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                URL_FOR_LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
               // progressDialog.setMessage("onResponse: "+response.toString());
                //showDialog();
                Log.d(TAG, "Register Response: " + response.toString());
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                   // boolean error = jObj.getBoolean("error");
                    //if (Integer.parseInt(jObj.getString("status_code")) == 200)
                    if (Integer.parseInt(jObj.getString("status_code")) == 200) {

                        // get profile data from json object(jObj)
                        // the idea is to save profile data to shared preferences
                        // and call them when ever needed
                        String email = jObj.getString("email");
                        String phone = jObj.getString("phone");
                        String id = jObj.getString("id");
                        String name = jObj.getString("full_name");
                        String profileData = response.toString();
                        if(!email.equals("") && !phone.equals("")) {
                         sharedpreferences = getSharedPreferences("profilepref", Context.MODE_PRIVATE);
                         SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putString("name", name);
                            editor.putString("phone", phone);
                            editor.putString("email", email);
                            editor.putString("id", id);
                            editor.putString("profile", profileData);
                            editor.commit();
                            // Launch Main activity
                            Intent intent = new Intent(
                                    LoginActivity.this,
                                    MainActivity.class);

                            startActivity(intent);
                            finish();
                        }

                    } else {


                        String errorMsg = jObj.getString("error");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {

                    Toast.makeText(getApplicationContext(),
                            e.getMessage(), Toast.LENGTH_LONG).show();

                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("password", password);
                return params;
            }

        };
        // Adding request to request queue
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(strReq,cancel_req_tag);
    }

    private void showDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }
    private void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

}




