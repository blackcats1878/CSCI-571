package com.example.ebayapp;

import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ItemsViewAdapter extends RecyclerView.Adapter<ItemsViewAdapter.MyViewHolder> {
    private JSONArray arr;
    private Context baseContext;
    private AppCompatActivity baseActivity;

    public ItemsViewAdapter(JSONArray arr, Context context, AppCompatActivity activity) {
        this.arr = arr;
        baseContext = context;
        baseActivity = activity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.items_view_single_view, parent, false);
        return new MyViewHolder(view, baseContext, baseActivity);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        JSONObject item = new JSONObject();
        try {
            item = (JSONObject) arr.get(position);
        } catch (JSONException ignored) {
        }

        try {
            setText(item, "itemId", holder.itemId);
            setText(item, "viewItemURL", holder.itemURL);
            if (holder.itemURL.getText() == "")
                holder.itemURL.setText("https://www.ebay.com");
            setText(item, "title", holder.itemTitle);
            String itemImage = "";
            if (item.has("galleryURL"))
                itemImage = (String) ((JSONArray) item.get("galleryURL")).get(0);
            if (itemImage.equals("") || itemImage.equals("https://thumbs1.ebaystatic.com/pict/04040_0.jpg"))
                itemImage = "https://www.csci571.com/hw/hw6/images/ebay_default.jpg";
            Picasso.get().load(itemImage).into(holder.itemImage);
            JSONObject itemShippingInfo = (JSONObject) ((JSONArray) item.get("shippingInfo")).get(0);
            holder.itemShippingInfo.setText(itemShippingInfo.toString());
            JSONObject shippingServiceCost = (JSONObject) ((JSONArray) itemShippingInfo.get("shippingServiceCost")).get(0);
            String itemShippingCost = (String) shippingServiceCost.get("__value__");
            if (Double.parseDouble(itemShippingCost) == 0)
                holder.itemShippingFee.setText(Html.fromHtml("<strong>FREE</strong> Shipping", Html.FROM_HTML_MODE_COMPACT));
            else
                holder.itemShippingFee.setText(Html.fromHtml("Shipping for $<strong>" + itemShippingCost + "</strong> ", Html.FROM_HTML_MODE_COMPACT));
            String itemTopRatedListing = (String) ((JSONArray) item.get("topRatedListing")).get(0);
            if (itemTopRatedListing.equals("true")) {
                holder.itemTopRated.setText("Top Rated Listing");
                holder.itemTopRated.setVisibility(View.VISIBLE);
            } else holder.itemTopRated.setVisibility(View.INVISIBLE);
            JSONObject itemCondition = (JSONObject) ((JSONArray) item.get("condition")).get(0);
            setText(itemCondition, "conditionDisplayName", holder.itemCondition);
            JSONObject itemSellingStatus = (JSONObject) ((JSONArray) item.get("sellingStatus")).get(0);
            JSONObject itemConvertedPrice = (JSONObject) ((JSONArray) itemSellingStatus.get("convertedCurrentPrice")).get(0);
            String itemPrice = (String) itemConvertedPrice.get("__value__");
            holder.itemPrice.setText("$" + itemPrice);
        } catch (JSONException ignored) {
        }
    }

    private void setText(JSONObject item, String key, TextView textView) throws JSONException {
        String text = "";
        if (item.has(key))
            text = (String) ((JSONArray) item.get(key)).get(0);
        textView.setText(text);
    }

    @Override
    public int getItemCount() {
        return arr.length();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        Context baseContext;
        AppCompatActivity baseActivity;
        TextView itemId;
        TextView itemURL;
        ImageView itemImage;
        TextView itemTitle;
        TextView itemShippingInfo;
        TextView itemShippingFee;
        TextView itemTopRated;
        TextView itemCondition;
        TextView itemPrice;

        public MyViewHolder(@NonNull View itemView, Context context, AppCompatActivity activity) {
            super(itemView);
            baseContext = context;
            baseActivity = activity;

            itemId = itemView.findViewById(R.id.item_id);
            itemURL = itemView.findViewById(R.id.item_url);
            itemImage = itemView.findViewById(R.id.item_image);
            itemTitle = itemView.findViewById(R.id.item_title);
            itemShippingInfo = itemView.findViewById(R.id.item_shipping_info);
            itemShippingFee = itemView.findViewById(R.id.item_shipping);
            itemTopRated = itemView.findViewById(R.id.item_top_rated);
            itemCondition = itemView.findViewById(R.id.item_condition);
            itemPrice = itemView.findViewById(R.id.item_price);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getSingleItem(itemId.getText());
                }
            });
        }

        public void getSingleItem(CharSequence itemId) {
            final String searchURL = "https://ebay-server-1621.appspot.com/search?itemId=" + itemId;
            Intent intent = new Intent(baseContext, ItemDetailView.class);
            intent.putExtra("searchURL", searchURL);
            intent.putExtra("itemTitle", itemTitle.getText());
            intent.putExtra("itemURL", itemURL.getText());
            intent.putExtra("itemShippingInfo", itemShippingInfo.getText());
            intent.putExtra("itemShippingFee", itemShippingFee.getText().toString());
            baseContext.startActivity(intent);
        }
    }
}
