package com.example.contacts_app;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.example.contacts_app.FeedReaderContract.FeedEntry;

public class MainActivity extends AppCompatActivity {


    private DBHelper helper;
    private SQLiteDatabase database;

    private static final String TAG = MainActivity.class.getSimpleName() + "_TAG";
    private static final String BASE_URL = "https://randomuser.me/api";
    private static String ADDRESS = "";
    private static String URL_PICTURE = "";
    public static String SEARCH_RECORD;

    OkHttpClient client;

    Button getUserBTN;
    TextView nameTV;
    TextView addressTV;
    TextView emailTV;
    ImageView userPictureIV;
    EditText searchRecordET;
    TextView usersAddedET;
    Button saveProfileBTN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helper = new DBHelper(this);
        database = helper.getWritableDatabase();

        getUserBTN = (Button) findViewById(R.id.btn_getRandomUser);
        nameTV = (TextView) findViewById(R.id.tv_name);
        addressTV = (TextView) findViewById(R.id.tv_address);
        emailTV = (TextView) findViewById(R.id.tv_email);
        userPictureIV = (ImageView) findViewById(R.id.iv_userPicture);
        searchRecordET = (EditText) findViewById(R.id.et_seacrRecord);
        usersAddedET = (TextView) findViewById(R.id.tv_users_Added);
        saveProfileBTN = (Button) findViewById(R.id.btn_save);

        client = new OkHttpClient.Builder().build();

    }


//_____________________OnClick Methods_____________________________________________________________

    public void getRandomUser(View view){
        getRandomJSON();
    }

    public void saveUser(View view) {
        saveProfile();
        readAllProfiles();
    }

    public void getProfileDetails(View view) {
        String value = searchRecordET.getText().toString();
        Intent intent = new Intent(MainActivity.this, ProflieDetailsActivity.class);
        intent.putExtra(SEARCH_RECORD, value);
        startActivity(intent);
    }

    public void goToContactsSaved(View view) {
        Intent intent = new Intent(MainActivity.this, ContactsActivity.class);
        startActivity(intent);
    }

//________________GET THE RANDOM JSON AND SET VARIABLES_____________________________________________

    public void getRandomJSON(){
        Request request = new Request.Builder().url(BASE_URL).build();

        client.newCall(request).enqueue(
            new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String resp = response.body().string();

                    //GET THE JSON RESPONSE
                    try{

                        JSONObject result = new JSONObject(resp);
                        JSONArray results = result.getJSONArray("results"); // Array of objects

                        JSONObject nameJSONObject = results.getJSONObject(0).getJSONObject("name");
                        JSONObject locationJSONObject = results.getJSONObject(0).getJSONObject("location");
                        JSONObject pictureJSONObject = results.getJSONObject(0).getJSONObject("picture");
                        final String emailJSONString = results.getJSONObject(0).getString("email");

                        Gson gson = new Gson();

                        //CREATE THE JSON OBJECT INTO A JAVA OBJECT
                        final Name nameObject = gson.fromJson(
                                String.valueOf(nameJSONObject),
                                Name.class);

                        final Location locationObject = gson.fromJson(
                                String.valueOf(locationJSONObject),
                                Location.class);

                        final Picture pictureObject = gson.fromJson(
                                String.valueOf(pictureJSONObject),
                                Picture.class);

                        //GET THE ATTACHED OBJECT ATTRIBUTES INTO THE CONSTANTS
                        URL_PICTURE = pictureObject.getMedium();
                        ADDRESS = locationObject.getStreet() + ", " +
                                locationObject.getCity() + ", " +
                                locationObject.getState() + ", " +
                                locationObject.getPostcode();

                        //RUN THIS TRHAED IN ORDER TO EDIT COMPONENT IN THE VIEW
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                nameTV.setText(nameObject.getFirst() + " " + nameObject.getLast());
                                addressTV.setText(ADDRESS);
                                emailTV.setText(emailJSONString);
                                new DownloadImageTask(userPictureIV).execute(pictureObject.getMedium());
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        );
    }

//____________________________READ ALL THE PROFILES SAVED___________________________________________

    public void readAllProfiles(){

        usersAddedET.setText("");
        String[] projection = {
                FeedEntry._ID,
                FeedEntry.COLUMN_NAME_NAME,
                FeedEntry.COLUMN_NAME_ADDRESS,
                FeedEntry.COLUMN_NAME_EMAIL
        };

        Cursor cursor = database.query(
                FeedEntry.TABLE_NAME,       // TABLE
                projection,                 //Projection
                null,                       //Selection Where
                null,                       //Values for selection
                null,                       //Group by
                null,                       //Filters
                null                        //Sort order
        );
        while (cursor.moveToNext()){

            long entryId = cursor.getLong(cursor.getColumnIndexOrThrow(FeedEntry._ID));
            String entryName = cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.COLUMN_NAME_NAME));
            String entryAddress = cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.COLUMN_NAME_ADDRESS));
            String entryEmail = cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.COLUMN_NAME_EMAIL));

            usersAddedET.append("[ " + entryId + " ] " + "NAME --> " + entryName + "\n");
        }
    }

//_____________________________SAVE THE PROFILE DETAILS_____________________________________________

    public void saveProfile(){
        //Bitmap imageBitmap = ((BitmapDrawable)userPictureIV.getDrawable()).getBitmap();
        //saveImageToExternalStorage(imageBitmap);

        ContentValues values = new ContentValues();
        values.put(FeedEntry.COLUMN_NAME_NAME, nameTV.getText().toString());
        values.put(FeedEntry.COLUMN_NAME_ADDRESS, addressTV.getText().toString());
        values.put(FeedEntry.COLUMN_NAME_EMAIL, emailTV.getText().toString());
        values.put(FeedEntry.COLUMN_NAME_PICTURE_LOCATION,URL_PICTURE);

        long recordId = database.insert(
                FeedEntry.TABLE_NAME,
                null,
                values);
        if (recordId > 0){
            Log.d(TAG, "saveRecord: Record saved");
            Toast.makeText(this, "Record SAVED", Toast.LENGTH_SHORT).show();
        }else {
            Log.d(TAG, "saveRecord: Record not saved");
            Toast.makeText(this, "Record NOT SAVED", Toast.LENGTH_SHORT).show();
        }
    }


}
