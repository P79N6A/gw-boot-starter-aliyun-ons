/*
 * Copyright 2014 The Netty Project
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
package com.aliyun.openservices.shade.io.netty.example.http.websocketx.benchmarkserver;

import com.aliyun.openservices.shade.io.netty.buffer.ByteBuf;
import com.aliyun.openservices.shade.io.netty.buffer.Unpooled;
import com.aliyun.openservices.shade.io.netty.channel.ChannelFuture;
import com.aliyun.openservices.shade.io.netty.channel.ChannelFutureListener;
import com.aliyun.openservices.shade.io.netty.channel.ChannelHandlerContext;
import com.aliyun.openservices.shade.io.netty.channel.SimpleChannelInboundHandler;
import com.aliyun.openservices.shade.io.netty.handler.codec.http.DefaultFullHttpResponse;
import com.aliyun.openservices.shade.io.netty.handler.codec.http.FullHttpRequest;
import com.aliyun.openservices.shade.io.netty.handler.codec.http.FullHttpResponse;
import com.aliyun.openservices.shade.io.netty.handler.codec.http.HttpHeaders;
import com.aliyun.openservices.shade.io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import com.aliyun.openservices.shade.io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import com.aliyun.openservices.shade.io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import com.aliyun.openservices.shade.io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import com.aliyun.openservices.shade.io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import com.aliyun.openservices.shade.io.netty.handler.codec.http.websocketx.WebSocketFrame;
import com.aliyun.openservices.shade.io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import com.aliyun.openservices.shade.io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import com.aliyun.openservices.shade.io.netty.util.CharsetUtil;

import static com.aliyun.openservices.shade.io.netty.handler.codec.http.HttpHeaders.Names.*;
import static com.aliyun.openservices.shade.io.netty.handler.codec.http.HttpMethod.*;
import static com.aliyun.openservices.shade.io.netty.handler.codec.http.HttpResponseStatus.*;
import static com.aliyun.openservices.shade.io.netty.handler.codec.http.HttpVersion.*;

/**
 * Handles handshakes and messages
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final String WEBSOCKET_PATH = "/websocket";

    private WebSocketServerHandshaker handshaker;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        // Handle a bad request.
        if (!req.getDecoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        // Allow only GET methods.
        if (req.getMethod() != GET) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }

        // Send the demo page and favicon.ico
        if ("/".equals(req.getUri())) {
            ByteBuf content = WebSocketServerBenchmarkPage.getContent(getWebSocketLocation(req));
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);

            res.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
            HttpHeaders.setContentLength(res, content.readableBytes());

            sendHttpResponse(ctx, req, res);
            return;
        }
        if ("/favicon.ico".equals(req.getUri())) {
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
            sendHttpResponse(ctx, req, res);
            return;
        }

        // Handshake
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                getWebSocketLocation(req), null, true, 5 * 1024 * 1024);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {

        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (frame instanceof TextWebSocketFrame) {
            // Echo the frame
            ctx.write(frame.retain());
            return;
        }
        if (frame instanceof BinaryWebSocketFrame) {
            // Echo the frame
            ctx.write(frame.retain());
            return;
        }
    }

    private static void sendHttpResponse(
            ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        // Generate an error page if response getStatus code is not OK (200).
        if (res.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpHeaders.setContentLength(res, res.content().readableBytes());
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpHeaders.isKeepAlive(req) || res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private static String getWebSocketLocation(FullHttpRequest req) {
        String location =  req.headers().get(HOST) + WEBSOCKET_PATH;
        if (WebSocketServer.SSL) {
            return "wss://" + location;
        } else {
            return "ws://" + location;
        }
    }
}
