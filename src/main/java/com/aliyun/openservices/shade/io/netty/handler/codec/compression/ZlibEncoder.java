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
package com.aliyun.openservices.shade.io.netty.handler.codec.compression;

import com.aliyun.openservices.shade.io.netty.buffer.ByteBuf;
import com.aliyun.openservices.shade.io.netty.channel.ChannelFuture;
import com.aliyun.openservices.shade.io.netty.channel.ChannelPromise;
import com.aliyun.openservices.shade.io.netty.handler.codec.MessageToByteEncoder;

/**
 * Compresses a {@link ByteBuf} using the deflate algorithm.
 */
public abstract class ZlibEncoder extends MessageToByteEncoder<ByteBuf> {

    protected ZlibEncoder() {
        super(false);
    }

    /**
     * Returns {@code true} if and only if the end of the compressed stream
     * has been reached.
     */
    public abstract boolean isClosed();

    /**
     * Close this {@link ZlibEncoder} and so finish the encoding.
     *
     * The returned {@link ChannelFuture} will be notified once the
     * operation completes.
     */
    public abstract ChannelFuture close();

    /**
     * Close this {@link ZlibEncoder} and so finish the encoding.
     * The given {@link ChannelFuture} will be notified once the operation
     * completes and will also be returned.
     */
    public abstract ChannelFuture close(ChannelPromise promise);

}
