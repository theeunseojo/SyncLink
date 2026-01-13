package com.SyncLink.adapter;

import com.SyncLink.enums.ServiceType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
public class CalendarAdapterFactory {
    private final List<CalendarAdapter> adapters;

    public CalendarAdapter getAdapter(ServiceType type){
        return adapters.stream()
                .filter(cal -> cal.supports(type))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("지원하지 않는 서비스입니다."));
    }
}
