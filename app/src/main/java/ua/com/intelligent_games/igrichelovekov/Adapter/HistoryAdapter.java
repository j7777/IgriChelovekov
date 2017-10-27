package ua.com.intelligent_games.igrichelovekov.Adapter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ua.com.intelligent_games.igrichelovekov.Classes.CircleTransform;
import ua.com.intelligent_games.igrichelovekov.R;
import ua.com.intelligent_games.igrichelovekov.SQL.CartSql;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    Context context;
    public ArrayList HistoryList;
    private CartSql dbHelper;
    private SQLiteDatabase dbDatabase;

    public HistoryAdapter(Context context) {
        this.context = context;
    }

    public HistoryAdapter(ArrayList<HashMap<String, String>> HistoryList) {
        this.HistoryList = HistoryList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view;

        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_history, viewGroup, false);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder ViewHolder, int i) {
        Map<String, String> hashmap;
        hashmap = (Map<String, String>) HistoryList.get(i);

        ViewHolder.title.setText(hashmap.get("post_title"));
        ViewHolder.date.setText(hashmap.get("cart_date"));
        ViewHolder.price.setText(hashmap.get("postmeta_price"));
        ViewHolder.CountBuy.setText(hashmap.get("cart_count"));

        switch(hashmap.get("cart_order_type")){
            case "0":
                ViewHolder.typeOrder.setImageResource(R.drawable.phone_gray);
                break;
            case "1":
                ViewHolder.typeOrder.setImageResource(R.drawable.phone_gray);
                break;
            case "2":
                ViewHolder.typeOrder.setImageResource(R.drawable.sms_gray);
                break;
            case "3":
                ViewHolder.typeOrder.setImageResource(R.drawable.sms_gray);
                break;
            case "4":
                ViewHolder.typeOrder.setImageResource(R.drawable.email_gray);
                break;
        }

        try{
            int historySum = Integer.parseInt(hashmap.get("postmeta_price")) * Integer.parseInt(hashmap.get("cart_count"));
            ViewHolder.HistorySum.setText(String.valueOf(historySum));
        }
        catch(Exception e){
            ViewHolder.HistorySum.setText("0");
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
    }

    @Override
    public int getItemCount() {
        return HistoryList == null ? 0 : HistoryList.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView cardItemLayout;
        TextView title;
        TextView date;
        TextView price;
        ImageView imagetumb;
        ImageView returnItemHistory;
        ImageView typeOrder;
        TextView CountBuy;
        TextView HistorySum;

        public ViewHolder(View itemView) {
            super(itemView);

            cardItemLayout = (CardView) itemView.findViewById(R.id.history_cardlist_item);
            title = (TextView) itemView.findViewById(R.id.title);
            date = (TextView) itemView.findViewById(R.id.date);
            price = (TextView) itemView.findViewById(R.id.price);
            imagetumb = (ImageView) itemView.findViewById(R.id.imageTumb);
            returnItemHistory = (ImageView) itemView.findViewById(R.id.returnItemHistory);
            typeOrder = (ImageView) itemView.findViewById(R.id.typeOrder);
            CountBuy = (TextView) itemView.findViewById(R.id.CountBuy);
            HistorySum = (TextView) itemView.findViewById(R.id.HistorySum);

            returnItemHistory.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            final int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                switch (v.getId()) {
                    case R.id.returnItemHistory:
                        final Snackbar[] snack = {Snackbar.make(v, R.string.history_confirm, Snackbar.LENGTH_LONG)};
                        final View viewSnack = snack[0].getView();
                        viewSnack.setBackgroundColor(Color.rgb(48, 63, 159));
                        final TextView textView = (TextView) viewSnack.findViewById(android.support.design.R.id.snackbar_text);
                        textView.setTextColor(Color.WHITE);
                        Snackbar but_snack = snack[0].setAction(R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    Map<String, String> hashmap = (Map<String, String>) HistoryList.get(position);

                                    dbHelper = new CartSql(v.getContext());
                                    dbDatabase = dbHelper.getWritableDatabase();

                                    String updateQuery = "UPDATE `" + dbHelper.TABLE_CART + "` SET `cart_status` = '0', `cart_order_type` = '0' WHERE `id` = '"+hashmap.get("id")+"'";
                                    dbDatabase.execSQL(updateQuery);

                                    HistoryList.remove(position);
                                    notifyItemRemoved(position);
                                } catch (Exception e) {
                                    snack[0] = Snackbar.make(v, R.string.error_dell_history, Snackbar.LENGTH_LONG);
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