package se.ifmo.pepe.icton.factory;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class KeyboardFactory {

    public static ReplyKeyboard createReplyKeyBoard(int numberOfButtons, int numberOfRows, String[] textData, String[] callbackData) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        inlineKeyboard.setKeyboard(createRawReplyKeyBoard(numberOfButtons, numberOfRows, textData, callbackData));
        return inlineKeyboard;
    }

    public static List<List<InlineKeyboardButton>> createRawReplyKeyBoard(int numberOfButtons, int numberOfRows, String[] textData, String[] callbackData) {
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
        return rowsInline;
    }

}