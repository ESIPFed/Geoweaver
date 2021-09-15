package com.gw.utils;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONObject;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.StringUtils;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

//import com.google.auth.Credentials;

public class GmailAPI {


    /*
    Sreten Cvetojevic 3/1/2021
    Created based on tutorials described in:
    1) https://www.youtube.com/watch?v=tGDn3V-mIOM
    2) https://www.youtube.com/watch?v=IZ1ZEjuJF8U
    1.Get code :
    https://accounts.google.com/o/oauth2/v2/auth?
     scope=https://mail.google.com&
     access_type=offline&
     redirect_uri=http://localhost&
     response_type=code&
     client_id=[Client ID]
    2. Get access_token and refresh_token
     curl \
    --request POST \
    --data "code=[Authentcation code from authorization link]&client_id=[Application Client Id]&client_secret=[Application Client Secret]&redirect_uri=http://localhost&grant_type=authorization_code" \
    https://accounts.google.com/o/oauth2/token
    3.Get new access_token using refresh_token
    curl \
    --request POST \
    --data "client_id=[your_client_id]&client_secret=[your_client_secret]&refresh_token=[refresh_token]&grant_type=refresh_token" \
    https://accounts.google.com/o/oauth2/token
    */
    private static final String APPLICATION_NAME = "Geoweaver";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String user = "me";
    static Gmail service = null;

    private ClassLoader classLoader = getClass().getClassLoader();


    public static void main(String[] args) throws IOException, GeneralSecurityException {

        System.err.print(System.getProperty("user.dir"));

        getGmailService();

        getMailBody("Google");

    }

    public static void getMailBody(String searchString) throws IOException {

        // Access Gmail inbox

        Gmail.Users.Messages.List request = service.users().messages().list(user).setQ(searchString);

        ListMessagesResponse messagesResponse = request.execute();
        request.setPageToken(messagesResponse.getNextPageToken());

        // Get ID of the email you are looking for
        String messageId = messagesResponse.getMessages().get(0).getId();

        Message message = service.users().messages().get(user, messageId).execute();

        // Print email body

        String emailBody = StringUtils
                .newStringUtf8(Base64.decodeBase64(message.getPayload().getParts().get(0).getBody().getData()));

        // System.out.println("Email body : " + emailBody);

    }

    public static Gmail getGmailService() throws IOException, GeneralSecurityException {


        String credentials = "{\"web\":{\"client_id\":\"471690502756-7inelnnpit9mp4dofk5sda2op0105d9u.apps.googleusercontent.com\",\"project_id\":\"watersmart-1614386745314\",\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"token_uri\":\"https://oauth2.googleapis.com/token\",\"auth_provider_x509_cert_url\":\"https://www.googleapis.com/oauth2/v1/certs\",\"client_secret\":\"fmqHZjd4H9VOeOymUfwY-O6V\",\"redirect_uris\":[\"http://localhost:8070/Geoweaver\"]}}";
        InputStream in = new ByteArrayInputStream(credentials.getBytes());
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

//        System.err.print(clientSecrets);
        // Credential builder
        
        Credential authorize = new GoogleCredential.Builder().setTransport(GoogleNetHttpTransport.newTrustedTransport())
                .setJsonFactory(JSON_FACTORY)
                .setClientSecrets(clientSecrets.getDetails().getClientId().toString(),
                        clientSecrets.getDetails().getClientSecret().toString())
                .build().setAccessToken(getAccessToken()).setRefreshToken(
                        "1//04xhq952D9aCfCgYIARAAGAQSNwF-L9IrHfxJgvKwZDiIHKSkIORtB3uxMmnKTQRUMoJhhYzQkukS731bRDtMEcWfFJJISYWVUNI");//Replace this

//        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream("/path/to/credentials.json"));
//        credentials.refreshIfExpired();
//        AccessToken token = credentials.getAccessToken();
//// OR
//        AccessToken token = credentials.refreshAccessToken();

        // Create Gmail service
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, authorize)
                .setApplicationName(GmailAPI.APPLICATION_NAME).build();

        return service;
    }

    private static String getAccessToken() {

        String accessToken = "";
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("grant_type", "refresh_token");
            params.put("client_id", "471690502756-7inelnnpit9mp4dofk5sda2op0105d9u.apps.googleusercontent.com"); //Replace this
            params.put("client_secret", "fmqHZjd4H9VOeOymUfwY-O6V");
            params.put("refresh_token",
                    "1//04xhq952D9aCfCgYIARAAGAQSNwF-L9IrHfxJgvKwZDiIHKSkIORtB3uxMmnKTQRUMoJhhYzQkukS731bRDtMEcWfFJJISYWVUNI"); //Replace this

            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (postData.length() != 0) {
                    postData.append('&');
                }
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

//            URL url = new URL("https://accounts.google.com/o/oauth2/v2/auth");
//            URL url = new URL("https://www.googleapis.com/oauth2/v4/token");
            URL url = new URL("https://oauth2.googleapis.com/token");

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.setRequestMethod("POST");
            con.getOutputStream().write(postDataBytes);

            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuffer buffer = new StringBuffer();
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                buffer.append(line);
            }

            // System.out.print(buffer.toString()); //this buffer is a html, not a json
             JSONObject json = new JSONObject(buffer.toString().trim());

             accessToken = json.getString("access_token");
             System.err.print("accessToken\n");
             System.err.print(accessToken);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return accessToken;
    }

}