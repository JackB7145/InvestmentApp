package com.example.investmentapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    String apiKey = "KN4OJJ69TA7SFREQ";
    ProgressDialog progress;
    int counter = 0;
    LinearLayout container;
    String randomNumber = null;
    String realUser = "";
    String realPass = "";
    String desiredUsername = "";
    String desiredPass1 = "";
    String email = "";
    boolean loggedIn = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View v = this.getWindow().getDecorView();
        v.setSystemUiVisibility(View.INVISIBLE);
        container = (LinearLayout) findViewById(R.id.container);
        getRecentNews();
        ImageButton profileButton = findViewById(R.id.profileButton);
        profileButton.setVisibility(View.INVISIBLE);
    }

    private void getRecentNews() {
        GetNewsTask task = new GetNewsTask();
        task.execute();

        try {
            List<String> newsList = task.get();
            // Use the newsList variable here
            for (String news : newsList) {
                Button button = new Button(this);
                button.setText(news);
                button.setGravity(Gravity.CENTER_HORIZONTAL);
                button.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Get the text of the button that was clicked
                        String query = ((Button) v).getText().toString();
                        // Perform a search for the article online
                        String url = "https://www.nytimes.com/search?query="+query;
                        // Open the web page with the URL of the article
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                        }
                    });
                container.addView(button);
            }
        } catch (ExecutionException e) {
            // Handle the exception here
        } catch (InterruptedException e) {
            // Handle the interruption here
        }
    }

    private class GetNewsTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... voids) {
            String apiNews = "https://api.nytimes.com/svc/search/v2/articlesearch.json?q=stocks&api-key=BSmG5ZActGr1M7GHJdJtGWdgF0EMOspc";
            List<String> headlines = new ArrayList<>();

            try {
                URL url = new URL(apiNews);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                StringBuilder stringBuilder = new StringBuilder();

                while(line != null){
                    line = bufferedReader.readLine();
                    stringBuilder.append(line);
                }
                JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                JSONArray jsonArray = jsonObject.getJSONObject("response").getJSONArray("docs");

                for(int i=0;i<jsonArray.length();i++){
                    JSONObject object = jsonArray.getJSONObject(i);
                    String headline = object.getJSONObject("headline").getString("main");
                    headlines.add(headline);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return headlines;
        }
    }

    public void LaunchSearch(View v) {
        Intent i = new Intent(this, search_screen.class);
        setContentView(R.layout.search_screen);
    }

    public void backtoMain(View v) {
        Intent i = new Intent(this, MainActivity.class);
        setContentView(R.layout.activity_main);

        if (loggedIn) {
            ImageButton settingsButton = findViewById(R.id.imageButton4);
            settingsButton.setVisibility(View.INVISIBLE);
            ImageButton profileButton = findViewById(R.id.profileButton);
            profileButton.setVisibility(View.VISIBLE);

        }
        else{
            ImageButton settingsButton = findViewById(R.id.imageButton4);
            settingsButton.setVisibility(View.VISIBLE);
            ImageButton profileButton = findViewById(R.id.profileButton);
            profileButton.setVisibility(View.INVISIBLE);
        }
    }

    private class TickerSearchTask extends AsyncTask<String, Void, String> {

        private ProgressDialog progress;
        String quoteName;
        String quotePrice;
        String quoteDesc;

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(MainActivity.this);
            progress.setTitle("Loading");
            progress.setMessage("Please wait ...");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String ticker = params[0];
            String apiPrice = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + URLEncoder.encode(ticker) + "&apikey=" + apiKey;
            String apiOverview = "https://www.alphavantage.co/query?function=OVERVIEW&symbol=" + URLEncoder.encode(ticker) + "&apikey=" + apiKey;

            try {
                URL url1 = new URL(apiPrice);
                URL url2 = new URL(apiOverview);
                HttpURLConnection connection1 = (HttpURLConnection) url1.openConnection();
                HttpURLConnection connection2 = (HttpURLConnection) url2.openConnection();
                connection1.setRequestMethod("GET");
                connection1.setConnectTimeout(5000);
                connection1.setReadTimeout(5000);
                connection2.setRequestMethod("GET");
                connection2.setConnectTimeout(5000);
                connection2.setReadTimeout(5000);

                int responseCode1 = connection1.getResponseCode();
                if (responseCode1 != 200) {
                    throw new RuntimeException("Failed : HTTP error code : " + connection1.getResponseCode());
                }

                int responseCode2 = connection2.getResponseCode();
                if (responseCode2 != 200) {
                    throw new RuntimeException("Failed : HTTP error code : " + connection2.getResponseCode());
                }

                BufferedReader br1 = new BufferedReader(new InputStreamReader((connection1.getInputStream())));
                BufferedReader br2 = new BufferedReader(new InputStreamReader((connection2.getInputStream())));

                StringBuilder stringBuilder1 = new StringBuilder();
                String output1;

                StringBuilder stringBuilder2 = new StringBuilder();
                String output2;

                while ((output1 = br1.readLine()) != null) {
                    stringBuilder1.append(output1);
                }

                while ((output2 = br2.readLine()) != null) {
                    stringBuilder2.append(output2);
                }

                JSONObject json1 = new JSONObject(stringBuilder1.toString());
                JSONObject quote1 = json1.getJSONObject("Global Quote");
                String quotePrice1 = quote1.getString("05. price");

                JSONObject json2 = new JSONObject(stringBuilder2.toString());
                String quoteName1 = json2.getString("Name");
                String quoteDesc1 = json2.getString("Description");

                quoteName = quoteName1;
                quotePrice = quotePrice1;
                quoteDesc = quoteDesc1;

                return "";

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            progress.dismiss();

            if (result != null) {
                TextView stockpriceText = findViewById(R.id.stockpriceText);
                stockpriceText.setText("Stock Price: " + quotePrice);
                stockpriceText.setVisibility(View.VISIBLE);

                TextView stocknameText = findViewById(R.id.stocknameText);
                stocknameText.setText("Stock Name: " + quoteName);
                stocknameText.setVisibility(View.VISIBLE);

                TextView stockdescText = findViewById(R.id.stockdescText);
                stockdescText.setText("Description: "+quoteDesc);
                stockdescText.setVisibility(View.VISIBLE);

            } else {
                Toast.makeText(MainActivity.this, "An error occurred while retrieving the stock price.", Toast.LENGTH_SHORT).show();
                quoteName = null;
                quotePrice = null;
                quoteDesc = null;
            }
        }
    }

    public void login(View v) {

        EditText usernameInput = findViewById(R.id.username);
        EditText passwordInput = findViewById(R.id.password);

        String user = usernameInput.getText().toString();
        String pass = passwordInput.getText().toString();

        boolean back = false;
        String text = "";

        // to get Context
        Context context = getApplicationContext();

        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);

        passwordInput.setBackgroundColor(Color.argb(100, 255, 255, 255));
        usernameInput.setBackgroundColor(Color.argb(100, 255, 255, 255));

        if (user.equals("") || pass.equals("")) {
            // message to display
            text = "Missing Information";
            passwordInput.setBackgroundColor(Color.argb(100, 255, 255, 255));
            usernameInput.setBackgroundColor(Color.argb(100, 255, 255, 255));

            if (user.equals("")) {
                usernameInput.setBackgroundColor(Color.argb(20, 255, 0, 0));
            }

            if (pass.equals("")) {
                passwordInput.setBackgroundColor(Color.argb(20, 255, 0, 0));
            }
        }

        else if (user.equals(realUser) && pass.equals(realPass)){

            loggedIn = true;
            text = "Welcome " + user;
            back = true;
            backtoMain(null);

        }

        else {
                if (user.equals(realUser)) {
                    passwordInput.setBackgroundColor(Color.argb(20, 255, 0, 0));
                    usernameInput.setBackgroundColor(Color.argb(100, 255, 255, 255));
                }
                else {
                    usernameInput.setBackgroundColor(Color.argb(20, 255, 0, 0));
                    passwordInput.setBackgroundColor(Color.argb(100, 255, 255, 255));
                }

                // toast time duration, can also set manual value
                int duration = Toast.LENGTH_LONG;
                toast = Toast.makeText(context, text, duration);
                text = "Invalid Username or Password";

            }

        // to show the toast
        toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.show();


        if (back) {
            backtoMain(null);
        }
    }

    public void registerNow(View v) {
        Intent i = new Intent(this, register.class);
        setContentView(R.layout.activity_register);
    }

    public void toLogin(View view){
        Intent i = new Intent(this, settings.class);
        setContentView(R.layout.activity_settings);
    }

    public void submit(View v) {

        EditText email1 = (EditText) findViewById(R.id.email);
        EditText desiredUsername1 = (EditText) findViewById(R.id.desiredUsername);
        EditText desiredPassword1 = (EditText) findViewById(R.id.desiredPassword);
        EditText desiredPassword2 = (EditText) findViewById(R.id.desiredPassword2);
        EditText code1 = (EditText) findViewById(R.id.accessCode) ;
        Button button2 = (Button) findViewById(R.id.button2);
        Button signUp = (Button) findViewById(R.id.signUp);

        email = email1.getText().toString();
        desiredUsername = desiredUsername1.getText().toString();
        desiredPass1 = desiredPassword1.getText().toString();
        String desiredPass2 = desiredPassword2.getText().toString();

        email1.setBackgroundColor(Color.argb(0, 0, 0, 0)); desiredUsername1.setBackgroundColor(Color.argb(0, 0, 0, 0)); desiredPassword1.setBackgroundColor(Color.argb(0, 0, 0, 0)); desiredPassword2.setBackgroundColor(Color.argb(0, 0, 0, 0));
        if (email.equals("")){email1.setBackgroundColor(Color.argb(20, 255, 0, 0));}
        if (desiredUsername.equals("")){desiredUsername1.setBackgroundColor(Color.argb(20, 255, 0, 0));}
        if (desiredPass1.equals("")){desiredPassword1.setBackgroundColor(Color.argb(20, 255, 0, 0));}
        if (desiredPass2.equals("")){desiredPassword2.setBackgroundColor(Color.argb(20, 255, 0, 0));}
        if (!desiredPass1.equals(desiredPass2)){desiredPassword1.setBackgroundColor(Color.argb(20, 255, 0, 0)); desiredPassword2.setBackgroundColor(Color.argb(20, 255, 0, 0));}

        if (desiredUsername.equals("") || desiredPass1.equals("") || desiredPass2.equals("") || !desiredPass1.equals(desiredPass2)){
            Toast toast = Toast.makeText(getApplicationContext(), "Missing Information", Toast.LENGTH_LONG);
            toast.show();
        }


                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    // Show error message or take other appropriate action
                    email1.setBackgroundColor(Color.argb(20, 255, 0, 0));

                    Toast toast = Toast.makeText(getApplicationContext(), "Invalid Email Address", Toast.LENGTH_LONG);
                    toast.show();
                }
                else {

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Properties props = new Properties();
                            props.put("mail.smtp.host", "smtp.gmail.com");
                            props.put("mail.smtp.socketFactory.port", "465");
                            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                            props.put("mail.smtp.auth", "true");
                            props.put("mail.smtp.port", "465");

                            Random random = new Random();
                            int randomNumber1 = (int) (Math.random() * (9999 - 1000 + 1)) + 1000;
                            randomNumber = Integer.toString(randomNumber1);

                            Session session = Session.getDefaultInstance(props,
                                    new javax.mail.Authenticator() {
                                        protected PasswordAuthentication getPasswordAuthentication() {
                                            return new PasswordAuthentication("WallStreetTrader2008", "cwpoklpsfgiupdyz");
                                        }
                                    });

                            try {
                                MimeMessage message = new MimeMessage(session);
                                message.setFrom(new InternetAddress("WallStreetTrader2008@gmail.com"));
                                message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
                                message.setSubject("Welcome To Wall Street Trader!");
                                message.setText("Hey, " + desiredUsername + " welcome to the team!\n\n\nHere is your access code: \n\n" + randomNumber);

                                Transport.send(message);

                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }).start();

                    email1.setVisibility(View.INVISIBLE);
                    desiredUsername1.setVisibility(View.INVISIBLE);
                    desiredPassword1.setVisibility(View.INVISIBLE);
                    desiredPassword2.setVisibility(View.INVISIBLE);
                    button2.setVisibility((View.INVISIBLE));
                    signUp.setVisibility(View.VISIBLE);
                    code1.setVisibility(View.VISIBLE);
                }
            }

            public void codeFunction(View v) {

                EditText code1 = (EditText) findViewById(R.id.accessCode);
                String code = code1.getText().toString();

                if (code.equals(randomNumber)) {

                    realUser = desiredUsername;
                    realPass = desiredPass1;

                    Toast toast = Toast.makeText(getApplicationContext(), "Thank you For Registering!", Toast.LENGTH_LONG);
                    toLogin(null);
                    toast.show();


                }
                else {
                    code1.setBackgroundColor(Color.argb(20, 255, 0, 0));
                    Toast toast = Toast.makeText(getApplicationContext(), "Invalid Access Code!", Toast.LENGTH_LONG);
                    toast.show();
                }


            }

            public void toProfile(View view) {
                Intent i = new Intent(MainActivity.this, settings.class);
                setContentView(R.layout.activity_profile);
                TextView emailShow = findViewById(R.id.address);
                TextView userShow = findViewById(R.id.usernameShow);
                TextView passShow = findViewById(R.id.passwordShow);

                emailShow.setText(email);
                userShow.setText(realUser);
                passShow.setText(realPass);

            }

            public void logout(View view) {

                loggedIn = false;
                backtoMain(null);
            }
        }















