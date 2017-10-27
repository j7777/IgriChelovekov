package ua.com.intelligent_games.igrichelovekov.Adapter;

import android.content.Context;
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

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    Context context;
    OnItemClickListener clickListener;
    private ArrayList NewsList;

    public NewsAdapter(Context context) {
        this.context = context;
    }

    public NewsAdapter(ArrayList<HashMap<String, String>> NewsList) {
        this.NewsList = NewsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view;

        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_news, viewGroup, false);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder ViewHolder, int i) {
        Map<String, String> hashmap;
        hashmap = (Map<String, String>) NewsList.get(i);

        ViewHolder.title.setText(hashmap.get("title"));
        ViewHolder.excerpt.setText(hashmap.get("excerpt"));
        ViewHolder.date.setText(hashmap.get("date"));

        if(hashmap.get("thumbnail").isEmpty() || hashmap.get("thumbnail") == null){
            ViewHolder.imagetumb.setImageResource(R.mipmap.ic_launcher);
        }
        else{
            Context imageContext = ViewHolder.imagetumb.getContext();
            Picasso.with(imageContext)
                    .load(hashmap.get("thumbnail"))
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
        return NewsList == null ? 0 : NewsList.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView cardItemLayout;
        TextView title;
        TextView excerpt;
        TextView date;
        ImageView imagetumb;

        public ViewHolder(View itemView) {
            super(itemView);

            cardItemLayout = (CardView) itemView.findViewById(R.id.news_cardlist_item);
            title = (TextView) itemView.findViewById(R.id.title);
            excerpt = (TextView) itemView.findViewById(R.id.excerpt);
            date = (TextView) itemView.findViewById(R.id.date);
            imagetumb = (ImageView) itemView.findViewById(R.id.imageTumb);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(v, getAdapterPosition());
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }
}