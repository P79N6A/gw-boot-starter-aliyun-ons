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
package com.aliyun.openservices.shade.io.netty.handler.codec.http.multipart;

import com.aliyun.openservices.shade.io.netty.buffer.ByteBuf;
import com.aliyun.openservices.shade.io.netty.channel.ChannelException;
import com.aliyun.openservices.shade.io.netty.handler.codec.http.HttpConstants;
import com.aliyun.openservices.shade.io.netty.util.AbstractReferenceCounted;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * Abstract HttpData implementation
 */
public abstract class AbstractHttpData extends AbstractReferenceCounted implements HttpData {

    private static final Pattern STRIP_PATTERN = Pattern.compile("(?:^\\s+|\\s+$|\\n)");
    private static final Pattern REPLACE_PATTERN = Pattern.compile("[\\r\\t]");

    protected final String name;
    protected long definedSize;
    protected long size;
    protected Charset charset = HttpConstants.DEFAULT_CHARSET;
    protected boolean completed;

    protected AbstractHttpData(String name, Charset charset, long size) {
        if (name == null) {
            throw new NullPointerException("name");
        }

        name = REPLACE_PATTERN.matcher(name).replaceAll(" ");
        name = STRIP_PATTERN.matcher(name).replaceAll("");

        if (name.isEmpty()) {
            throw new IllegalArgumentException("empty name");
        }

        this.name = name;
        if (charset != null) {
            setCharset(charset);
        }
        definedSize = size;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    @Override
    public Charset getCharset() {
        return charset;
    }

    @Override
    public void setCharset(Charset charset) {
        if (charset == null) {
            throw new NullPointerException("charset");
        }
        this.charset = charset;
    }

    @Override
    public long length() {
        return size;
    }

    @Override
    public ByteBuf content() {
        try {
            return getByteBuf();
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    @Override
    protected void deallocate() {
        delete();
    }

    @Override
    public HttpData retain() {
        super.retain();
        return this;
    }

    @Override
    public HttpData retain(int increment) {
        super.retain(increment);
        return this;
    }
}
