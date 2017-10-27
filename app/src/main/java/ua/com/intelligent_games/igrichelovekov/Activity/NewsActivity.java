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

import ua.com.intelligent_games.igrichelovekov.Adapter.NewsAdapter;
import ua.com.intelligent_games.igrichelovekov.Classes.RecyclerItemClickListener;
import ua.com.intelligent_games.igrichelovekov.Json.JSONParser;
import ua.com.intelligent_games.igrichelovekov.R;

public class NewsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {
    private SearchView searchView;
    private ArrayList NewsList = new ArrayList<HashMap<String, String>>();
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private NewsAdapter adapter;
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
        setContentView(R.layout.activity_news);
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
                                new JSONParseNews().execute();
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

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.news_contentView);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        recyclerView = (RecyclerView) findViewById(R.id.news_scrollableview);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        if(savedInstanceState != null){
            NewsList = savedInstanceState.getStringArrayList("NewsList");
        }

        adapter = new NewsAdapter(NewsList);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Map<String, String> hashmap;
                        hashmap = (Map<String, String>) NewsList.get(position);

                        Intent intent = new Intent(NewsActivity.this, NewsFullActivity.class);
                        intent.putExtra("news_title", hashmap.get("title"));
                        intent.putExtra("news_date", hashmap.get("date"));
                        intent.putExtra("news_excerpt", hashmap.get("excerpt"));
                        intent.putExtra("news_content", hashmap.get("content"));
                        intent.putExtra("news_url", hashmap.get("url"));
                        intent.putExtra("news_thumbnail", hashmap.get("thumbnail"));
                        intent.putExtra("news_image", hashmap.get("image"));
                        startActivity(intent);
                    }
                })
        );

        if(isOnline()){
            new JSONParseNews().execute();
        }
        else{
            loader.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRefresh() {
        if(isOnline()){
            if(!endLoad) {
                new JSONParseNews().execute();
            }
            else{
                mSwipeRefreshLayout.setRefreshing(false);

                if(NewsList.size() > 0) {
                    Snackbar snack = Snackbar.make(findViewById(android.R.id.content), R.string.nothing_more_download, Snackbar.LENGTH_LONG);
                    View view = snack.getView();
                    view.setBackgroundColor(Color.rgb(48, 63, 159));
                    TextView textView = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(Color.WHITE);
                    snack.show();
                }
            }
        }
    }

    private class JSONParseNews extends AsyncTask<String, String, JSONObject> {
        @Override
        protected void onPreExecute() {
            loadCount = Integer.parseInt(setting.getString("count_load", "10"));
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            JSONParser jParser = new JSONParser();

            url = "http://intelligent-games.com.ua/?games_api=1&get_news=1&start="+loadStart+"&end="+loadCount;

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
                    ArrayList<HashMap<String, String>> NewNewsList = new ArrayList<HashMap<String, String>>();

                    for (int i = 0, max = arr.length(); i < max; i++) {
                        hm = new HashMap<>();

                        JSONObject item = (JSONObject) arr.get(i);

                        hm.put("title", item.getString("title"));
                        hm.put("date", item.getString("date"));
                        hm.put("excerpt", item.getString("excerpt"));
                        hm.put("content", item.getString("content"));
                        hm.put("url", item.getString("url"));
                        hm.put("thumbnail", item.getString("thumbnail"));
                        hm.put("image", item.getString("image"));

                        NewNewsList.add(hm);
                    }

                    for (int i = 0, max = NewsList.size(); i < max; i++) {
                        NewNewsList.add((HashMap<String, String>) NewsList.get(i));
                    }

                    NewsList = NewNewsList;

                    adapter = new NewsAdapter(NewsList);
                    recyclerView.setAdapter(adapter);

                    LinearLayoutManager layoutManager = new LinearLayoutManager(NewsActivity.this);
                    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                    layoutManager.scrollToPosition(arr.length());
                    recyclerView.setLayoutManager(layoutManager);

                    loadStart = NewsList.size();
                }
                else{
                    endLoad = true;

                    if(NewsList.size() == 0){
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
        outState.putStringArrayList("NewsList", NewsList);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        NewsList = savedInstanceState.getStringArrayList("NewsList");
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
            Intent intent = new Intent(NewsActivity.this, SettingsActivity.class);
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
            Intent intent = new Intent(NewsActivity.this, MainActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_categories) {
            Intent intent = new Intent(NewsActivity.this, CatActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_news) {
            Intent intent = new Intent(NewsActivity.this, NewsActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_about) {
            Intent intent = new Intent(NewsActivity.this, AboutActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_cart) {
            Intent intent = new Intent(NewsActivity.this, CartActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_history) {
            Intent intent = new Intent(NewsActivity.this, HistoryActivity.class);
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
