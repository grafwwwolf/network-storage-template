package ru.gb.storage.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.gb.storage.commons.message.AuthenticationMessage;
import ru.gb.storage.commons.message.DateMessage;
import ru.gb.storage.commons.message.Message;
import ru.gb.storage.commons.message.TextMessage;

public class FirstServerHandler extends SimpleChannelInboundHandler<Message> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("New Active channel");
        TextMessage answer = new TextMessage();
        answer.setText("Successfully connection");
        ctx.writeAndFlush(answer);
    }

    protected void channelRead0(ChannelHandlerContext ctx, Message message) {

        if (message instanceof TextMessage) {
            TextMessage msg = (TextMessage) message;
            System.out.println(msg.getText());
            ctx.writeAndFlush(message);
        }

        if (message instanceof DateMessage) {
            DateMessage msg = (DateMessage) message;
            System.out.println(msg.getDate());
            ctx.writeAndFlush(message);
        }

        if (message instanceof AuthenticationMessage) {
            AuthenticationMessage msg = (AuthenticationMessage) message;
            System.out.println("Пришел запрос на аутентификацию");
            System.out.println("Поиск в БД пользователей запись с именем: " + msg.getLogin());
            System.out.println("Пользователь найден");
            System.out.println("Проверка пароля: " + msg.getPassword());
            System.out.println("Пароль совпадает");
            TextMessage outMessage = new TextMessage();
            outMessage.setText("Вход подтвержден");
            ctx.writeAndFlush(outMessage);
        }
    }
}
