package top.fifthlight.mergetools.merger.api;

import java.io.OutputStream;

public interface MergeEntry {
    void write(OutputStream output) throws Exception;
}

