/*
 * Copyright 2013 The Netty Project
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
package com.aliyun.openservices.shade.io.netty.handler.codec.spdy;

import com.aliyun.openservices.shade.io.netty.channel.CombinedChannelDuplexHandler;

/**
 * A combination of {@link SpdyHttpDecoder} and {@link SpdyHttpEncoder}
 */
public final class SpdyHttpCodec
        extends CombinedChannelDuplexHandler<SpdyHttpDecoder, SpdyHttpEncoder> {
    /**
     * Creates a new instance with the specified decoder options.
     */
    public SpdyHttpCodec(SpdyVersion version, int maxContentLength) {
        super(new SpdyHttpDecoder(version, maxContentLength), new SpdyHttpEncoder(version));
    }

    /**
     * Creates a new instance with the specified decoder options.
     */
    public SpdyHttpCodec(SpdyVersion version, int maxContentLength, boolean validateHttpHeaders) {
        super(new SpdyHttpDecoder(version, maxContentLength, validateHttpHeaders), new SpdyHttpEncoder(version));
    }
}
