package org.codenova.moneylog.controller;

import lombok.AllArgsConstructor;
import org.codenova.moneylog.entity.Category;
import org.codenova.moneylog.entity.Expense;
import org.codenova.moneylog.entity.User;
import org.codenova.moneylog.query.DailyExpense;
import org.codenova.moneylog.repository.CategoryRepository;
import org.codenova.moneylog.repository.ExpenseRepository;
import org.codenova.moneylog.request.SearchPeriodRequest;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@Controller
@AllArgsConstructor
@RequestMapping("/expense")
public class ExpenseController {
    CategoryRepository categoryRepository;
    ExpenseRepository expenseRepository;

    @GetMapping("/history")
    public String historyHandle(Model model,
                                @ModelAttribute SearchPeriodRequest searchPeriodRequest,
                                @SessionAttribute("user") Optional<User> user) {


        if (user.isEmpty()) {
            return "redirect:/auth/login";
        }

        LocalDate startDate;
        LocalDate endDate;
        if (searchPeriodRequest.getStartDate() != null && searchPeriodRequest.getEndDate() != null) {
            startDate = searchPeriodRequest.getStartDate();
            endDate = searchPeriodRequest.getEndDate();
        } else {
            LocalDate today = LocalDate.now();
            startDate = today.minusDays(today.getDayOfMonth() - 1);
            endDate = today.plusMonths(1).minusDays(1);
        }

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);


        model.addAttribute("expenseList", expenseRepository.findWithCategoryByUserId(user.get().getId()));

        List<Category> categoryList = categoryRepository.selectAll();
        model.addAttribute("categoryList", categoryList);

        model.addAttribute("now", LocalDate.now());
        return "expense/history";
    }

    @PostMapping("/history")
    public String historyPost(@ModelAttribute Expense expense, @SessionAttribute("user") Optional<User> user,
                              Model model) {
        if (user.isEmpty()) {
            return "redirect:/auth/login";
        }

        expense.setUserId(user.get().getId());
        expenseRepository.create(expense);

        return "redirect:/expense/history";
    }

    @GetMapping("/report")
    public String reportHandle(Model model, @SessionAttribute("user") User user) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(today.getDayOfMonth() - 1);
        LocalDate endDate = startDate.plusDays(1).minusDays(1);

        model.addAttribute("categoryExpense",
                expenseRepository.getCategoryExpenseByUserIdOrderByCategoryId(user.getId(), startDate, endDate));

        List<DailyExpense> list =
                expenseRepository.getDailyExpenseByUserIdAndPeriod(user.getId(), startDate, endDate);
        Map<LocalDate, DailyExpense> dateMap = new HashMap<>();

        for (DailyExpense expense : list) {
            dateMap.put(expense.getExpenseDate(), expense);
        }
        List<DailyExpense> fullList = new ArrayList<>();
        for (int i = 0; startDate.plusDays(i).isBefore(endDate) || startDate.plusDays(i).isEqual(endDate); i++) {
            LocalDate d = startDate.plusDays(i);
            if (dateMap.get(d) != null) {
                fullList.add(dateMap.get(d));
            } else {
                fullList.add(DailyExpense.builder().expenseDate(d).total(0).build());
            }
        }
        model.addAttribute("dailyExpense", fullList);

        return "expense/report";
    }
}
