package ru.vsu.csf.pchernyshov.photogallery.domain;

import java.util.List;

public class PhotoMetadata {
    private final String id;
    private final String url;
    private final String description;
    private final List<String> likers;

    public PhotoMetadata(String id, String url, String description, List<String> likers) {
        this.id = id;
        this.url = url;
        this.description = description;
        this.likers = likers;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getLikers() {
        return likers;
    }
}
