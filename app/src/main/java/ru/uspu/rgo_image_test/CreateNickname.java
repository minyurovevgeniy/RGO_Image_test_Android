package ru.uspu.rgo_image_test;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class CreateNickname extends Activity {

    String nickname="";
    String response="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_nickname);

        Button goHome = findViewById(R.id.go_start_screen);
        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(CreateNickname.this, MainActivity.class);
                startActivity(intent);
            }
        });

        Button create_nickname = findViewById(R.id.create_nickname);
        create_nickname.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                EditText nicknameInput = findViewById(R.id.new_nickname);
                nickname= nicknameInput.getText().toString();

                NetworkChecker checker = new NetworkChecker(CreateNickname.this);
                if (checker.isNetworkAvailable())
                {
                    new Thread(createNickname).start();
                }
                else
                {
                    Toast.makeText(CreateNickname.this,"Отсутствует подключение к Интернету", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }


    public Runnable createNickname = new Runnable()
    {
        @Override
        public void run() {

            byte[] data = null;
            String params = "student="+nickname;
            try {
                URL url = new URL("http://rgo-image.xn--100-5cdnry0bhchmgqi5d.xn--p1ai/php/createStudent.php");
                try {

                    URLConnection connection = url.openConnection();
                    HttpURLConnection httpConnection = (HttpURLConnection) connection;
                    httpConnection.setConnectTimeout(10000);
                    httpConnection.setReadTimeout(10000);
                    httpConnection.setRequestMethod("POST");
                    // Open communications link (network traffic occurs here).
                    //httpConnection.connect();
                    // give it 15 seconds to respond

                    connection.setDoOutput(true);
                    connection.setDoInput(true);

                    connection.setRequestProperty("Content-Length", "" + Integer.toString(params.getBytes().length));
                    OutputStream os = connection.getOutputStream();
                    data = params.getBytes("UTF-8");
                    os.write(data);

                    //if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK)
                    //{
                    try
                    {
                        // read the output from the server

                        BufferedReader reader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
                        StringBuffer stringBuilder = new StringBuffer();

                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            stringBuilder.append(line);
                        }

                        JSONObject myJSONObject = new JSONObject(stringBuilder.toString());
                        response = myJSONObject.getString("response");

                    }
                    catch (Exception e)
                    {
                    }
                } catch (IOException r)
                {
                    r.getMessage();
                }
            } catch (MalformedURLException e)
            {
                e.getMessage();
            }
            finally {}

            runOnUiThread(new Runnable() {
                @Override
                public void run()
                {
                    Toast.makeText(CreateNickname.this,response,Toast.LENGTH_LONG).show();
                }
            });
        }
    };
}
