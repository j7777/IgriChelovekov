package ua.com.intelligent_games.igrichelovekov.Adapter;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ua.com.intelligent_games.igrichelovekov.Activity.CartActivity;
import ua.com.intelligent_games.igrichelovekov.Activity.ProductFullActivity;
import ua.com.intelligent_games.igrichelovekov.Classes.CircleTransform;
import ua.com.intelligent_games.igrichelovekov.R;
import ua.com.intelligent_games.igrichelovekov.SQL.CartSql;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    Context context;
    public ArrayList CartList;
    private CartSql dbHelper;
    private SQLiteDatabase dbDatabase;

    public CartAdapter(Context context) {
        this.context = context;
    }

    public CartAdapter(ArrayList<HashMap<String, String>> CartList) {
        this.CartList = CartList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_cart, viewGroup, false);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder ViewHolder, final int i) {
        final Map<String, String> hashmap = (Map<String, String>) CartList.get(i);

        ViewHolder.title.setText(hashmap.get("post_title"));
        ViewHolder.date.setText(hashmap.get("cart_date"));
        ViewHolder.price.setText(hashmap.get("postmeta_price"));
        ViewHolder.CountBuy.setText(hashmap.get("cart_count"));

        try{
            int cartSum = Integer.parseInt(hashmap.get("postmeta_price")) * Integer.parseInt(hashmap.get("cart_count"));
            ViewHolder.CartSum.setText(String.valueOf(cartSum));
        }
        catch(Exception e){
            ViewHolder.CartSum.setText("0");
        }

        if(hashmap.get("image_thumbnail").isEmpty() || hashmap.get("image_thumbnail") == null){
            ViewHolder.imagetumb.setImageResource(R.mipmap.ic_launcher);
        }
        else{
            Context imageContext = ViewHolder.imagetumb.getContext();
            Picasso.with(imageContext)
                    .load(hashmap.get("image_thumbnail"))
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .placeholder(R.drawable.load_image)
                    .error(R.drawable.error_load_image)
                    .fit()
                    .transform(new CircleTransform())
                    .into(ViewHolder.imagetumb);
        }

        ViewHolder.CountBuy.addTextChangedListener(new TextWatcher() {
            int pos = i;
            String price = hashmap.get("postmeta_price");

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try{
                    String newCartSum = Integer.toString(Integer.parseInt(price) * Integer.parseInt(s.toString()));
                    ViewHolder.CartSum.setText(newCartSum);
                    hashmap.put("cart_count", s.toString());
                    CartList.set(pos, hashmap);

                    dbHelper = new CartSql(ViewHolder.cardItemLayout.getContext());
                    dbDatabase = dbHelper.getWritableDatabase();

                    String updateQuery = "UPDATE `" + dbHelper.TABLE_CART + "` SET `cart_count` = '"+s.toString()+"' WHERE `id` = '"+hashmap.get("id")+"'";
                    dbDatabase.execSQL(updateQuery);
                }
                catch(Exception e){
                    ViewHolder.CartSum.setText("0");
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public int getItemCount() {
        return CartList == null ? 0 : CartList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView cardItemLayout;
        TextView title;
        TextView date;
        TextView price;
        ImageView imagetumb;
        ImageView dellItemCart;
        EditText CountBuy;
        TextView CartSum;

        public ViewHolder(View itemView) {
            super(itemView);

            cardItemLayout = (CardView) itemView.findViewById(R.id.cart_cardlist_item);
            title = (TextView) itemView.findViewById(R.id.title);
            date = (TextView) itemView.findViewById(R.id.date);
            price = (TextView) itemView.findViewById(R.id.price);
            imagetumb = (ImageView) itemView.findViewById(R.id.imageTumb);
            dellItemCart = (ImageView) itemView.findViewById(R.id.dellItemCart);
            CountBuy = (EditText) itemView.findViewById(R.id.CountBuy);
            CartSum = (TextView) itemView.findViewById(R.id.CartSum);

            dellItemCart.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            final int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                switch (v.getId()) {
                    case R.id.dellItemCart:
                        final Snackbar[] snack = {Snackbar.make(v, R.string.cart_confirm, Snackbar.LENGTH_LONG)};
                        final View viewSnack = snack[0].getView();
                        viewSnack.setBackgroundColor(Color.rgb(48, 63, 159));
                        final TextView textView = (TextView) viewSnack.findViewById(android.support.design.R.id.snackbar_text);
                        textView.setTextColor(Color.WHITE);
                        Snackbar but_snack = snack[0].setAction(R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    Map<String, String> hashmap = (Map<String, String>) CartList.get(position);

                                    dbHelper = new CartSql(v.getContext());
                                    dbDatabase = dbHelper.getWritableDatabase();

                                    String dellQuery = "DELETE FROM `" + dbHelper.TABLE_CART + "` WHERE `id` = '" + hashmap.get("id") + "'";
                                    dbDatabase.execSQL(dellQuery);

                                    CartList.remove(position);
                                    notifyItemRemoved(position);
                                } catch (Exception e) {
                                    snack[0] = Snackbar.make(v, R.string.error_dell_cart, Snackbar.LENGTH_LONG);
                                    viewSnack.setBackgroundColor(Color.rgb(48, 63, 159));
                                    textView.setTextColor(Color.WHITE);
                                    snack[0].show();
                                }
                            }
                        });
                        but_snack.setActionTextColor(Color.WHITE);
                        snack[0].show();
                }
            }
        }
    }
}