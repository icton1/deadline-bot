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
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import se.ifmo.pepe.icton.constant.ChatStates;
import se.ifmo.pepe.icton.constant.Constants;
import se.ifmo.pepe.icton.factory.KeyboardFactory;
import se.ifmo.pepe.icton.model.Student;
import se.ifmo.pepe.icton.repository.StudentRepository;
import se.ifmo.pepe.icton.util.IsuUtils;

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
                            student.setGroup(text);
                            student.setLabs(IsuUtils.parseSchedule(student.getGroup()));
                            studentRepository.save(student);
                            System.out.println(student.getLabs());
                            silent.execute(new SendMessage()
                                    .setChatId(chatId)
                                    .setText(student.labsInfo())
                                    .setParseMode("HTML"));
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

    /*
     * * REPLIES END * *
     */





    /*
     * * UTILITIES START * *
     */

    /*
     * * UTILITIES END * *
     */
}
