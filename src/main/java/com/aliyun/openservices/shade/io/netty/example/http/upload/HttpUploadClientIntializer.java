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
package com.aliyun.openservices.shade.io.netty.example.http.upload;

import com.aliyun.openservices.shade.io.netty.channel.ChannelInitializer;
import com.aliyun.openservices.shade.io.netty.channel.ChannelPipeline;
import com.aliyun.openservices.shade.io.netty.channel.socket.SocketChannel;
import com.aliyun.openservices.shade.io.netty.handler.codec.http.HttpClientCodec;
import com.aliyun.openservices.shade.io.netty.handler.codec.http.HttpContentDecompressor;
import com.aliyun.openservices.shade.io.netty.handler.ssl.SslContext;
import com.aliyun.openservices.shade.io.netty.handler.stream.ChunkedWriteHandler;

public class HttpUploadClientIntializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;

    public HttpUploadClientIntializer(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        if (sslCtx != null) {
            pipeline.addLast("ssl", sslCtx.newHandler(ch.alloc()));
        }

        pipeline.addLast("codec", new HttpClientCodec());

        // Remove the following line if you don't want automatic content decompression.
        pipeline.addLast("inflater", new HttpContentDecompressor());

        // to be used since huge file transfer
        pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());

        pipeline.addLast("handler", new HttpUploadClientHandler());
    }
}
