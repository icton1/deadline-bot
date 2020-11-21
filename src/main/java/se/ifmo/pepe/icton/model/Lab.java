package se.ifmo.pepe.icton.model;

import lombok.Data;
import se.ifmo.pepe.icton.constant.Emoji;

import java.io.Serializable;

@Data
public class Lab implements Serializable {

    private Integer week = null; //0 - even, 1 - odd
    private Integer weekday = null;
    private String name = null;
    private Integer frequency = 1;

    private Boolean notificationIsOn = false;
    private Integer mode = 0;

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

    public void setMode(Integer mode) {
        this.mode = mode;
    }

    public String getMode() {
        switch (mode) {
            case 1 -> {
                return "1 раз в неделю";
            }
            case 2 -> {
                return "1 раз в две недели";
            }
            case 3 -> {
                return "Каждый день";
            }
            default -> {
                return "Не указано";
            }
        }
    }

    @Override
    public String toString() {
        return String.format(
                "LABA: {name = %s | week = %s | weekday = %s | frequency = %s}\n",
                name, week, weekday, frequency
        );
    }

    public String showPossibleFeatures() {
        return String.format(
                "<b>Уведомления:</b> %s\n" +
                        "<b>Режим отправки:</b> %s\n",
                this.getNotificationIsOn() ? Emoji.HEAVY_CHECK_MARK : Emoji.CROSS_MARK,
                this.getMode() != null ? this.getMode() : Emoji.CROSS_MARK
                );
    }




}
