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
