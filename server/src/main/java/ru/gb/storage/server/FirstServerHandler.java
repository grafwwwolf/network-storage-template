package ru.gb.storage.server;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.gb.storage.commons.message.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FirstServerHandler extends SimpleChannelInboundHandler<Message> {

    private RandomAccessFile randomAccessFile = null;
    private int counter = 0;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("New Active channel");
        TextMessage answer = new TextMessage();
        answer.setText("Successfully connection to server");
        ctx.writeAndFlush(answer);
    }

    protected void channelRead0(ChannelHandlerContext ctx, Message message) throws IOException {

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
            if (randomAccessFile == null) {
                final File file = new File(msg.getPathFromFile());
                randomAccessFile = new RandomAccessFile(file, "r");
                sendFile(ctx, msg.getPathToFile());
            }
        }

        if (message instanceof FileContentMessage) {
            FileContentMessage fcm = (FileContentMessage) message;
            try (final RandomAccessFile accessFile = new RandomAccessFile(fcm.getToFile(), "rw")) {
                accessFile.seek(fcm.getStartPosition());
                accessFile.write(fcm.getContent());
                System.out.println(fcm.getContent());
                if (fcm.isLast()) {
                    System.out.println("Загрузка файла окончена");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    protected void sendFile(ChannelHandlerContext ctx, String toFile) throws IOException {

        if (randomAccessFile != null) {
            final byte[] fileContent;
            final long available = randomAccessFile.length() - randomAccessFile.getFilePointer();
            if (available > 64 * 1024) {
                fileContent = new byte[64 * 1024];
            } else {
                fileContent = new byte[(int) available];
            }
            final FileContentMessage fileContentMessage = new FileContentMessage();
            fileContentMessage.setToFile(toFile);
            fileContentMessage.setStartPosition(randomAccessFile.getFilePointer());
            randomAccessFile.read(fileContent);
            fileContentMessage.setContent(fileContent);
            boolean last = randomAccessFile.getFilePointer() == randomAccessFile.length();
            fileContentMessage.setLast(last);
            ctx.channel().writeAndFlush(fileContentMessage).addListener((ChannelFutureListener) channelFuture -> {
                if (!last) {
                    sendFile(ctx, toFile);
                    System.out.println("Message sent " + ++counter);
                }
            });
            if (last) {
                randomAccessFile.close();
                randomAccessFile = null;
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
        if (randomAccessFile != null) {
            randomAccessFile.close();
        }
    }
}
