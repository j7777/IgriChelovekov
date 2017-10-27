package ua.com.intelligent_games.igrichelovekov.Activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
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

import java.util.ArrayList;
import java.util.HashMap;

import ua.com.intelligent_games.igrichelovekov.Adapter.HistoryAdapter;
import ua.com.intelligent_games.igrichelovekov.R;
import ua.com.intelligent_games.igrichelovekov.SQL.CartSql;

public class HistoryActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {
    private SearchView searchView;
    private ArrayList HistoryList = new ArrayList<HashMap<String, String>>();
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private HistoryAdapter adapter;
    private LinearLayout loader;

    private CartSql dbHelper;
    private SQLiteDatabase dbDatabase;

    private int loadStart = 0;
    private int loadCount;
    private boolean endLoad = false;

    private SharedPreferences setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setting = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        loader = (LinearLayout) findViewById(R.id.loader);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.history_contentView);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        recyclerView = (RecyclerView) findViewById(R.id.history_scrollableview);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        if(savedInstanceState != null){
            HistoryList = savedInstanceState.getStringArrayList("HistoryList");
        }

        adapter = new HistoryAdapter(HistoryList);
        recyclerView.setAdapter(adapter);

        dbHelper = new CartSql(HistoryActivity.this);
        dbDatabase = dbHelper.getWritableDatabase();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar snack = Snackbar.make(view, R.string.history_confirm_clear, Snackbar.LENGTH_LONG);
                final View viewSnack = snack.getView();
                viewSnack.setBackgroundColor(Color.rgb(48, 63, 159));
                TextView textView = (TextView) viewSnack.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(Color.WHITE);
                Snackbar but_snack = snack.setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try{
                            String dellQuery = "DELETE FROM `" + dbHelper.TABLE_CART + "` WHERE `cart_status` = '1'";
                            dbDatabase.execSQL(dellQuery);

                            adapter.HistoryList.removeAll(HistoryList);
                            adapter.notifyDataSetChanged();
                        }
                        catch(Exception e){
                            Snackbar snack = Snackbar.make(findViewById(android.R.id.content), R.string.error_history_clear, Snackbar.LENGTH_LONG);
                            viewSnack.setBackgroundColor(Color.rgb(48, 63, 159));
                            TextView textView = (TextView)viewSnack .findViewById(android.support.design.R.id.snackbar_text);
                            textView.setTextColor(Color.WHITE);
                            snack.show();
                        }
                    }
                });
                but_snack.setActionTextColor(Color.WHITE);
                snack.show();
            }
        });

        HistoryList = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> hm;
        loadCount = Integer.parseInt(setting.getString("count_load", "10"));
        int counter = 0;

        String query = "SELECT * FROM `" + dbHelper.TABLE_CART + "` WHERE `cart_status` = '1' ORDER BY `id` DESC LIMIT "+loadStart+", "+loadCount;
        Cursor cursor = dbDatabase.rawQuery(query, null);

        if(cursor != null) {
            while (cursor.moveToNext()) {
                hm = new HashMap<>();
                hm.put("id", cursor.getString(cursor.getColumnIndex("id")));
                hm.put("post_ID", cursor.getString(cursor.getColumnIndex("post_ID")));
                hm.put("post_date", cursor.getString(cursor.getColumnIndex("post_date")));
                hm.put("post_content", cursor.getString(cursor.getColumnIndex("post_content")));
                hm.put("post_custom_excerpt", cursor.getString(cursor.getColumnIndex("post_custom_excerpt")));
                hm.put("post_title", cursor.getString(cursor.getColumnIndex("post_title")));
                hm.put("image_thumbnail", cursor.getString(cursor.getColumnIndex("image_thumbnail")));
                hm.put("post_name", cursor.getString(cursor.getColumnIndex("post_name")));
                hm.put("post_guid", cursor.getString(cursor.getColumnIndex("post_guid")));
                hm.put("postmeta_sku", cursor.getString(cursor.getColumnIndex("postmeta_sku")));
                hm.put("postmeta_price", cursor.getString(cursor.getColumnIndex("postmeta_price")));
                hm.put("postmeta_sale_price", cursor.getString(cursor.getColumnIndex("postmeta_sale_price")));
                hm.put("postmeta_regular_price", cursor.getString(cursor.getColumnIndex("postmeta_regular_price")));
                hm.put("postmeta_weight", cursor.getString(cursor.getColumnIndex("postmeta_weight")));
                hm.put("postmeta_length", cursor.getString(cursor.getColumnIndex("postmeta_length")));
                hm.put("postmeta_width", cursor.getString(cursor.getColumnIndex("postmeta_width")));
                hm.put("postmeta_height", cursor.getString(cursor.getColumnIndex("postmeta_height")));
                hm.put("cart_count", cursor.getString(cursor.getColumnIndex("cart_count")));
                hm.put("cart_date", cursor.getString(cursor.getColumnIndex("cart_date")));
                hm.put("cart_date_stamp", cursor.getString(cursor.getColumnIndex("cart_date_stamp")));
                hm.put("cart_order_type", cursor.getString(cursor.getColumnIndex("cart_order_type")));
                hm.put("cart_status", cursor.getString(cursor.getColumnIndex("cart_status")));

                HistoryList.add(hm);
                counter++;
            }
        }
        else{
            Snackbar snack = Snackbar.make(findViewById(android.R.id.content), R.string.error_load, Snackbar.LENGTH_LONG);
            View view = snack.getView();
            view.setBackgroundColor(Color.rgb(48, 63, 159));
            TextView textView = (TextView)view .findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.WHITE);
            snack.show();
        }
        cursor.close();

        if(counter == 0){
            Snackbar snack = Snackbar.make(findViewById(android.R.id.content), R.string.history_empty, Snackbar.LENGTH_LONG);
            View view = snack.getView();
            view.setBackgroundColor(Color.rgb(48, 63, 159));
            TextView textView = (TextView)view .findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.WHITE);
            snack.show();
        }
        else if(counter < loadCount){
            endLoad = true;
        }

        adapter = new HistoryAdapter(HistoryList);
        recyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(HistoryActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(HistoryList.size());
        recyclerView.setLayoutManager(layoutManager);

        loadStart = HistoryList.size();
        loader.setVisibility(View.GONE);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        if(!endLoad) {
            dbHelper = new CartSql(HistoryActivity.this);
            dbDatabase = dbHelper.getWritableDatabase();
            loadCount = Integer.parseInt(setting.getString("count_load", "10"));
            HashMap<String, Object> hm;
            int counter = 0;

            String query = "SELECT * FROM `" + dbHelper.TABLE_CART + "` WHERE `cart_status` = '1' ORDER BY `id` DESC LIMIT "+loadStart+", "+loadCount;
            Cursor cursor = dbDatabase.rawQuery(query, null);

            if(cursor != null) {
                while (cursor.moveToNext()) {
                    hm = new HashMap<>();
                    hm.put("id", cursor.getString(cursor.getColumnIndex("id")));
                    hm.put("post_ID", cursor.getString(cursor.getColumnIndex("post_ID")));
                    hm.put("post_date", cursor.getString(cursor.getColumnIndex("post_date")));
                    hm.put("post_content", cursor.getString(cursor.getColumnIndex("post_content")));
                    hm.put("post_custom_excerpt", cursor.getString(cursor.getColumnIndex("post_custom_excerpt")));
                    hm.put("post_title", cursor.getString(cursor.getColumnIndex("post_title")));
                    hm.put("image_thumbnail", cursor.getString(cursor.getColumnIndex("image_thumbnail")));
                    hm.put("post_name", cursor.getString(cursor.getColumnIndex("post_name")));
                    hm.put("post_guid", cursor.getString(cursor.getColumnIndex("post_guid")));
                    hm.put("postmeta_sku", cursor.getString(cursor.getColumnIndex("postmeta_sku")));
                    hm.put("postmeta_price", cursor.getString(cursor.getColumnIndex("postmeta_price")));
                    hm.put("postmeta_sale_price", cursor.getString(cursor.getColumnIndex("postmeta_sale_price")));
                    hm.put("postmeta_regular_price", cursor.getString(cursor.getColumnIndex("postmeta_regular_price")));
                    hm.put("postmeta_weight", cursor.getString(cursor.getColumnIndex("postmeta_weight")));
                    hm.put("postmeta_length", cursor.getString(cursor.getColumnIndex("postmeta_length")));
                    hm.put("postmeta_width", cursor.getString(cursor.getColumnIndex("postmeta_width")));
                    hm.put("postmeta_height", cursor.getString(cursor.getColumnIndex("postmeta_height")));
                    hm.put("cart_count", cursor.getString(cursor.getColumnIndex("cart_count")));
                    hm.put("cart_date", cursor.getString(cursor.getColumnIndex("cart_date")));
                    hm.put("cart_date_stamp", cursor.getString(cursor.getColumnIndex("cart_date_stamp")));
                    hm.put("cart_order_type", cursor.getString(cursor.getColumnIndex("cart_order_type")));
                    hm.put("cart_status", cursor.getString(cursor.getColumnIndex("cart_status")));

                    HistoryList.add(hm);
                    counter++;
                }
            }
            else{
                Snackbar snack = Snackbar.make(findViewById(android.R.id.content), R.string.error_load, Snackbar.LENGTH_LONG);
                View view = snack.getView();
                view.setBackgroundColor(Color.rgb(48, 63, 159));
                TextView textView = (TextView)view .findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(Color.WHITE);
                snack.show();
            }
            cursor.close();

            if(counter < loadCount || counter == 0){
                endLoad = true;
            }

            adapter = new HistoryAdapter(HistoryList);
            recyclerView.setAdapter(adapter);
            mSwipeRefreshLayout.setRefreshing(false);
        }
        else{
            mSwipeRefreshLayout.setRefreshing(false);

            if(HistoryList.size() > 0) {
                Snackbar snack = Snackbar.make(findViewById(android.R.id.content), R.string.nothing_more_download, Snackbar.LENGTH_LONG);
                View view = snack.getView();
                view.setBackgroundColor(Color.rgb(48, 63, 159));
                TextView textView = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(Color.WHITE);
                snack.show();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("HistoryList", HistoryList);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        HistoryList = savedInstanceState.getStringArrayList("HistoryList");
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
            Intent intent = new Intent(HistoryActivity.this, SettingsActivity.class);
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
            Intent intent = new Intent(HistoryActivity.this, MainActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_categories) {
            Intent intent = new Intent(HistoryActivity.this, CatActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_news) {
            Intent intent = new Intent(HistoryActivity.this, NewsActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_about) {
            Intent intent = new Intent(HistoryActivity.this, AboutActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_cart) {
            Intent intent = new Intent(HistoryActivity.this, CartActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_history) {
            Intent intent = new Intent(HistoryActivity.this, HistoryActivity.class);
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
