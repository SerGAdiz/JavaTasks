package com.javarush.task.task30.task3008.client;

public class ClientGuiController extends Client {
     private ClientGuiModel model = new ClientGuiModel();
     private ClientGuiView view = new ClientGuiView(this);

    public static void main(String[] args) {
        ClientGuiController controller = new ClientGuiController();
        controller.run();
    }

    @Override
    protected SocketThread getSocketThread() {
        GuiSocketThread guiSocketThread = new GuiSocketThread();
        return guiSocketThread;
    }
    @Override
    public void run() {
        getSocketThread().run();
    }

    @Override
    public int getServerPort() {
        return view.getServerPort();
    }
    @Override
    public String getUserName() {
        return view.getUserName();
    }
    @Override
    public String getServerAddress() {
        return view.getServerAddress();
    }

    public ClientGuiModel getModel() {
        return model;
    }


    public class GuiSocketThread extends SocketThread {
        public void processIncomingMessage(String message) {
            model.setNewMessage(message);
            view.refreshMessages();
        }
        public void informAboutAddingNewUser(String userName) {
            model.addUser(userName);
            view.refreshUsers();
        }
        public void informAboutDeletingNewUser(String userName) {
            model.deleteUser(userName);
            view.refreshUsers();
        }
        public void notifyConnectionStatusChanged(boolean clientConnected) {
            view.notifyConnectionStatusChanged(clientConnected);
        }
    }

}
