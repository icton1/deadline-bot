package se.ifmo.pepe.icton.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Lab implements Serializable {

    private Integer week = null; //0 - even, 1 - odd
    private Integer weekday = null;
    private String name = null;
    private Integer frequency = 1;

    public Lab setFrequency(Integer frequency) {
        this.frequency = frequency;
        return this;
    }

    public Lab setName(String name) {
        this.name = name;
        return this;
    }

    public Lab setWeek(int week) {
        this.week = week;
        return this;
    }

    public Lab setWeekday(Integer weekday) {
        this.weekday = weekday;
        return this;
    }

    @Override
    public String toString() {
        return String.format(
                "LABA: {name = %s | week = %s | weekday = %s | frequency = %s}\n",
                name, week, weekday, frequency
        );
    }


}
