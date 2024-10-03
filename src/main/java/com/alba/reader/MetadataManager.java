package com.alba.reader;
import org.json.*;
import java.io.*;
import java.util.*;

public class MetadataManager {
    InputStream inputStream  = null;
    public MetadataManager(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public JSONObject getMetadata() {
        JSONTokener tokener = new JSONTokener(inputStream);
        return new JSONObject(tokener);
    }

    public JSONObject XMLtoMetadata() {
        Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
        String xml = scanner.hasNext() ? scanner.next() : "";
        return XML.toJSONObject(xml);
    }
}
