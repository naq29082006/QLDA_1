package com.example.duan1;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duan1.services.ApiServices;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatisticsActivity extends AppCompatActivity {
    private LineChart lineChart;
    private PieChart pieChart;
    private TextView tvDelivered, tvPending, tvCancelled;
    private RecyclerView rcvTop5;
    private Top5Adapter adapter;

    private ApiServices apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        lineChart = findViewById(R.id.lineChart);
        pieChart = findViewById(R.id.pieChart);
        tvDelivered = findViewById(R.id.tvDelivered);
        tvPending = findViewById(R.id.tvPending);
        tvCancelled = findViewById(R.id.tvCancelled);
        rcvTop5 = findViewById(R.id.rcvTop5);

        rcvTop5.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Top5Adapter(new ArrayList<>());
        rcvTop5.setAdapter(adapter);

        apiService = RetrofitClient.getInstance().create(ApiServices.class);

        fetchTop5Orders();
        fetchReport();
        fetchWeeklyOrders();
    }

    private void fetchTop5Orders() {
        apiService.getTop5Orders().enqueue(new Callback<List<Top5Response.TopOrder>>() {
            @Override
            public void onResponse(Call<List<Top5Response.TopOrder>> call, Response<List<Top5Response.TopOrder>> response) {
                if(response.body() != null) {
                    adapter.updateData(response.body());
                }
            }
            @Override
            public void onFailure(Call<List<Top5Response.TopOrder>> call, Throwable t) {
                Toast.makeText(StatisticsActivity.this, "Lấy top5 orders thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchReport() {
        apiService.getReport().enqueue(new Callback<Top5Response.ReportResponse>() {
            @Override
            public void onResponse(Call<Top5Response.ReportResponse> call, Response<Top5Response.ReportResponse> response) {
                if(response.body() != null) {
                    Top5Response.ReportResponse report = response.body();
                    updatePieChart(report);
                }
            }
            @Override
            public void onFailure(Call<Top5Response.ReportResponse> call, Throwable t) {
                Toast.makeText(StatisticsActivity.this, "Lấy report thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchWeeklyOrders() {
        apiService.getReport().enqueue(new Callback<Top5Response.ReportResponse>() {
            @Override
            public void onResponse(Call<Top5Response.ReportResponse> call, Response<Top5Response.ReportResponse> response) {
                if(response.body() != null && response.body().weeklyOrders != null) {
                    updateLineChart(response.body().weeklyOrders);
                }
            }
            @Override
            public void onFailure(Call<Top5Response.ReportResponse> call, Throwable t) {
                Toast.makeText(StatisticsActivity.this, "Lấy weekly orders thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLineChart(List<Integer> weeklyOrders) {
        ArrayList<Entry> entries = new ArrayList<>();
        for(int i=0;i<weeklyOrders.size();i++){
            entries.add(new Entry(i, weeklyOrders.get(i)));
        }
        LineDataSet dataSet = new LineDataSet(entries, "Số đơn hàng");
        dataSet.setLineWidth(2f);
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.getDescription().setEnabled(false);
        lineChart.invalidate();
    }

    private void updatePieChart(Top5Response.ReportResponse report) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(report.deliveredPercent, "Đã giao"));
        entries.add(new PieEntry(report.pendingPercent, "Đang chờ"));
        entries.add(new PieEntry(report.cancelledPercent, "Hủy"));

        PieDataSet dataSet = new PieDataSet(entries, "Tỉ lệ đơn hàng");
        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate();

        tvDelivered.setText("Đã giao: " + report.deliveredPercent + "%");
        tvPending.setText("Đang chờ: " + report.pendingPercent + "%");
        tvCancelled.setText("Hủy: " + report.cancelledPercent + "%");
    }

}

// Top5Adapter.java