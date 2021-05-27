package ru.vsu.csf.pchernyshov.photogallery.adapter;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PhotosHash {
    private final ConcurrentSkipListMap<String, PhotoEntry> hash = new ConcurrentSkipListMap<>();
    private final ExecutorService executorService;
    private final Map<String, Consumer<Bitmap>> handlers = new ConcurrentHashMap<>();
    private final int maxSize;

    public PhotosHash(ExecutorService executorService, int maxSize) {
        this.executorService = executorService;
        this.maxSize = maxSize;
    }

    public void notifyWhenCalculated(String id, Consumer<Bitmap> handler) {
        PhotoEntry entry = hash.get(id);
        if (entry != null) {
            handler.accept(entry.bitmap);
        } else {
            handlers.put(id, handler);
        }
    }

    public void calculate(String id, Supplier<Bitmap> supplier) {
        executorService.execute(() -> {
            PhotoEntry entry = hash.computeIfAbsent(id, key -> {
                if (hash.size() >= maxSize) {
                    hash.pollFirstEntry();
                }
                return new PhotoEntry(supplier.get(), Instant.now());
            });
            handlers.computeIfPresent(id, (k, v) -> {
                v.accept(entry.bitmap);
                return null;
            });
        });
    }

    public boolean contains(String id) {
        return hash.containsKey(id);
    }

    private static class PhotoEntry implements Comparable<PhotoEntry> {
        private final Bitmap bitmap;
        private final Instant createdAt;

        public PhotoEntry(Bitmap bitmap, Instant createdAt) {
            this.bitmap = bitmap;
            this.createdAt = createdAt;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        @Override
        public int compareTo(@NonNull PhotoEntry photoEntry) {
            return createdAt.compareTo(photoEntry.createdAt);
        }
    }
}
