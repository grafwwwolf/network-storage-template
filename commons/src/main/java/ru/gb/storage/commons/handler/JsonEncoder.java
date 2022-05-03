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

        byte[] outMessage = OBJECT_MAPPER.writeValueAsBytes(message);
        out.add(ctx.alloc().buffer().writeBytes(outMessage));
    }
}
