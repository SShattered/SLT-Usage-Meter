package com.sshattered.sltusagemeter;

import static android.content.Context.CARRIER_CONFIG_SERVICE;
import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class SLTHandler {
    private final String TAG = "SLTHandler";
    @SuppressLint("StaticFieldLeak")
    private static Context _context = null;
    @SuppressLint("StaticFieldLeak")
    private static volatile SLTHandler Instance = null;
    private final RequestQueue requestQueue;
    private final ConnDetails connDetails;
    private final HashMap<String, String> headers;
    private final HashMap<String, String> params;
    public PackUsage _packUsage;

    public static SLTHandler Instance(Context context) {
        _context = context;
        if (Instance == null) {
            synchronized (SLTHandler.class){
                if(Instance == null)
                    Instance = new SLTHandler();
            }
        }
        return Instance;
    }

    private SLTHandler(){
        connDetails = new ConnDetails();
        headers = new HashMap<>();
        params = new HashMap<>();
        _packUsage = new PackUsage();
        requestQueue = Volley.newRequestQueue(_context);

        headers.put("x-ibm-client-id", "d79fda9a-2e2c-4436-9be2-29748ff00983");
        headers.put("HOST", "omniscapp.slt.lk");
        //headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Connection", "close");
    }

    public boolean GetLoginTelephone() {
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        };

        try {
            params.put("UserName", connDetails.getEmail());
            params.put("Password", connDetails.getPassword());
            params.put("channelID", "MOBILE");
            params.put("grant_type", "password");
            params.put("fiebaseId",
                    "cLTgbTE3Th-PoEBwntImU8%3AAPA91bG7k6PiyO16w3XC3GjUWNpg_F-LkWULuuEqVbopHFZZjLWhnH5m86CHg11IBauJJYFCn-_ExuKdgsUPx2CwLDyLbyVnEgw5lom9qvhzJzq6-DICnCGgm143oiVRd9JYinGJIorc");
            params.put("appVersion", "1.7.0");
            params.put("osType", "Android");

            //Get access token
            String result = RequestSync(Request.Method.POST,
                    SLTResources.MAIN_URL + SLTResources.LOGIN_URL,
                    headers,
                    params,
                    errorListener);
            Log.d(TAG, result);
            myShit(result);
            connDetails.setAccessToken(new JSONObject(result).getString("accessToken"));
            Log.d(TAG, connDetails.getAccessToken());

            //Get account details
            headers.put("authorization", "Bearer " + connDetails.getAccessToken());
            params.clear();
            result = RequestSync(Request.Method.GET,
                    SLTResources.MAIN_URL + SLTResources.ACC_DETAILS_URL +
                    "?username=" + connDetails.getEmail(),
                    headers,
                    params,
                    errorListener);
            Log.d(TAG, result);
            GetTelephone(result);

            params.clear();
            result = RequestSync(Request.Method.GET,
                    SLTResources.MAIN_URL + SLTResources.ACC_SERVICE_DETAILS_URL +
                    "?telephoneNo=" + connDetails.getTelephoneNo(),
                    headers,
                    params,
                    errorListener);
            JSONObject jsonObject =  new JSONObject(result).getJSONObject("dataBundle");
            JSONArray jsonArray = jsonObject.getJSONArray("listofBBService");
            JSONObject jsonObject1 = jsonArray.getJSONObject(0);
            Log.d(TAG, jsonObject1.getString("serviceID"));
            connDetails.setServiceId(jsonObject1.getString("serviceID"));

            ////////////////////////////////////
            try{
                params.clear();
                result = RequestSync(Request.Method.GET,
                        SLTResources.MAIN_URL + SLTResources.USAGE_SUM_URL +
                                "?subscriberID=" + connDetails.getServiceId(),
                        headers,
                        params,
                        errorListener);
                float pack, total, day, dayLimit, night, nightLimit;
                jsonObject = new JSONObject(result).getJSONObject("dataBundle");
                jsonObject1 = jsonObject.getJSONObject("my_package_summary");
                pack = (float)jsonObject1.getDouble("limit");
                total = (float)jsonObject1.getDouble("used");
                JSONObject jsonObject2 = jsonObject.getJSONObject("my_package_info");

                //Day
                jsonArray = jsonObject2.getJSONArray("usageDetails");
                JSONObject jsonObject3 = jsonArray.getJSONObject(0);
                day = (float)jsonObject3.getDouble("used");
                dayLimit = (float)jsonObject3.getDouble("limit");

                //Night
                nightLimit = pack - dayLimit;
                night = total - day;
                Log.d(TAG, jsonObject3.getString("used"));

                _packUsage.day = dayLimit - day;
                _packUsage.pack = pack;
                _packUsage.night = nightLimit - night;
                _packUsage.total = pack - total;

                SaveDetails();
            }catch (Exception exception){
                Log.d(TAG, exception.toString());
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private void GetTelephone(String result){
        try{
            JSONObject jsonObject = new JSONObject(result);
            boolean success = jsonObject.getBoolean("isSuccess");
            if (success) {
                JSONArray jArray = jsonObject.getJSONArray("dataBundle");
                JSONObject jObj = jArray.getJSONObject(0);
                Log.d("MainActivity", jObj.getString("telephoneno"));
                connDetails.setTelephoneNo(jObj.getString("telephoneno"));
            }
        }catch (Exception e){
            Log.d(TAG, e.toString());
        }
    }

    private void myShit(String data){
        String output = "";
        try {
            final InflaterInputStream infStream = new InflaterInputStream(new ByteArrayInputStream(data.getBytes()), new Inflater(true));
            final GZIPInputStream gStream = new GZIPInputStream(infStream);
            final InputStreamReader reader = new InputStreamReader(gStream);
            final BufferedReader in = new BufferedReader(reader);
            String read;
            while ((read = in.readLine()) != null) {
                output += read;
            }
            reader.close();
            in.close();
            gStream.close();
        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }
    }

    public String RequestSync(int method,
                              String URL,
                              HashMap<String, String> headers,
                              HashMap<String, String> params,
                              Response.ErrorListener errorListener) {

        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest request = new StringRequest(method, URL, future, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };
        request.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 15000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 0;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        requestQueue.add(request);


        try {
            return future.get(30, TimeUnit.SECONDS); // this will block
        } catch (InterruptedException e) {
            Log.e("Retrieve cards api call interrupted.", Objects.requireNonNull(e.getMessage()));
            errorListener.onErrorResponse(new VolleyError(e));
        } catch (ExecutionException e) {
            Log.e("Retrieve cards api call failed.", Objects.requireNonNull(e.getMessage()));
            errorListener.onErrorResponse(new VolleyError(e));
        } catch (TimeoutException e) {
            Log.e("Retrieve cards api call timed out.", Objects.requireNonNull(e.getMessage()));
            errorListener.onErrorResponse(new VolleyError(e));
        }
        return null;
    }

    public String GZipRequest(int method,
                              String URL,
                              HashMap<String, String> headers,
                              HashMap<String, String> params,
                              Response.ErrorListener errorListener) {

        RequestFuture<String> future = RequestFuture.newFuture();
        GZipRequest request = new GZipRequest(method, URL, future, errorListener){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };
        requestQueue.add(request);


        try {
            return future.get(30, TimeUnit.SECONDS); // this will block
        } catch (InterruptedException e) {
            Log.e("Retrieve cards api call interrupted.", Objects.requireNonNull(e.getMessage()));
            errorListener.onErrorResponse(new VolleyError(e));
        } catch (ExecutionException e) {
            Log.e("Retrieve cards api call failed.", Objects.requireNonNull(e.getMessage()));
            errorListener.onErrorResponse(new VolleyError(e));
        } catch (TimeoutException e) {
            Log.e("Retrieve cards api call timed out.", Objects.requireNonNull(e.getMessage()));
            errorListener.onErrorResponse(new VolleyError(e));
        }
        return null;
    }

    public void SetLogin(String email, String password){
        this.connDetails.setEmail(email);
        this.connDetails.setPassword(password);
    }

    public void SaveDetails(){
        SharedPreferences sp = _context.getSharedPreferences(TAG, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("email", connDetails.getEmail());
        editor.putString("password", connDetails.getPassword());
        editor.apply();
    }

    public String[] GetDetails(){
        SharedPreferences sp = _context.getSharedPreferences(TAG, MODE_PRIVATE);
        return new String[] {sp.getString("email", ""), sp.getString("password", "")};
    }
}