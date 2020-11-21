package se.ifmo.pepe.icton.util;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import se.ifmo.pepe.icton.model.Lab;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;


public class IsuUtils {

    @SneakyThrows
    public static Set<Lab> parseSchedule(String group)  {
        String url = String.format("https://itmo.ru/ru/schedule/0/%s/schedule.htm", group);
        Document doc = Jsoup
                .connect(url)
                .get();

        Set<Lab> labSet = new HashSet<>();
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
                    if (labSet.contains(lab))
                        labSet.forEach(l -> {
                            if (l.equals(lab)) l.setFrequency(l.getFrequency() + 1);
                        });
                    else
                        labSet.add(lab);

                }

            }
        });
        return labSet;
    }
}
