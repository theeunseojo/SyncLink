package com.SyncLink.domain;

import java.time.LocalDateTime;
import java.util.Comparator;

/**
 * 일정의 시작/종료 시점을 표현하는 불변 클래스.
 * 빈 시간 계산 알고리즘에서 사용됩니다.
 */
public record TimePoint(LocalDateTime time, int type) {

    public static final int START = 1; // 일정 시작
    public static final int END = -1; // 일정 종료

    /**
     * 시간순 정렬 Comparator.
     * 같은 시간일 경우 시작 지점(+1)이 먼저 오도록 정렬합니다.
     */
    public static Comparator<TimePoint> comparator() {
        return Comparator
                .comparing(TimePoint::time)
                .thenComparing(TimePoint::type, Comparator.reverseOrder());
    }
}
