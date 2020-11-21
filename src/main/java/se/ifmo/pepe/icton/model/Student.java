package se.ifmo.pepe.icton.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;


@Data
@Entity
@Table(name = "students")
public class Student implements Serializable {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "state")
    private Integer state;

    @Column(name = "stud_group")
    private String group = null;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "lab_info")
    private Map<Lab, Lab> labs = new HashMap<>();

    /*
     * * FLUENT(BUILDER) SETTERS * *
     */

    public Student setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public Student setState(Integer state) {
        this.state = state;
        return this;
    }

    public Student setGroup(String group) {
        this.group = group;
        return this;
    }

    public Student setLabs(Map<Lab, Lab> labs) {
        this.labs = labs;
        return this;
    }

    public String labsInfo() {
        StringBuilder sb = new StringBuilder();
        this.labs.forEach((k,lab) ->
                sb.append(String.format(
                        "<b>Название:</b> %s\n" +
                                "<b>Неделя:</b> %s\n" +
                                "<b>День недели:</b> %s\n" +
                                "<b>Осталось до конца семестра:</b> %s\n\n",
                        lab.getName(),
                        resolveWeek(lab.getWeek()),
                        resolveWeekday(lab.getWeekday()),
                        resolveEstimatedDays(lab.getFrequency()))));
        return sb.toString();
    }

    public String labInfo(Lab lab) {
        return String.format(
                "<b>Название:</b> %s\n" +
                        "<b>Неделя:</b> %s\n" +
                        "<b>День недели:</b> %s\n" +
                        "<b>Осталось до конца семестра:</b> %s\n\n",
                lab.getName(),
                resolveWeek(lab.getWeek()),
                resolveWeekday(lab.getWeekday()),
                resolveEstimatedDays(lab.getFrequency()));
    }

    private String resolveWeek(Integer week) {
        switch (week) {
            case 0 -> {
                return "Четная";
            }
            case 1 -> {
                return "Нечетная";
            }
            default -> {
                return "На всякий случай";
            }
        }
    }

    private String resolveWeekday(Integer weekday) {
        switch (weekday) {
            case 1 -> {
                return "ПН";
            }
            case 2 -> {
                return "ВТ";
            }
            case 3 -> {
                return "СР";
            }
            case 4 -> {
                return "ЧТ";
            }
            case 5 -> {
                return "ПТ";
            }
            case 6 -> {
                return "СБ";
            }
            case 7 -> {
                return "ВСК";
            }
            default -> {
                return "ХЗ";
            }
        }
    }

    public int resolveEstimatedDays(Integer frequency) {
        Calendar today = Calendar.getInstance();
        int year = today.get(Calendar.YEAR);
        GregorianCalendar first_sem = new GregorianCalendar();
        first_sem.set(Calendar.DAY_OF_MONTH, 31);
        first_sem.set(Calendar.MONTH, Calendar.DECEMBER);
        first_sem.set(Calendar.YEAR, year);

        GregorianCalendar second_sem = new GregorianCalendar();
        second_sem.set(Calendar.DAY_OF_MONTH, 30);
        second_sem.set(Calendar.MONTH, Calendar.JUNE);
        second_sem.set(Calendar.YEAR, year + 1);

        if (today.after(first_sem))
            return ((Math.abs(today.get(Calendar.DAY_OF_YEAR) - second_sem.get(Calendar.DAY_OF_YEAR)) / 7) / 2 / frequency);
        else
            return ((Math.abs(today.get(Calendar.DAY_OF_YEAR) - first_sem.get(Calendar.DAY_OF_YEAR)) / 7) / 2 / frequency);
    }


}
