package app.com.dawn2dusk;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.R.drawable.sym_def_app_icon;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,LoaderManager.LoaderCallbacks<Cursor>,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, LocationListener {
private FirebaseAuth mAuth;
  TextView risedatatv,setdatatv,durdatatv,nic,loctext,risename,durname,setname;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    final static int REQUEST_LOCATION = 199;
    public static final String ACTION_DATA_UPDATED="app.com.dawn2dusk.ACTION_DATA_UPDATED";
    private Location mLastLocation;
    public int flag=0;
    public String latitude="",longitude="";
    private GoogleApiClient mGoogleApiClient;
    // Google client to interact with Google API


    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest mLocationRequest;
    private double currentLatitude;
    private double currentLongitude;
    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters
    Date fdate,sdate;
    SharedPreferences myPrefs;
    SunDataDbHelper mDbHelper;
    public static final String TAG = HomeActivity.class.getSimpleName();
    SQLiteDatabase db;
    String address;
    Location mLocation;
    ProgressDialog pDialog;
    SimpleDateFormat formaty,formatm,formatd;
    SwipeRefreshLayout swpl;
    ImageView riseiv,setiv,duriv;
    MaterialCalendarView materialCalendarView;
    private FirebaseAuth.AuthStateListener mAuthListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mDbHelper=new SunDataDbHelper(this);
        db=mDbHelper.getWritableDatabase();
        getSupportLoaderManager().initLoader(0, null,this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        myPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        materialCalendarView=(MaterialCalendarView)findViewById(R.id.calendarView);
        swpl=(SwipeRefreshLayout)findViewById(R.id.swpl);
        risedatatv = (TextView)findViewById(R.id.risedatatv);
        setdatatv = (TextView)findViewById(R.id.setdatatv);
        durdatatv = (TextView)findViewById(R.id.durdatatv);
        risename = (TextView)findViewById(R.id.risetv);
        setname = (TextView)findViewById(R.id.settv);
        durname = (TextView)findViewById(R.id.durtv);
        loctext = (TextView)findViewById(R.id.loctv);
        nic=(TextView)findViewById(R.id.nic);
        riseiv=(ImageView)findViewById(R.id.riseiv);
        setiv=(ImageView)findViewById(R.id.setiv);
        duriv=(ImageView)findViewById(R.id.duriv);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {

                        Log.v("Location error ", String.valueOf(connectionResult.getErrorCode()));
                    }
                }).build();
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.M)
        {
            checkDevicePermission();
        }
        else
        {
            noLocation();
        }
        loadData();
        swpl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });
        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                formaty=new SimpleDateFormat("yyyy");
                formatm=new SimpleDateFormat("mm");
                formatd=new SimpleDateFormat("dd");
                int m=date.getMonth()+1;
                if(isNetworkAvailable(HomeActivity.this)) {
                    swpl.setRefreshing(true);
                    latitude = myPrefs.getString("currentLatitude", "");
                    longitude = myPrefs.getString("currentLongitude", "");
                    if(latitude != null && !latitude.equals("") && longitude != null && !longitude.equals("")) {
                        new FetchDataAsyncTask().execute(latitude,longitude,formaty.format(date.getDate()) + "-" + String.valueOf(m) + "-" + formatd.format(date.getDate()));
                    }
                    else
                    {
                        loctext.setVisibility(View.GONE);
                        nic.setText("No Location Available. Enable Location Access");
                        nic.setVisibility(View.VISIBLE);
                        riseiv.setVisibility(View.INVISIBLE);
                        risename.setVisibility(View.INVISIBLE);
                        risedatatv.setVisibility(View.INVISIBLE);
                        setiv.setVisibility(View.INVISIBLE);
                        setname.setVisibility(View.INVISIBLE);
                        setdatatv.setVisibility(View.INVISIBLE);
                        duriv.setVisibility(View.INVISIBLE);
                        durname.setVisibility(View.INVISIBLE);
                        durdatatv.setVisibility(View.INVISIBLE);
                        materialCalendarView.setVisibility(View.INVISIBLE);
                    }
                }
                else
                {
                    Toast.makeText(HomeActivity.this,"No Internet Connection!",Toast.LENGTH_SHORT).show();
                }
            }
        });
       mAuth=FirebaseAuth.getInstance();
    mAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()==null)
                {
                    startActivity(new Intent(HomeActivity.this,LoginActivity.class));

                }
            }
        };

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View hView =  navigationView.getHeaderView(0);
        TextView username=(TextView)hView.findViewById(R.id.username);
        TextView useremail=(TextView)hView.findViewById(R.id.useremail);
        ImageView profile_image=(ImageView)hView.findViewById(R.id.profile_image);
        username.setText(myPrefs.getString("profile_name", ""));
        useremail.setText(myPrefs.getString("profile_email", ""));



    }


    @Override
    public void onResume(){
        super.onResume();
        getSupportLoaderManager().restartLoader(0, null, this);


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            moveTaskToBack(true);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_logout) {
       mAuth.signOut();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader = new CursorLoader(this,
                SunDataContract.SunDataEntry.CONTENT_URI,
                SunDataContract.SunDataEntry.PROJECTION,
                null,
                null,
                null
        );
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data != null && data.moveToFirst()) {
            // do whatever, ie String myString=cursor.getString(0)
            // in case you fetch a string as the first element of your projection
            if(swpl.isRefreshing())
            {
                swpl.setRefreshing(false);
            }
            risedatatv.setText(data.getString(1));
            setdatatv.setText(data.getString(2));
            durdatatv.setText(data.getString(3));
            DateFormat f=new SimpleDateFormat("yyyy-MM-dd");
            try {
                sdate=f.parse(data.getString(4));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if((data.getString(7)!=null)&&(data.getString(7)!="")) {
                loctext.setText(data.getString(4) + " in " + data.getString(7));
            }
            else
            {
                loctext.setText(data.getString(4));
            }
            materialCalendarView.setSelectedDate(sdate);
            materialCalendarView.setCurrentDate(sdate);
            loctext.setVisibility(View.VISIBLE);
            nic.setVisibility(View.GONE);
            riseiv.setVisibility(View.VISIBLE);
            risename.setVisibility(View.VISIBLE);
            risedatatv.setVisibility(View.VISIBLE);
            setiv.setVisibility(View.VISIBLE);
            setname.setVisibility(View.VISIBLE);
            setdatatv.setVisibility(View.VISIBLE);
            duriv.setVisibility(View.VISIBLE);
            durname.setVisibility(View.VISIBLE);
            durdatatv.setVisibility(View.VISIBLE);
            materialCalendarView.setVisibility(View.VISIBLE);
            Intent dataUpdatedIntent=new Intent(ACTION_DATA_UPDATED);
            this.sendBroadcast(dataUpdatedIntent);
            Log.d(TAG,"Broadcast Sent"+ACTION_DATA_UPDATED);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 200:
                Log.d("sss", "Marshmallow+");
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED&&grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    mGoogleApiClient.connect();
                    noLocation();
                    //loadData();

                } else if(grantResults[0] == PackageManager.PERMISSION_DENIED&&grantResults[1] == PackageManager.PERMISSION_DENIED) {

                    boolean should = (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION));
                    if(should){
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Permission Denied");
                        builder.setMessage("Dawn2Dusk needs location access permissions to find the exact Sun Data.");
                        builder.setPositiveButton("Deny", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                        builder.setNegativeButton("Try Again", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                ActivityCompat.requestPermissions(HomeActivity.this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},200);
                            }
                        });
                        builder.show();
                    }
                    else{

                    }

                }
                break;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        // Note that this can be NULL if last location isn't already known.
        if (mCurrentLocation != null) {
            // Print current location if not null
            Log.d("DEBUG", "current location: " + mCurrentLocation.toString());
            LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            currentLatitude = latLng.latitude;
            currentLongitude = latLng.longitude;
            myPrefs.edit().putString("currentLatitude", "" + currentLatitude).commit();
            myPrefs.edit().putString("currentLongitude", "" + currentLongitude).commit();
//
            if(isNetworkAvailable(this))
            getAddressFromLocation(mCurrentLocation, this, new GeocoderHandler());
            //loadData();
        }
        startLocationUpdates();
    }
    // Trigger new location updates at interval
    protected void startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)
                .setFastestInterval(2000);
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
    }
    public static void getAddressFromLocation(
            final Location location, final Context context, final Handler handler) {
        Thread thread = new Thread() {
            @Override public void run() {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                String result = null;
                try {
                    List<Address> list = geocoder.getFromLocation(
                            location.getLatitude(), location.getLongitude(), 1);
                    if (list != null && list.size() > 0) {
                        Address address = list.get(0);
                        // sending back first address line and locality
                        result = address.getLocality()+","+address.getCountryName();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Impossible to connect to Geocoder", e);
                } finally {
                    Message msg = Message.obtain();
                    msg.setTarget(handler);
                    if (result != null) {
                        msg.what = 1;
                        Bundle bundle = new Bundle();
                        bundle.putString("address", result);
                        msg.setData(bundle);
                    } else
                        msg.what = 0;
                    msg.sendToTarget();
                }
            }
        };
        thread.start();
    }
    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String result;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    result = bundle.getString("address");
                    break;
                default:
                    result = null;
            }
            // replace by what you need to do
            myPrefs.edit().putString("address", "" + result).commit();
        }
    }
    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(this, "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onLocationChanged(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();
        myPrefs.edit().putString("currentLatitude", "" + currentLatitude).commit();
        myPrefs.edit().putString("currentLongitude", "" + currentLongitude).commit();
        if(isNetworkAvailable(this))
        getAddressFromLocation(location, this, new GeocoderHandler());
        // loadData();
//        Illegal State Exception Bug Fixed by bharatwaaj on 13/08/2016
//        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, new MainFragment()).commit();

        try {
            stopLocationUpdates();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void stopLocationUpdates() throws Exception{
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient,this);
            // mGoogleApiClient = null;
        }
    }
    public class FetchDataAsyncTask extends AsyncTask<String, Void, String> {

        private final String LOG_TAG = FetchDataAsyncTask.class.getSimpleName();

        @Override
        protected String doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            String lat=params[0];
            String lng=params[1];
            String date=params[2];
            String address=myPrefs.getString("address", "");
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String JsonStr = null;
            String format = "json";

            try {
                final String BASE_URL =getResources().getString(R.string.base_url)+"lat="+lat+"&lng="+lng+"&date="+date;

                URL url = new URL(BASE_URL);

                Log.v(LOG_TAG, "Built URI " + BASE_URL);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                JsonStr = buffer.toString();
                getSunDataFromJson(JsonStr,lat,lng,date,address);
                Log.v(LOG_TAG, "Forecast string: " + JsonStr);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final Exception e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return null;

        }
        private void getSunDataFromJson(String JsonStr,String lat,String lng,String date,String address) throws JSONException
        {
            try
            {
                Cursor mCursor;
                JSONObject Jstring=new JSONObject(JsonStr);
                JSONObject res = Jstring.getJSONObject("results");
                String sunrise = res.getString("sunrise");
                String sunset = res.getString("sunset");
                String day_length = res.getString("day_length");
                ContentValues contentValues = new ContentValues();
                contentValues.put(SunDataContract.SunDataEntry.COLUMN_SUNDATA_ID,"1");
                contentValues.put(SunDataContract.SunDataEntry.COLUMN_SUNRISE,sunrise);
                contentValues.put(SunDataContract.SunDataEntry.COLUMN_SUNSET,sunset);
                contentValues.put(SunDataContract.SunDataEntry.COLUMN_DAY_LENGTH,day_length);
                contentValues.put(SunDataContract.SunDataEntry.COLUMN_DATE,date);
                contentValues.put(SunDataContract.SunDataEntry.COLUMN_LAT,lat);
                contentValues.put(SunDataContract.SunDataEntry.COLUMN_LNG,lng);
                contentValues.put(SunDataContract.SunDataEntry.COLUMN_ADR,address);
                mCursor=getContentResolver().query(SunDataContract.SunDataEntry.CONTENT_URI, SunDataContract.SunDataEntry.PROJECTION,null,null,null);
                if(mCursor.getCount()==0) {
                    getContentResolver().insert(SunDataContract.SunDataEntry.CONTENT_URI, contentValues);
                    Log.d(LOG_TAG, "Successfully inserted");
                }
                else
                {
                    getContentResolver().delete(SunDataContract.SunDataEntry.CONTENT_URI,null,null);
                    Log.d(LOG_TAG, "Successfully deleted");
                    getContentResolver().insert(SunDataContract.SunDataEntry.CONTENT_URI, contentValues);
                    Log.d(LOG_TAG, "Successfully inserted");
                }
                mCursor=getContentResolver().query(SunDataContract.SunDataEntry.CONTENT_URI, SunDataContract.SunDataEntry.PROJECTION,null,null,null);
                Log.d(LOG_TAG,"Total Data in table:"+String.valueOf(mCursor.getCount()));

            }
            catch(JSONException e)
            {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
        }
        /*
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    if(swpl.isRefreshing())
                    {
                        swpl.setRefreshing(false);
                    }
                JSONObject Jstring=new JSONObject(result);
                JSONObject res = Jstring.getJSONObject("results");
                String sunrise = res.getString("sunrise");
                String sunset = res.getString("sunset");
                String day_length = res.getString("day_length");
                risedatatv.setText(sunrise);
                setdatatv.setText(sunset);
                durdatatv.setText(day_length);
                    loctext.setVisibility(View.VISIBLE);
                    nic.setVisibility(View.GONE);
                    riseiv.setVisibility(View.VISIBLE);
                    risename.setVisibility(View.VISIBLE);
                    risedatatv.setVisibility(View.VISIBLE);
                    setiv.setVisibility(View.VISIBLE);
                    setname.setVisibility(View.VISIBLE);
                    setdatatv.setVisibility(View.VISIBLE);
                    duriv.setVisibility(View.VISIBLE);
                    durname.setVisibility(View.VISIBLE);
                    durdatatv.setVisibility(View.VISIBLE);
                    materialCalendarView.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // New data is back from the server.  Hooray!
            }
        }*/
    }
    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
    public void loadData()
    {
        if(isNetworkAvailable(this))
        {

            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = df.format(c.getTime());
            DateFormat d=new SimpleDateFormat("yyyy-MM-dd");
            try {
                fdate=d.parse(formattedDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            materialCalendarView.setSelectedDate(fdate);
            latitude = myPrefs.getString("currentLatitude", "");
            longitude = myPrefs.getString("currentLongitude", "");
            if(latitude != null && !latitude.equals("") && longitude != null && !longitude.equals("")) {
                new FetchDataAsyncTask().execute(latitude,longitude, formattedDate);
            }
            else
            {
                swpl.setRefreshing(false);
                loctext.setVisibility(View.GONE);
                nic.setText("Enable Location Access & Refresh");
                nic.setVisibility(View.VISIBLE);
                riseiv.setVisibility(View.INVISIBLE);
                risename.setVisibility(View.INVISIBLE);
                risedatatv.setVisibility(View.INVISIBLE);
                setiv.setVisibility(View.INVISIBLE);
                setname.setVisibility(View.INVISIBLE);
                setdatatv.setVisibility(View.INVISIBLE);
                duriv.setVisibility(View.INVISIBLE);
                durname.setVisibility(View.INVISIBLE);
                durdatatv.setVisibility(View.INVISIBLE);
                materialCalendarView.setVisibility(View.INVISIBLE);
            }
        }
        else
        {
            swpl.setRefreshing(false);
            loctext.setVisibility(View.GONE);
            nic.setVisibility(View.VISIBLE);
            riseiv.setVisibility(View.INVISIBLE);
            risename.setVisibility(View.INVISIBLE);
            risedatatv.setVisibility(View.INVISIBLE);
            setiv.setVisibility(View.INVISIBLE);
            setname.setVisibility(View.INVISIBLE);
            setdatatv.setVisibility(View.INVISIBLE);
            duriv.setVisibility(View.INVISIBLE);
            durname.setVisibility(View.INVISIBLE);
            durdatatv.setVisibility(View.INVISIBLE);
            materialCalendarView.setVisibility(View.INVISIBLE);


        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_LOCATION:
                switch (resultCode) {
                    case Activity.RESULT_CANCELED: {
                        try {
                            stopLocationUpdates();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
//                        Modified by Bharat 13/08/2016
//                        checkDevicePermission();
                        break;
                    }

                    default: {
                        break;
                    }
                }
                break;
            /*case FILTER_REQUEST:
                if (resultCode == FILTER_REQUEST) {
                    LatLng latLng = null;
                    String address = "";
                    if (data.getExtras().getParcelable("Latlng") != null) {
                        latLng = data.getExtras().getParcelable("Latlng");
                    }
                    String isVeg = data.getStringExtra("isVeg");
                    Boolean isFilter = data.getBooleanExtra("isFilter", false);
                    if (data.getStringExtra("address") != null) {
                        address = data.getStringExtra("address");
                    }
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    getSupportFragmentManager().popBackStack();
                    ft.replace(R.id.main_fragment, new HomeFragment(latLng, isVeg, isFilter, address));
                    ft.commit();
                }
            break;*/
        }
    }

    private void checkDevicePermission() {
       /* Dexter.checkPermissions(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {

                noLocation();
            }
            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);*/
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},200);
        }
        else
        {
            noLocation();
        }
    }

    public boolean noLocation() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            enableLoc();
            return true;
        }

        enableLoc();
        return false;
    }

    private void enableLoc() {
        Log.v("enableloc", ">>>>>>>>");
       /* if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {

                            Log.v("Location error ", String.valueOf(connectionResult.getErrorCode()));
                        }
                    }).build();
            new GoogleApiClientInit(mGoogleApiClient);*/
        mGoogleApiClient.connect();

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(30 * 1000);
        mLocationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(HomeActivity.this, REQUEST_LOCATION);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                }
            }
        });
    }
}
