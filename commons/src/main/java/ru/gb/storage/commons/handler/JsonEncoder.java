package ru.gb.storage.commons.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import ru.gb.storage.commons.message.Message;

import java.util.List;

public class JsonEncoder extends MessageToMessageEncoder<Message> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    protected void encode(ChannelHandlerContext ctx, Message message, List<Object> out) throws Exception {

        System.out.println("Out message: " + message);
        String outMessage = OBJECT_MAPPER.writeValueAsString(message);
        System.out.println("convert out message in: " + outMessage);
        out.add(outMessage);
    }
}
