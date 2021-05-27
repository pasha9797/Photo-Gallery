package ru.vsu.csf.pchernyshov.photogallery.client;

import ru.vsu.csf.pchernyshov.photogallery.domain.Credentials;

public class MockAuthClient implements AuthClient {
    private final Credentials correctCreds;

    public MockAuthClient(Credentials correctCreds) {
        this.correctCreds = correctCreds;
    }

    @Override
    public boolean authenticate(Credentials credentials) {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return correctCreds.equals(credentials);
    }
}
