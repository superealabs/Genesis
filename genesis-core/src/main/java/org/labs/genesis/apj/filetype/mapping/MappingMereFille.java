package org.labs.genesis.apj.filetype.mapping;

import org.labs.genesis.apj.filetype.ApjFile;
import java.util.HashMap;

public class MappingMereFille extends ApjFile {
    @Override
    public HashMap<String, Object> getPrimaryHashMap() {
        return new HashMap<>();
    }

    @Override
    public HashMap<String, Object> buildMetadata() {
        return new HashMap<>();
    }

}
