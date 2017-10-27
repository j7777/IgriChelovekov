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
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ua.com.intelligent_games.igrichelovekov.Adapter.ProductAdapter;
import ua.com.intelligent_games.igrichelovekov.Classes.RecyclerItemClickListener;
import ua.com.intelligent_games.igrichelovekov.Json.JSONParser;
import ua.com.intelligent_games.igrichelovekov.R;

public class SearchActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {
    private SearchView searchView;
    private ArrayList ProductList = new ArrayList<HashMap<String, String>>();
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProductAdapter adapter;
    private String query;
    private TextView textQuery;
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
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setting = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        loader = (LinearLayout) findViewById(R.id.loader);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isOnline()){
                    loadStart = 0;
                    ProductList = new ArrayList<HashMap<String, String>>();

                    mSwipeRefreshLayout.setRefreshing(true);
                    new JSONParseProduct().execute();
                }
            }
        });

        textQuery = (TextView) findViewById(R.id.textQuery);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.product_contentView);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        recyclerView = (RecyclerView) findViewById(R.id.product_scrollableview);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        if(savedInstanceState != null){
            ProductList = savedInstanceState.getStringArrayList("ProductList");
        }

        adapter = new ProductAdapter(ProductList);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Map<String, String> hashmap;
                        hashmap = (Map<String, String>) ProductList.get(position);

                        Intent intent = new Intent(SearchActivity.this, ProductFullActivity.class);
                        intent.putExtra("post_ID", hashmap.get("post_ID"));
                        intent.putExtra("post_date", hashmap.get("post_date"));
                        intent.putExtra("post_content", hashmap.get("post_content"));
                        intent.putExtra("post_custom_excerpt", hashmap.get("post_custom_excerpt"));
                        intent.putExtra("post_title", hashmap.get("post_title"));
                        intent.putExtra("post_name", hashmap.get("post_name"));
                        intent.putExtra("post_guid", hashmap.get("post_guid"));

                        intent.putExtra("cat_object_id", hashmap.get("cat_object_id"));
                        intent.putExtra("cat_term_taxonomy_id", hashmap.get("cat_term_taxonomy_id"));
                        intent.putExtra("cat_term_id", hashmap.get("cat_term_id"));
                        intent.putExtra("cat_description", hashmap.get("cat_description"));
                        intent.putExtra("cat_parent", hashmap.get("cat_parent"));
                        intent.putExtra("cat_count", hashmap.get("cat_count"));
                        intent.putExtra("cat_name", hashmap.get("cat_name"));
                        intent.putExtra("cat_slug", hashmap.get("cat_slug"));

                        intent.putExtra("postmeta_sku", hashmap.get("postmeta_sku"));
                        intent.putExtra("postmeta_downloadable", hashmap.get("postmeta_downloadable"));
                        intent.putExtra("postmeta_virtual", hashmap.get("postmeta_virtual"));
                        intent.putExtra("postmeta_price", hashmap.get("postmeta_price"));
                        intent.putExtra("postmeta_visibility", hashmap.get("postmeta_visibility"));
                        intent.putExtra("postmeta_stock", hashmap.get("postmeta_stock"));
                        intent.putExtra("postmeta_stock_status", hashmap.get("postmeta_stock_status"));
                        intent.putExtra("postmeta_backorders", hashmap.get("postmeta_backorders"));
                        intent.putExtra("postmeta_manage_stock", hashmap.get("postmeta_manage_stock"));
                        intent.putExtra("postmeta_sale_price", hashmap.get("postmeta_sale_price"));
                        intent.putExtra("postmeta_regular_price", hashmap.get("postmeta_regular_price"));
                        intent.putExtra("postmeta_weight", hashmap.get("postmeta_weight"));
                        intent.putExtra("postmeta_length", hashmap.get("postmeta_length"));
                        intent.putExtra("postmeta_height", hashmap.get("postmeta_height"));
                        intent.putExtra("postmeta_height", hashmap.get("postmeta_height"));
                        intent.putExtra("postmeta_tax_status", hashmap.get("postmeta_tax_status"));
                        intent.putExtra("postmeta_tax_class", hashmap.get("postmeta_tax_class"));
                        intent.putExtra("postmeta_sale_price_dates_from", hashmap.get("postmeta_sale_price_dates_from"));
                        intent.putExtra("postmeta_sale_price_dates_to", hashmap.get("postmeta_sale_price_dates_to"));
                        intent.putExtra("postmeta_min_variation_price", hashmap.get("postmeta_min_variation_price"));
                        intent.putExtra("postmeta_max_variation_price", hashmap.get("postmeta_max_variation_price"));
                        intent.putExtra("postmeta_min_variation_regular_price", hashmap.get("postmeta_min_variation_regular_price"));
                        intent.putExtra("postmeta_max_variation_regular_price", hashmap.get("postmeta_max_variation_regular_price"));
                        intent.putExtra("postmeta_min_variation_sale_price", hashmap.get("postmeta_min_variation_sale_price"));
                        intent.putExtra("postmeta_max_variation_sale_price", hashmap.get("postmeta_max_variation_sale_price"));

                        intent.putExtra("postmeta_featured", hashmap.get("postmeta_featured"));
                        intent.putExtra("postmeta_file_path", hashmap.get("postmeta_file_path"));
                        intent.putExtra("postmeta_download_limit", hashmap.get("postmeta_download_limit"));
                        intent.putExtra("postmeta_download_expiry", hashmap.get("postmeta_download_expiry"));
                        intent.putExtra("postmeta_product_url", hashmap.get("postmeta_product_url"));
                        intent.putExtra("postmeta_button_text", hashmap.get("postmeta_button_text"));
                        intent.putExtra("postmeta_total_sales", hashmap.get("postmeta_total_sales"));
                        intent.putExtra("postmeta_edit_lock", hashmap.get("postmeta_edit_lock"));
                        intent.putExtra("postmeta_edit_last", hashmap.get("postmeta_edit_last"));
                        intent.putExtra("postmeta_purchase_note", hashmap.get("postmeta_purchase_note"));
                        intent.putExtra("postmeta_product_attributes", hashmap.get("postmeta_product_attributes"));
                        intent.putExtra("postmeta_sold_individually", hashmap.get("postmeta_sold_individually"));
                        intent.putExtra("postmeta_product_image_gallery", hashmap.get("postmeta_product_image_gallery"));
                        intent.putExtra("postmeta_layout", hashmap.get("postmeta_layout"));
                        intent.putExtra("postmeta_thumbnail_id", hashmap.get("postmeta_thumbnail_id"));

                        intent.putExtra("image_thumbnail", hashmap.get("image_thumbnail"));
                        intent.putExtra("image_full", hashmap.get("image_full"));
                        startActivity(intent);
                    }
                })
        );

        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }
    }

    private void doMySearch(String query) {
        if(isOnline()){
            textQuery.setText(getString(R.string.string_search) + " " + query);
            mSwipeRefreshLayout.setRefreshing(true);
            new JSONParseProduct().execute();
        }
        else{
            loader.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRefresh() {
        if(isOnline()){
            if(!endLoad) {
                new JSONParseProduct().execute();
            }
            else{
                mSwipeRefreshLayout.setRefreshing(false);

                if(ProductList.size() > 0) {
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

    private class JSONParseProduct extends AsyncTask<String, String, JSONObject> {
        @Override
        protected void onPreExecute() {
            loadCount = Integer.parseInt(setting.getString("count_load", "10"));
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            JSONParser jParser = new JSONParser();

            try {
                url = "http://intelligent-games.com.ua/?games_api=1&get_search="+ URLEncoder.encode(query, "UTF-8")+"&start="+loadStart+"&end="+loadCount;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

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
                    ArrayList<HashMap<String, String>> NewProductList = new ArrayList<HashMap<String, String>>();

                    for (int i = 0, max = arr.length(); i < max; i++) {
                        hm = new HashMap<>();

                        JSONObject item = (JSONObject) arr.get(i);
                        JSONObject post = (JSONObject) item.get("post");
                        JSONObject cat = (JSONObject) item.get("cat");
                        JSONObject postmeta = (JSONObject) item.get("postmeta");
                        JSONObject thumbnail = (JSONObject) item.get("thumbnail");

                        hm.put("post_ID", post.getString("ID"));
                        hm.put("post_date", post.getString("post_date"));
                        hm.put("post_content", post.getString("post_content"));
                        hm.put("post_custom_excerpt", post.getString("post_custom_excerpt"));
                        hm.put("post_title", post.getString("post_title"));
                        hm.put("post_name", post.getString("post_name"));
                        hm.put("post_guid", post.getString("guid"));

                        hm.put("cat_object_id", cat.getString("object_id"));
                        hm.put("cat_term_taxonomy_id", cat.getString("term_taxonomy_id"));
                        hm.put("cat_term_id", cat.getString("term_id"));
                        hm.put("cat_description", cat.getString("description"));
                        hm.put("cat_parent", cat.getString("parent"));
                        hm.put("cat_count", cat.getString("count"));
                        hm.put("cat_name", cat.getString("name"));
                        hm.put("cat_slug", cat.getString("slug"));

                        hm.put("postmeta_sku", postmeta.getString("_sku"));
                        hm.put("postmeta_downloadable", postmeta.getString("_downloadable"));
                        hm.put("postmeta_virtual", postmeta.getString("_virtual"));
                        hm.put("postmeta_price", postmeta.getString("_price"));
                        hm.put("postmeta_visibility", postmeta.getString("_visibility"));
                        hm.put("postmeta_stock", postmeta.getString("_stock"));
                        hm.put("postmeta_stock_status", postmeta.getString("_stock_status"));
                        hm.put("postmeta_backorders", postmeta.getString("_backorders"));
                        hm.put("postmeta_manage_stock", postmeta.getString("_manage_stock"));
                        hm.put("postmeta_sale_price", postmeta.getString("_sale_price"));
                        hm.put("postmeta_regular_price", postmeta.getString("_regular_price"));
                        hm.put("postmeta_weight", postmeta.getString("_weight"));
                        hm.put("postmeta_length", postmeta.getString("_length"));
                        hm.put("postmeta_width", postmeta.getString("_width"));
                        hm.put("postmeta_height", postmeta.getString("_height"));
                        hm.put("postmeta_tax_status", postmeta.getString("_tax_status"));
                        hm.put("postmeta_tax_class", postmeta.getString("_tax_class"));
                        hm.put("postmeta_sale_price_dates_from", postmeta.getString("_sale_price_dates_from"));
                        hm.put("postmeta_sale_price_dates_to", postmeta.getString("_sale_price_dates_to"));
                        hm.put("postmeta_min_variation_price", postmeta.getString("_min_variation_price"));
                        hm.put("postmeta_max_variation_price", postmeta.getString("_max_variation_price"));
                        hm.put("postmeta_min_variation_regular_price", postmeta.getString("_min_variation_regular_price"));
                        hm.put("postmeta_max_variation_regular_price", postmeta.getString("_max_variation_regular_price"));
                        hm.put("postmeta_min_variation_sale_price", postmeta.getString("_min_variation_sale_price"));
                        hm.put("postmeta_max_variation_sale_price", postmeta.getString("_max_variation_sale_price"));
                        hm.put("postmeta_featured", postmeta.getString("_featured"));
                        hm.put("postmeta_file_path", postmeta.getString("_file_path"));
                        hm.put("postmeta_download_limit", postmeta.getString("_download_limit"));
                        hm.put("postmeta_download_expiry", postmeta.getString("_download_expiry"));
                        hm.put("postmeta_product_url", postmeta.getString("_product_url"));
                        hm.put("postmeta_button_text", postmeta.getString("_button_text"));
                        hm.put("postmeta_total_sales", postmeta.getString("total_sales"));
                        hm.put("postmeta_edit_lock", postmeta.getString("_edit_lock"));
                        hm.put("postmeta_edit_last", postmeta.getString("_edit_last"));
                        hm.put("postmeta_purchase_note", postmeta.getString("_purchase_note"));
                        hm.put("postmeta_product_attributes", postmeta.getString("_product_attributes"));
                        hm.put("postmeta_sold_individually", postmeta.getString("_sold_individually"));
                        hm.put("postmeta_product_image_gallery", postmeta.getString("_product_image_gallery"));
                        hm.put("postmeta_layout", postmeta.getString("_layout"));
                        hm.put("postmeta_thumbnail_id", postmeta.getString("_thumbnail_id"));

                        hm.put("image_thumbnail", thumbnail.getString("image_thumbnail"));
                        hm.put("image_full", thumbnail.getString("image_full"));

                        NewProductList.add(hm);
                    }

                    for (int i = 0, max = ProductList.size(); i < max; i++) {
                        NewProductList.add((HashMap<String, String>) ProductList.get(i));
                    }

                    ProductList = NewProductList;

                    adapter = new ProductAdapter(ProductList);
                    recyclerView.setAdapter(adapter);

                    LinearLayoutManager layoutManager = new LinearLayoutManager(SearchActivity.this);
                    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                    layoutManager.scrollToPosition(arr.length());
                    recyclerView.setLayoutManager(layoutManager);

                    loadStart = ProductList.size();
                }
                else{
                    endLoad = true;

                    if(ProductList.size() == 0){
                        Snackbar snack = Snackbar.make(findViewById(android.R.id.content), R.string.no_search, Snackbar.LENGTH_LONG);
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
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("ProductList", ProductList);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ProductList = savedInstanceState.getStringArrayList("ProductList");
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(SearchActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_products) {
            Intent intent = new Intent(SearchActivity.this, MainActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_categories) {
            Intent intent = new Intent(SearchActivity.this, CatActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_news) {
            Intent intent = new Intent(SearchActivity.this, NewsActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_about) {
            Intent intent = new Intent(SearchActivity.this, AboutActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_cart) {
            Intent intent = new Intent(SearchActivity.this, CartActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_history) {
            Intent intent = new Intent(SearchActivity.this, HistoryActivity.class);
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
