package ru.vsu.csf.pchernyshov.photogallery.client;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.util.Consumer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import ru.vsu.csf.pchernyshov.photogallery.R;
import ru.vsu.csf.pchernyshov.photogallery.domain.Credentials;
import ru.vsu.csf.pchernyshov.photogallery.domain.Page;
import ru.vsu.csf.pchernyshov.photogallery.domain.PhotoMetadata;
import ru.vsu.csf.pchernyshov.photogallery.service.SaveSharedPreference;

public class MockPhotosClient implements PhotosClient {
    private final Context context;
    private final List<PhotoMetadata> mockPhotoMetadata;
    private final Timer timer = new Timer();
    private final String dirToUploadPhoto;

    public MockPhotosClient(Context context, String dirToUploadPhoto) {
        this.context = context;
        this.dirToUploadPhoto = dirToUploadPhoto;
        List<PhotoMetadata> savedMockPhotoMetadata = SaveSharedPreference.getMockPhotosMetadata(context.getApplicationContext());
        if (savedMockPhotoMetadata == null) {
            mockPhotoMetadata = generateMockPhotosMetadata();
            SaveSharedPreference.saveMockPhotosMetadata(context.getApplicationContext(), mockPhotoMetadata);
        } else {
            mockPhotoMetadata = savedMockPhotoMetadata;
        }

    }

    @Override
    public Page<PhotoMetadata> getPhotosPage(int pageSize, int page, String startingFromPhoto, Credentials credentials) {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<PhotoMetadata> subList;
        List<String> ids = mockPhotoMetadata.stream().map(PhotoMetadata::getId).collect(Collectors.toList());
        if (startingFromPhoto != null && ids.contains(startingFromPhoto)) {
            subList = mockPhotoMetadata.subList(ids.indexOf(startingFromPhoto), mockPhotoMetadata.size());
        } else {
            subList = mockPhotoMetadata;
        }

        int fromIndex = page * pageSize;
        int toIndex = fromIndex + pageSize;
        if (toIndex > subList.size()) {
            toIndex = subList.size();
        }
        return new Page<>(
                copyMockMetadata(subList.subList(fromIndex, toIndex)),
                pageSize,
                (int) Math.ceil(((double) subList.size()) / pageSize)
        );
    }

    @Override
    public void downloadPhotoAsync(String url, Credentials credentials, Consumer<String> onDownloaded) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    String filename = dirToUploadPhoto + "/" + url + ".jpg";
                    File imageFile = new File(filename);
                    if (!imageFile.exists()) {
                        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.class.getField("img" + url).getInt(0));
                        saveBitmapToFile(imageFile, bm, Bitmap.CompressFormat.JPEG, 100);
                    }
                    onDownloaded.accept(filename);
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }
        }, 0);
    }

    @Override
    public void setPhotoLiked(String id, Credentials credentials) {
        mockPhotoMetadata.stream().filter(p -> p.getId().equals(id)).findFirst().ifPresent(p -> p.getLikers().add(credentials.getUsername()));
        SaveSharedPreference.saveMockPhotosMetadata(context.getApplicationContext(), mockPhotoMetadata);
    }

    @Override
    public void setPhotoUnliked(String id, Credentials credentials) {
        mockPhotoMetadata.stream().filter(p -> p.getId().equals(id)).findFirst().ifPresent(p -> p.getLikers().remove(credentials.getUsername()));
        SaveSharedPreference.saveMockPhotosMetadata(context.getApplicationContext(), mockPhotoMetadata);
    }

    @Override
    public void uploadPhoto(Uri photoPath, String description, Consumer<PhotoMetadata> onUploaded) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try (InputStream input = context.getContentResolver().openInputStream(photoPath)) {
                    String id = nextId();
                    File file = new File(dirToUploadPhoto, id + ".jpg");
                    try (OutputStream output = new FileOutputStream(file)) {
                        byte[] buffer = new byte[4 * 1024]; // or other buffer size
                        int read;

                        while ((read = input.read(buffer)) != -1) {
                            output.write(buffer, 0, read);
                        }
                        output.flush();
                        PhotoMetadata photoMetadata = new PhotoMetadata(id, id, description, new ArrayList<>());
                        synchronized (mockPhotoMetadata) {
                            mockPhotoMetadata.add(0, photoMetadata);
                        }
                        SaveSharedPreference.saveMockPhotosMetadata(context.getApplicationContext(), mockPhotoMetadata);
                        onUploaded.accept(photoMetadata);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0);
    }

    private String nextId() {
        synchronized (mockPhotoMetadata) {
            return "" + mockPhotoMetadata.stream().map(PhotoMetadata::getId).map(Integer::parseInt).max(Integer::compareTo).map(i -> i + 1).orElse(0);
        }
    }

    private List<PhotoMetadata> copyMockMetadata(List<PhotoMetadata> photos) {
        return photos.stream().map(photo -> new PhotoMetadata(photo.getId(), photo.getUrl(), photo.getDescription(), new ArrayList<>(photo.getLikers()))).collect(Collectors.toList());
    }

    private List<PhotoMetadata> generateMockPhotosMetadata() {
        File directory = new File(dirToUploadPhoto);
        if (!directory.exists()) {
            directory.mkdir();
        }
        List<PhotoMetadata> mockPhotos = new ArrayList<>(35);
        Random random = new Random();
        for (int i = 34; i >= 0; i--) {
            List<String> likers = new ArrayList<>();
            for (int j = 0; j < random.nextInt(10); j++) {
                likers.add("user" + j);
            }
            mockPhotos.add(new PhotoMetadata(i + "", i + "", "Photo description " + Integer.toHexString(random.nextInt(10000)), likers));
        }
        return mockPhotos;
    }

    private boolean saveBitmapToFile(File imageFile, Bitmap bm, Bitmap.CompressFormat format, int quality) {
        if (imageFile.exists()) {
            return true;
        }
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bm.compress(format, quality, fos);
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
