package ru.uspu.rgo_image_test;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

public class Results extends Activity {

    ArrayList<Test> testsModel = new ArrayList<>();
    testAdapter anAdapter;
    testHolder holder;
    String studentToken;
    String testToken;
    String testTitle;
    int correctQuestionsCount=0;
    int totalQuestionsCount=0;
    String wrongQuestions="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        Button goHome = findViewById(R.id.go_home);
        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(Results.this, MainActivity.class);
                startActivity(intent);
            }
        });

        ListView testsList = findViewById(R.id.test_list_result);

        studentToken=getIntent().getStringExtra("studentToken");

        anAdapter = new testAdapter();
        testsList.setAdapter(anAdapter);

        new Thread(getTests).start();

        Button refreshTests = findViewById(R.id.refresh_tests_results);
        refreshTests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                NetworkChecker checker = new NetworkChecker(Results.this);
                if (checker.isNetworkAvailable())
                {
                    new Thread(getTests).start();
                }
                else
                {
                    Toast.makeText(Results.this, "Отсутствует подключение к Интернету", Toast.LENGTH_SHORT).show();
                }
            }
        });

        testsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                NetworkChecker checker = new NetworkChecker(Results.this);
                if (checker.isNetworkAvailable())
                {
                    testToken = testsModel.get(i).testToken;
                    new Thread(getTestCorrectInfo).start();
                }
                else
                {
                    Toast.makeText(Results.this, "Отсутствует подключение к Интернету", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public class testAdapter extends ArrayAdapter<Test>
    {
        public testAdapter() {
            super(Results.this, R.layout.test_item, testsModel);
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
        private TextView test_id;
        private TextView test_description;

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


    public Runnable getTests = new Runnable()
    {
        @Override
        public void run() {

            byte[] data = null;
            //String params = "nickname="+nickname+"&testToken="+testToken;
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


    public Runnable getTestCorrectInfo = new Runnable()
    {
        @Override
        public void run() {

            byte[] data = null;
            String params = "studentToken="+studentToken+"&testToken="+testToken;
            try {
                URL url = new URL("http://rgo-image.xn--100-5cdnry0bhchmgqi5d.xn--p1ai/php/getResults.php");
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
                        testTitle=myJSONObject.getString("testTitle");
                        correctQuestionsCount = myJSONObject.getInt("rightAnswersCount");
                        totalQuestionsCount = myJSONObject.getInt("questionsCount");
                        wrongQuestions = myJSONObject.getString("wrong_questions");

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
                    TextView currentCorrectAnswersCount = findViewById(R.id.current_correct_answers_count);
                    currentCorrectAnswersCount.setText(Integer.toString(correctQuestionsCount));

                    TextView currentTotalAnswersCount = findViewById(R.id.current_total_answers_count);
                    currentTotalAnswersCount.setText(Integer.toString(totalQuestionsCount));

                    TextView currentTest = findViewById(R.id.current_test);
                    currentTest.setText(testTitle);

                    TextView wrong_questions = findViewById(R.id.wrong_questions);
                    wrong_questions.setText(wrongQuestions);
                }
            });
        }
    };
}
