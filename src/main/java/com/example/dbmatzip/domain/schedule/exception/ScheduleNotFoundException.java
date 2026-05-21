package com.example.dbmatzip.domain.schedule.exception;

public class ScheduleNotFoundException extends RuntimeException {

    public ScheduleNotFoundException(Long scheduleId) {
        super("일정을 찾을 수 없습니다. id=" + scheduleId);
    }
}
