package ua.com.intelligent_games.igrichelovekov.Activity;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ua.com.intelligent_games.igrichelovekov.Adapter.CartAdapter;
import ua.com.intelligent_games.igrichelovekov.Json.JSONParser;
import ua.com.intelligent_games.igrichelovekov.R;
import ua.com.intelligent_games.igrichelovekov.SQL.CartSql;

public class CartActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {
    private SearchView searchView;
    private ArrayList CartList = new ArrayList<HashMap<String, String>>();
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CartAdapter adapter;
    private String catName = "";
    private LinearLayout loader;
    private CartSql dbHelper;
    private SQLiteDatabase dbDatabase;

    private String str_phone1;
    private String str_phone2;
    private String str_email;
    private String str_desc;
    private Intent intent;

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
        setContentView(R.layout.activity_cart);
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

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.cart_contentView);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        recyclerView = (RecyclerView) findViewById(R.id.cart_scrollableview);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        if(savedInstanceState != null){
            CartList = savedInstanceState.getStringArrayList("CartList");
        }

        adapter = new CartAdapter(CartList);
        recyclerView.setAdapter(adapter);

        dbHelper = new CartSql(CartActivity.this);
        dbDatabase = dbHelper.getWritableDatabase();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isOnline()){
                    new JSONParseData().execute();
                }
                else{
                    loader.setVisibility(View.GONE);
                }
            }
        });

        CartList = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> hm;
        loadCount = Integer.parseInt(setting.getString("count_load", "10"));
        int counter = 0;

        String query = "SELECT * FROM `" + dbHelper.TABLE_CART + "` WHERE `cart_status` = '0' ORDER BY `id` DESC LIMIT "+loadStart+", "+loadCount;
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

                CartList.add(hm);
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
            Snackbar snack = Snackbar.make(findViewById(android.R.id.content), R.string.cart_empty, Snackbar.LENGTH_LONG);
            View view = snack.getView();
            view.setBackgroundColor(Color.rgb(48, 63, 159));
            TextView textView = (TextView)view .findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.WHITE);
            snack.show();
        }
        else if(counter < loadCount){
            endLoad = true;
        }

        adapter = new CartAdapter(CartList);
        recyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(CartActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(CartList.size());
        recyclerView.setLayoutManager(layoutManager);

        loadStart = CartList.size();
        loader.setVisibility(View.GONE);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        if(!endLoad) {
            dbHelper = new CartSql(CartActivity.this);
            dbDatabase = dbHelper.getWritableDatabase();
            loadCount = Integer.parseInt(setting.getString("count_load", "10"));
            HashMap<String, Object> hm;
            int counter = 0;

            String query = "SELECT * FROM `" + dbHelper.TABLE_CART + "` WHERE `cart_status` = '0' ORDER BY `id` DESC LIMIT "+loadStart+", "+loadCount;
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

                    CartList.add(hm);
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

            adapter = new CartAdapter(CartList);
            recyclerView.setAdapter(adapter);
            mSwipeRefreshLayout.setRefreshing(false);
        }
        else{
            mSwipeRefreshLayout.setRefreshing(false);

            if(CartList.size() > 0) {
                Snackbar snack = Snackbar.make(findViewById(android.R.id.content), R.string.nothing_more_download, Snackbar.LENGTH_LONG);
                View view = snack.getView();
                view.setBackgroundColor(Color.rgb(48, 63, 159));
                TextView textView = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(Color.WHITE);
                snack.show();
            }
        }
    }

    public static class Item{
        public final String text;
        public final int icon;
        public Item(String text, Integer icon) {
            this.text = text;
            this.icon = icon;
        }
        @Override
        public String toString() {
            return text;
        }
    }

    private void onClickList(int which){
        int totalSum = 0;
        int totalCount = 0;
        String order = getResources().getString(R.string.order_str) + ":\n";

        if(which != 5) {
            for (int i = 0, max = adapter.CartList.size(); i < max; i++) {
                try {
                    Map<String, String> hashmap = (Map<String, String>) adapter.CartList.get(i);
                    int sum = Integer.parseInt(hashmap.get("postmeta_price")) * Integer.parseInt(hashmap.get("cart_count"));
                    totalSum += sum;
                    totalCount += Integer.parseInt(hashmap.get("cart_count"));
                    order += hashmap.get("post_title") +
                            " - "
                            + hashmap.get("cart_count") +
                            " " +
                            getResources().getString(R.string.count_str) +
                            " x " +
                            hashmap.get("postmeta_price") +
                            " " +
                            getResources().getString(R.string.grn) +
                            " = " +
                            sum +
                            " " +
                            getResources().getString(R.string.grn) +
                            "\n";

                    String updateQuery = "UPDATE `" + dbHelper.TABLE_CART + "` SET `cart_status` = '1', `cart_order_type` = '" + which + "' WHERE `id` = '" + hashmap.get("id") + "'";
                    dbDatabase.execSQL(updateQuery);
                } catch (Exception e) {
                }
            }

            adapter.CartList.removeAll(CartList);
            adapter.notifyDataSetChanged();

            order += getResources().getString(R.string.total_count_str) + ": " + totalCount + " " + getResources().getString(R.string.count_str) + "\n";
            order += getResources().getString(R.string.total_sum_str) + ": " + totalSum + " " + getResources().getString(R.string.grn);
        }

        switch (which) {
            case 0:
                //Позвонить
                intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + str_phone1));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                break;
            case 1:
                //Позвонить
                intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + str_phone2));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                break;
            case 2:
                if(totalSum > 0) {
                    //Отправить смс
                    intent = new Intent(Intent.ACTION_SEND);
                    intent.setData(Uri.parse("smsto:"));
                    intent.putExtra("address", str_phone1);
                    intent.putExtra("sms_body", order);
                    intent.setType("text/plain");
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
                else{
                    Snackbar snack = Snackbar.make(findViewById(android.R.id.content), R.string.error_send_cart, Snackbar.LENGTH_LONG);
                    View view = snack.getView();
                    view.setBackgroundColor(Color.rgb(48, 63, 159));
                    TextView textView = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(Color.WHITE);
                    snack.show();
                }
                break;
            case 3:
                if(totalSum > 0) {
                    //Отправить смс
                    intent = new Intent(Intent.ACTION_SEND);
                    intent.setData(Uri.parse("smsto:"));
                    intent.putExtra("address", str_phone2);
                    intent.putExtra("sms_body", order);
                    intent.setType("text/plain");
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
                else{
                    Snackbar snack = Snackbar.make(findViewById(android.R.id.content), R.string.error_send_cart, Snackbar.LENGTH_LONG);
                    View view = snack.getView();
                    view.setBackgroundColor(Color.rgb(48, 63, 159));
                    TextView textView = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(Color.WHITE);
                    snack.show();
                }
                break;
            case 4:
                if(totalSum > 0) {
                    //Отправить email
                    intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:" + str_email));
                    intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mess_send));
                    intent.putExtra(Intent.EXTRA_TEXT, order);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
                else{
                    Snackbar snack = Snackbar.make(findViewById(android.R.id.content), R.string.error_send_cart, Snackbar.LENGTH_LONG);
                    View view = snack.getView();
                    view.setBackgroundColor(Color.rgb(48, 63, 159));
                    TextView textView = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(Color.WHITE);
                    snack.show();
                }
                break;
        }
    }

    private class JSONParseData extends AsyncTask<String, String, JSONObject> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            JSONParser jParser = new JSONParser();

            url = "http://intelligent-games.com.ua/?games_api=1&get_about=1";

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

                    for (int i = 0, max = arr.length(); i < max; i++) {
                        JSONObject item = (JSONObject) arr.get(i);

                        str_phone1 = item.getString("phone1");
                        str_phone2 = item.getString("phone2");
                        str_email = item.getString("email");
                        str_desc = item.getString("desc");
                    }

                    final Item[] items = {
                            new Item(str_phone1, R.drawable.phone_gray),
                            new Item(str_phone2, R.drawable.phone_gray),
                            new Item(str_phone1, R.drawable.sms_gray),
                            new Item(str_phone2, R.drawable.sms_gray),
                            new Item(str_email, R.drawable.email_gray),
                            new Item(getString(R.string.send_cancel), R.drawable.cancel_gray),
                    };

                    ListAdapter adapter = new ArrayAdapter<Item>(
                            CartActivity.this,
                            android.R.layout.select_dialog_item,
                            android.R.id.text1,
                            items){
                        public View getView(int position, View convertView, ViewGroup parent) {
                            //Use super class to create the View
                            View v = super.getView(position, convertView, parent);
                            TextView tv = (TextView)v.findViewById(android.R.id.text1);

                            //Put the image on the TextView
                            tv.setCompoundDrawablesWithIntrinsicBounds(items[position].icon, 0, 0, 0);

                            //Add margin between image and text (support various screen densities)
                            int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
                            tv.setCompoundDrawablePadding(dp5);

                            return v;
                        }
                    };

                    new AlertDialog.Builder(CartActivity.this)
                            .setTitle(R.string.sel_send)
                            .setAdapter(adapter, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    onClickList(item);
                                }
                            }).show();
                }
                else{
                    Snackbar snack = Snackbar.make(findViewById(android.R.id.content), R.string.error_load, Snackbar.LENGTH_LONG);
                    View view = snack.getView();
                    view.setBackgroundColor(Color.rgb(48, 63, 159));
                    TextView textView = (TextView)view .findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(Color.WHITE);
                    snack.show();
                }
                loader.setVisibility(View.GONE);
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
        outState.putStringArrayList("CartList", CartList);
        outState.putString("str_phone1", str_phone1);
        outState.putString("str_phone2", str_phone2);
        outState.putString("str_email", str_email);
        outState.putString("str_desc", str_desc);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        CartList = savedInstanceState.getStringArrayList("CartList");
        str_phone1 = savedInstanceState.getString("str_phone1");
        str_phone2 = savedInstanceState.getString("str_phone2");
        str_email = savedInstanceState.getString("str_email");
        str_desc = savedInstanceState.getString("str_desc");
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
            Intent intent = new Intent(CartActivity.this, SettingsActivity.class);
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
            Intent intent = new Intent(CartActivity.this, MainActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_categories) {
            Intent intent = new Intent(CartActivity.this, CatActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_news) {
            Intent intent = new Intent(CartActivity.this, NewsActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_about) {
            Intent intent = new Intent(CartActivity.this, AboutActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_cart) {
            Intent intent = new Intent(CartActivity.this, CartActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_history) {
            Intent intent = new Intent(CartActivity.this, HistoryActivity.class);
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
