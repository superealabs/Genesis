package org.labs.genesis.apj.filetype;

import java.util.HashMap;

public interface ApjMetadataProvider {
    public HashMap<String,Object> getPrimaryHashMap();
    public HashMap<String,Object> buildMetadata();
}
