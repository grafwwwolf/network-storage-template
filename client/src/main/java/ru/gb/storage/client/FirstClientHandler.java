package ru.gb.storage.client;

import io.netty.channel.ChannelHandlerContext;
import ru.gb.storage.commons.message.*;
import ru.gb.storage.server.FirstServerHandler;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class FirstClientHandler extends FirstServerHandler {

    private JTextField msgInputField;
    private JTextArea chatArea;
    private ChannelHandlerContext context;
    private List<String> availableRequests = new ArrayList<>();

    public FirstClientHandler(JTextField msgInputField, JTextArea chatArea) {
        this.msgInputField = msgInputField;
        this.chatArea = chatArea;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("New Active channel");
        autoRunRequestList();
        context = ctx;

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message message) throws IOException {

        if (message instanceof TextMessage) {
            TextMessage msg = (TextMessage) message;
            System.out.println(msg.getText());
            chatArea.append(msg.getText() + "\n");
        }


//        if (message instanceof DateMessage) {
//            DateMessage msg = (DateMessage) message;
//            System.out.println(msg.getDate());
//            ctx.writeAndFlush(message);
//        }

//        if (message instanceof AuthenticationMessage) {
//            AuthenticationMessage msg = (AuthenticationMessage) message;
//            System.out.println("Пришел запрос на аутентификацию");
//            System.out.println("Поиск в БД пользователей запись с именем: " + msg.getLogin());
//            System.out.println("Пользователь найден");
//            System.out.println("Проверка пароля: " + msg.getPassword());
//            System.out.println("Пароль совпадает");
//            TextMessage outMessage = new TextMessage();
//            outMessage.setText("Вход подтвержден");
//            ctx.writeAndFlush(outMessage);
//        }

//        if (message instanceof FileRequestMessage) {
//            FileRequestMessage msg = (FileRequestMessage) message;
//            if (randomAccessFile == null) {
//                final File file = new File(msg.getPath());
//                randomAccessFile = new RandomAccessFile(file, "r");
//                sendFile(ctx);
//            }
//        }
    }

    void sendMessage(String message) {

        if (message.startsWith("nws")) {
            message = message.replaceAll("\\s+", " ");
            System.out.println(message);
            if (checkRequest(message)) {
                if (message.startsWith("nws log")) {
                    System.out.println(message);
                    String[] logon = message.split(" ");
                    if (logon.length == 4) {
                        AuthenticationMessage msg = new AuthenticationMessage();
                        msg.setLogin(logon[2]);
                        msg.setPassword(logon[3]);
                        System.out.println("log: " + msg.getLogin() + ", pswd: " + msg.getPassword());
                        context.writeAndFlush(msg);
                    } else {
                        System.out.println("Введены неверные данные для авторизации");
                        chatArea.append("Введены неверные данные для авторизации" + "\n");
                    }
                }
            } else {
                System.out.println("Введен некорректный запрос");
                chatArea.append("Введен некорректный запрос:" + "\n");
            }
        } else {
            TextMessage msg = new TextMessage();
            msg.setText(message);
            System.out.println(message);
            context.writeAndFlush(msg);
        }

        chatArea.append(message + "\n");
//        context.writeAndFlush(msg);
    }

    void autoRunRequestList() {
        availableRequests.add("nws log");
    }

    boolean checkRequest(String request) {
        String[] rq = request.split(" ");
        if (rq.length < 2) {
            return false;
        }
        return availableRequests.contains(rq[0] + " " + rq[1]);
    }

}
