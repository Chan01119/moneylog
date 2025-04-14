package org.codenova.moneylog.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.codenova.moneylog.entity.Expense;
import org.codenova.moneylog.query.CategoryExpense;
import org.codenova.moneylog.query.DailyExpense;
import org.codenova.moneylog.query.ExpenseWithCategory;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ExpenseRepository {
    int create(Expense expense);

    List<Expense> selectByUserId(int userId);

    List<Expense> selectByUserIdAndDuration(@Param("userId") int userId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    public List<ExpenseWithCategory> findWithCategoryByUserId(@Param("userId") int userId);

    int getTotalAmountByUserIdAndPeriod(@Param("userId") int userId,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    List<Expense> getTop3ExpenseByUserId(@Param("userId") int userId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    List<CategoryExpense> getCategoryExpenseByUserId(@Param("userId") int userId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    List<CategoryExpense> getCategoryExpenseByUserIdOrderByCategoryId(@Param("userId") int userId,
                                                                      @Param("startDate") LocalDate startDate,
                                                                      @Param("endDate") LocalDate endDate);

     List<DailyExpense> getDailyExpenseByUserIdAndPeriod(@Param("userId") int userId,
                                                         @Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);

     List<String> getDistinctDescription(@Param("word") String word);
}


