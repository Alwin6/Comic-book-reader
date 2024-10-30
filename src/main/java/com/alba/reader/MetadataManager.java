package com.alba.reader;
import org.json.*;
import java.io.*;
import java.util.*;

public class MetadataManager {
    InputStream inputStream;
    public MetadataManager(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * @return a JSONObject retrieved from the input stream, assuming the input stream is from a json file
     */
    public JSONObject getMetadata() {
        JSONTokener jsonTokener = new JSONTokener(inputStream);
        return new JSONObject(jsonTokener);
    }

    /**
     * @return a JSONObject retrieved from the input stream, assuming the input stream is an xml file
     */
    public JSONObject XMLtoMetadata() {
        Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
        String xml = scanner.hasNext() ? scanner.next() : "";
        JSONObject metadata = XML.toJSONObject(xml);
        for (Iterator<String> it = metadata.keys(); it.hasNext(); ) {
            String key = it.next();

            try {
                return metadata.getJSONObject(key);
            } catch (Exception ignored) {

            }
        }
        return metadata;
    }
}
