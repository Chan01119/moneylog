package org.codenova.moneylog;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.DayOfWeek;
import java.time.LocalDate;

@SpringBootTest
public class LocalDateTests {

    @Test
    public void test01() {
        LocalDate today = LocalDate.now();

        DayOfWeek dow = today.getDayOfWeek();
        System.out.println(dow);
        int value = today.getDayOfWeek().getValue();
        System.out.println(value);
        System.out.println(today.plusDays(1).getDayOfWeek().getValue());
        System.out.println(today.plusDays(2).getDayOfWeek().getValue());
        System.out.println(today.plusDays(5).getDayOfWeek().getValue());

        LocalDate d = LocalDate.of(2025, 1, 24);
        LocalDate firstDayOfWeek = d.minusDays( d.getDayOfWeek().getValue());
        LocalDate lastDayOfWeek = d.minusDays(7 - d.getDayOfWeek().getValue());
        System.out.println(firstDayOfWeek);
        System.out.println(lastDayOfWeek);
        // 한주의 시작을 일 ~ 토 로 보고 싶으면 ?
    }


    @Test
    public void test02() {
        LocalDate today = LocalDate.now();
        System.out.println(today);

    }

}
