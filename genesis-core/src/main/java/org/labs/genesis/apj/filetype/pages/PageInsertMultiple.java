package org.labs.genesis.apj.filetype.pages;

import org.labs.genesis.apj.filetype.ApjFile;
import java.util.HashMap;

public class PageInsertMultiple extends ApjFile {

    @Override
    public HashMap<String, Object> getPrimaryHashMap() {
        return new HashMap<>();
    }

    @Override
    public HashMap<String, Object> buildMetadata() {
        return new HashMap<>();
    }

}
