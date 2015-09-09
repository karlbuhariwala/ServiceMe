package webApi;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.example.karlbuha.serviceme.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import Helpers.MyProgressWindow;

public class ApiCallService extends IntentService {

    public ApiCallService() {
        super("ApiCallService");
    }

    protected void onHandleIntent(Intent intent) {
        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        String command = intent.getStringExtra("command");
        Boolean showProgress = true;
        if(intent.getStringExtra("showProgress") != null) {
            showProgress = Boolean.parseBoolean(intent.getStringExtra("showProgress"));
        }

        int successCode = Integer.parseInt(intent.getStringExtra("successCode"));
        int failureCode = 2;
        if(intent.getStringExtra("failureCode") != null) {
            failureCode = Integer.parseInt(intent.getStringExtra("failureCode"));
        }

        String inputJson = intent.getStringExtra("data");
        String apiCall = intent.getStringExtra("apiCall");
        Bundle bundle = new Bundle();

        if (command.equals("query")) {
            if(showProgress) {
                receiver.send(1, Bundle.EMPTY);
            }
            try {
                URL url = new URL(getResources().getString(R.string.api_endpoint) + apiCall);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");

                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                outputStreamWriter.write(inputJson);
                outputStreamWriter.flush();
                outputStreamWriter.close();
                urlConnection.connect();

                StringBuilder sb = new StringBuilder();
                int HttpResult = urlConnection.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            urlConnection.getInputStream(), "utf-8"));
                    String line;
                    while ((line = br.readLine()) != null) {
                        String appendText = line + System.getProperty("line.separator");
                        sb.append(appendText);
                    }

                    br.close();
                } else {
                    // Todo: Make some error handling in the APICallService
                    System.out.println(urlConnection.getResponseMessage());
                }

                bundle.putString("results", sb.toString());
                receiver.send(successCode, bundle);
            } catch (Exception e) {
                bundle.putString(Intent.EXTRA_TEXT, e.toString());
                receiver.send(failureCode, bundle);
            } finally {
                if(showProgress) {
                    MyProgressWindow.DismissProgressWindow();
                }
            }
        }
    }

    public static void CallService (Object activity, Boolean showProgress, String apiName, String jsonString, String successCode){
        MyResultReceiver myResultReceiver = new MyResultReceiver(new Handler());
        myResultReceiver.setReceiver((MyResultReceiver.Receiver)activity);
        Intent intent = new Intent(Intent.ACTION_SYNC, null, (Activity)activity, ApiCallService.class);
        intent.putExtra("receiver", myResultReceiver);
        intent.putExtra("command", "query");
        if(!showProgress) {
            intent.putExtra("showProgress", "false");
        }

        intent.putExtra("successCode", successCode);
        intent.putExtra("apiCall", apiName);
        intent.putExtra("data", jsonString);
        ((Activity)activity).startService(intent);
    }
}
