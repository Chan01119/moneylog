package org.codenova.moneylog.response;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@Builder
public class DailyChartResponse {
    private List<LocalDate> date;
    private List<Long> data;
}
