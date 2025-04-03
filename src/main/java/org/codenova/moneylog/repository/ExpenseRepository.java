package org.codenova.moneylog.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.codenova.moneylog.entity.Expense;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ExpenseRepository {
    int create(Expense expense);
    List<Expense> selectByUserId(int userId);
    List<Expense> selectByUserIdAndDuration(@Param("userId")String userId,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate")LocalDate endDate);
}
