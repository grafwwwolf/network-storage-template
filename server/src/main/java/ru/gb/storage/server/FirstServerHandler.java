package ru.gb.storage.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.gb.storage.commons.message.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FirstServerHandler extends SimpleChannelInboundHandler<Message> {

    private int counter = 0;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("New Active channel");
        TextMessage answer = new TextMessage();
        answer.setText("Successfully connection");
        ctx.writeAndFlush(answer);
    }

    protected void channelRead0(ChannelHandlerContext ctx, Message message) throws FileNotFoundException {

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

        if (message instanceof FileRequestMessage) {
            FileRequestMessage msg = (FileRequestMessage) message;
            final File file = new File(msg.getPath());

            try (final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
                while (randomAccessFile.getFilePointer() != randomAccessFile.length()) {
                    final byte[] fileContent;
                    final long available = randomAccessFile.length() - randomAccessFile.getFilePointer();
                    if (available > 64 * 1024) {
                        fileContent = new byte[64 * 1024];
                    } else {
                        fileContent = new byte[(int) available];
                    }
                    final FileContentMessage fileContentMessage = new FileContentMessage();
                    fileContentMessage.setStartPosition(randomAccessFile.getFilePointer());
                    randomAccessFile.read(fileContent);
                    fileContentMessage.setContent(fileContent);
                    fileContentMessage.setLast(randomAccessFile.getFilePointer() == randomAccessFile.length());
                    ctx.writeAndFlush(fileContentMessage);
                    System.out.println("Message sent " + ++counter);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Channel inactive");
    }
}
