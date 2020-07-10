package com.app.indent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

//features to add:
//1. recycler view to scroll through jokes
//2. fav button to add joke into fav list
//3. share button to share the jokes
//4. cached jokes
//5. refresh upper loading scheme
//6. get new jokes on going down
//7. change theme to dark on UI setting

public class MainActivity extends AppCompatActivity {

    private LinearLayout linearLayout;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<String> dataset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        linearLayout = findViewById(R.id.linear_layout);
        //adding recycler view
        recyclerView = findViewById(R.id.recycler_view);

        setSupportActionBar(myToolbar);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        dataset = new ArrayList<String>();
        getJokes(10);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new MyAdapter(dataset);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE){
                    getJokes(5);
                }
            }
        });

    }

    // create the menu in the app bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    // to add actions to the menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_fav:
                // user chose the "Favorite" action, mark the current item as favorite
                Snackbar.make(linearLayout,"Added to favourite", Snackbar.LENGTH_LONG).show();
                return true;

            case R.id.reset:
                // user setting button for UI settings
                Snackbar.make(linearLayout,"Resetting contents", Snackbar.LENGTH_LONG).show();
                return true;

            case R.id.settings:
                Snackbar.make(linearLayout,"Going to setting", Snackbar.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // take input number of jokes, and add jokes to dataset
    private void getJokes(int number){
        String url = "http://api.icndb.com/jokes/random/"+number;

        // creating 1 MB cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024);

        Network network = new BasicNetwork(new HurlStack());

        RequestQueue queue = new RequestQueue(cache, network);

        queue.start();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //todo: response.toString() to get the data
                        //Snackbar.make(linearLayout, "Downloaded 5 jokes", Snackbar.LENGTH_LONG).show();
                        Log.d("Main Activity", response.toString());

                        try {
                            if(response!=null){
                                JSONArray jsonArray = response.getJSONArray("value");
                                if(jsonArray!=null){
                                    for(int i = 0; i<jsonArray.length();i++){
                                        JSONObject object = jsonArray.getJSONObject(i);
                                        dataset.add(object.getString("joke"));
                                    }
                                }
                            }
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //todo: handle error
                        Log.d("Main Activity", error.toString());
                        Snackbar.make(linearLayout, "Retry!", Snackbar.LENGTH_LONG).show();
                    }

                });
        Toast.makeText(getApplicationContext(), number+" jokes added!", Toast.LENGTH_SHORT).show();
        queue.add(jsonObjectRequest);
    }


}
