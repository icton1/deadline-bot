package se.ifmo.pepe.icton.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import se.ifmo.pepe.icton.factory.KeyboardFactory;
import se.ifmo.pepe.icton.model.Lab;
import se.ifmo.pepe.icton.model.Student;
import se.ifmo.pepe.icton.repository.StudentRepository;
import se.ifmo.pepe.icton.util.IsuUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
                                student.setState(ChatStates.INPUT_GROUP);
                                studentRepository.save(student);
                            } else {
                                silent.send("Привет, введи свою группу", ctx.chatId());
                                student.setState(ChatStates.INPUT_GROUP);
                                studentRepository.save(student);
                            }
                        }
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
                        openOptions(student, msgId, 0);
                        student.setState(ChatStates.OPTIONS);
                        studentRepository.save(student);
                    };
                    new Thread(r).start();
                }
            }
            switch (state) {
                case ChatStates.OPTIONS -> {
                    if (data.contains("#")) {
                            openOptions(student, msgId, Integer.parseInt(data.substring(1)));
                    } else if (data.contains("this_")) {

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

    public void openOptions(Student student, int msgId, int index) {
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
            silent.execute(new EditMessageText()
                    .setMessageId(msgId)
                    .setText(student.labInfo(labs.get(index)))
                    .setChatId(student.getUserId())
                    .setParseMode("HTML")
                    .setReplyMarkup(new InlineKeyboardMarkup()
                            .setKeyboard(keyboard))
            );
        };
        new Thread(r).start();

    }

    public void showOptionsForParticularLab(Student student, int msgId, int index) {
        ArrayList<Lab> labs = new ArrayList<>(student.getLabs().values());
        List<List<InlineKeyboardButton>> keyboard = KeyboardFactory.createRawReplyKeyBoard(2, 2,
                new String[]{"Настроить", "Готово"},
                new String[]{"this_" + index, "done"});
        silent.execute(new EditMessageText()
                .setMessageId(msgId)
                .setText(student.labInfo(labs.get(index)))
                .setChatId(student.getUserId())
                .setParseMode("HTML")
                .setReplyMarkup(new InlineKeyboardMarkup()
                        .setKeyboard(keyboard))
        );
    }

    public void sendLabNotification(Lab lab, HashMap<String, String> options) {
        //TODO: notification
    }

    private void hideReplyMarkup(int messageId, long chatId) {
        EditMessageReplyMarkup ed = new EditMessageReplyMarkup();
        silent.execute(ed
                .setChatId(chatId)
                .setMessageId(messageId)
                .setReplyMarkup(null));
    }

    /*
     * * UTILITIES END * *
     */
}
