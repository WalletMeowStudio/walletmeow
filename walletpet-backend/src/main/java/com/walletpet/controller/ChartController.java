package com.walletpet.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.walletpet.service.TransactionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/charts")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://127.0.0.1:5500", allowedHeaders = "*", allowCredentials = "true")
public class ChartController {

    private final TransactionService transactionService;

    @GetMapping("/summary")
    public Map<String, Object> getSummary(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        // 1. 設定日期範圍（預設當月）
        LocalDate start = (startDate != null) ? LocalDate.parse(startDate) : LocalDate.now().withDayOfMonth(1);
        LocalDate end = (endDate != null) ? LocalDate.parse(endDate) : LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        // 2. 呼叫你已經測通的 Service 取得原始數據
        Map<String, Object> fullData = transactionService.searchTransactions(
                "default", // 測試用 ID
                start, 
                end, 
                null, null, null, 0, 10
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> serviceSummary = (Map<String, Object>) fullData.get("summary");
        Object itemList = fullData.get("items");

        // 3. 封裝給前端 Dashboard 讀取的格式
        Map<String, Object> dashboardData = new HashMap<>();
        dashboardData.put("month_income", serviceSummary.get("totalIncome"));
        dashboardData.put("month_expense", serviceSummary.get("totalExpense"));
        dashboardData.put("net_amount", serviceSummary.get("balance"));
        dashboardData.put("items", itemList); // 最近交易清單

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", dashboardData);
        response.put("items", itemList); // 雙重保險

        return response;
    }
}

