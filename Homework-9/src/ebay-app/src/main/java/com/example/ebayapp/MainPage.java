package com.example.ebayapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


public class MainPage extends AppCompatActivity {

    HashMap<String, String> sortOptions = new HashMap<String, String>() {{
        put("Best Match", "BestMatch");
        put("Price: Highest first", "CurrentPriceHighest");
        put("Price + Shipping: Highest first", "PricePlusShippingHighest");
        put("Price + Shipping: Lowest first", "PricePlusShippingLowest");
    }};

    private static class SearchOptions {
        String keywords = "";
        double minPrice = 0;
        double maxPrice = 0.0;
        ArrayList<String> conditions = new ArrayList<>();
        String sortOrder = "";
    }

    private SearchOptions options = new SearchOptions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);
        setSupportActionBar((Toolbar) findViewById(R.id.app_bar));
        Objects.requireNonNull(getSupportActionBar()).setTitle("eBay Catalog Search");

        Button searchButton = findViewById(R.id.search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    boolean flag = search();
                    if (flag) {
                        String url = generateUrl(options);
                        Intent intent = new Intent(MainPage.this, ItemsView.class);
                        intent.putExtra("url", url);
                        intent.putExtra("keywords", options.keywords);
                        startActivity(intent);
                    } else {
                        Context context = getApplicationContext();
                        String text = "Please fix all fields with errors";
                        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
                        toast.show();
                    }
                } catch (Exception ignored) {
                }
            }
        });

        Button clearButton = findViewById(R.id.clear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear();
            }
        });
    }

    protected void clear() {
        clearEditText(R.id.keyword);
        clearEditText(R.id.min_price);
        clearEditText(R.id.max_price);
        clearCheckBox(R.id.condition_new);
        clearCheckBox(R.id.condition_used);
        clearCheckBox(R.id.condition_unspecified);
        Spinner spinner = findViewById(R.id.sort);
        spinner.setSelection(0);
        clearError(R.id.keyword_error);
        clearError(R.id.price_error);
    }

    private void clearEditText(int id) {
        EditText editText = findViewById(id);
        editText.setText("");
    }

    private void clearCheckBox(int id) {
        CheckBox checkBox = findViewById(id);
        if (checkBox.isChecked()) {
            checkBox.toggle();
        }
    }

    private void clearError(int id) {
        TextView textView = findViewById(id);
        textView.setVisibility(View.GONE);
    }

    private boolean search() {
        boolean flag = true;
        EditText editText = findViewById(R.id.keyword);
        options.keywords = editText.getText().toString();
        if (checkCondition(options.keywords.equals(""), findViewById(R.id.keyword_error)))
            flag = false;

        editText = findViewById(R.id.min_price);
        String value = editText.getText().toString();
        if (!value.equals(""))
            options.minPrice = Double.parseDouble(value);
        else options.minPrice = 0;
        editText = findViewById(R.id.max_price);
        value = editText.getText().toString();
        if (!value.equals(""))
            options.maxPrice = Double.parseDouble(value);
        else options.maxPrice = 0;
        if (checkCondition(options.maxPrice > 0 && options.minPrice > options.maxPrice, findViewById(R.id.price_error)))
            flag = false;

        addCondition(R.id.condition_new, "New");
        addCondition(R.id.condition_used, "Used");
        addCondition(R.id.condition_unspecified, "Unspecified");

        Spinner sortSpinner = findViewById(R.id.sort);
        options.sortOrder = sortSpinner.getSelectedItem().toString();

        return flag;
    }

    private boolean checkCondition(boolean conditions, View error) {
        if (conditions) {
            error.setVisibility(View.VISIBLE);
            return true;
        } else error.setVisibility(View.GONE);
        return false;
    }

    private void addCondition(int id, String condition) {
        CheckBox checkBox = findViewById(id);
        if (checkBox.isChecked())
            options.conditions.add(condition);
    }

    private String generateUrl(SearchOptions options) {
        String url = "https://ebay-server-1621.appspot.com/search?";
        url += "keywords=" + options.keywords + "&sortOrder=" + sortOptions.get(options.sortOrder);
        url += addSearchOption(options.conditions.size() > 0, "conditions", String.join(",", options.conditions));
        url += addSearchOption(options.minPrice > 0, "MinPrice", String.valueOf(options.minPrice));
        url += addSearchOption(options.maxPrice > 0, "MaxPrice", String.valueOf(options.maxPrice));
        return url;
    }

    private String addSearchOption(boolean condition, String key, String value) {
        if (condition)
            return "&" + key + "=" + value;
        return "";
    }
}