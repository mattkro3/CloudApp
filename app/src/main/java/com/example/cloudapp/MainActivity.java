package com.example.cloudapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.http.HttpMethodName;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.amazonaws.mobileconnectors.apigateway.ApiRequest;
import com.amazonaws.mobileconnectors.apigateway.ApiResponse;
import com.amazonaws.util.IOUtils;
import com.example.cloudapp.clientsdk.CreateTableAddRecordsAndReadAPIClient;
import com.example.cloudapp.databinding.ActivityMainBinding;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

//public class MainActivity extends AppCompatActivity implements View.OnClickListener {
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    //private Button button;
    private ApiClientFactory factory;
    private CreateTableAddRecordsAndReadAPIClient client;
    private TextView name2;
    Button buttonNotify;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonNotify = findViewById(R.id.Notify);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("My notification", "My notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        name2 = (TextView) findViewById(R.id.Name2);

        factory = new ApiClientFactory();
        client = factory.build(CreateTableAddRecordsAndReadAPIClient.class);

        //button = findViewById(R.id.button);
        //button.setOnClickListener(this);
        if(isInternetAvailable()) {
            System.out.println("Internet Availible");
        } else {
            System.out.println("no internet");
        }

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText name = (EditText) findViewById(R.id.Name);
                String name_string = name.getText().toString();
                EditText empID = (EditText) findViewById(R.id.EmpID);
                String empID_string = empID.getText().toString();
                System.out.println(name_string + empID_string);
                new MyAsyncTask().execute(empID_string, name_string);
                name.setText("");
                empID.setText("");
            }
        });

        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("clicked button 2");
                EditText empID2 = (EditText) findViewById(R.id.EmpID2);
                String empID2_string = empID2.getText().toString();
                //try {
                new FindAsyncTask().execute(empID2_string);
                System.out.println("Internet? " + isInternetAvailable());
                empID2.setText("");
            }
        });

        Button clearbutton = findViewById(R.id.clearbutton);
        clearbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView name2 = (TextView) findViewById(R.id.Name2);
                name2.setText("");
            }
        });

        buttonNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "My notification");
                builder.setContentTitle("Notification");
                builder.setContentText("This is a notification");
                builder.setSmallIcon(R.drawable.ic_launcher_background);
                builder.setAutoCancel(true);

                NotificationManagerCompat managerCompat = NotificationManagerCompat.from(MainActivity.this);
                managerCompat.notify(1, builder.build());
            }
        });
    }



    public boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            //You can replace it with your name
            return !ipAddr.equals("");

        } catch (Exception e) {
            return false;
        }
    }

    private class MyAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            ApiRequest request = new ApiRequest();
            JSONObject requestBody = new JSONObject();
            try {
                requestBody.put("EmpID", Integer.valueOf(strings[0]));
                requestBody.put("Name", strings[1]);
            } catch(Exception e) {
                System.out.println(e);
            }
            System.out.println(requestBody);
            request.withBody(requestBody.toString());
            request.withHttpMethod(HttpMethodName.POST);
            request.withPath("/CreateTableAddRecordsAndRead");
            request.addHeader("Content-Length", String.valueOf(requestBody.toString().getBytes().length));
            request.addHeader("Content-Type", "application/json");
            request.addHeader("Connection", "keep-alive");
            System.out.println(request.getHeaders());
            System.out.println(request.getPath());
            System.out.println(request.getHttpMethod());
            System.out.println("Processing...");
            ApiResponse output = client.execute(request);
            System.out.println(output.getStatusText());
            System.out.println(output.getStatusCode());
            return null;
        }
    }

    private class FindAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            ApiRequest request = new ApiRequest();
            request.withParameter("EmpID", strings[0]);
            request.withHttpMethod(HttpMethodName.GET);
            request.withPath("/GetNameFromID");
            //request.addHeader("Content-Length", String.valueOf(requestBody.toString().getBytes().length));
            request.addHeader("Content-Type", "application/json");
            System.out.println(request.getHeaders());
            System.out.println(request.getPath());
            System.out.println(request.getHttpMethod());
            System.out.println(request.getParameters());
            System.out.println("Processing...");
            ApiResponse output = client.execute(request);
            System.out.println(output.getStatusText());
            System.out.println(output.getStatusCode());
            String name = "Couldn't find";
            //JsonParser jsonParser = new JsonParser();
            System.out.println(output.getHeaders());
            try {
                //System.out.println("Name: " + output.getContent().getClass().getName());
                //JSONObject result = (JSONObject) jsonParser.p;
                //name = new String(output.getContent().readAllBytes(), StandardCharsets.UTF_8);
                name = IOUtils.toString(output.getContent());
                JSONObject obj = new JSONObject(name);
                name = obj.get("Name").toString();

            } catch(Exception e) {
                System.out.println(e);
            }
            System.out.println("Name: " + name);
            return name;
        }

        protected void onPostExecute(String name) {
            name2.setText(name);
        }


    }

    /*
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button:
                // Do something
                EditText name = (EditText) findViewById(R.id.Name);
                String name_string = name.getText().toString();
                EditText empID = (EditText) findViewById(R.id.EmpID);
                String empID_string = empID.getText().toString();
                System.out.println(name_string + empID_string);
                new MyAsyncTask().execute(empID_string, name_string);
                name.setText("");
                empID.setText("");
                break;
            case R.id.button2:
                System.out.println("clicked button 2");
                EditText empID2 = (EditText) findViewById(R.id.EmpID2);
                String empID2_string = empID2.getText().toString();
                String name2 = "";
                //try {
                new FindAsyncTask().execute(empID2_string);
                //} catch(Exception e) {
                //    System.out.println(e);
                //}
                //TextView nametext2 = (TextView) findViewById(R.id.Name2);
                //nametext2.setText(name2);
                break;
            default:
                System.out.println("didn't work");
                break;
        }
    }*/

    /*
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        Button send_button = (Button) findViewById(R.id.button);
        send_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                EditText name = (EditText) findViewById(R.id.Name);
                String name_string = name.getText().toString();
                EditText empID = (EditText) findViewById(R.id.EmpID);
                String empID_string = empID.getText().toString();
                System.out.println(name_string);
                System.out.println(empID_string);
                Log.i("message", name_string);
                // code to do something with variables in database
                if(true) { // add code for if login credentials are valid

                }
            }
        });

    }*/

}