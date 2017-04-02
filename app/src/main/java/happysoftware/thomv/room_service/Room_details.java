package happysoftware.thomv.room_service;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;

public class Room_details extends AppCompatActivity {

    TextView lable_;
    ListView _listView ;
    String[] _values = {"The App is loading"};
    Calendar myCalendar;
    TextView tv_date;

    String _userId = "";
    String _roomid = "";

    AsyncTask _JSON_save;
    AsyncTask _JSON_get;

    private static final String TAG = Room_details.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_details);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        String taken = getIntent().getStringExtra("taken");
        _userId = getIntent().getStringExtra("user");
        _roomid = getIntent().getStringExtra("room_ID");

        ConstraintLayout Layout = (ConstraintLayout) findViewById(R.id.Ly_Status);
        if(taken == "1") {
            //red
            Layout.setBackgroundColor(0xFFFF0000);
        }
        else{
            //green
            Layout.setBackgroundColor(0xFF00FF00);
        }


        _JSON_get = new getJSON_Reservations().execute();
        _JSON_save = new getJSON_SaveReservation();

        _listView = (ListView) findViewById(R.id.Lv_Time);
        tv_date = (TextView) findViewById(R.id.tv_Date);
        myCalendar = Calendar.getInstance();

        this.updateLabel();

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        tv_date.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(Room_details.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        this.updatearray();
    }

    //Updates listview
    private void updatearray(){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, _values);

        // Assign adapter to ListView
        _listView.setAdapter(adapter);
    }

    public void onSave(View view){
        _JSON_save.execute();
    }

    public void onTimeClick(View view){
        lable_ = (TextView)  findViewById(view.getId());
        Calendar mcurrentTime = Calendar.getInstance();

        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);

        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(Room_details.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                lable_.setText( selectedHour + ":" + selectedMinute);
            }
        }, hour, minute, true);//Yes 24 hour time

        mTimePicker.setTitle("Selecteer de tijd");
        mTimePicker.show();
    }

        private void updateLabel() {

            String myFormat = "MM/dd/yy"; //In which you need put here
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

            tv_date.setText(sdf.format(myCalendar.getTime()));
        }

    private class getJSON_SaveReservation extends AsyncTask<Void, Integer, String[]>{

        String get_Startime = "";
        String get_Endtime = "";
        String get_Date = "";
        String get_Note = "";
        @Override
        protected String[] doInBackground(Void... params ){

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView tv_start = (TextView)findViewById(R.id.Tv_sTime);
                    get_Startime = tv_start.getText().toString();

                    TextView tv_stop = (TextView)findViewById(R.id.tv_EndTime);
                    get_Endtime = tv_stop.getText().toString();

                    TextView tv_Date = (TextView)findViewById(R.id.tv_Date);
                    get_Date = tv_Date.getText().toString();

                    EditText Et_Disc = (EditText)findViewById(R.id.Et_description);
                    get_Note = Et_Disc.getText().toString();
                }
            });

            String url = "http://5.196.94.178/setRooms.php";
            HashMap<String, String> data = new HashMap<>();
            data.put("ID", _roomid);
            data.put("User_ID", _userId);
            data.put("Start_Time",get_Startime+" "+get_Date);
            data.put("Stop_Time", get_Startime+" "+get_Date);
            data.put("note", get_Note);
            int i;
            try{
                Httphandler sh = new Httphandler();
                // Making a request to url and getting response
                String jsonStr = sh.makeServiceCall(url,data);

                if( !jsonStr.equals(null)){
                    try{

                        //Logs JsonString to LogCat
                        Log.v("GetReservations", jsonStr);
                        JSONObject jsonobj = new JSONObject(jsonStr);

                        // Getting JSON Array node
                        JSONArray jsonArray = jsonobj.getJSONArray("room_reservation");

                        // gets data succesfull
                        for(i=0; i <= jsonArray.length()-1; i++){

                            //publishProgress(i);
                            JSONObject c = jsonArray.getJSONObject(i);

                            _values[i] = c.getString("Reservations");

                            //Wirtes debug data to LogCat
                            Log.v("GetReservations", c.getString("values"));
                        }
                    }
                    catch (final JSONException e){

                        //Makes a toast message on the UI Thread and displays error form E value
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Json parsing error: " + e.getMessage(),
                                        Toast.LENGTH_LONG)
                                        .show();
                            }
                        });
                    }
                }
                else{
                    //Makes a toast message on the UI Thread and displays error form E value
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "Couldn't get json from server.");
                            Toast.makeText(getApplicationContext(),
                                    "Couldn't get json from server. Check LogCat for possible errors!",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });}
            }
            catch (final RuntimeException e){

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("HttpHandeler: ","There isn't a sever connect: RuntimeExeption"+e);
                        Toast.makeText(getApplicationContext(), "Check if you internet connection is still working. Couldn't connect to the internet", Toast.LENGTH_LONG).show();
                    }
                });
            }
            return null;
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Log.i("Json File", "onProgressUpdate(): " + String.valueOf(values[0]));

        }
        @Override
        protected void onPostExecute(String[] result) {
            //Refreshes values array in the listView
            updatearray();
        }
    }


    //Get JSON from server
    private class getJSON_Reservations extends AsyncTask<Void, Integer, String>{

        String get_Date = "";
        @Override
        protected String doInBackground(Void... params) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView tv_start = (TextView) findViewById(R.id.Tv_sTime);
                    get_Date = tv_start.getText().toString();
                }
            });
            int i=0;
            //Creating a httphandeler protocol from HttpHandeler
            String url = "http://5.196.94.178/getRoom_Reservation.php";
            HashMap<String, String> data = new HashMap<>();
            data.put("room_id", _roomid);
            data.put("date", get_Date);

            try{
                Httphandler sh = new Httphandler();
                // Making a request to url and getting response
                String jsonStr = sh.makeServiceCall(url,data);

                if( !jsonStr.equals(null)){
                    try{

                        //Logs JsonString to LogCat
                        Log.v("CreateReservation", jsonStr);
                        JSONObject jsonobj = new JSONObject(jsonStr);

                        // Getting JSON Array node
                        JSONArray jsonArray = jsonobj.getJSONArray("room_reservation");

                        // gets data succesfull
                        for(i=0; i <= jsonArray.length()-1; i++){

                            //publishProgress(i);
                            JSONObject c = jsonArray.getJSONObject(i);

                            _values[i] = "Reservation: "+c.getString("Start_Time")+" "+c.getString("End_Time");

                            //Wirtes debug data to LogCat
                            Log.v("CreateReservation", c.getString("done"));
                        }
                    }
                    catch (final JSONException e){

                        //Makes a toast message on the UI Thread and displays error form E value
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Json parsing error: " + e.getMessage(),
                                        Toast.LENGTH_LONG)
                                        .show();
                            }
                        });
                    }
                }
                else{
                    //Makes a toast message on the UI Thread and displays error form E value
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "Couldn't get json from server.");
                            Toast.makeText(getApplicationContext(),
                                    "Couldn't get json from server. Check LogCat for possible errors!",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                    return null;
                }
            }
            catch (final RuntimeException e){

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("HttpHandeler: ","There isn't a sever connect: RuntimeExeption"+e);
                        Toast.makeText(getApplicationContext(), "Check if you internet connection is still working. Couldn't connect to the internet", Toast.LENGTH_LONG).show();
                    }
                });
            }

            return null;
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Log.i("Json File", "onProgressUpdate(): " + String.valueOf(values[0]));

        }
        @Override
        protected void onPostExecute(String result) {
            //Refreshes values array in the listView
            updatearray();
        }


    }

}

