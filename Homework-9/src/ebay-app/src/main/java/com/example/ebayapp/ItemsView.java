package com.example.ebayapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

import java.util.Objects;

public class ItemsView extends AppCompatActivity {
    private static String keywords = null;
    private static JSONArray arr = new JSONArray();
    private SwipeRefreshLayout swipeRefreshLayout;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String intentURL = intent.getStringExtra("url");
        String intentKeywords = intent.getStringExtra("keywords");
        if (intentKeywords != null) {
            keywords = intentKeywords;
            setContentView(R.layout.progress_bar);
            runQuery(intentURL, intentKeywords);
        } else displayItems(keywords);
    }

    private void runQuery(final String url, final String keywords) {
        System.out.println(url);
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        arr = response;
                        if (arr.length() == 0)
                            displayNoRecords();
                        else
                            displayItems(keywords);
                        refreshLayout(url, keywords);
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

    private void displayNoRecords() {
        setContentView(R.layout.items_view_no_records);
        setUpActionBar();
        Context context = getApplicationContext();
        String text = "No Records";
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.show();
    }

    private void displayItems(String keywords) {
        setContentView(R.layout.items_view);
        setUpActionBar();
        displayNumberOfItemsLabel(keywords);
        createRecyclerView();
    }

    private void setUpActionBar() {
        setSupportActionBar((Toolbar) findViewById(R.id.app_bar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Search Results");
    }

    private void displayNumberOfItemsLabel(String keyword) {
        String text = "Showing <strong><font color=#0063D1>" + arr.length() + "</font></strong> results for <strong><font color=#0063D1>" + keyword + "</font></strong>";
        TextView textView = findViewById(R.id.showing_result);
        textView.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT));
    }

    private void createRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(new ItemsViewAdapter(arr, this, this));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.HORIZONTAL));
    }

    private void refreshLayout(final String url, final String keywords) {
        swipeRefreshLayout = findViewById(R.id.refreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                System.out.println("Running query once more!");
                runQuery(url, keywords);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        onBackPressed();
        return true;
    }
}
