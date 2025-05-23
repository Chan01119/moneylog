package org.codenova.moneylog.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.codenova.moneylog.entity.User;
import org.codenova.moneylog.query.CategoryExpense;
import org.codenova.moneylog.repository.ExpenseRepository;
import org.codenova.moneylog.response.ChartDataResponse;
import org.codenova.moneylog.response.DailyChartResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/api/expense")
@AllArgsConstructor
public class ExpenseApiController {

    private ExpenseRepository expenseRepository;
    private ObjectMapper objectMapper;

    @GetMapping("/next-month")
    @ResponseBody
    public String nextMonth(@RequestParam("date")LocalDate date){
        return date.plusMonths(1).minusDays(1).toString();
    }

    @GetMapping("/auto-complete")
    @ResponseBody
    public String autoComplete(@RequestParam("word") String word) throws JsonProcessingException {
        List<String> list = expenseRepository.getDistinctDescription(word+"%");


        return objectMapper.writeValueAsString(list);
    }

    @GetMapping("/dataset/category")
    @ResponseBody
    public String datasetCategory(@SessionAttribute("user") User user) throws JsonProcessingException {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(today.getDayOfMonth()-1);
        LocalDate endDate= startDate.plusMonths(1).minusDays(1);
        List<CategoryExpense> categoryExpenses
                = expenseRepository.getCategoryExpenseByUserIdOrderByCategoryId(user.getId(), startDate, endDate);
        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();

        for (CategoryExpense categoryExpense : categoryExpenses) {
            labels.add(categoryExpense.getCategoryName());
            data.add(categoryExpense.getTotal());
        }


        return objectMapper.writeValueAsString(
                ChartDataResponse.builder().labels(labels).data(data).build()
        );
//        return objectMapper.writeValueAsString(categoryExpenses);
    }

    @GetMapping("/dataset/daily")
    @ResponseBody
    public String datasetDaily(@SessionAttribute("user") User user) throws JsonProcessingException {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(today.getDayOfMonth()-1);
        LocalDate endDate= startDate.plusMonths(1).minusDays(1);

        List<LocalDate> date = new ArrayList<>();
        List<Long> data = new ArrayList<>();

        return objectMapper.writeValueAsString(
                DailyChartResponse.builder().date(date).data(data).build()
        );
    }
}
