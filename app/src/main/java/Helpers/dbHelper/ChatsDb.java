package Helpers.dbHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import DataContract.DataModels.ChatMessage;

public class ChatsDb extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ServiceMe.db";
    private static final String CHATS_TABLE_NAME = "chats";
    private static final String CHATS_COLUMN_ID = "id";
    private static final String CHATS_COLUMN_CASE_ID = "caseId";
    private static final String CHATS_COLUMN_CHAT_ID = "chatId";
    private static final String CHATS_COLUMN_SENDER_ID = "senderId";
    private static final String CHATS_COLUMN_TYPE = "type";
    private static final String CHATS_COLUMN_DATA = "data";
    private static final String CHATS_COLUMN_TIMESTAMP = "timestamp";
    private static final String CHATS_COLUMN_SENT_TO_SERVER = "sentToServer";
    private static final String CHATS_COLUMN_DELIVERED = "delivered";
    private static final String CHATS_COLUMN_READ = "read";

    public ChatsDb(Context context)
    {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table chats " +
                        "(id integer primary key autoincrement," +
                        "caseId text," +
                        "chatId text," +
                        "senderId text," +
                        "type int," +
                        "data text," +
                        "timestamp long," +
                        "sentToServer text," +
                        "delivered text," +
                        "read text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS " + CHATS_TABLE_NAME);
        onCreate(db);
    }

    public void insertChatMessage(String caseId, String senderId, int typeOfMessage, String data){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ChatsDb.CHATS_COLUMN_CASE_ID, caseId);
        contentValues.put(ChatsDb.CHATS_COLUMN_SENDER_ID, senderId);
        contentValues.put(ChatsDb.CHATS_COLUMN_TYPE, typeOfMessage);
        contentValues.put(ChatsDb.CHATS_COLUMN_DATA, data);
        Date date = new Date(System.currentTimeMillis());
        contentValues.put(ChatsDb.CHATS_COLUMN_TIMESTAMP, date.getTime());
        db.insert(ChatsDb.CHATS_TABLE_NAME, null, contentValues);
    }

    public List<ChatMessage> getCharMessages(String caseId, int messageId, int numberOfMessages, List<Pair<String, String>> userIdNamePairs) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " +
                            ChatsDb.CHATS_COLUMN_ID +
                            ", " + ChatsDb.CHATS_COLUMN_SENDER_ID +
                            ", " + ChatsDb.CHATS_COLUMN_TYPE +
                            ", " + ChatsDb.CHATS_COLUMN_DATA +
                            ", " + ChatsDb.CHATS_COLUMN_TIMESTAMP +
                        " FROM " +
                            ChatsDb.CHATS_TABLE_NAME +
                        " WHERE " +
                            ChatsDb.CHATS_COLUMN_CASE_ID + " = ? " +
                            " {0} " +
                        " ORDER BY " +
                            ChatsDb.CHATS_COLUMN_ID + " DESC " +
                        " LIMIT " + numberOfMessages;
        String[] args;
        if(messageId == 0){
            query = MessageFormat.format(query, "");
            args = new String[] { caseId };
        }
        else{
            query = MessageFormat.format(query, "AND " + ChatsDb.CHATS_COLUMN_ID + " > ?");
            args = new String[] { caseId, Integer.toString(messageId) };
        }
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, args);
        }
        catch   (Exception ex){

        }

        if(cursor == null){
            return new ArrayList<>();
        }

        cursor.moveToFirst();

        // This is a work around as C#->Json->Java does not work for Dictionary to HashMap
        HashMap<String, String> userIdNameMap = new HashMap<>();
        for (Pair<String, String> p : userIdNamePairs) {
            userIdNameMap.put(p.first, p.second);
        }

        List<ChatMessage> dataToReturn = new ArrayList<>();
        while(!cursor.isAfterLast()){
            ChatMessage message = new ChatMessage();
            message.senderId = cursor.getString(cursor.getColumnIndex(ChatsDb.CHATS_COLUMN_SENDER_ID));
            if(userIdNameMap.containsKey(message.senderId)){
                message.senderName = userIdNameMap.get(message.senderId);
            }
            else {
                message.senderName = message.senderId;
            }

            message.type = cursor.getInt(cursor.getColumnIndex(ChatsDb.CHATS_COLUMN_TYPE));
            message.messageData = cursor.getString(cursor.getColumnIndex(ChatsDb.CHATS_COLUMN_DATA));
            Long timestamp = cursor.getLong(cursor.getColumnIndex(ChatsDb.CHATS_COLUMN_TIMESTAMP));
            message.timestamp = new Date(timestamp);
            message.id  = cursor.getInt(cursor.getColumnIndex(ChatsDb.CHATS_COLUMN_ID));

            dataToReturn.add(message);
            cursor.moveToNext();
        }

        cursor.close();
        return dataToReturn;
    }
}
