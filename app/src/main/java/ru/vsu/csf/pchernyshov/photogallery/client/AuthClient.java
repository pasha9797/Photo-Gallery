package ru.vsu.csf.pchernyshov.photogallery.client;

import ru.vsu.csf.pchernyshov.photogallery.domain.Credentials;

public interface AuthClient {

    boolean authenticate(Credentials credentials);
}
