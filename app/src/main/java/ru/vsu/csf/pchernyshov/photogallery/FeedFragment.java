package ru.vsu.csf.pchernyshov.photogallery;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;

import ru.vsu.csf.pchernyshov.photogallery.adapter.PhotosViewAdapter;
import ru.vsu.csf.pchernyshov.photogallery.client.MockPhotosClient;
import ru.vsu.csf.pchernyshov.photogallery.client.PhotosClient;
import ru.vsu.csf.pchernyshov.photogallery.domain.Credentials;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FeedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FeedFragment extends Fragment {
    private static final String CREDENTIALS = "creds";
    private static final int PICK_IMAGE = 8364;
    private static final String PHOTOS_DIRECTORY = Environment.getExternalStorageDirectory() + "/photoGallery";
    private final int imageLoadingPlaceHolder = R.drawable.ic_access_time_black_24dp;
    private PhotosClient photosClient;
    private PhotosViewAdapter photosViewAdapter;
    RecyclerView recyclerView;

    private Credentials credentials;

    public FeedFragment() {
        // Required empty public constructor
    }

    public static FeedFragment newInstance(Credentials credentials) {
        FeedFragment fragment = new FeedFragment();
        Bundle args = new Bundle();
        args.putSerializable(CREDENTIALS, credentials);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Serializable creds = getArguments().getSerializable(CREDENTIALS);
            if (creds != null) {
                credentials = (Credentials) creds;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        photosClient = new MockPhotosClient(this.getContext(), PHOTOS_DIRECTORY);

        recyclerView = getView().findViewById(R.id.photosView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this.getContext(), 1);
        recyclerView.setLayoutManager(layoutManager);
        photosViewAdapter = new PhotosViewAdapter(this.getActivity(), photosClient, credentials, imageLoadingPlaceHolder);
        recyclerView.setAdapter(photosViewAdapter);

        FloatingActionButton fab = getView().findViewById(R.id.upload_photo_button);
        if (credentials == null) {
            fab.hide();
        }
        fab.setOnClickListener(
                v -> {
                    Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    getIntent.setType("image/*");

                    Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickIntent.setType("image/*");

                    Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

                    startActivityForResult(chooserIntent, PICK_IMAGE);
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE && data != null) {
            recyclerView.smoothScrollToPosition(0);
            photosClient.uploadPhoto(data.getData(), "Description providing is to be implemented.", photoMetadata -> photosViewAdapter.notifyPhotoUploaded(photoMetadata));
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
