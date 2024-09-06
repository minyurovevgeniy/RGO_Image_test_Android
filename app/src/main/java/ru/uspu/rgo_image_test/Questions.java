package ru.uspu.rgo_image_test;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import static android.graphics.Bitmap.createBitmap;

import static android.graphics.Bitmap.createBitmap;

public class Questions extends Activity {

    String nickname;
    int currentQuestionOrder =0;
    int questionType;
    String uri;

    String questionOpenVariant="";
    String questionText="";
    String rawJSON="";
    String testToken="";
    String studentToken="";

    String questionCount="";

    ArrayList<String> variantsArray= new ArrayList<>();
    ArrayList<String> answers= new ArrayList<>();

    RadioGroup rg;
    EditText myEditText;
    ArrayList<Bitmap> images = new ArrayList<>();
    int nextQuestion=0;
    Bitmap questionOpenImage;
    String questionOpenAnswer="";

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_questions);



        final LinearLayout layout = findViewById(R.id.questions);
        layout.post(new Runnable()
        {
            @Override
            public void run()
            {
                layout.setOnTouchListener(new OnSwipeTouchListener(Questions.this)
                {
                    public void onSwipeRight()
                    {
                        new Thread(getBackQuestion).start();
                    }
                    public void onSwipeLeft()
                    {
                        new Thread(getForwardQuestion).start();
                    }
                });
            }
        });

        /*
        Button backMove = findViewById(R.id.back);
        backMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(getBackQuestion).start();
            }
        });


        Button forwardMove = findViewById(R.id.forward);
        forwardMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                new Thread(getForwardQuestion).start();
            }
        });
        */
        TextView test_heading = findViewById(R.id.test_heading);

        rg= (RadioGroup) findViewById(R.id.single_answer_container);

        nickname=getIntent().getStringExtra("nickname");
        //nicknameToken=getIntent().getStringExtra("nicknameToken");

        studentToken=getIntent().getStringExtra("nicknameToken");
        testToken=getIntent().getStringExtra("testToken");
        test_heading.setText(getIntent().getStringExtra("testTitle"));

        Button show_result = findViewById(R.id.show_result);
        show_result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Questions.this, Results.class);
                intent.putExtra("studentToken", studentToken);
                intent.putExtra("testToken", testToken);
                new Thread(getForwardQuestion).start();
                startActivity(intent);
            }
        });

        new Thread(getFirstQuestion).start();
    }




    public Runnable getForwardQuestion = new Runnable()
    {
        @Override
        public void run() {
            byte[] data = null;
            String answer="";
            if (questionType==1)
            {
                answer=String.valueOf(rg.getCheckedRadioButtonId());
            }
            else
            {
                if (questionType==2)
                {

                    StringBuilder strBulider= new StringBuilder();
                    LinearLayout container = findViewById(R.id.container);
                    int childrenCount=container.getChildCount();
                    {
                        for (int i=0;i<childrenCount;i++)
                        {
                            LinearLayout ll = (LinearLayout) container.getChildAt(i);

                            CheckBox chBox = (CheckBox)ll.getChildAt(0);
                            if (chBox.isChecked())
                            {
                                strBulider.append(chBox.getId()+"_");
                            }

                        }
                    }
                    answer=strBulider.toString();
                }
                else
                {
                    answer=myEditText.getText().toString();
                }
            }

            String params = "test="+testToken+"&student="+studentToken+"&student_answer="+answer+"&question_type="+questionType+"&current_question="+ currentQuestionOrder;
            try {
                URL url = new URL("http://rgo-image.xn--100-5cdnry0bhchmgqi5d.xn--p1ai/php/getForwardQuestion.php");
                try {

                    URLConnection connection = url.openConnection();
                    HttpURLConnection httpConnection = (HttpURLConnection) connection;
                    httpConnection.setConnectTimeout(100000);
                    httpConnection.setReadTimeout(100000);
                    httpConnection.setRequestMethod("POST");

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
                        while ((line = reader.readLine()) != null)
                        {
                            stringBuilder.append(line);
                        }

                        rawJSON = stringBuilder.toString();
                        Log.d("json",rawJSON);
                        JSONObject myJSONObject = new JSONObject(stringBuilder.toString());
                        questionText = myJSONObject.getString("question_text");
                        currentQuestionOrder = myJSONObject.getInt("question_order");
                        questionType = myJSONObject.getInt("question_type");

                        images.clear();
                        if (questionType==3)
                        {
                            questionOpenAnswer=myJSONObject.getString("answer");

                            questionOpenImage=createBitmap(downloadBitmap(myJSONObject.getString("variants")));
                        }
                        else
                        {
                            answers.clear();
                            variantsArray.clear();
                            JSONArray jArray = myJSONObject.getJSONArray("answer");
                            for (int i = 0; i < jArray.length(); i++)
                            {
                                answers.add(jArray.getString(i));
                            }


                            JSONArray jsonArray = myJSONObject.getJSONArray("variants");
                            for (int i = 0; i < jsonArray.length(); i++)
                            {
                                variantsArray.add(jsonArray.getString(i));
                                uri = jsonArray.getString(i);
                                images.add(createBitmap(downloadBitmap(uri.split("__")[1])));
                            }
                        }
                    }
                    catch (Exception e) {
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
                public void run() {
                    try {

                        TextView question = findViewById(R.id.question);
                        question.setText("Вопрос №"+ currentQuestionOrder +"/"+questionCount+" "+questionText);
                        LinearLayout container = findViewById(R.id.container);
                        container.removeAllViews();
                        String[] idAndText;
                        rg.removeAllViewsInLayout();


                        Bitmap image=null;
                        ImageView imageView = null;
                        if (questionType==1)
                        {
                            rg.removeAllViewsInLayout();
                            RadioButton[] rb = new RadioButton[variantsArray.size()];
                            rg.setOrientation(RadioGroup.VERTICAL);

                            for(int i=0; i<variantsArray.size(); i++)
                            {
                                LinearLayout linearLayout = new LinearLayout(Questions.this);
                                linearLayout.setOrientation(LinearLayout.HORIZONTAL);

                                rb[i] = new RadioButton(Questions.this);
                                //rb[i].setBackgroundResource(R.drawable.radio_button);
                                rb[i].setButtonDrawable(R.drawable.radio_button);
                                idAndText = variantsArray.get(i).split("__");
                                rb[i].setId(Integer.parseInt(idAndText[0]));
                                imageView = new ImageView(Questions.this);
                                imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                imageView.setImageBitmap(images.get(i));

                                for (int k = 0; k < answers.size(); k++)
                                {
                                    if (answers.get(k).equals(idAndText[0]))
                                    {
                                        rb[i].setChecked(true);
                                    }
                                }

                                /*
                                linearLayout.addView(rb[i]);
                                linearLayout.addView(imageView);
                                rg.addView(linearLayout);
                                 */

                                rg.addView(imageView);
                                rg.addView(rb[i]);
                            }
                        }
                        else
                        {
                            if (questionType==2)
                            {
                                container.removeAllViews();
                                for(int i = 0; i < variantsArray.size(); i++)
                                {
                                    LinearLayout linearLayout = new LinearLayout(Questions.this);
                                    linearLayout.setOrientation(LinearLayout.HORIZONTAL);

                                    CheckBox cb = new CheckBox(Questions.this);
                                    idAndText=variantsArray.get(i).split("__");

                                    cb.setId(Integer.parseInt(idAndText[0]));
                                    image = images.get(i);
                                    imageView = new ImageView(Questions.this);
                                    imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    imageView.setImageBitmap(image);
                                    for (int k=0;k<answers.size();k++)
                                    {
                                        if (String.valueOf(cb.getId()).equals(answers.get(k)))
                                        {
                                            cb.setChecked(true);
                                        }
                                    }
                                    linearLayout.addView(cb);
                                    linearLayout.addView(imageView);
                                    container.addView(linearLayout);
                                }
                            }
                            else
                            {
                                container.removeAllViews();
                                LinearLayout linearLayout = new LinearLayout(Questions.this);
                                linearLayout.setOrientation(LinearLayout.VERTICAL);
                                LinearLayout.LayoutParams mRparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                linearLayout.setLayoutParams(mRparams);
                                myEditText = new EditText(Questions.this);

                                myEditText.setLayoutParams(mRparams);
                                myEditText.setText(questionOpenAnswer);
                                ImageView imageOpen = new ImageView(Questions.this);
                                imageOpen.setImageBitmap(questionOpenImage);
                                imageOpen.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                                linearLayout.addView(imageOpen);
                                linearLayout.addView(myEditText);
                                container.addView(linearLayout);
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            });
        }
    };


    public Runnable getBackQuestion = new Runnable()
    {
        @Override
        public void run() {
            byte[] data = null;
            String answer="";
            if (questionType==1)
            {
                answer=String.valueOf(rg.getCheckedRadioButtonId());
            }
            else
            {
                if (questionType==2)
                {

                    StringBuilder strBulider= new StringBuilder();
                    LinearLayout container = findViewById(R.id.container);
                    int childrenCount=container.getChildCount();
                    {
                        for (int i=0;i<childrenCount;i++)
                        {
                            LinearLayout ll = (LinearLayout) container.getChildAt(i);

                            CheckBox chBox = (CheckBox)ll.getChildAt(0);
                            if (chBox.isChecked())
                            {
                                strBulider.append(chBox.getId()+"_");
                            }

                        }
                    }
                    answer=strBulider.toString();
                }
                else
                {
                    answer=myEditText.getText().toString();
                }
            }

            String params = "test="+testToken+"&student="+studentToken+"&student_answer="+answer+"&question_type="+questionType+"&current_question="+ currentQuestionOrder;
            try {
                URL url = new URL("http://rgo-image.xn--100-5cdnry0bhchmgqi5d.xn--p1ai/php/getBackQuestion.php");
                try {

                    URLConnection connection = url.openConnection();
                    HttpURLConnection httpConnection = (HttpURLConnection) connection;
                    httpConnection.setConnectTimeout(100000);
                    httpConnection.setReadTimeout(100000);
                    httpConnection.setRequestMethod("POST");

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
                        while ((line = reader.readLine()) != null)
                        {
                            stringBuilder.append(line);
                        }

                        rawJSON = stringBuilder.toString();
                        Log.d("json",rawJSON);
                        JSONObject myJSONObject = new JSONObject(stringBuilder.toString());
                        questionText = myJSONObject.getString("question_text");
                        currentQuestionOrder = myJSONObject.getInt("question_order");
                        questionType = myJSONObject.getInt("question_type");

                        images.clear();
                        if (questionType==3)
                        {
                            questionOpenAnswer=myJSONObject.getString("answer");

                            questionOpenImage=createBitmap(downloadBitmap(myJSONObject.getString("variants")));
                        }
                        else
                        {
                            answers.clear();
                            variantsArray.clear();
                            JSONArray jArray = myJSONObject.getJSONArray("answer");
                            for (int i = 0; i < jArray.length(); i++)
                            {
                                answers.add(jArray.getString(i));
                            }


                            JSONArray jsonArray = myJSONObject.getJSONArray("variants");
                            for (int i = 0; i < jsonArray.length(); i++)
                            {
                                variantsArray.add(jsonArray.getString(i));
                                uri = jsonArray.getString(i);
                                images.add(createBitmap(downloadBitmap(uri.split("__")[1])));
                            }
                        }
                    }
                    catch (Exception e) {
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
                public void run() {
                    try {

                        TextView question = findViewById(R.id.question);
                        question.setText("Вопрос №"+ currentQuestionOrder +"/"+questionCount+" "+questionText);
                        LinearLayout container = findViewById(R.id.container);
                        container.removeAllViews();
                        String[] idAndText;
                        rg.removeAllViewsInLayout();


                        Bitmap image=null;
                        ImageView imageView = null;
                        if (questionType==1)
                        {
                            rg.removeAllViewsInLayout();
                            RadioButton[] rb = new RadioButton[variantsArray.size()];
                            rg.setOrientation(RadioGroup.VERTICAL);

                            for(int i=0; i<variantsArray.size(); i++)
                            {
                                LinearLayout linearLayout = new LinearLayout(Questions.this);
                                linearLayout.setOrientation(LinearLayout.HORIZONTAL);

                                rb[i] = new RadioButton(Questions.this);
                                //rb[i].setBackgroundResource(R.drawable.radio_button);
                                rb[i].setButtonDrawable(R.drawable.radio_button);
                                idAndText = variantsArray.get(i).split("__");
                                rb[i].setId(Integer.parseInt(idAndText[0]));
                                imageView = new ImageView(Questions.this);
                                imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                imageView.setImageBitmap(images.get(i));

                                for (int k = 0; k < answers.size(); k++)
                                {
                                    if (answers.get(k).equals(idAndText[0]))
                                    {
                                        rb[i].setChecked(true);
                                    }
                                }

                                rg.addView(imageView);
                                rg.addView(rb[i]);
                            }
                        }
                        else
                        {
                            if (questionType==2)
                            {
                                container.removeAllViews();
                                for(int i = 0; i < variantsArray.size(); i++)
                                {
                                    LinearLayout linearLayout = new LinearLayout(Questions.this);
                                    linearLayout.setOrientation(LinearLayout.HORIZONTAL);

                                    CheckBox cb = new CheckBox(Questions.this);
                                    idAndText=variantsArray.get(i).split("__");

                                    cb.setId(Integer.parseInt(idAndText[0]));
                                    image = images.get(i);
                                    imageView = new ImageView(Questions.this);
                                    imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    imageView.setImageBitmap(image);
                                    for (int k=0;k<answers.size();k++)
                                    {
                                        if (String.valueOf(cb.getId()).equals(answers.get(k)))
                                        {
                                            cb.setChecked(true);
                                        }
                                    }
                                    linearLayout.addView(cb);
                                    linearLayout.addView(imageView);
                                    container.addView(linearLayout);
                                }
                            }
                            else
                            {
                                container.removeAllViews();
                                LinearLayout linearLayout = new LinearLayout(Questions.this);
                                linearLayout.setOrientation(LinearLayout.VERTICAL);
                                LinearLayout.LayoutParams mRparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                linearLayout.setLayoutParams(mRparams);
                                myEditText = new EditText(Questions.this);

                                myEditText.setLayoutParams(mRparams);
                                myEditText.setText(questionOpenAnswer);
                                ImageView imageOpen = new ImageView(Questions.this);
                                imageOpen.setImageBitmap(questionOpenImage);
                                imageOpen.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                                linearLayout.addView(imageOpen);
                                linearLayout.addView(myEditText);
                                container.addView(linearLayout);
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            });
        }
    };

    public Bitmap downloadBitmap(String URL_string)
    {
        Bitmap bitmap = null;
        try
        {
            InputStream input = new java.net.URL(URL_string).openStream();
            // Decode Bitmap
            bitmap =   BitmapFactory.decodeStream(input);
        }
        catch (IOException e)
        {

        }
        return bitmap;
    }

    public Runnable getFirstQuestion = new Runnable()
    {
        @Override
        public void run() {
            byte[] data = null;
            String params = "studentToken="+studentToken+"&testToken="+testToken;
            try {
                URL url = new URL("http://rgo-image.xn--100-5cdnry0bhchmgqi5d.xn--p1ai/php/getFirstQuestion.php");
                try
                {
                    URLConnection connection = url.openConnection();
                    HttpURLConnection httpConnection = (HttpURLConnection) connection;
                    httpConnection.setConnectTimeout(100000);
                    httpConnection.setReadTimeout(100000);
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
                        while ((line = reader.readLine()) != null)
                        {
                            stringBuilder.append(line);
                        }

                        rawJSON = stringBuilder.toString();
                        Log.d("json",rawJSON);
                        JSONObject myJSONObject = new JSONObject(rawJSON);
                        questionText = myJSONObject.optString("question_text");
                        currentQuestionOrder = myJSONObject.optInt("question_order");
                        questionType = myJSONObject.optInt("question_type");
                        questionCount=myJSONObject.optString("question_count");

                        images.clear();
                        if (questionType==3)
                        {
                            questionOpenAnswer=myJSONObject.getString("answer");

                            questionOpenImage=createBitmap(downloadBitmap(myJSONObject.getString("variants")));
                        }
                        else
                        {
                            answers.clear();
                            JSONArray jArray = myJSONObject.getJSONArray("answer");
                            for (int i = 0; i < jArray.length(); i++)
                            {
                                answers.add(jArray.getString(i));
                            }


                            variantsArray.clear();
                            jArray = myJSONObject.getJSONArray("variants");
                            for (int k = 0; k < jArray.length(); k++)
                            {
                                uri = jArray.optString(k);
                                variantsArray.add(uri);
                                images.add(createBitmap(downloadBitmap(uri.split("__")[1])));
                            }
                        }
                    }
                    catch (Exception e) {
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
                public void run() {
                    try {
                        TextView question = findViewById(R.id.question);
                        question.setText("Вопрос №"+ currentQuestionOrder +"/"+questionCount+" "+questionText);
                        LinearLayout container = findViewById(R.id.container);
                        container.removeAllViews();
                        String[] idAndText;
                        rg.removeAllViewsInLayout();


                        Bitmap image = null;
                        ImageView imageView = null;
                        if (questionType == 1) {
                            rg.removeAllViewsInLayout();
                            RadioButton[] rb = new RadioButton[variantsArray.size()];

                            rg.setOrientation(RadioGroup.VERTICAL);
                            for (int i = 0; i < variantsArray.size(); i++) {
                                LinearLayout linearLayout = new LinearLayout(Questions.this);
                                linearLayout.setOrientation(LinearLayout.HORIZONTAL);

                                rb[i] = new RadioButton(Questions.this);
                                rb[i].setButtonDrawable(R.drawable.radio_button);
                                idAndText = variantsArray.get(i).split("__");
                                rb[i].setId(Integer.parseInt(idAndText[0]));
                                imageView = new ImageView(Questions.this);
                                imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                imageView.setImageBitmap(images.get(i));

                                for (int k = 0; k < answers.size(); k++) {
                                    if (answers.get(k).equals(idAndText[0])) {
                                        rb[i].setChecked(true);
                                    }
                                }

                                rg.addView(imageView);
                                rg.addView(rb[i]);
                            }
                        }
                        else
                            {
                            if (questionType == 2) {

                                container.removeAllViews();
                                for (int i = 0; i < variantsArray.size(); i++)
                                {
                                    LinearLayout linearLayout = new LinearLayout(Questions.this);
                                    linearLayout.setOrientation(LinearLayout.HORIZONTAL);

                                    CheckBox cb = new CheckBox(Questions.this);
                                    idAndText = variantsArray.get(i).split("__");

                                    cb.setId(Integer.parseInt(idAndText[0]));
                                    image = images.get(i);
                                    imageView = new ImageView(Questions.this);
                                    imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    imageView.setImageBitmap(image);
                                    for (int k = 0; k < answers.size(); k++) {
                                        if (String.valueOf(cb.getId()).equals(answers.get(k))) {
                                            cb.setChecked(true);
                                        }
                                    }
                                    linearLayout.addView(cb);
                                    linearLayout.addView(imageView);
                                    container.addView(linearLayout);
                                }
                            }
                            else
                            {
                                container.removeAllViews();
                                LinearLayout linearLayout = new LinearLayout(Questions.this);
                                linearLayout.setOrientation(LinearLayout.VERTICAL);
                                LinearLayout.LayoutParams mRparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                linearLayout.setLayoutParams(mRparams);
                                myEditText = new EditText(Questions.this);

                                myEditText.setLayoutParams(mRparams);
                                myEditText.setText(questionOpenAnswer);
                                ImageView imageOpen = new ImageView(Questions.this);
                                imageOpen.setImageBitmap(questionOpenImage);
                                imageOpen.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                                linearLayout.addView(imageOpen);
                                linearLayout.addView(myEditText);
                                container.addView(linearLayout);
                            }
                        }
                    }
                    catch (Exception e) {
                    }
                }
            });
        }
    };
}
