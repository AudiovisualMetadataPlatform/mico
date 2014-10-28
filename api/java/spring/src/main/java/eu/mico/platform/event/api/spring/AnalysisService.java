/**
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
package eu.mico.platform.event.api.spring;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for MICO platform analyser. The annotated analyser are automatically connected to the MICO platform at startup and automatically disconnected at shutdown.
 *
 * @author Kai Schlegel (kai.schlegel@googlemail.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface AnalysisService {
    /**
     * A unique ID (URI) that identifies this service and its functionality.
     */
    String id();

    /**
     * The type of output produced by this service as symbolic identifier (e.g. MIME type).
     */
    String provides();

    /**
     * The type of input required by this service as symbolic identifier (e.g. MIME type).
     */
    String requires();

    /**
     * The queue name that should be used by the messaging infrastructure for this service. If explicitly set,
     * this can be used to allow several services listen to the same queue and effectively implement a round-robin load
     * balancing. If queue name is not set, the event API will choose a random queue name.
     */
    String queueName() default "";
}