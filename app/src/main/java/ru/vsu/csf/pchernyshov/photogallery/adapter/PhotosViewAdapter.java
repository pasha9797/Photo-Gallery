package ru.vsu.csf.pchernyshov.photogallery.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.vsu.csf.pchernyshov.photogallery.R;
import ru.vsu.csf.pchernyshov.photogallery.client.PhotosClient;
import ru.vsu.csf.pchernyshov.photogallery.domain.Credentials;
import ru.vsu.csf.pchernyshov.photogallery.domain.Page;
import ru.vsu.csf.pchernyshov.photogallery.domain.PhotoMetadata;

public class PhotosViewAdapter extends RecyclerView.Adapter<PhotosViewAdapter.PhotoViewHolder> {
    private final int REQUEST_PAGING_SIZE = 10;
    private final int MAX_BITMAP_CACHE_SIZE = 50;
    private final int DOWNLOAD_SURROUNDING_PHOTOS_RADIUS = 2;
    private final int imageLoadingPlaceHolder;
    private final Credentials credentials;
    private final PhotosClient photosClient;
    private final Activity mainActivity;
    private final List<PhotoMetadata> loadedPhotoMetadata = new ArrayList<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private PhotosHash photosHash = new PhotosHash(executorService, MAX_BITMAP_CACHE_SIZE);
    private int pagesDownloaded = 0;
    private int totalPages = 1;
    private String startingFromImage = null;


    public PhotosViewAdapter(Activity mainActivity, PhotosClient photosClient, Credentials credentials, int imageLoadingPlaceHolder) {
        this.mainActivity = mainActivity;
        this.credentials = credentials;
        this.photosClient = photosClient;
        this.imageLoadingPlaceHolder = imageLoadingPlaceHolder;
        downloadMorePhotosMetadata();
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.photo_view_layout, viewGroup, false);
        return new PhotoViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final PhotoViewHolder viewHolder, int i) {
        viewHolder.photoMetadata = loadedPhotoMetadata.get(i);
        if (credentials != null) {
            viewHolder.like.setImageResource(loadedPhotoMetadata.get(i).getLikers().contains(credentials.getUsername()) ? R.drawable.ic_favorite_red_24dp : R.drawable.ic_favorite_border_red_24dp);
        } else {
            viewHolder.like.setImageResource(R.drawable.ic_favorite_gray_24dp);
        }
        viewHolder.title.setText(loadedPhotoMetadata.get(i).getDescription());
        viewHolder.img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        viewHolder.img.setImageResource(imageLoadingPlaceHolder);
        viewHolder.likedCount.setText(loadedPhotoMetadata.get(i).getLikers().size() + "");

        photosHash.notifyWhenCalculated(loadedPhotoMetadata.get(i).getId(), bitmap -> mainActivity.runOnUiThread(() -> viewHolder.img.setImageBitmap(bitmap)));
        downloadNearestPhotos(i);

        if (i >= loadedPhotoMetadata.size() - 2 && pagesDownloaded <= i / REQUEST_PAGING_SIZE + 1) {
            // we are close to the end of photos metadata list, check if more can be loaded.
            executorService.execute(() -> {
                downloadMorePhotosMetadata();
                mainActivity.runOnUiThread(this::notifyDataSetChanged);
            });
        }
    }

    @Override
    public int getItemCount() {
        return loadedPhotoMetadata.size();
    }

    public void notifyPhotoUploaded(PhotoMetadata newPhotoMetadata) {
        synchronized (loadedPhotoMetadata){
            loadedPhotoMetadata.add(0, newPhotoMetadata);
        }
        mainActivity.runOnUiThread(this::notifyDataSetChanged);
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder {
        private PhotoMetadata photoMetadata = null;
        private TextView title;
        private ImageView img;
        private ImageView like;
        private TextView likedCount;

        @SuppressLint("SetTextI18n")
        public PhotoViewHolder(View view) {
            super(view);

            title = view.findViewById(R.id.description);
            img = view.findViewById(R.id.photo);
            like = view.findViewById(R.id.like);
            if (credentials != null) {
                like.setOnClickListener(view1 -> {
                    if (photoMetadata != null) {
                        if (photoMetadata.getLikers().contains(credentials.getUsername())) {
                            photosClient.setPhotoUnliked(photoMetadata.getId(), credentials);
                            photoMetadata.getLikers().remove(credentials.getUsername());
                            like.setImageResource(R.drawable.ic_favorite_border_red_24dp);
                        } else {
                            photosClient.setPhotoLiked(photoMetadata.getId(), credentials);
                            photoMetadata.getLikers().add(credentials.getUsername());
                            like.setImageResource(R.drawable.ic_favorite_red_24dp);
                        }
                        likedCount.setText(photoMetadata.getLikers().size() + "");
                    }
                });
            }
            likedCount = view.findViewById(R.id.likeCount);
        }
    }

    private void downloadMorePhotosMetadata() {
        synchronized (loadedPhotoMetadata) {
            if (pagesDownloaded < totalPages) {
                Page<PhotoMetadata> page = photosClient.getPhotosPage(REQUEST_PAGING_SIZE, pagesDownloaded, startingFromImage, credentials);
                totalPages = page.getTotalPages();
                pagesDownloaded++;
                loadedPhotoMetadata.addAll(page.getData());
                if (startingFromImage == null && loadedPhotoMetadata.size() > 0) {
                    startingFromImage = loadedPhotoMetadata.get(0).getId();
                }
            }
        }
    }

    private void downloadNearestPhotos(int i) {
        getSurroundingIds(i, DOWNLOAD_SURROUNDING_PHOTOS_RADIUS).forEach(ind ->
                photosClient.downloadPhotoAsync(
                        loadedPhotoMetadata.get(ind).getUrl(),
                        credentials,
                        path -> photosHash.calculate(loadedPhotoMetadata.get(ind).getId(), () -> BitmapFactory.decodeFile(path))
                )
        );
    }

    private List<Integer> getSurroundingIds(int i, int radius) {
        int start = i - radius;
        if (start < 0) {
            start = 0;
        }
        int end = i + radius;
        if (end >= loadedPhotoMetadata.size()) {
            end = loadedPhotoMetadata.size() - 1;
        }
        List<Integer> result = new ArrayList<>();
        for (int ind = start; ind <= end; ind++) {
            if (!photosHash.contains(loadedPhotoMetadata.get(ind).getId()))
                result.add(ind);
        }
        return result;
    }
}
