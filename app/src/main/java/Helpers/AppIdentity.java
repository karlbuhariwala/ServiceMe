package Helpers;

import android.content.Context;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public final class AppIdentity {
    private AppIdentity() {
    }

    private static final String fileName = "IdentityFile";
    private static AppIdentity instance = new AppIdentity();
    private AppIdentityStorageContainer container = new AppIdentityStorageContainer();

    // Update HERE 1/3
    public static String userId;
    public static boolean verified;

    public static void UpdateIdentityInFile(Context context) {
        // Update HERE 2/3
        AppIdentity.instance.container.userId = AppIdentity.userId;
        AppIdentity.instance.container.verified = AppIdentity.verified;
        String jsonString = new Gson().toJson(AppIdentity.instance.container);
        try {
            FileOutputStream fos = context.openFileOutput(AppIdentity.fileName, Context.MODE_PRIVATE);
            fos.write(jsonString.getBytes());
            fos.close();
        } catch (Exception ex) {
            String message = ex.getMessage();
            // Todo: Log the message
        }
    }

    public static void LoadIdentityFromFile(Context context){
        try{
            FileInputStream fis = context.openFileInput(AppIdentity.fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line;
            StringBuilder sb = new StringBuilder();
            while((line = br.readLine()) != null){
                String stringToAppend = line + System.getProperty("line.separator");
                sb.append(stringToAppend);
            }

            fis.close();
            AppIdentity.instance.container = new Gson().fromJson(sb.toString(), AppIdentityStorageContainer.class);

            // Update HERE 3/3
            AppIdentity.userId = AppIdentity.instance.container.userId;
            AppIdentity.verified = AppIdentity.instance.container.verified;
        }catch (Exception ex) {
            String message = ex.getMessage();
            // Todo: Log the message
        }
    }

    private class AppIdentityStorageContainer{
        public String userId;
        public Boolean verified;
    }
}



