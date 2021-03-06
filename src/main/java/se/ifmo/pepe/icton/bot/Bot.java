package se.ifmo.pepe.icton.bot;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.abilitybots.api.toggle.CustomToggle;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import se.ifmo.pepe.icton.constant.ChatStates;
import se.ifmo.pepe.icton.constant.Constants;
import se.ifmo.pepe.icton.constant.Emoji;
import se.ifmo.pepe.icton.factory.KeyboardFactory;
import se.ifmo.pepe.icton.model.Lab;
import se.ifmo.pepe.icton.model.Student;
import se.ifmo.pepe.icton.repository.StudentRepository;
import se.ifmo.pepe.icton.util.IsuUtils;

import java.util.*;
import java.util.function.Consumer;

@Component
public class Bot extends AbilityBot {
    private static final CustomToggle toggle = new CustomToggle()
            .turnOff("commands");

    @Autowired
    StudentRepository studentRepository;

    protected Bot(@Value("${bot.token}")
                          String botToken,
                  @Value("${bot.username}")
                          String botUsername) {
        super(botToken, botUsername, toggle);
    }

    @Override
    public int creatorId() {
        return Constants.CREATOR_ID;
    }

    /*
     * * ABILITIES START * *
     */

    public Ability startAbility() {
        return Ability.builder()
                .name("start")
                .privacy(Privacy.PUBLIC)
                .locality(Locality.USER)
                .input(0)
                .action(ctx -> {
                    new Thread(() -> {
                        if (studentRepository.findById(ctx.chatId()).isEmpty()) {
                            silent.send("Привет, введи свою группу", ctx.chatId());
                            Student student = new Student()
                                    .setUserId(ctx.chatId())
                                    .setState(ChatStates.INPUT_GROUP);
                            studentRepository.save(student);
                        } else if (studentRepository.findById(ctx.chatId()).isPresent()) {
                            Student student = studentRepository.findById(ctx.chatId()).get();
                            if (student.getGroup() != null) {
                                silent.execute(new SendMessage()
                                        .setText(String.format("Бот сохранил твою группу номер <b>%s</b> с прошлого обращения." +
                                                "\n\n" +
                                                "Если все верно, нажми на кнопку <b>старт</b>, чтобы мы обновили твое расписание" +
                                                "\n\n" +
                                                "Если номер группы неверный, введи его заново", student.getGroup()))
                                        .setChatId(ctx.chatId())
                                        .setReplyMarkup(KeyboardFactory.createReplyKeyBoard(1, 1, new String[]{"Старт"}, new String[]{"start"}))
                                        .setParseMode("HTML"));
                            } else {
                                silent.send("Привет, введи свою группу", ctx.chatId());
                            }
                            student.setState(ChatStates.INPUT_GROUP);
                            studentRepository.save(student);
                        }
                    }).start();
                })
                .build();
    }

