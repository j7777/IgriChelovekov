package ua.com.intelligent_games.igrichelovekov.Activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ua.com.intelligent_games.igrichelovekov.Adapter.CatAdapter;
import ua.com.intelligent_games.igrichelovekov.Classes.RecyclerItemClickListener;
import ua.com.intelligent_games.igrichelovekov.Json.JSONParser;
import ua.com.intelligent_games.igrichelovekov.R;

public class CatActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {
    private SearchView searchView;
    private ArrayList CatList = new ArrayList<HashMap<String, String>>();
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CatAdapter adapter;
    private LinearLayout loader;

    private String url;
    private JSONObject obj = null;
    private int loadStart = 0;
    private int loadCount;
    private boolean endLoad = false;

    private SharedPreferences setting;
    private Boolean use_connect = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setting = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        loader = (LinearLayout) findViewById(R.id.loader);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSwipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        if(isOnline()){
                            if (!endLoad) {
                                mSwipeRefreshLayout.setRefreshing(true);
                                new JSONParseCat().execute();
                            } else {
                                mSwipeRefreshLayout.setRefreshing(false);
                                Snackbar snack = Snackbar.make(findViewById(android.R.id.content), R.string.nothing_more_download, Snackbar.LENGTH_LONG);
                                View view = snack.getView();
                                view.setBackgroundColor(Color.rgb(48, 63, 159));
                                TextView textView = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                                textView.setTextColor(Color.WHITE);
                                snack.show();
                            }
                        }
                    }
                });
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.cat_contentView);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        recyclerView = (RecyclerView) findViewById(R.id.cat_scrollableview);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        if(savedInstanceState != null){
            CatList = savedInstanceState.getStringArrayList("CatList");
        }

        adapter = new CatAdapter(CatList);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Map<String, String> hashmap;
                        hashmap = (Map<String, String>) CatList.get(position);

                        Intent intent = new Intent(CatActivity.this, CatFullActivity.class);
                        intent.putExtra("cat_term_taxonomy_id", hashmap.get("term_taxonomy_id"));
                        intent.putExtra("cat_term_id", hashmap.get("term_id"));
                        intent.putExtra("cat_description", hashmap.get("description"));
                        intent.putExtra("cat_custom_excerpt", hashmap.get("custom_excerpt"));
                        intent.putExtra("cat_parent", hashmap.get("parent"));
                        intent.putExtra("cat_count", hashmap.get("count"));
                        intent.putExtra("cat_name", hashmap.get("name"));
                        intent.putExtra("cat_slug", hashmap.get("slug"));
                        intent.putExtra("cat_meta_id", hashmap.get("meta_id"));
                        intent.putExtra("cat_woocommerce_term_id", hashmap.get("woocommerce_term_id"));
                        intent.putExtra("cat_meta_key", hashmap.get("meta_key"));
                        intent.putExtra("cat_meta_value", hashmap.get("meta_value"));
                        intent.putExtra("cat_ID", hashmap.get("ID"));
                        intent.putExtra("cat_post_date", hashmap.get("post_date"));
                        intent.putExtra("cat_post_content", hashmap.get("post_content"));
                        intent.putExtra("cat_post_title", hashmap.get("post_title"));
                        intent.putExtra("cat_post_excerpt", hashmap.get("post_excerpt"));
                        intent.putExtra("cat_post_name", hashmap.get("post_name"));
                        intent.putExtra("cat_image_thumbnail", hashmap.get("image_thumbnail"));
                        intent.putExtra("cat_image_full", hashmap.get("image_full"));
                        startActivity(intent);
                    }
                })
        );

        if(isOnline()){
            new JSONParseCat().execute();
        }
        else{
            loader.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRefresh() {
        if(isOnline()){
            if(!endLoad) {
                new JSONParseCat().execute();
            }
            else{
                mSwipeRefreshLayout.setRefreshing(false);

                if(CatList.size() > 0) {
                    Snackbar snack = Snackbar.make(findViewById(android.R.id.content), R.string.nothing_more_download, Snackbar.LENGTH_LONG);
                    View view = snack.getView();
                    view.setBackgroundColor(Color.rgb(48, 63, 159));
                    TextView textView = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(Color.WHITE);
                    snack.show();
                };
            }
        }
    }

    private class JSONParseCat extends AsyncTask<String, String, JSONObject> {
        @Override
        protected void onPreExecute() {
            loadCount = Integer.parseInt(setting.getString("count_load", "10"));
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            JSONParser jParser = new JSONParser();

            url = "http://intelligent-games.com.ua/?games_api=1&get_categories=1&start="+loadStart+"&end="+loadCount;

            // Getting JSON from URL
            JSONObject json = null;
            try {
                json = jParser.getJSONFromUrl(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return json;
        }
        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if(json != null) {
                    // Getting JSON Array
                    obj = json.getJSONObject("data");
                    JSONArray arr = obj.getJSONArray("arr");

                    if(arr.length() < loadCount){
                        endLoad = true;
                    }

                    HashMap<String, String> hm;
                    ArrayList<HashMap<String, String>> NewCatList = new ArrayList<HashMap<String, String>>();

                    for (int i = 0, max = arr.length(); i < max; i++) {
                        hm = new HashMap<>();

                        JSONObject item = (JSONObject) arr.get(i);

                        hm.put("term_taxonomy_id", item.getString("term_taxonomy_id"));
                        hm.put("term_id", item.getString("term_id"));
                        hm.put("description", item.getString("description"));
                        hm.put("custom_excerpt", item.getString("custom_excerpt"));
                        hm.put("parent", item.getString("parent"));
                        hm.put("count", item.getString("count"));
                        hm.put("name", item.getString("name"));
                        hm.put("slug", item.getString("slug"));
                        hm.put("meta_id", item.getString("meta_id"));
                        hm.put("woocommerce_term_id", item.getString("woocommerce_term_id"));
                        hm.put("meta_key", item.getString("meta_key"));
                        hm.put("meta_value", item.getString("meta_value"));
                        hm.put("ID", item.getString("ID"));
                        hm.put("post_date", item.getString("post_date"));
                        hm.put("post_content", item.getString("post_content"));
                        hm.put("post_title", item.getString("post_title"));
                        hm.put("post_excerpt", item.getString("post_excerpt"));
                        hm.put("post_name", item.getString("post_name"));
                        hm.put("image_thumbnail", item.getString("image_thumbnail"));
                        hm.put("image_full", item.getString("image_full"));

                        NewCatList.add(hm);
                    }

                    for (int i = 0, max = CatList.size(); i < max; i++) {
                        NewCatList.add((HashMap<String, String>) CatList.get(i));
                    }

                    CatList = NewCatList;

                    adapter = new CatAdapter(CatList);
                    recyclerView.setAdapter(adapter);

                    LinearLayoutManager layoutManager = new LinearLayoutManager(CatActivity.this);
                    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                    layoutManager.scrollToPosition(arr.length());
                    recyclerView.setLayoutManager(layoutManager);

                    loadStart = CatList.size();
                }
                else{
                    endLoad = true;

                    if(CatList.size() == 0){
                        Snackbar snack = Snackbar.make(findViewById(android.R.id.content), R.string.error_load, Snackbar.LENGTH_LONG);
                        View view = snack.getView();
                        view.setBackgroundColor(Color.rgb(48, 63, 159));
                        TextView textView = (TextView)view .findViewById(android.support.design.R.id.snackbar_text);
                        textView.setTextColor(Color.WHITE);
                        snack.show();
                    }
                }

                loader.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isOnline() {
        use_connect = setting.getBoolean("use_connect", true);
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            }
            else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE && !use_connect) {
                return true;
            }
            else{
                Snackbar snack = Snackbar.make(findViewById(android.R.id.content), R.string.error_wifi, Snackbar.LENGTH_LONG);
                View view = snack.getView();
                view.setBackgroundColor(Color.rgb(48, 63, 159));
                TextView textView = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(Color.WHITE);
                snack.show();
                return false;
            }
        }
        else {
            Snackbar snack = Snackbar.make(findViewById(android.R.id.content), R.string.error_connect, Snackbar.LENGTH_LONG);
            View view = snack.getView();
            view.setBackgroundColor(Color.rgb(48, 63, 159));
            TextView textView = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.WHITE);
            snack.show();
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("CatList", CatList);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        CatList = savedInstanceState.getStringArrayList("CatList");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.action_search));

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(CatActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_products) {
            Intent intent = new Intent(CatActivity.this, MainActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_categories) {
            Intent intent = new Intent(CatActivity.this, CatActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_news) {
            Intent intent = new Intent(CatActivity.this, NewsActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_about) {
            Intent intent = new Intent(CatActivity.this, AboutActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_cart) {
            Intent intent = new Intent(CatActivity.this, CartActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_history) {
            Intent intent = new Intent(CatActivity.this, HistoryActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_exit) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
