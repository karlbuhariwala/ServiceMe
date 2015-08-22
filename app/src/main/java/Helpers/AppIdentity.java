package Helpers;

import android.content.Context;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.List;

public final class AppIdentity {
    private AppIdentity() {
    }

    private static final String fileName = "IdentityFile";
    private static AppIdentity instance = new AppIdentity();
    private AppIdentityStorageContainer container = new AppIdentityStorageContainer();

    // Update HERE 1/2
    public static String userId = "userId";
    public static String verified = "verified";
    public static String contactPref = "contactPref";
    public static String emailAddress = "emailAddress";

    public static void UpdateResource(Context context, String resourceToUpdate, Object value){
        AppIdentity.LoadIdentityFromFile(context);
        try {
            Field resource = AppIdentityStorageContainer.class.getField(resourceToUpdate);
            resource.set(instance.container, value);
        } catch (Exception e) {
            e.printStackTrace();
        }

        AppIdentity.UpdateIdentityInFile(context);
    }

    public static Object GetResource(Context context, String resourceToGet){
        AppIdentity.LoadIdentityFromFile(context);
        try {
            Field resource = AppIdentityStorageContainer.class.getField(resourceToGet);
            return resource.get(instance.container);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static void UpdateIdentityInFile(Context context) {
        String jsonString = new Gson().toJson(AppIdentity.instance.container);
        try {
            FileOutputStream fos = context.openFileOutput(AppIdentity.fileName, Context.MODE_PRIVATE);
            fos.write(jsonString.getBytes());
            fos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void LoadIdentityFromFile(Context context){
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
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private class AppIdentityStorageContainer{
        // Update HERE 2/2
        public String userId;
        public Boolean verified;
        public List<String> contactPref;
        public String emailAddress;
    }
}