    public Ability menuAbility() {
        return Ability.builder()
                .name("menu")
                .input(0)
                .locality(Locality.USER)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {
                    new Thread(() -> {
                        Runnable r = () -> {
                            Student student = studentRepository.findById(ctx.chatId()).get();
                            openOptions(student, Math.toIntExact(ctx.chatId()), 0, true);
                            student.setState(ChatStates.OPTIONS);
                            studentRepository.save(student);
                        };
                        new Thread(r).start();
                    }).start();
                })
                .build();
    }

    /*
     * * ABILITIES END * *
     */


    /*
     * * REPLIES START * *
     */

    public Reply messageReply() {
        Consumer<Update> action = update -> {
            Message msg = update.getMessage();
            int userId = msg.getFrom().getId();
            long chatId = msg.getChatId();
            int msgId = msg.getMessageId();
            String text = msg.getText();
            Student student = studentRepository.findById((long) userId).get();
            final Integer state = student.getState();
            SendMessage sendMessageRequest = null;

            switch (state) {
                case ChatStates.INPUT_GROUP -> {
                    Runnable r = () -> {
                        if (text.matches("[a-zA-Z]\\d{4,6}[a-zA-Z]?")) {
                            if (IsuUtils.parseSchedule(text).size() != 0) {
                                student.setGroup(text);
                                student.setLabs(IsuUtils.parseSchedule(text));
                                studentRepository.save(student);
                                silent.execute(new SendMessage()
                                        .setChatId(chatId)
                                        .setText(student.labsInfo())
                                        .setParseMode("HTML")
                                        .setReplyMarkup(KeyboardFactory.createReplyKeyBoard(1, 1, new String[]{"Перейти к настройкам"}, new String[]{"opts"})));
                            } else
                                silent.send("Такой группы не существует", chatId);

                        } else {
                            silent.send("Невалидный номер группы", chatId);
                        }
                    };
                    new Thread(r).start();

                }
            }
        };
        return Reply.of(action, update -> !update.getMessage().isGroupMessage()
                && !update.getMessage().isCommand());
    }

    public Reply buttonReply() {
        Consumer<Update> action = update -> {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String data = callbackQuery.getData();
            Message msg = callbackQuery.getMessage();
            int userId = callbackQuery.getFrom().getId();
            long chatId = msg.getChatId();
            int msgId = msg.getMessageId();
            Student student = studentRepository.findById((long) userId).get();
            final Integer state = student.getState();
            switch (data) {
                case "start" -> {
                    Runnable r = () -> {
                        hideReplyMarkup(msgId, chatId);
                        if (IsuUtils.parseSchedule(student.getGroup()).size() != 0) {
                            student.setLabs(IsuUtils.parseSchedule(student.getGroup()));
                            studentRepository.save(student);
                            silent.execute(new SendMessage()
                                    .setChatId(chatId)
                                    .setText(student.labsInfo())
                                    .setParseMode("HTML")
                                    .setReplyMarkup(KeyboardFactory.createReplyKeyBoard(1, 1,
                                            new String[]{"Перейти к настройкам"}, new String[]{"opts"})));
                        } else
                            silent.send("Такой группы не существует", chatId);

                    };
                    new Thread(r).start();
                }
                case "opts" -> {
                    Runnable r = () -> {
                        hideReplyMarkup(msgId, chatId);
                        openOptions(student, msgId, 0, false);
                        student.setState(ChatStates.OPTIONS);
                        studentRepository.save(student);
                    };
                    new Thread(r).start();
                }
            }
            switch (state) {
                case ChatStates.OPTIONS -> {
                    if (data.contains("#")) {
                        openOptions(student, msgId, Integer.parseInt(data.substring(1)), false);
                    } else if (data.contains("this_")) {
                        showOptionsForParticularLab(student, msgId, Integer.parseInt(data.substring(5)));
                    } else if (data.contains("mode_")) {
                        changeNotificationMode(student, msgId, Integer.parseInt(data.substring(5)));
                    } else if (data.contains("m_")) {
                        applyNotificationModeToParticularLab(student, Integer.parseInt(data.substring(4)),
                                Integer.parseInt(data.substring(2, 3)), msgId);
                    } else if (data.contains("back_")) {
                        openOptions(student, msgId, Integer.parseInt(data.substring(5)), false);
                    } else if (data.equals("done")) {
                        hideReplyMarkup(msgId, chatId);
                        silent.execute(new SendMessage()
                                .setText("Отлично, настройки применены." +
                                        "\n\n" +
                                        "Чтобы изменить настройки введи <b>/menu</b>" +
                                        "\n" +
                                        "Чтобы сбросить настройки введи <b>/start</b>")
                                .setParseMode("HTML")
                                .setChatId(chatId));
                    } else if (data.contains("cancel_")) {
                        showOptionsForParticularLab(student, msgId, Integer.parseInt(data.substring(7)));
                    } else if (data.contains("off_")) {
                        ArrayList<Lab> labs = new ArrayList<>(student.getLabs().values());
                        Lab lab = labs.get(Integer.parseInt(data.substring(4)));
                        lab.setSendDate(null);
                        lab.setNotificationIsOn(false);
                        lab.setMode(0);
                        student.getLabs().replace(labs.get(Integer.parseInt(data.substring(4))), lab);
                        studentRepository.save(student);
                        showOptionsForParticularLab(student, msgId, Integer.parseInt(data.substring(4)));
                    }
                }
            }

        };
        return Reply.of(action, Update::hasCallbackQuery);
    }
    /*
     * * REPLIES END * *
     */

    /*
     * * UTILITIES START * *
     */

    public void openOptions(Student student, int msgId, int index, boolean fromCmd) {
        Runnable r = () -> {
            ArrayList<Lab> labs = new ArrayList<>(student.getLabs().values());
            List<List<InlineKeyboardButton>> keyboard = null;
            if (labs.size() > 1) {
                if (index == 0)
                    keyboard = KeyboardFactory.createRawReplyKeyBoard(3, 2,
                            new String[]{"Настроить", ">>", "Готово"},
                            new String[]{"this_" + index, "#" + (index + 1), "done"});
                else if (index < labs.size() - 1)
                    keyboard = KeyboardFactory.createRawReplyKeyBoard(4, 2,
                            new String[]{"<<", ">>", "Настроить", "Готово"},
                            new String[]{"#" + (index - 1), "#" + (index + 1), "this_" + index, "done"});
                else if (index == labs.size() - 1)
                    keyboard = KeyboardFactory.createRawReplyKeyBoard(3, 2,
                            new String[]{"<<", "Настроить", "Готово"},
                            new String[]{"#" + (index - 1), "this_" + index, "done"});
            } else {
                keyboard = KeyboardFactory.createRawReplyKeyBoard(2, 2,
                        new String[]{"Настроить", "Готово"},
                        new String[]{"this_" + index, "done"});
            }
            String messageText = String.format(
                    "%s" + "%s",
                    student.labInfo(labs.get(index)),
                    labs.get(index).showPossibleFeatures()
            );
            if (fromCmd)
                silent.execute(new SendMessage()
                        .setChatId((long) msgId)
                        .setText(messageText)
                        .setParseMode("HTML")
                        .setReplyMarkup(new InlineKeyboardMarkup()
                                .setKeyboard(keyboard)));
            else
                silent.execute(new EditMessageText()
                        .setMessageId(msgId)
                        .setText(messageText)
                        .setChatId(student.getUserId())
                        .setParseMode("HTML")
                        .setReplyMarkup(new InlineKeyboardMarkup()
                                .setKeyboard(keyboard))
                );
        };
        new Thread(r).start();

    }

    public void showOptionsForParticularLab(Student student, int msgId, int index) {
        Runnable r = () -> {
            ArrayList<Lab> labs = new ArrayList<>(student.getLabs().values());
            List<List<InlineKeyboardButton>> keyboard;
            if (labs.get(index).getNotificationIsOn()) {
                keyboard = KeyboardFactory.createRawReplyKeyBoard(3, 3,
                        new String[]{"Выбрать режим отправки", "Отключить уведомления", "Назад"},
                        new String[]{"mode_" + index, "off_" + index, "back_" + index}
                );
            } else {
                keyboard = KeyboardFactory.createRawReplyKeyBoard(2, 2,
                        new String[]{"Выбрать режим отправки", "Назад"},
                        new String[]{"mode_" + index, "back_" + index}
                );
            }
            String messageText = String.format(
                    "%s" + "%s",
                    student.labInfo(labs.get(index)),
                    labs.get(index).showPossibleFeatures()
            );
            silent.execute(new EditMessageText()
                    .setMessageId(msgId)
                    .setText(messageText)
                    .setChatId(student.getUserId())
                    .setParseMode("HTML")
                    .setReplyMarkup(new InlineKeyboardMarkup()
                            .setKeyboard(keyboard))
            );
        };
        new Thread(r).start();
    }

    public void changeNotificationMode(Student student, int msgId, int index) {
        Runnable r = () -> {
            List<List<InlineKeyboardButton>> keyboard = KeyboardFactory.createRawReplyKeyBoard(4, 4,
                    new String[]{"1 раз в неделю",
                            "1 раз в две недели",
                            "Каждый день",
                            "Отмена"},
                    new String[]{"m_1_" + index,
                            "m_2_" + index,
                            "m_3_" + index,
                            "cancel_" + index}
            );
            silent.execute(new EditMessageText()
                    .setMessageId(msgId)
                    .setText("Выбери режим отправки")
                    .setChatId(student.getUserId())
                    .setParseMode("HTML")
                    .setReplyMarkup(new InlineKeyboardMarkup()
                            .setKeyboard(keyboard))
            );
        };
        new Thread(r).start();
    }

    public void applyNotificationModeToParticularLab(Student student, int index, int mode, int mshId) {
        Runnable r = () -> {
            ArrayList<Lab> labs = new ArrayList<>(student.getLabs().values());
            Lab lab = labs.get(index);
            lab.setMode(mode);
            lab.setNotificationIsOn(true);
            student.getLabs().replace(labs.get(index), lab);
            lab.setSendDate(calculateDateForNotification(lab, student));
            studentRepository.save(student);
            showOptionsForParticularLab(student, mshId, index);
        };
        new Thread(r).start();
    }

    @Scheduled(cron = "0 0 12 * * ?")
    public void sendLabNotification() {
        Runnable r = () -> {
            Iterable<Student> students = studentRepository.findAll();
            ArrayList<Lab> labs = new ArrayList<>();
            ArrayList<Lab> set = new ArrayList<>();
            students.forEach(s -> {
                s.getLabs().forEach((k, v) -> {
                    if (v.getSendDate() != null) {
                        if (v.getNotificationIsOn()) {
                            v.setChatId(s.getUserId());
                            labs.add(v);
                        }
                    }
                });
            });
            if (!labs.isEmpty())
                for (Lab l : labs) {
                    if (new Date().after(l.getSendDate()))
                        set.add(l);
                }

            if (!set.isEmpty())
                set.forEach(l -> silent.send(String.format("%s Пора делать лабу по предмету \"%s\"",
                        Emoji.HEAVY_EXCLAMATION_MARK_SYMBOL ,l.getName().substring(0, l.getName().length() - 5)), l.getChatId()));

            labs.clear();
            set.clear();
            students.forEach(s -> {
                Map<Lab, Lab> futureLabs = new HashMap<>(s.getLabs());
                futureLabs.forEach((k, v) -> {
                    v.setSendDate(calculateDateForNotification(v, s));
                    futureLabs.replace(k, v);
                });
                studentRepository.save(s);
            });
        };
        new Thread(r).start();
    }

    private Date calculateDateForNotification(Lab lab, Student student) {
        Calendar today = Calendar.getInstance();
        int tomorrow = today.get(Calendar.DAY_OF_MONTH) + 1;
        int currentWeekOFYear = today.get(Calendar.WEEK_OF_YEAR);
        if (lab.getNotificationIsOn() || student.resolveEstimatedDays(lab.getFrequency()) > 0) {
            switch (lab.getModeCode()) {
                case 1 -> {
                    return getWeekDateForParticularLab(lab, currentWeekOFYear);
                }
                case 2 -> {
                    if (currentWeekOFYear % 2 != lab.getWeek()) {
                        currentWeekOFYear += 1;
                    }
                    return getWeekDateForParticularLab(lab, currentWeekOFYear);
                }
                case 3 -> {
                    today.set(Calendar.DAY_OF_MONTH, tomorrow);
                    today.set(Calendar.HOUR, 12);
                    today.set(Calendar.MINUTE, 0);
                    today.set(Calendar.SECOND, 0);
                    return today.getTime();
                }
            }
        }
        return null;
    }

    @NotNull
    private Date getWeekDateForParticularLab(Lab lab, int currentWeekOFYear) {
        Date desiredDate;
        Calendar cal = Calendar.getInstance();
        int weekday = lab.getWeekday() + 1 > 7 ? 1 : lab.getWeekday() + 1;
        cal.setWeekDate(cal.get(Calendar.YEAR), currentWeekOFYear, weekday - 3 <= 0 ? 7 - Math.abs(weekday - 3) : weekday - 3);
        cal.set(Calendar.HOUR, 12);
        cal.set(Calendar.MINUTE, 0);
        desiredDate = cal.getTime();
        return desiredDate;
    }

    private void hideReplyMarkup(int messageId, long chatId) {
        Runnable r = () -> {
            EditMessageReplyMarkup ed = new EditMessageReplyMarkup();
            silent.execute(ed
                    .setChatId(chatId)
                    .setMessageId(messageId)
                    .setReplyMarkup(null));
        };
        new Thread(r).start();
    }

    /*
     * * UTILITIES END * *
     */
}
