package se.ifmo.pepe.icton.factory;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class KeyboardFactory {

    public static ReplyKeyboard createReplyKeyBoard(int numberOfButtons, int numberOfRows, String[] textData, String[] callbackData) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        int index = 0;
        boolean modality = numberOfButtons % numberOfRows == 0;
        for (int i = 0; i < numberOfRows; i++) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            if (!modality)
                for (int j = 0; j <= numberOfButtons / numberOfRows; j++) {
                    rowInline.add(new InlineKeyboardButton().setText(textData[index]).setCallbackData(callbackData[index]));
                    index++;
                    if (index == numberOfButtons) break;
                }
            else
                for (int j = 0; j < numberOfButtons / numberOfRows; j++) {
                    rowInline.add(new InlineKeyboardButton().setText(textData[index]).setCallbackData(callbackData[index]));
                    index++;
                    if (index > numberOfButtons) break;
                }
            rowsInline.add(rowInline);
        }
        inlineKeyboard.setKeyboard(rowsInline);
        return inlineKeyboard;
    }

    public static ReplyKeyboard createReplyKeyboardWithUrlButton(String url) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(new InlineKeyboardButton().setText("Начать").setCallbackData("start"));
        rowsInline.add(rowInline);
        rowInline = new ArrayList<>();
        rowInline.add(new InlineKeyboardButton().setText("Поделиться").setCallbackData("share")
                .setUrl(url));
        rowsInline.add(rowInline);
        inlineKeyboard.setKeyboard(rowsInline);
        return inlineKeyboard;
    }

    public static ReplyKeyboard createReplyKeyboardWithUrlButtonOnly(String url) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(new InlineKeyboardButton().setText("Поделиться").setCallbackData("share")
                .setUrl(url));
        rowsInline.add(rowInline);
        inlineKeyboard.setKeyboard(rowsInline);
        return inlineKeyboard;
    }

    public static ReplyKeyboard createReplyKeyboardForQuestion(int numberOfAnswers){
        String[] btnText = new String[numberOfAnswers];
        String[] btnCallback = new String[numberOfAnswers];
        for (int i = 0; i < numberOfAnswers; i++) {
            btnText[i] = String.format("Вариант ответа номер #%d", (i + 1));
            btnCallback[i] = String.valueOf(i + 1);
        }
        ReplyKeyboard keyboard = KeyboardFactory.createReplyKeyBoard(numberOfAnswers, numberOfAnswers,
                btnText, btnCallback);
        return keyboard;
    }

}