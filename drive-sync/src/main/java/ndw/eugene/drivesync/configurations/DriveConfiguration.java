package ndw.eugene.drivesync.configurations;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.UserCredentials;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration
public class DriveConfiguration {
    private final String applicationName;
    private final String credentialsFilePath;

    private final String tokenRefresh;

    public DriveConfiguration(
            @Value("${application.name}")
            String applicationName,
            @Value("${application.token.directory.path}")
            String tokensDirectoryPath,
            @Value("${application.credentials.file.path}")
            String credentialsFilePath,
            @Value("${application.service.email}")
            String serviceEmail,
            @Value("${token.refresh}")
            String tokenRefresh) {
        this.applicationName = applicationName;
        this.credentialsFilePath = credentialsFilePath;
        this.tokenRefresh = tokenRefresh;
    }

    @Bean
    public Drive getDrive(JsonFactory jsonFactory,
                          NetHttpTransport httpTransport,
                          HttpCredentialsAdapter httpCredentialsAdapter) {
        return new Drive.Builder(httpTransport, jsonFactory, httpCredentialsAdapter)
                .setApplicationName(applicationName)
                .build();
    }

    @Bean
    public JsonFactory getJsonFactory() {
        return GsonFactory.getDefaultInstance();
    }

    @Bean
    public NetHttpTransport getHttpTransport() throws GeneralSecurityException, IOException {
        return GoogleNetHttpTransport.newTrustedTransport();
    }

    @Bean
    public Tika getTika() {
        return new Tika();
    }

    @Bean
    public HttpCredentialsAdapter getHttpCredentialsAdapter() throws IOException, GeneralSecurityException {
        InputStream in = DriveConfiguration.class.getResourceAsStream(credentialsFilePath);
        if (in == null) {
            throw new FileNotFoundException("file not found");
        }

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(getJsonFactory(), new InputStreamReader(in));
        String clientId = clientSecrets.getDetails().getClientId();
        String clientSecret = clientSecrets.getDetails().getClientSecret();

        UserCredentials credentials = UserCredentials.newBuilder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setAccessToken(new AccessToken(getNewToken(tokenRefresh, clientId, clientSecret), null))
                .setRefreshToken(tokenRefresh)
                .build();

        return new HttpCredentialsAdapter(credentials);
    }

    private String getNewToken(String refreshToken, String clientId, String clientSecret) throws IOException, GeneralSecurityException {
        ArrayList<String> scopes = new ArrayList<>();
        scopes.add(DriveScopes.DRIVE);
        TokenResponse tokenResponse = new GoogleRefreshTokenRequest(getHttpTransport(), getJsonFactory(),
                refreshToken, clientId, clientSecret).setScopes(scopes).setGrantType("refresh_token").execute();

        return tokenResponse.getAccessToken();
    }
}
