package com.example.karlbuha.serviceme;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiCallService extends IntentService {

    public ApiCallService() {
        super("ApiCallService");
    }

    protected void onHandleIntent(Intent intent) {
        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        String command = intent.getStringExtra("command");
        String inputJson = intent.getStringExtra("data");
        String apiCall = intent.getStringExtra("apiCall");
        Bundle bundle = new Bundle();
        if (command.equals("query")) {
            receiver.send(1, Bundle.EMPTY);
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
                try {
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
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    urlConnection.disconnect();
                }

                bundle.putString("results", sb.toString());
                receiver.send(2, bundle);
            } catch (Exception e) {
                bundle.putString(Intent.EXTRA_TEXT, e.toString());
                receiver.send(3, bundle);
            }
        }
    }
}
