package ru.vsu.csf.pchernyshov.photogallery.domain;

import java.util.List;

public class Page<T> {
    private final List<T> data;
    private final int currentPage;
    private final int totalPages;

    public Page(List<T> data, int currentPage, int totalPages) {
        this.data = data;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
    }

    public List<T> getData() {
        return data;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
