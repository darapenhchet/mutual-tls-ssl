package nl.altindag.client.service;

import static nl.altindag.client.ClientType.OLD_JDK_HTTP_CLIENT;
import static nl.altindag.client.Constants.HEADER_KEY_CLIENT_TYPE;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.annotations.VisibleForTesting;

import nl.altindag.client.ClientException;
import nl.altindag.client.ClientType;
import nl.altindag.client.SSLContextHelper;
import nl.altindag.client.model.ClientResponse;

@Service
public class OldJdkHttpClientWrapper extends RequestService {

    private static final String HTTP_REQUEST = "http:";
    private static final String HTTPS_REQUEST = "https:";

    private final SSLContextHelper sslContextHelper;

    @Autowired
    public OldJdkHttpClientWrapper(SSLContextHelper sslContextHelper) {
        this.sslContextHelper = sslContextHelper;
    }

    @Override
    public ClientResponse executeRequest(String url) throws Exception {
        HttpURLConnection connection;
        if (url.contains(HTTP_REQUEST)) {
            connection = createHttpURLConnection(url);
        } else if (url.contains(HTTPS_REQUEST)) {
            HttpsURLConnection httpsURLConnection = createHttpsURLConnection(url);
            httpsURLConnection.setHostnameVerifier(sslContextHelper.getHostnameVerifier());
            httpsURLConnection.setSSLSocketFactory(sslContextHelper.getSslContext().getSocketFactory());
            connection = httpsURLConnection;
        } else {
            throw new ClientException("Could not create a http client for one of these reasons: "
                                              + "invalid url, "
                                              + "security is enable while using an url with http or "
                                              + "security is disable while using an url with https");
        }

        connection.setRequestMethod("GET");
        connection.setRequestProperty(HEADER_KEY_CLIENT_TYPE, getClientType().getValue());
        String responseBody = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
        return new ClientResponse(responseBody, connection.getResponseCode());
    }

    @VisibleForTesting
    HttpURLConnection createHttpURLConnection(String url) throws IOException {
        return (HttpURLConnection) new URL(url).openConnection();
    }

    @VisibleForTesting
    HttpsURLConnection createHttpsURLConnection(String url) throws IOException {
        return (HttpsURLConnection) new URL(url).openConnection();
    }

    @Override
    public ClientType getClientType() {
        return OLD_JDK_HTTP_CLIENT;
    }

}
