/*
 *  Copyright 2009, Mahmood Ali.
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are
 *  met:
 *
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following disclaimer
 *      in the documentation and/or other materials provided with the
 *      distribution.
 *    * Neither the name of Mahmood Ali. nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.notnoop.apns.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.notnoop.apns.ApnsNotification;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.EnhancedApnsNotification;
import com.notnoop.exceptions.NetworkIOException;

abstract class AbstractApnsService implements ApnsService {
    private ApnsFeedbackConnection feedback;
    private AtomicInteger c = new AtomicInteger();

    public AbstractApnsService(ApnsFeedbackConnection feedback) {
        this.feedback = feedback;
    }

    public EnhancedApnsNotification push(String deviceToken, String payload, String deviceId) throws NetworkIOException {
        EnhancedApnsNotification notification =
            new EnhancedApnsNotification(c.incrementAndGet(), EnhancedApnsNotification.MAXIMUM_EXPIRY, deviceToken, payload);
        notification.setDeviceId(deviceId);
        push(notification);
        return notification;
    }

    public EnhancedApnsNotification push(String deviceToken, String payload, Date expiry, String deviceId) throws NetworkIOException {
        EnhancedApnsNotification notification =
            new EnhancedApnsNotification(c.incrementAndGet(), (int)(expiry.getTime() / 1000), deviceToken, payload);
        notification.setDeviceId(deviceId);
        push(notification);
        return notification;
    }

    public EnhancedApnsNotification push(byte[] deviceToken, byte[] payload, String deviceId) throws NetworkIOException {
        EnhancedApnsNotification notification =
            new EnhancedApnsNotification(c.incrementAndGet(), EnhancedApnsNotification.MAXIMUM_EXPIRY, deviceToken, payload);
        notification.setDeviceId(deviceId);
        push(notification);
        return notification;
    }

    public EnhancedApnsNotification push(byte[] deviceToken, byte[] payload, int expiry, String deviceId) throws NetworkIOException {
        EnhancedApnsNotification notification =
            new EnhancedApnsNotification(c.incrementAndGet(), expiry, deviceToken, payload);
        notification.setDeviceId(deviceId);
        push(notification);
        return notification;
    }

    public abstract void push(ApnsNotification message) throws NetworkIOException;

    public Map<String, Date> getInactiveDevices() throws NetworkIOException {
        return feedback.getInactiveDevices();
    }
}
