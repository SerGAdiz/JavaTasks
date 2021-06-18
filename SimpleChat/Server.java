package com.javarush.task.task30.task3008;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("Please enter port:");
        int portServer = ConsoleHelper.readInt();

        try (ServerSocket serverSocket = new ServerSocket(portServer)) {
            System.out.println("Server started!");
            while(true) {
                Socket clientSocket = serverSocket.accept();
                new Handler(clientSocket).start();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static class Handler extends Thread {
        private Socket socket;

        public Handler (Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            Message message;
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST)); //запрос имени пользователя
                message = connection.receive();  // read from InputStream socket (get message)

                if (message.getType() == MessageType.USER_NAME && message.getData() != "" && !connectionMap.containsKey(message.getData())) {
                    connectionMap.put(message.getData(), connection); // добавляем нового пользователя
                    connection.send(new Message(MessageType.NAME_ACCEPTED));
                    System.out.println(message.getData() + " was accepted");
                    break;
                }
            }
            return message.getData(); // возврат имени user
        }
        private void notifyUsers(Connection connection, String userName) throws IOException { //
            for (Map.Entry<String, Connection> user: connectionMap.entrySet()) {
                String nameOtherClient = user.getKey();
                if (!nameOtherClient.equals(userName)) {
                    connection.send(new Message(MessageType.USER_ADDED, nameOtherClient));
                }
            }

        }
        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            Message message;
            while (true) {
                message = connection.receive(); //get message client
                if (message.getType() == MessageType.TEXT) {
                    String str = userName + ": " + message.getData();
                    sendBroadcastMessage(new Message(MessageType.TEXT, str));
                } else {
                    ConsoleHelper.writeMessage("error");
                }
            }
        }

        @Override
        public void run() {
            ConsoleHelper.writeMessage("connection was successful " + socket.getRemoteSocketAddress());

            try(Connection connection = new Connection(socket)) {
                String name = serverHandshake(connection);

                sendBroadcastMessage(new Message(MessageType.USER_ADDED, name));
                notifyUsers(connection, name);
                serverMainLoop(connection, name);
                connectionMap.remove(name);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, name));

            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("error while communicating with the remote address");
            }
            ConsoleHelper.writeMessage("connection with remove address closed");
        }
    }

    public static void sendBroadcastMessage(Message message) {
        try {
            for (String key: connectionMap.keySet()) {
                connectionMap.get(key).send(message);
            }
        } catch (IOException e) {
            System.out.println("cant send message :(");
        }
    }
}
