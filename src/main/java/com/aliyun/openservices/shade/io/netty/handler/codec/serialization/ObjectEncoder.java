/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.aliyun.openservices.shade.io.netty.handler.codec.serialization;

import com.aliyun.openservices.shade.io.netty.buffer.ByteBuf;
import com.aliyun.openservices.shade.io.netty.buffer.ByteBufOutputStream;
import com.aliyun.openservices.shade.io.netty.channel.ChannelHandler.Sharable;
import com.aliyun.openservices.shade.io.netty.channel.ChannelHandlerContext;
import com.aliyun.openservices.shade.io.netty.handler.codec.MessageToByteEncoder;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * An encoder which serializes a Java object into a {@link ByteBuf}.
 * <p>
 * Please note that the serialized form this encoder produces is not
 * compatible with the standard {@link ObjectInputStream}.  Please use
 * {@link ObjectDecoder} or {@link ObjectDecoderInputStream} to ensure the
 * interoperability with this encoder.
 */
@Sharable
public class ObjectEncoder extends MessageToByteEncoder<Serializable> {
    private static final byte[] LENGTH_PLACEHOLDER = new byte[4];

    @Override
    protected void encode(ChannelHandlerContext ctx, Serializable msg, ByteBuf out) throws Exception {
        int startIdx = out.writerIndex();

        ByteBufOutputStream bout = new ByteBufOutputStream(out);
        bout.write(LENGTH_PLACEHOLDER);
        ObjectOutputStream oout = new CompactObjectOutputStream(bout);
        oout.writeObject(msg);
        oout.flush();
        oout.close();

        int endIdx = out.writerIndex();

        out.setInt(startIdx, endIdx - startIdx - 4);
    }
}
