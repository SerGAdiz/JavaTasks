package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BotClient extends Client {

    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }

    @Override
    protected SocketThread getSocketThread() {
        BotSocketThread botSocketThread = new BotSocketThread();
        return botSocketThread;
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        return "date_bot_" + (int)(Math.random() * 100);
    }

    public class BotSocketThread extends SocketThread {
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            ConsoleHelper.writeMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("d.MM.YYYY");
            ConsoleHelper.writeMessage(message);

            String[] arr = message.split(":");
            if (arr.length != 2) return;

            String infoForName = arr[0];
            String botHelp = arr[1].trim();

            switch (botHelp) {
                case ("дата"):
                    sendTextMessage("Информация для " + infoForName + ": " + dateFormat.format(Calendar.getInstance().getTime()));  break;
                case ("день"):
                    dateFormat = new SimpleDateFormat("d");
                    sendTextMessage("Информация для " + infoForName + ": " + dateFormat.format(Calendar.getInstance().getTime()));  break;
                case("месяц"):
                    dateFormat = new SimpleDateFormat("MMMM");
                    sendTextMessage("Информация для " + infoForName + ": " + dateFormat.format(Calendar.getInstance().getTime()));  break;
                case("год"):
                    dateFormat = new SimpleDateFormat("YYYY");
                    sendTextMessage("Информация для " + infoForName + ": " + dateFormat.format(Calendar.getInstance().getTime()));  break;
                case("время"):
                    dateFormat = new SimpleDateFormat("H:mm:ss");
                    sendTextMessage("Информация для " + infoForName + ": " + dateFormat.format(Calendar.getInstance().getTime()));  break;
                case("час"):
                    dateFormat = new SimpleDateFormat("H");
                    sendTextMessage("Информация для " + infoForName + ": " + dateFormat.format(Calendar.getInstance().getTime()));  break;
                case("минуты"):
                    dateFormat = new SimpleDateFormat("m");
                    sendTextMessage("Информация для " + infoForName + ": " + dateFormat.format(Calendar.getInstance().getTime()));  break;
                case("секунды"):
                    dateFormat = new SimpleDateFormat("s");
                    sendTextMessage("Информация для " + infoForName + ": " + dateFormat.format(Calendar.getInstance().getTime()));  break;
            } //check request (day, hour....)

        }
    }
}
