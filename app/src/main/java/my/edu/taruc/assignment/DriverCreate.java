package my.edu.taruc.assignment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DriverCreate extends AppCompatActivity {

    private Spinner StartPlace, DestinationPlace;
    private TextView AvailableSlot, WaitTime, Charge, Note;
    private String URL_SAVE = "https://yaptw-wa16.000webhostapp.com/insert_carpool.php";
    private String DELETE_URL_HOST = "https://yaptw-wa16.000webhostapp.com/delete_carpool_where.php";
    private String StudentName = "" ;
    private String StudentID = "" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_create);

        StartPlace = findViewById(R.id.Input_SpinnerStart);
        DestinationPlace = findViewById(R.id.Input_SpInnerDestination);
        AvailableSlot = findViewById(R.id.Input_TextSlot);
        WaitTime = findViewById(R.id.Input_TextWaitTime);
        Charge = findViewById(R.id.Input_TextCharge);
        Note = findViewById(R.id.Input_StringNote);

        SharedPreferences prefs =   getApplicationContext().getSharedPreferences("PrefText", MODE_PRIVATE);
        StudentName = prefs.getString("StudentName", "No name defined");//"No name defined" is the default value.
        StudentID = prefs.getString("StudentID", "No ID defined"); //0 is the default value.

    }

    private Intent newIntent;
    public void createCarPool(View view) {
        if (StartPlace.getSelectedItemPosition() != 0 && DestinationPlace.getSelectedItemPosition() != 0 && DestinationPlace.getSelectedItemPosition() != StartPlace.getSelectedItemPosition()) {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mDialog.setMessage("Loading...");
            mDialog.show();

            saveData();

            newIntent = new Intent(this, CarPoolRoom.class);
            newIntent.putExtra("RoomID", StudentID);
            newIntent.putExtra("isHost", true);
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "Please choose a proper location", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void saveData(){
        //Send data
        try {
            StringRequest postRequest = new StringRequest(
                    Request.Method.POST,
                    URL_SAVE,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            JSONObject jsonObject;
                            try {
                                jsonObject = new JSONObject(response);
                                int success = jsonObject.getInt("success");
                                String message = jsonObject.getString("message");
                                if (success==0) {
                                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, DELETE_URL_HOST + "?RoomID=" + StudentID, null,
                                            new Response.Listener<JSONObject>() {
                                                @Override
                                                public void onResponse(JSONObject response) {
                                                }
                                            },
                                            new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                }
                                            });

                                    // Add the request to the RequestQueue.
                                    NetworkCalls.getInstance().addToRequestQueue(jsonObjectRequest);

                                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                                }else{
                                    startActivity(newIntent);
                                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), "Error. " + error.toString(), Toast.LENGTH_LONG).show();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("RoomID", StudentID);
                    params.put("Driver", StudentName);
                    params.put("Charges", Charge.getText().toString() );
                    params.put("Slot", AvailableSlot.getText().toString() );
                    params.put("CurrentSlot", "0");
                    params.put("FromLocation", StartPlace.getSelectedItem().toString() );
                    params.put("ToLocation", DestinationPlace.getSelectedItem().toString() );
                    int temp = Integer.parseInt(WaitTime.getText().toString());;
                    Date currentTime = new Date(Calendar.getInstance().getTimeInMillis() + (temp * 60000));
                    params.put("CreatedTime", new SimpleDateFormat("HH:mm:ss").format(currentTime));
                    params.put("Note",Note.getText().toString());
                    params.put("isStart","false");
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("Content-Type", "application/x-www-form-urlencoded");
                    return params;
                }
            };
            NetworkCalls.getInstance().addToRequestQueue(postRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}