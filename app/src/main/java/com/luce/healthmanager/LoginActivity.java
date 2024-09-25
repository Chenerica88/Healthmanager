package com.luce.healthmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerButton;
    // 定義 LinearLayout 變量來表示自定義的按鈕
    LinearLayout googleLoginButton, facebookLoginButton, lineLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // 登入界面
        Button registerButton = findViewById(R.id.register_button);

        // 初始化按鈕
        googleLoginButton = findViewById(R.id.google_login_button);
        facebookLoginButton = findViewById(R.id.facebook_login_button);
        lineLoginButton = findViewById(R.id.line_login_button);

        usernameEditText = findViewById(R.id.username_input);
        passwordEditText = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳轉到 RegisterActivity
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "請輸入用戶名和密碼", Toast.LENGTH_SHORT).show();
            } else {
                new LoginTask().execute(username, password);
            }
        });
        // Google 登入按鈕點擊事件
        googleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 這裡放置 Google 登入邏輯

            }
        });

        // Facebook 登入按鈕點擊事件
        facebookLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 這裡放置 Facebook 登入邏輯

            }
        });

        // Line 登入按鈕點擊事件
        lineLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 這裡放置 Line 登入邏輯

            }
        });
    }

    private class LoginTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            String password = params[1];
            String result = null;

            try {
                URL url = new URL("http://192.168.50.38:8080/HealthcareManager/api/auth/login");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                String jsonInputString = "{\"username\":\"" + username + "\", \"password\":\"" + password + "\"}";

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int statusCode = connection.getResponseCode(); // 獲取狀態碼
                BufferedReader br;
                if (statusCode == HttpURLConnection.HTTP_OK) { // 200
                    br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
                } else {
                    br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"));
                }

                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                result = response.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);

                    // 檢查響應中是否包含token
                    if (jsonResponse.has("token")) {
                        String token = jsonResponse.getString("token");
                        Toast.makeText(LoginActivity.this, "登入成功", Toast.LENGTH_SHORT).show();
                        // 保存 token 到 SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("jwt_token", token);
                        editor.apply();

                        // 使用公共的 ParseTokenTask
                        new ParseTokenTask(LoginActivity.this, new ParseTokenTask.ParseTokenCallback() {
                            @Override
                            public void onParseTokenCompleted(JSONObject userData) {
                                if (userData != null) {
                                    try {
                                        String username = userData.getString("username");
                                        String userId = userData.getString("id");

                                        // 保存用户数据到 SharedPreferences
                                        editor.putString("username", username);
                                        editor.putString("userId", userId);
                                        editor.apply();

                                        // 跳转到 MainActivity
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        intent.putExtra("showHealthFragment", true);
                                        startActivity(intent);
                                        finish();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Toast.makeText(LoginActivity.this, "解析用戶數據出錯", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(LoginActivity.this, "解析 token 失敗", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).execute(token);

                    } else {
                        // 如果返回中不包含token，顯示錯誤訊息
                        String message = jsonResponse.getString("message");
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(LoginActivity.this, "解析返回訊息時出錯", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // Google 登入處理邏輯
    private void googleSignIn() {
        // 這裡實作 Google 登入邏輯，例如使用 GoogleSignInClient
        Toast.makeText(LoginActivity.this, "Google 登入", Toast.LENGTH_SHORT).show();
    }

    // Facebook 登入處理邏輯
    private void facebookSignIn() {
        // 這裡實作 Facebook 登入邏輯
        Toast.makeText(LoginActivity.this, "Facebook 登入", Toast.LENGTH_SHORT).show();
    }

    // 這裡可以實作一個方法，將用戶資料透過 API 送至後端伺服器
    // private void sendUserDataToServer(String username, String password, String email, String phone, String birthday) {
    //     // 實作與後端 API 的連接
    // }
}
