package ua.com.intelligent_games.igrichelovekov.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import ua.com.intelligent_games.igrichelovekov.Json.JSONParser;
import ua.com.intelligent_games.igrichelovekov.R;

public class AboutActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private SearchView searchView;
    private TextView phone1;
    private TextView phone2;
    private TextView email;
    private TextView desc;
    private String str_phone1;
    private String str_phone2;
    private String str_email;
    private String str_desc;
    private Intent intent;
    private LinearLayout loader;

    private String url;
    private JSONObject obj = null;

    private SharedPreferences setting;
    private Boolean use_connect = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setting = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        loader = (LinearLayout) findViewById(R.id.loader);

        phone1 = (TextView) findViewById(R.id.number1);
        phone2 = (TextView) findViewById(R.id.number2);
        email = (TextView) findViewById(R.id.mail);
        desc = (TextView) findViewById(R.id.desc);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Item[] items = {
                        new Item(str_phone1, R.drawable.phone_gray),
                        new Item(str_phone2, R.drawable.phone_gray),
                        new Item(str_phone1, R.drawable.sms_gray),
                        new Item(str_phone2, R.drawable.sms_gray),
                        new Item(str_email, R.drawable.email_gray),
                        new Item(getString(R.string.send_cancel), R.drawable.cancel_gray),
                };

                ListAdapter adapter = new ArrayAdapter<Item>(
                        AboutActivity.this,
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


                new AlertDialog.Builder(AboutActivity.this)
                        .setTitle(R.string.sel_send)
                        .setAdapter(adapter, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                onClickList(item);
                            }
                        }).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if(savedInstanceState != null){
            str_phone1 = savedInstanceState.getString("str_phone1");
            str_phone2 = savedInstanceState.getString("str_phone2");
            str_email = savedInstanceState.getString("str_email");
            str_desc = savedInstanceState.getString("str_desc");
        }

        if(isOnline()){
            new JSONParseData().execute();
        }
        else{
            loader.setVisibility(View.GONE);
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
                //Отправить смс
                intent = new Intent(Intent.ACTION_SEND);
                intent.setData(Uri.parse("smsto:"));
                intent.putExtra("address", str_phone1);
                intent.putExtra("sms_body", getString(R.string.mess_send));
                intent.setType("text/plain");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                break;
            case 3:
                //Отправить смс
                intent = new Intent(Intent.ACTION_SEND);
                intent.setData(Uri.parse("smsto:"));
                intent.putExtra("address", str_phone2);
                intent.putExtra("sms_body", getString(R.string.mess_send));
                intent.setType("text/plain");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                break;
            case 4:
                //Отправить email
                intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + str_email));
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mess_send));
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.mess_send_email));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
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

                    phone1.setText(str_phone1);
                    phone2.setText(str_phone2);
                    email.setText(str_email);
                    desc.setText(str_desc);
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
        outState.putString("str_phone1", str_phone1);
        outState.putString("str_phone2", str_phone2);
        outState.putString("str_email", str_email);
        outState.putString("str_desc", str_desc);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
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
            Intent intent = new Intent(AboutActivity.this, SettingsActivity.class);
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
            Intent intent = new Intent(AboutActivity.this, MainActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_categories) {
            Intent intent = new Intent(AboutActivity.this, CatActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_news) {
            Intent intent = new Intent(AboutActivity.this, NewsActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_about) {
            Intent intent = new Intent(AboutActivity.this, AboutActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_cart) {
            Intent intent = new Intent(AboutActivity.this, CartActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_history) {
            Intent intent = new Intent(AboutActivity.this, HistoryActivity.class);
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
