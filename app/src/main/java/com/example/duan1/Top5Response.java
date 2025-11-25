package com.example.duan1;

import java.util.List;

public class Top5Response {
    public List<TopOrder> list;
    public static class TopOrder {
        public int order_id;
        public String product;
        public String customer;
        public int daysWaiting;
    }
    public class ReportResponse {
        public List<Integer> weeklyOrders; // số đơn hàng mỗi ngày
        public int deliveredPercent;
        public int pendingPercent;
        public int cancelledPercent;
    }
}
