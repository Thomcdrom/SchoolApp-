package happysoftware.thomv.room_service;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class Room_Select extends AppCompatActivity {

    ListView _listView ;
    String[] _values = {"The App is loading"};
    boolean _resume;

    String[] _ID;
    String[] _Room;

    AsyncTask _JSON;

    SwipeRefreshLayout _mSwipeRefreshLayout;

    private static final String TAG = Room_Select.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room__select);

        _listView = (ListView) findViewById(R.id.room_service_grid);

        _JSON = new getJSON().execute();

        // ListView Item Click Listener
        _listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                try{
                    _JSON.cancel(true);
                    Intent details = new Intent(Room_Select.this, Room_details.class );
                        String selected = ((TextView) view).getText().toString();
                        details.putExtra("id", _ID[position]);
                        details.putExtra("taken", _Room[position]);
                        details.putExtra("user", getIntent().getStringExtra("user"));
                        startActivity(details);
                    _resume = true;
                }
                catch (Exception e){
                    Log.e(TAG, "Can't start Intent");
                }
            }
        });

        //Gets RefeshLayout and sets Listener
        _mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        _mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(_resume == true) {
            _JSON.cancel(true);
  //          _JSON_save = new getJSON().execute();
            _resume = false;
        }
    }

    //Updates listview
    private void updatearray(){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, _values);

        // Assign adapter to ListView
        _listView.setAdapter(adapter);
    }

    //Refeshed conntent from server
    private void refreshContent(){
        //Eventhandeler for refresh activity
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                _JSON.cancel(true);
//                _JSON_save = new getJSON().execute();

                _mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }



    //Get JSON from server
    private class getJSON extends AsyncTask<Void, Integer, String[]>{
        @Override
        protected String[] doInBackground(Void... params) {
            int i=0;
            //Creating a httphandeler protocol from HttpHandeler
            String url = "http://5.196.94.178/getRoom.php";
            HashMap<String, String> data = new HashMap<>();
            try{
                Httphandler sh = new Httphandler();
                // Making a request to url and getting response
                String jsonStr = sh.makeServiceCall(url,data);

                if( !jsonStr.equals(null)){
                    try{

                        //Logs JsonString to LogCat
                        Log.v("allTaskList", jsonStr);
                        JSONObject jsonobj = new JSONObject(jsonStr);

                        // Getting JSON Array node
                        JSONArray login = jsonobj.getJSONArray("Tasks");

                        //Makes a new string array from Values
                        _values = new String[login.length()];
                        _ID = new String[login.length()];
                        _Room = new String[login.length()];

                        // gets data succesfull
                        for(i=0; i <= login.length()-1; i++){
                          //publishProgress(i);
                            JSONObject c = login.getJSONObject(i);

                            //Inserts String values into String Array Values
                            _values[i] = "Room Name: " + c.getString("Room_Name")+" Room Location: "+c.getString("Room_Location");
                            _ID[i] = c.getString("Room_ID");
                            _Room[i] = c.getString("Room_Ocupied");

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
        protected void onPostExecute(String[] result) {
            //Refreshes values array in the listView
            updatearray();
        }


    }
}
