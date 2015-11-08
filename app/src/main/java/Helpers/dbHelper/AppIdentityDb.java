package Helpers.dbHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import java.util.Date;

public class AppIdentityDb extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ServiceMe.db";
    private static final String TABLE_NAME = "AppIdentity";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_FIELD_NAME = "fieldName";
    private static final String COLUMN_FIELD_VALUE = "fieldValue";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    public static String userId = "userId";
    public static String userName = "userName";
    public static String verified = "verified";
    public static String contactPref = "contactPref";
    public static String emailAddress = "emailAddress";
    public static String landingPage = "landingPage";
    public static String isAgent = "isAgent";
    public static String isManager = "isManager";

    public AppIdentityDb (Context context)
    {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table AppIdentity " +
                        "(id integer primary key autoincrement," +
                        "fieldName text," +
                        "fieldValue text," +
                        "timestamp long)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void InsertUpdateResource(String fieldName, String fieldValue) {
        Cursor cursor = GetCursor(fieldName);
        if(cursor == null){
            return;
        }

        if(cursor.getCount() > 0) {
            UpdateResource(fieldName, fieldValue);
        }
        else {
            InsertResource(fieldName, fieldValue);
        }
    }

    private void InsertResource(String fieldName, String fieldValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(AppIdentityDb.COLUMN_FIELD_NAME, fieldName);
        contentValues.put(AppIdentityDb.COLUMN_FIELD_VALUE, fieldValue);
        Date date = new Date(System.currentTimeMillis());
        contentValues.put(AppIdentityDb.COLUMN_TIMESTAMP, date.getTime());
        db.insert(AppIdentityDb.TABLE_NAME, null, contentValues);
    }

    private void UpdateResource(String fieldName, String fieldValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        String filter = AppIdentityDb.COLUMN_FIELD_NAME + "=?";
        ContentValues args = new ContentValues();
        args.put(AppIdentityDb.COLUMN_FIELD_VALUE, fieldValue);
        db.update(AppIdentityDb.TABLE_NAME, args, filter, new String[] { fieldName });
    }

    public String GetResource(String fieldName) {
        Cursor cursor = GetCursor(fieldName);
        if(cursor == null){
            return "";
        }

        cursor.moveToFirst();

        String dataToReturn = "";
        if(!cursor.isAfterLast()){
            dataToReturn = cursor.getString(cursor.getColumnIndex(AppIdentityDb.COLUMN_FIELD_VALUE));
        }

        cursor.close();
        return dataToReturn;
    }

    @Nullable
    private Cursor GetCursor(String fieldName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " +
                AppIdentityDb.COLUMN_ID +
                ", " + AppIdentityDb.COLUMN_FIELD_NAME +
                ", " + AppIdentityDb.COLUMN_FIELD_VALUE +
                " FROM " +
                AppIdentityDb.TABLE_NAME +
                " WHERE " +
                AppIdentityDb.COLUMN_FIELD_NAME + " = ? ";

        String[] args = new String[] { fieldName };
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, args);
        }
        catch (Exception ex){

        }
        return cursor;
    }
}
