package com.example.duan1;

import java.util.List;

public class OrdersByWeekResponse {
    public List<DayData> data;
    public static class DayData {
        public String day;
        public int orders;
    }
}
