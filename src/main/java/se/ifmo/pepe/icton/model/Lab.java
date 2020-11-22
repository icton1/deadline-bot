package se.ifmo.pepe.icton.model;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import se.ifmo.pepe.icton.constant.Emoji;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
public class Lab implements Serializable, Comparable<Lab> {

    private Integer week = null; //0 - even, 1 - odd
    private Integer weekday = null;
    private String name = null;
    private Integer frequency = 1;
    private Long chatId = null;
    private Boolean notificationIsOn = false;
    private Boolean sent = false;
    private Integer mode = 0;
    private Date sendDate = null;


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

    public int getModeCode() {
        return this.mode;
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
                return String.valueOf(Emoji.CROSS_MARK);
            }
        }
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM hh:mm");

        return String.format(
                "LABA: {name = %s | week = %s | weekday = %s | frequency = %s | date = %s}\n",
                name, week, weekday, frequency, sdf.format(sendDate)
        );
    }

    public String showPossibleFeatures() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM hh:mm");

        return String.format(
                "<b>Уведомления:</b> %s\n" +
                        "<b>Режим отправки:</b> %s\n" +
                        "<b>Следующее уведомление:</b> %s\n",
                this.getNotificationIsOn() ? Emoji.HEAVY_CHECK_MARK : Emoji.CROSS_MARK,
                this.getMode() != null ? this.getMode() : Emoji.CROSS_MARK,
                this.getSendDate() != null ? sdf.format(this.getSendDate()) : Emoji.CROSS_MARK
                );
    }


    @Override
    public int compareTo(@NotNull Lab o) {
        return this.getSendDate().compareTo(o.getSendDate());
    }
}
