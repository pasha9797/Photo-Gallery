package ru.vsu.csf.pchernyshov.photogallery;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.Serializable;

import ru.vsu.csf.pchernyshov.photogallery.domain.Credentials;
import ru.vsu.csf.pchernyshov.photogallery.service.SaveSharedPreference;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    private static final String CREDENTIALS = "creds";

    private Credentials credentials;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(Credentials credentials) {
        ProfileFragment fragment = new ProfileFragment();
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
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextView username = getView().findViewById(R.id.username_title);
        TextView signInSignOut = getView().findViewById(R.id.sign_in_out_button);
        if (credentials != null) {
            signInSignOut.setText(R.string.sign_out);
            username.setText(credentials.getUsername());
        } else {
            signInSignOut.setText(R.string.sign_in);
            username.setText(R.string.anonymous_user);
        }
        signInSignOut.setOnClickListener(view1 -> {
            Intent i = new Intent(view1.getContext(), LoginActivity.class);
            SaveSharedPreference.removeCredentials(view1.getContext().getApplicationContext());
            startActivity(i);
        });
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
