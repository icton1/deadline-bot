package se.ifmo.pepe.icton.util;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import se.ifmo.pepe.icton.model.Lab;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;


public class IsuUtils {

    @SneakyThrows
    public static Map<Lab, Lab> parseSchedule(String group)  {
        String url = String.format("https://itmo.ru/ru/schedule/0/%s/schedule.htm", group);
        Document doc = Jsoup
                .connect(url)
                .get();

        Map<Lab, Lab> map = new HashMap<>();
        doc.body().getElementsByTag("table").forEach(element -> {
            if (element.toString().contains("Лаб")) {
                int j = 0;
                int weekday = Integer.parseInt(element.getElementsByTag("table").attr("id").replaceAll("day", ""));
                for (int i = 2; i <= element.getElementsByTag("dd").eachText().size(); ) {
                    Lab lab = new Lab();
                    int week = element.getElementsByTag("dt").eachText().get(j).equals("четная неделя") ? 0 : 1;
                    String name;
                    if (element.getElementsByTag("dd").eachText().get(i).contains("Лаб"))
                        name = element.getElementsByTag("dd").eachText().get(i);
                    else {
                        name = element.getElementsByTag("dd").eachText().get(i + 3);
                        week = element.getElementsByTag("dt").eachText().get(j + 4).equals("четная неделя") ? 0 : 1;
                        i += 3;
                        j += 4;
                    }
                    lab.setWeekday(weekday)
                            .setName(name)
                            .setWeek(week);
                    i += 3;
                    j += 4;
                    if (map.containsKey(lab))
                        map.get(lab).setFrequency(map.get(lab).getFrequency() + 1);
                    else
                        map.put(lab, lab);

                }

            }
        });
        return map;
    }
}
