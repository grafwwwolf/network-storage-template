package ru.gb.storage.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;

import java.util.ArrayList;
import java.util.List;

public class Server {

    private final int port;

    public Server(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws InterruptedException {

        new Server(9000).start();
    }

    public void start() throws InterruptedException {

        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap server = new ServerBootstrap();

            server.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel nioSocketChannel) {

                            nioSocketChannel.pipeline().addLast(

                                    new ChannelInboundHandlerAdapter() {

                                        private List<Byte> byteList = new ArrayList();

                                        @Override
                                        public void channelRegistered(ChannelHandlerContext ctx) {
                                            System.out.println("channelRegistered");
                                        }

                                        @Override
                                        public void channelUnregistered(ChannelHandlerContext ctx) {
                                            System.out.println("channelUnregistered");
                                        }

                                        @Override
                                        public void channelActive(ChannelHandlerContext ctx) {
                                            System.out.println("channelActive");
                                        }

                                        @Override
                                        public void channelInactive(ChannelHandlerContext ctx) {
                                            System.out.println("channelInactive");
                                        }

                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) {

                                            final ByteBuf message = (ByteBuf) msg;
                                            byte m = message.readByte();

                                            if (m != 13) {
                                                byteList.add(m);
                                            } else {

                                                byte[] bytes = new byte[byteList.size()];
                                                for (int i = 0; i < byteList.size(); i++) {
                                                    bytes[i] = byteList.get(i);
                                                }
                                                final ByteBuf outBuf = Unpooled.wrappedBuffer(bytes);
                                                ctx.writeAndFlush(outBuf);
                                                byteList.clear();
                                            }
                                            ReferenceCountUtil.release(msg);
                                        }

                                        @Override
                                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                            System.out.println(cause);
                                            ctx.close();
                                        }
                                    }
                            );

                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture channelFuture = server.bind(port).sync();

            System.out.println("Server started");
            channelFuture.channel().closeFuture().sync();

        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
