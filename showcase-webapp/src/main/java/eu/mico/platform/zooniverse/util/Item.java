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

package eu.mico.platform.zooniverse.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Item {
    private boolean finished;
    private boolean error;
    private ZonedDateTime created;
    private String errorMessage;
    private URI uri;

    public boolean hasFinished() { return finished; }
    public boolean hasError() { return error; }
    public ZonedDateTime getCreationDateTime() { return created; }
    public String getErrorMessage() { return errorMessage; }
    public URI getUri() { return uri; }

    public void setHasError(String hasError) { this.error = true ? hasError.trim().equalsIgnoreCase("true") : false; }
    public void setFinished(String finished) { this.finished = true ? finished.trim().equalsIgnoreCase("true") : false; }
    public void setTime(String time) {
        this.created = ZonedDateTime.from(DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault()).parse(time));
    }
    public void setError(String error) {this.errorMessage = error;}
    public void setUri(String uri) throws URISyntaxException {this.uri = new URI(uri); }
}
