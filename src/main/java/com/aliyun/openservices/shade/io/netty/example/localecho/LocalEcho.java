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
package com.aliyun.openservices.shade.io.netty.example.localecho;

import com.aliyun.openservices.shade.io.netty.bootstrap.Bootstrap;
import com.aliyun.openservices.shade.io.netty.bootstrap.ServerBootstrap;
import com.aliyun.openservices.shade.io.netty.channel.Channel;
import com.aliyun.openservices.shade.io.netty.channel.ChannelFuture;
import com.aliyun.openservices.shade.io.netty.channel.ChannelInitializer;
import com.aliyun.openservices.shade.io.netty.channel.EventLoopGroup;
import com.aliyun.openservices.shade.io.netty.channel.local.LocalAddress;
import com.aliyun.openservices.shade.io.netty.channel.local.LocalChannel;
import com.aliyun.openservices.shade.io.netty.channel.local.LocalEventLoopGroup;
import com.aliyun.openservices.shade.io.netty.channel.local.LocalServerChannel;
import com.aliyun.openservices.shade.io.netty.channel.nio.NioEventLoopGroup;
import com.aliyun.openservices.shade.io.netty.handler.logging.LogLevel;
import com.aliyun.openservices.shade.io.netty.handler.logging.LoggingHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public final class LocalEcho {

    static final String PORT = System.getProperty("port", "test_port");

    public static void main(String[] args) throws Exception {
        // Address to bind on / connect to.
        final LocalAddress addr = new LocalAddress(PORT);

        EventLoopGroup serverGroup = new LocalEventLoopGroup();
        EventLoopGroup clientGroup = new NioEventLoopGroup(); // NIO event loops are also OK
        try {
            // Note that we can use any event loop to ensure certain local channels
            // are handled by the same event loop thread which drives a certain socket channel
            // to reduce the communication latency between socket channels and local channels.
            ServerBootstrap sb = new ServerBootstrap();
            sb.group(serverGroup)
              .channel(LocalServerChannel.class)
              .handler(new ChannelInitializer<LocalServerChannel>() {
                  @Override
                  public void initChannel(LocalServerChannel ch) throws Exception {
                      ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                  }
              })
              .childHandler(new ChannelInitializer<LocalChannel>() {
                  @Override
                  public void initChannel(LocalChannel ch) throws Exception {
                      ch.pipeline().addLast(
                              new LoggingHandler(LogLevel.INFO),
                              new LocalEchoServerHandler());
                  }
              });

            Bootstrap cb = new Bootstrap();
            cb.group(clientGroup)
              .channel(LocalChannel.class)
              .handler(new ChannelInitializer<LocalChannel>() {
                  @Override
                  public void initChannel(LocalChannel ch) throws Exception {
                      ch.pipeline().addLast(
                              new LoggingHandler(LogLevel.INFO),
                              new LocalEchoClientHandler());
                  }
              });

            // Start the server.
            sb.bind(addr).sync();

            // Start the client.
            Channel ch = cb.connect(addr).sync().channel();

            // Read commands from the stdin.
            System.out.println("Enter text (quit to end)");
            ChannelFuture lastWriteFuture = null;
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            for (;;) {
                String line = in.readLine();
                if (line == null || "quit".equalsIgnoreCase(line)) {
                    break;
                }

                // Sends the received line to the server.
                lastWriteFuture = ch.writeAndFlush(line);
            }

            // Wait until all messages are flushed before closing the channel.
            if (lastWriteFuture != null) {
                lastWriteFuture.awaitUninterruptibly();
            }
        } finally {
            serverGroup.shutdownGracefully();
            clientGroup.shutdownGracefully();
        }
    }
}
