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

package eu.mico.platform.persistence.model;

import org.openrdf.model.URI;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 */
public interface Asset {

    String STORAGE_SERVICE_URN_PREFIX="urn:eu.mico-project:storage.location:";

    URI getLocation();

    String getFormat();

    String getName();

    void setName(String name);

    void setFormat(String format);

    /**
     * Return a new output stream for writing to the content. Any existing content will be overwritten.
     *
     * @return
     */
    OutputStream getOutputStream() throws IOException;

    /**
     * Return a new input stream for reading the content.
     *
     * @return
     */
    InputStream getInputStream() throws IOException;


}
