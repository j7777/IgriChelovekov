package ua.com.intelligent_games.igrichelovekov.Activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.SearchView;
import android.util.Log;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Date;

import ua.com.intelligent_games.igrichelovekov.Classes.CircleTransform;
import ua.com.intelligent_games.igrichelovekov.R;
import ua.com.intelligent_games.igrichelovekov.SQL.CartSql;

public class ProductFullActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private SearchView searchView;
    String image;
    private CartSql dbHelper;
    private SQLiteDatabase dbDatabase;

    private SharedPreferences setting;
    private Boolean use_connect = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_full);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setting = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbHelper = new CartSql(ProductFullActivity.this);
                dbDatabase = dbHelper.getWritableDatabase();

                Date date = new Date();
                long date_stamp = date.getTime();

                String insertQuery = "INSERT INTO `" + dbHelper.TABLE_CART + "`"+
                        "(`id`, " +
                        "`post_ID`, " +
                        "`post_date`, " +
                        "`post_content`, " +
                        "`post_custom_excerpt`, " +
                        "`post_title`, " +
                        "`image_thumbnail`, " +
                        "`post_name`, " +
                        "`post_guid`, " +
                        "`postmeta_sku`, " +
                        "`postmeta_price`, " +
                        "`postmeta_sale_price`, " +
                        "`postmeta_regular_price`, " +
                        "`postmeta_weight`, " +
                        "`postmeta_length`, " +
                        "`postmeta_width`, " +
                        "`postmeta_height`, " +
                        "`cart_count`, " +
                        "`cart_date`, " +
                        "`cart_date_stamp`, " +
                        "`cart_order_type`, " +
                        "`cart_status`)"+
                        " VALUES " +
                        "(NULL," +
                        "'"+getIntent().getExtras().getString("post_ID")+"'," +
                        "'"+getIntent().getExtras().getString("post_date")+"'," +
                        "'"+getIntent().getExtras().getString("post_content")+"'," +
                        "'"+getIntent().getExtras().getString("post_custom_excerpt")+"'," +
                        "'"+getIntent().getExtras().getString("post_title")+"'," +
                        "'"+getIntent().getExtras().getString("image_thumbnail")+"'," +
                        "'"+getIntent().getExtras().getString("post_name")+"'," +
                        "'"+getIntent().getExtras().getString("post_guid")+"'," +
                        "'"+getIntent().getExtras().getString("postmeta_sku")+"'," +
                        "'"+getIntent().getExtras().getString("postmeta_price")+"'," +
                        "'"+getIntent().getExtras().getString("postmeta_sale_price")+"'," +
                        "'"+getIntent().getExtras().getString("postmeta_regular_price")+"'," +
                        "'"+getIntent().getExtras().getString("postmeta_weight")+"'," +
                        "'"+getIntent().getExtras().getString("postmeta_length")+"'," +
                        "'"+getIntent().getExtras().getString("postmeta_width")+"'," +
                        "'"+getIntent().getExtras().getString("postmeta_height")+"'," +
                        "'1'," +
                        "date('now')," +
                        "'"+date_stamp+"'," +
                        "'0'," +
                        "'0')";

                try {
                    dbDatabase.execSQL(insertQuery);

                    Snackbar snack = Snackbar.make(view, R.string.add_to_cart, Snackbar.LENGTH_LONG);
                    View viewSnack = snack.getView();
                    viewSnack.setBackgroundColor(Color.rgb(48, 63, 159));
                    TextView textView = (TextView) viewSnack.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(Color.WHITE);
                    Snackbar but_snack = snack.setAction(R.string.self_to_cart, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(ProductFullActivity.this, CartActivity.class);
                            startActivity(intent);
                        }
                    });
                    but_snack.setActionTextColor(Color.WHITE);
                    snack.show();
                }
                catch (Exception e){
                    Snackbar snack = Snackbar.make(view, R.string.error_add_cart, Snackbar.LENGTH_LONG);
                    View viewSnack = snack.getView();
                    viewSnack.setBackgroundColor(Color.rgb(48, 63, 159));
                    TextView textView = (TextView) viewSnack.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(Color.WHITE);
                    snack.show();
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        String title = getIntent().getExtras().getString("post_title");
        String date = getIntent().getExtras().getString("post_date");
        String content = getIntent().getExtras().getString("post_content");
        String url = getIntent().getExtras().getString("post_guid");
        String price = getIntent().getExtras().getString("postmeta_price");
        String thumbnail = getIntent().getExtras().getString("image_thumbnail");
        image = getIntent().getExtras().getString("image_full");

        url = url.replace("#038;", "");

        TextView tv_title = (TextView) findViewById(R.id.title);
        tv_title.setText(title);

        TextView tv_date = (TextView) findViewById(R.id.date);
        tv_date.setText(date);

        TextView tv_content = (TextView) findViewById(R.id.text);
        tv_content.setText(content);

        TextView tv_url = (TextView) findViewById(R.id.url);
        tv_url.setText(url);

        TextView tv_price = (TextView) findViewById(R.id.price);
        tv_price.setText(price + " " + getString(R.string.grn));

        ImageView iv_thumbnail = (ImageView) findViewById(R.id.thumbnail);
        if(thumbnail.isEmpty() || thumbnail == null || !isOnline()){
            iv_thumbnail.setImageResource(R.mipmap.ic_launcher);
        }
        else{
            Picasso.with(ProductFullActivity.this)
                    .load(thumbnail)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .placeholder(R.drawable.load_image)
                    .error(R.drawable.error_load_image)
                    .fit()
                    .transform(new CircleTransform())
                    .into(iv_thumbnail);
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

    public void onClick(View view) {
        if(!image.isEmpty() && image != null){
            Intent intent = new Intent(ProductFullActivity.this, FullImageActivity.class);
            intent.putExtra("image_full", image);
            startActivity(intent);
        }
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
            Intent intent = new Intent(ProductFullActivity.this, SettingsActivity.class);
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
            Intent intent = new Intent(ProductFullActivity.this, MainActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_categories) {
            Intent intent = new Intent(ProductFullActivity.this, CatActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_news) {
            Intent intent = new Intent(ProductFullActivity.this, NewsActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_about) {
            Intent intent = new Intent(ProductFullActivity.this, AboutActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_cart) {
            Intent intent = new Intent(ProductFullActivity.this, CartActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_history) {
            Intent intent = new Intent(ProductFullActivity.this, HistoryActivity.class);
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
