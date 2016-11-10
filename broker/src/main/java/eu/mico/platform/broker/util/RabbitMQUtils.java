/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.mico.platform.broker.util;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Method;
import com.rabbitmq.client.ShutdownSignalException;

/**
 */
public class RabbitMQUtils {

    /**
     * Check if the given Throwable was caused by a {@link ShutdownSignalException} with a
     * {@link com.rabbitmq.client.AMQP.Channel.Close} reason that has the provided replyCode.
     *
     * @param t the Throwable to check
     * @param replyCode the replyCode tho check for
     * @return {@code true} if the t has a getCause with the given replyCode
     */
    public static boolean isCausedByChannelCloseException(final Throwable t, int replyCode) {
        final Throwable cause = t.getCause();
        if (cause == null || cause == t) return false;
        if (cause instanceof ShutdownSignalException) {
            final ShutdownSignalException sse = (ShutdownSignalException) cause;
            final Method reason = sse.getReason();
            if (reason instanceof AMQP.Channel.Close) {
                AMQP.Channel.Close close = (AMQP.Channel.Close) reason;
                return close.getReplyCode() == replyCode;
            } else {
                return false;
            }
        } else {
            return isCausedByChannelCloseException(cause, replyCode);
        }
    }


    private RabbitMQUtils() {
        // static only
    }
}
