package com.example.ebayapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Objects;

public class ItemDetailView extends AppCompatActivity {

    private static class ItemDetail {
        String searchURL = "";
        String url = "";
        String title = "";
        String shippingInfo = "";
        String shippingFee = "";
    }

    private ItemDetail itemDetail;
    private JSONObject item;
    private boolean isProductFragmentPopulated = false;
    private boolean isShippingFragmentPopulated = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getItemDetail();
        setupView();
        runQuery(itemDetail.searchURL);
    }

    private void getItemDetail() {
        Intent intent = getIntent();
        itemDetail = new ItemDetail();
        itemDetail.searchURL = intent.getStringExtra("searchURL");
        itemDetail.title = intent.getStringExtra("itemTitle");
        itemDetail.url = intent.getStringExtra("itemURL");
        itemDetail.shippingInfo = intent.getStringExtra("itemShippingInfo");
        itemDetail.shippingFee = intent.getStringExtra("itemShippingFee");
    }

    private void setupView() {
        setContentView(R.layout.item_detail_view);
        setSupportActionBar((Toolbar) findViewById(R.id.app_bar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle(itemDetail.title);
        createPageAdapter();
        View progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        TextView spinningText = findViewById(R.id.spinning_text);
        spinningText.setVisibility(View.GONE);
    }

    private void createPageAdapter() {
        ItemDetailViewPagerAdapter adapter = new ItemDetailViewPagerAdapter(getSupportFragmentManager());
        addFragments(adapter);
        ViewPager viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        // Setup icons for each tab
        Objects.requireNonNull(tabLayout.getTabAt(0)).setIcon(R.drawable.information_variant_selected);
        Drawable drawable = getDrawable(R.drawable.ic_seller);
        assert drawable != null;
        drawable.setColorFilter(new BlendModeColorFilter(ContextCompat.getColor(this, R.color.ic_launcher_background), BlendMode.SRC_ATOP));
        Objects.requireNonNull(tabLayout.getTabAt(1)).setIcon(R.drawable.ic_seller);
        Objects.requireNonNull(tabLayout.getTabAt(2)).setIcon(R.drawable.truck);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        if (!isProductFragmentPopulated) {
                            populateProductFragment("1 populate 1");
                        }
                        isShippingFragmentPopulated = false;
                        break;
                    case 1:
                        if (!isProductFragmentPopulated && findViewById(R.id.image_gallery) != null) {
                            populateProductFragment("2 populate 1");
                        }
                        if (!isShippingFragmentPopulated && findViewById(R.id.shipping_information) != null) {
                            populateShippingFragment("2 populate 3");
                        }
                        break;
                    case 2:
                        if (!isShippingFragmentPopulated) {
                            populateShippingFragment("3 populate 3");
                        }
                        isProductFragmentPopulated = false;
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void populateProductFragment(String s) {
        try {
            System.out.println(s);
            populateProductFragment();
        } catch (JSONException ignored) {
        }
    }

    private void populateShippingFragment(String s) {
        try {
            System.out.println(s);
            populateShippingFragment();
        } catch (JSONException ignored) {
        }
    }

    private void addFragments(ItemDetailViewPagerAdapter adapter) {
        adapter.addFragment(new ItemDetailViewProductFragment(), "PRODUCT");
        adapter.addFragment(new ItemDetailViewSellerFragment(), "SELLER INFO");
        adapter.addFragment(new ItemDetailViewShippingFragment(), "SHIPPING");
    }

    private void runQuery(final String url) {
        System.out.println(url);
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            View progressBar = findViewById(R.id.progress_bar);
                            progressBar.setVisibility(View.GONE);
                            displayItemDetails(response);
                        } catch (JSONException ignored) {
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("Error: " + error);
                    }
                });
        queue.add(request);
    }

    private void displayItemDetails(JSONObject jsonObject) throws JSONException {

        item = (JSONObject) jsonObject.get("Item");
        populateProductFragment();
        populateSellerFragment();
    }

    @SuppressLint("SetTextI18n")
    private void populateProductFragment() throws JSONException {
        View productView = findViewById(R.id.detail);
        productView.setVisibility(View.VISIBLE);
        createImageGallery(item);
        setText(R.id.item_title_detail, itemDetail.title);
        JSONObject convertedCurrentPrice = (JSONObject) item.get("ConvertedCurrentPrice");
        double price = Double.parseDouble(convertedCurrentPrice.get("Value").toString());
        setText(R.id.item_price_detail, "$" + price);
        setText(R.id.item_shipping_detail, Html.fromHtml(itemDetail.shippingFee, Html.FROM_HTML_MODE_COMPACT));

        boolean featuresFlag = false;
        JSONObject itemSpecifics = (JSONObject) item.get("ItemSpecifics");
        ConstraintLayout featuresContainer = findViewById(R.id.product_features_container);
        if (itemSpecifics.has("Subtitle"))
            featuresFlag = getSubtitleOrBrand(featuresContainer, (JSONObject) itemSpecifics.get("Subtitle"), R.id.item_subtitle_detail_label, R.id.item_subtitle_detail_text);
        JSONArray nameValueList = (JSONArray) itemSpecifics.get("NameValueList");

        int count = 0;
        ConstraintLayout specificationsContainer = findViewById(R.id.product_specifications_container);
        LinearLayout layout = findViewById(R.id.product_specifications_items);
        for (int i = 0; i < nameValueList.length(); i++) {
            JSONObject entry = (JSONObject) nameValueList.get(i);
            if ((entry.get("Name")).equals("Brand"))
                featuresFlag = getSubtitleOrBrand(featuresContainer, entry, R.id.item_brand_detail_label, R.id.item_brand_detail_text);
            else if (count < 5) {
                JSONArray array = (JSONArray) entry.get("Value");
                StringBuilder values = new StringBuilder((String) array.get(0));
                if (array.length() > 1)
                    for (int j = 1; j < array.length(); j++)
                        values.append(", ").append((String) array.get(j));
                TextView textView = new TextView(getApplicationContext());
                textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                textView.setText("\u2022 " + values);
                layout.addView(textView);
                count += 1;
            }
        }
        if (!featuresFlag) {
            featuresContainer.setVisibility(View.GONE);
            View view = findViewById(R.id.view_one);
            view.setVisibility(View.GONE);
        }
        if (count == 0)
            specificationsContainer.setVisibility(View.GONE);
        isProductFragmentPopulated = true;
    }

    private void createImageGallery(JSONObject item) throws JSONException {
        JSONArray pictureURL = (JSONArray) item.get("PictureURL");
        LinearLayout imageGallery = findViewById(R.id.image_gallery);
        for (int i = 0; i < pictureURL.length(); i++) {
            String url = (String) pictureURL.get(i);
            ImageView imageView = new ImageView(getApplicationContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(800, LinearLayout.LayoutParams.MATCH_PARENT);
            lp.setMargins(0, 0, 10, 0);
            imageView.setLayoutParams(lp);
            Picasso.get().load(url).into(imageView);
            imageGallery.addView(imageView);
        }
    }

    private boolean getSubtitleOrBrand(ConstraintLayout container, JSONObject entry, int id_label, int id_text) throws JSONException {
        container.setVisibility(View.VISIBLE);
        JSONArray values = (JSONArray) entry.get("Value");
        TextView label = findViewById(id_label);
        label.setVisibility(View.VISIBLE);
        TextView textView = findViewById(id_text);
        textView.setText((String) values.get(0));
        textView.setVisibility(View.VISIBLE);
        return true;
    }

    private void setText(int id, String text) {
        TextView textview = findViewById(id);
        textview.setText(text);
    }

    private void setText(int id, Spanned text) {
        TextView textview = findViewById(id);
        textview.setText(text);
    }

    private void populateSellerFragment() throws JSONException {
        addInformation(item, R.id.seller_information, "Seller");
        addInformation(item, R.id.return_policies_information, "ReturnPolicy");
    }

    private void addInformation(JSONObject item, int id, String s) throws JSONException {
        LinearLayout layout = findViewById(id);
        JSONObject jsonObject = (JSONObject) item.get(s);
        for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
            String key = it.next();
            String keyString = String.join(" ", key.split("(?=\\p{Upper})"));
            String valueString = jsonObject.get(key).toString();
            if (s.equals("ReturnPolicy")) {
                valueString = String.join(" ", valueString.split("(?=\\p{Upper})"));
            }
            String text = "<strong><font color:grey>" + keyString + ":</font></strong> " + valueString;
            TextView textView = new TextView(getApplicationContext());
            textView.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT));
            layout.addView(textView);
        }
    }

    private void populateShippingFragment() throws JSONException {
        LinearLayout layout = findViewById(R.id.shipping_information);
        JSONObject jsonObject = new JSONObject(itemDetail.shippingInfo);
        for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
            String key = it.next();
            if (key.equals("shippingServiceCost"))
                continue;
            JSONArray jsonArray = (JSONArray) jsonObject.get(key);
            key = key.substring(0, 1).toUpperCase() + key.substring(1);
            String keyString = String.join(" ", key.split("(?=\\p{Upper})"));
            String text = "<strong><font color:grey>" + keyString + ":</font></strong> " + jsonArray.get(0);
            TextView textView = new TextView(getApplicationContext());
            textView.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT));
            layout.addView(textView);
        }
        isShippingFragmentPopulated = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.redirect, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.redirect) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(itemDetail.url));
            startActivity(intent);
        } else onBackPressed();
        return true;
    }
}
