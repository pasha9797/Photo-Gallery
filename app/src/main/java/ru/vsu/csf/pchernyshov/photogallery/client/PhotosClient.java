package ru.vsu.csf.pchernyshov.photogallery.client;

import android.net.Uri;
import android.support.v4.util.Consumer;

import ru.vsu.csf.pchernyshov.photogallery.domain.Credentials;
import ru.vsu.csf.pchernyshov.photogallery.domain.Page;
import ru.vsu.csf.pchernyshov.photogallery.domain.PhotoMetadata;

public interface PhotosClient {

    Page<PhotoMetadata> getPhotosPage(int pageSize, int page, String startingFromPhoto, Credentials credentials);

    void downloadPhotoAsync(String url, Credentials credentials, Consumer<String> onDownloaded);

    void setPhotoLiked(String id, Credentials credentials);

    void setPhotoUnliked(String id, Credentials credentials);

    void uploadPhoto(Uri photoPath, String description, Consumer<PhotoMetadata> onUploaded);
}
