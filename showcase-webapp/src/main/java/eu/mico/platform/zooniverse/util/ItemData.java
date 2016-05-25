package eu.mico.platform.zooniverse.util;

import java.util.Collections;
import java.util.Map;

/**
 * @author Horst Stadler (horst.stadler@salzburgresearch.at)
 * @since 17.05.2016
 */

public class ItemData {

    private final Map<String, Object> rawItemData;

    public ItemData(Map<String, Object> rawItemData) {
        this.rawItemData = rawItemData;
    }

    public boolean hasFinished() {
        if (((String)this.rawItemData.get("finished")).trim().equalsIgnoreCase("false"))
            return false;
        return true;
    }

    public boolean hasErrors() {
        if (((String)this.rawItemData.get("hasError")).trim().equalsIgnoreCase("false"))
            return false;
        return true;
    }

    public Map<String, Object> getData() {
        return Collections.unmodifiableMap(this.rawItemData);
    }

}
