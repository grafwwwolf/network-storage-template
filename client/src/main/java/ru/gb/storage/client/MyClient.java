package ru.gb.storage.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import ru.gb.storage.commons.handler.JsonDecoder;
import ru.gb.storage.commons.handler.JsonEncoder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyClient extends JFrame {

    private JTextField msgInputField;
    private JTextArea chatArea;
    private ExecutorService cachedService;
    private final NioEventLoopGroup group = new NioEventLoopGroup(1);
    private Channel channel;
    private FirstClientHandler firstClientHandler;

    private boolean noTakedEnd = true;

    public MyClient() throws HeadlessException {

        start();
        prepareGUI();
    }

    public void start() {
        cachedService = Executors.newSingleThreadExecutor();
        cachedService.execute(() -> {
//            final NioEventLoopGroup group = new NioEventLoopGroup(1);

            try {
                Bootstrap bootstrap = new Bootstrap()
                        .group(group)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.SO_KEEPALIVE, true)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) {
                                socketChannel.pipeline().addLast(
                                        new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 3, 0, 3),
                                        new LengthFieldPrepender(3),
                                        new JsonDecoder(),
                                        new JsonEncoder(),
//                                    new SimpleChannelInboundHandler<Message>() {
//                                        @Override
//                                        public void channelActive(ChannelHandlerContext ctx) {
//                                            System.out.println("channel active");
//                                            final FileRequestMessage message = new FileRequestMessage();
//                                            message.setPath("F:\\wrc.mkv");
//                                            ctx.writeAndFlush(message);
//                                            System.out.println(message);
//                                        }
//
//                                        @Override
//                                        protected void channelRead0(ChannelHandlerContext ctx, Message message) {
//                                            System.out.println("inc msg: " + message);
//                                            if (message instanceof FileContentMessage) {
//                                                FileContentMessage fcm = (FileContentMessage) message;
//
//                                                try (final RandomAccessFile randomAccessFile = new RandomAccessFile("E:\\programms\\Warcraft.mkv", "rw")) {
//                                                    randomAccessFile.seek(fcm.getStartPosition());
//                                                    randomAccessFile.write(fcm.getContent());
//                                                    if (fcm.isLast()) {
//                                                        ctx.close();
//                                                    }
//                                                } catch (FileNotFoundException e) {
//                                                    e.printStackTrace();
//                                                } catch (IOException e) {
//                                                    e.printStackTrace();
//                                                }
//                                            }
//                                        }
//                                    }
//                                        new FirstClientHandler(msgInputField, chatArea)
                                        firstClientHandler = new FirstClientHandler(msgInputField, chatArea)
                                );
                            }
                        });

                System.out.println("Client started");

//                Channel channel = bootstrap.connect("localhost", 9000).sync().channel();
                channel = bootstrap.connect("localhost", 9000).sync().channel();
                channel.closeFuture().sync();

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                group.shutdownGracefully();
            }
        });


    }

    public void prepareGUI() {
        // Параметры окна
        setBounds(600, 300, 500, 500);
        setTitle("NWS command console");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        msgInputField = new JTextField();
        add(bottomPanel, BorderLayout.SOUTH);
        bottomPanel.add(msgInputField, BorderLayout.CENTER);
//        btnSendMsg.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
////                sendMessage();
//            }
//        });
        msgInputField.addActionListener(e -> sendMessage());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                if (Objects.nonNull(channel)) {
                    channel.close();
                }
                if (Objects.nonNull(group)) {
                    group.shutdownGracefully();
                }
                cachedService.shutdown();

                if (noTakedEnd) {
//              dos.writeUTF("/end");
                }
            }
        });

        setVisible(true);
    }

    private void sendMessage() {
        String msg = msgInputField.getText();
        if (msg != null && !msg.trim().isEmpty()) {
            firstClientHandler.sendMessage(msg);
            msgInputField.setText("");
            msgInputField.grabFocus();
            if (msg.startsWith("/end")) {
                chatArea.append("-------------Вы закрыли соединение-------------\n");
                msgInputField.setText("");
                msgInputField.setEditable(false);
            }
        }
    }
}
