package ru.vsu.csf.pchernyshov.photogallery.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import ru.vsu.csf.pchernyshov.photogallery.domain.Credentials;
import ru.vsu.csf.pchernyshov.photogallery.domain.PhotoMetadata;

public class SaveSharedPreference {
    static final String PREF_USERNAME = "username";
    static final String PREF_PASSWORD = "password";
    static final String MOCK_PHOTOS_METADARA = "mock_photos_metadata";

    static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void setCredentials(Context ctx, Credentials credentials) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USERNAME, credentials.getUsername());
        editor.putString(PREF_PASSWORD, credentials.getPassword());
        editor.apply();
    }

    public static Credentials getCredentials(Context ctx) {
        SharedPreferences pref = getSharedPreferences(ctx);
        String username = pref.getString(PREF_USERNAME, null);
        String password = pref.getString(PREF_PASSWORD, null);
        if (username == null || password == null) {
            return null;
        }
        return new Credentials(username, password);
    }

    public static void removeCredentials(Context ctx) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.remove(PREF_USERNAME);
        editor.remove(PREF_PASSWORD);
        editor.apply();
    }

    public static void saveMockPhotosMetadata(Context ctx, List<PhotoMetadata> photoMetadata) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        Gson gson = new Gson();
        String json = gson.toJson(photoMetadata);
        editor.putString(MOCK_PHOTOS_METADARA, json);
        editor.apply();
    }

    public static List<PhotoMetadata> getMockPhotosMetadata(Context ctx) {
        SharedPreferences pref = getSharedPreferences(ctx);
        Gson gson = new Gson();
        String json = pref.getString(MOCK_PHOTOS_METADARA, null);
        if (json == null) {
            return null;
        }
        Type type = new TypeToken<ArrayList<PhotoMetadata>>(){}.getType();
        return gson.fromJson(json, type);
    }
}