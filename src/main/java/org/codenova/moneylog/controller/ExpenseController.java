package org.codenova.moneylog.controller;

import lombok.AllArgsConstructor;
import org.codenova.moneylog.entity.Category;
import org.codenova.moneylog.entity.Expense;
import org.codenova.moneylog.entity.User;
import org.codenova.moneylog.repository.CategoryRepository;
import org.codenova.moneylog.repository.ExpenseRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
@AllArgsConstructor
@RequestMapping("/expense")
public class ExpenseController {
    CategoryRepository categoryRepository;
    ExpenseRepository expenseRepository;

    @GetMapping("/history")
    public String historyHandle(Model model, @SessionAttribute("user") Optional<User> user) {

        if (user.isEmpty()) {
            return "redirect:/auth/login";
        }

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
}
