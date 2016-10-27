package eu.mico.platform.broker.api.rest;

import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

import eu.mico.platform.broker.api.MICOBroker.ExtractorStatus;
import eu.mico.platform.broker.model.MICOCamelRoute.ExtractorConfiguration;

@ApiObject(name="ExtractorInformation",description="general information about an extractor service")
public class ExtractorInfo {

    @ApiObjectField(description = "unique name of extractor service")
    private String name;

    @ApiObjectField(description = "version of extractor service")
    private String version;

    @ApiObjectField(description = "process configuration of extractor service")
    private String mode;

    @ApiObjectField(description = "current status of extractor service")
    private ExtractorStatus state = null;

    public ExtractorInfo(String name, String version, String mode) {
        super();
        this.name = name;
        this.version = version;
        this.mode = mode;
    }

    public ExtractorInfo(String name, String version, String mode,
            ExtractorStatus status) {
        super();
        this.name = name;
        this.version = version;
        this.mode = mode;
        this.state = status;
    }

    public ExtractorInfo(ExtractorConfiguration extractor,
            ExtractorStatus eStatus) {
        this.name = extractor.getExtractorId();
        this.version = extractor.getVersion();
        this.mode = extractor.getModeId();
        this.state = eStatus;
    }

    @Override
    public String toString() {
        return String.format(
                "ExtractorInfo [name=%s, version=%s, mode=%s, state=%s]", name,
                version, mode, state);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public ExtractorStatus getState() {
        return state;
    }

    public void setState(ExtractorStatus state) {
        this.state = state;
    }

}
