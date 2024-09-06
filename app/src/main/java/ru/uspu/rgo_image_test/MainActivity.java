package ru.uspu.rgo_image_test;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MainActivity extends Activity {

    String userNickname ="";
    String nicknameToken="";
    String testToken;
    String testTitle="";
    String response="";

    SharedPreferences sPref;

    ArrayList<Test> testsModel = new ArrayList<>();
    testAdapter anAdapter;
    testHolder holder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText nicknameInput = findViewById(R.id.nickname);

        sPref = getPreferences(MODE_PRIVATE);
        userNickname = sPref.getString("nickname", "");
        nicknameInput.setText(userNickname);

        Button create_nickname_button = findViewById(R.id.create_nickname_button);
        create_nickname_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CreateNickname.class);
                startActivity(intent);
            }
        });

        new Thread(getTests).start();

        Button refreshTests = findViewById(R.id.refresh_tests);
        refreshTests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                NetworkChecker checker = new NetworkChecker(MainActivity.this);
                if (checker.isNetworkAvailable())
                {
                    new Thread(getTests).start();
                }
                else
                {
                    Toast.makeText(MainActivity.this,"Отсутствует подключение к Интернету",Toast.LENGTH_SHORT).show();
                }
            }
        });

        ListView testsList = findViewById(R.id.test_list);

        anAdapter = new testAdapter();
        testsList.setAdapter(anAdapter);

        testsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                NetworkChecker checker = new NetworkChecker(MainActivity.this);
                if (checker.isNetworkAvailable())
                {
                    if (testsModel.size() > 0)
                    {
                        EditText nicknameInput = findViewById(R.id.nickname);
                        userNickname = nicknameInput.getText().toString().trim();
                        nicknameInput.setText(userNickname);
                        String nicknameToCheck = userNickname.replaceAll(" ","");
                        if (!nicknameToCheck.equals("")) {
                            sPref = getPreferences(MODE_PRIVATE);
                            SharedPreferences.Editor ed = sPref.edit();
                            ed.putString("nickname", userNickname);
                            ed.commit();

                            testToken = testsModel.get(i).testToken;
                            testTitle = testsModel.get(i).testTitle;
                            new Thread(runTest).start();
                        }
                        else
                        {
                            Toast.makeText(MainActivity.this, "Введите псевдоним", Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, "Тесты не загружены", Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Отсутствует подключение к Интернету", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public Runnable getTests = new Runnable()
    {
        @Override
        public void run() {

            byte[] data = null;
            try {
                URL url = new URL("http://rgo-image.xn--100-5cdnry0bhchmgqi5d.xn--p1ai/php/getTests.php");
                try {

                    URLConnection connection = url.openConnection();
                    HttpURLConnection httpConnection = (HttpURLConnection) connection;
                    httpConnection.setConnectTimeout(10000);
                    httpConnection.setReadTimeout(10000);
                    httpConnection.setRequestMethod("GET");
                    // Open communications link (network traffic occurs here).
                    //httpConnection.connect();
                    // give it 15 seconds to respond

                    connection.setDoOutput(true);
                    connection.setDoInput(true);

                    //connection.setRequestProperty("Content-Length", "" + Integer.toString(params.getBytes().length));
                    OutputStream os = connection.getOutputStream();
                    //data = params.getBytes("UTF-8");
                    //os.write(data);

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
                        JSONArray testsJSON = myJSONObject.getJSONArray("tests");


                        testsModel= new ArrayList<>();
                        for(int i=0;i<testsJSON.length();i++)
                        {
                            testsModel.add(
                                    new Test
                                            (
                                                    testsJSON.getJSONObject(i).getString("token"),
                                                    testsJSON.getJSONObject(i).getString("id"),
                                                    testsJSON.getJSONObject(i).getString("title"),
                                                    testsJSON.getJSONObject(i).getString("description")
                                            ));
                        }

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
                    try
                    {
                        anAdapter.clear();
                        int length = testsModel.size();
                        for (int i = 0; i < length; i++)
                        {
                            anAdapter.add(testsModel.get(i));
                        }
                    }
                    catch (Exception e) {
                    }
                }
            });
        }
    };


    //


    public Runnable runTest = new Runnable()
    {
        @Override
        public void run() {

            byte[] data = null;
            String params = "nickname="+ userNickname;
            try {
                URL url = new URL("http://rgo-image.xn--100-5cdnry0bhchmgqi5d.xn--p1ai/php/checkAccess.php");
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

                        nicknameToken = myJSONObject.optString("nicknameToken");

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
                    if (nicknameToken.equals(""))
                    {
                        Toast.makeText(MainActivity.this,"Пользователь не найден",Toast.LENGTH_LONG).show();
                    }
                    else
                        {
                        Intent intent = new Intent(MainActivity.this, Questions.class);
                        intent.putExtra("nicknameToken", nicknameToken);
                        intent.putExtra("nickname", userNickname);
                        intent.putExtra("testToken", testToken);
                        intent.putExtra("testTitle", testTitle);
                        startActivity(intent);
                    }
                }
            });
        }
    };


    public class testAdapter extends ArrayAdapter<Test>
    {
        public testAdapter() {
            super(MainActivity.this, R.layout.test_item, testsModel);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            int pos =position;
            int maxPosition = testsModel.size()-1;

            if (pos>maxPosition)
            {
                pos=maxPosition;
            }

            View row = convertView;
            holder = null;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.test_item, parent, false);
                holder = new testHolder(row);
                row.setTag(holder);
            } else {
                holder = (testHolder) row.getTag();
            }
            holder.PopulateFrom(testsModel.get(pos));
            return row;
        }
    }

    public class testHolder {
        private TextView test_title;
        private TextView test_description;
        private TextView test_id;

        public testHolder(View row) {
            test_title = (TextView) row.findViewById(R.id.test_title);
            test_id = (TextView) row.findViewById(R.id.test_id);
            test_description = (TextView) row.findViewById(R.id.test_description);
        }

        void PopulateFrom(Test r)
        {
            test_title.setText(r.getTestTitle());
            test_id.setText(String.valueOf(r.getTestId()));
            test_description.setText(r.getTestDescription());
        }
    }

}