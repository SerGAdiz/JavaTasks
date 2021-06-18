package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.Connection;
import com.javarush.task.task30.task3008.ConsoleHelper;
import com.javarush.task.task30.task3008.Message;
import com.javarush.task.task30.task3008.MessageType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    protected Connection connection;
    private volatile boolean clientConnected = false;

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    public void run() {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                System.out.println("error");
                return;
            }
        }
        if (clientConnected) System.out.println("Соединение установлено.\n" +
                "Для выхода наберите команду 'exit'.");
        else System.out.println("Произошла ошибка во время работы клиента.");
        String message = "";
        while (clientConnected && !message.equals("exit")) {
            message = ConsoleHelper.readString();
            if (shouldSendTextFromConsole()) {
                sendTextMessage(message);
            }
        }
    }

    protected String getServerAddress() {
        System.out.println("input IP:");
        return ConsoleHelper.readString();
    } //get IP from console (user must input)
    protected int getServerPort() {
        System.out.println("input Port:");
        return ConsoleHelper.readInt();
    }     //get port from console (user must input)
    protected String getUserName() {
        System.out.println("input your Name:");
        return ConsoleHelper.readString();
    } //get user name from console (user must input)
    protected boolean shouldSendTextFromConsole() {
        return true;
    }
    protected SocketThread getSocketThread() {
        SocketThread socketThread = new SocketThread();
        return socketThread;
    }
    protected void sendTextMessage(String text) {
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            System.out.println("error with send message");
            clientConnected = false;
        }
    }

    public class SocketThread extends Thread {

        protected void processIncomingMessage(String message) {
            System.out.println(message);
        } //output message to console
        protected void informAboutAddingNewUser(String userName) {
            System.out.println(userName + " присоединился к чату.");
        }
        protected void informAboutDeletingNewUser(String userName) {
            System.out.println(userName + " покинул чат.");
        }
        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }

        }
        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();  //read from InputStream socket (get message)

                if (message.getType() == MessageType.NAME_REQUEST) {   //check messageType
                    connection.send(new Message(MessageType.USER_NAME, getUserName()));
                }
                else if (message.getType() == MessageType.NAME_ACCEPTED) {   //check messageType
                    notifyConnectionStatusChanged(true);
                    break;
                } else if (message.getType() == MessageType.NAME_REQUEST) {
                    clientHandshake();
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        } //handler of requests
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            Message message;
            while (true) {
                message = connection.receive(); //get message (from in.socket)

                if (message.getType() == MessageType.TEXT) {
                    processIncomingMessage(message.getData());
                    clientMainLoop();
                }
                else if (message.getType() == MessageType.USER_ADDED) {
                    informAboutAddingNewUser(message.getData());
                    clientMainLoop();
                }
                else if(message.getType() == MessageType.USER_REMOVED) {
                    informAboutDeletingNewUser(message.getData());
                    clientMainLoop();
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }


        }

        @Override
        public void run() {
            try {
                Socket socket = new Socket(getServerAddress(), getServerPort());
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();

            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }
    }
}
