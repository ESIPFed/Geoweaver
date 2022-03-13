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

        SecretVar sv = BeanTool.getBean(SecretVar.class);
        sv.refresh();
        String credentials = sv.credentials;

        InputStream in = new ByteArrayInputStream(credentials.getBytes());
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        
//        System.err.print(clientSecrets);
        // Credential builder
        
        Credential authorize = new GoogleCredential.Builder().setTransport(GoogleNetHttpTransport.newTrustedTransport())
                .setJsonFactory(JSON_FACTORY)
                .setClientSecrets(clientSecrets.getDetails().getClientId().toString(),
                        clientSecrets.getDetails().getClientSecret().toString())
                .build().setAccessToken(getAccessToken()).setRefreshToken(
                    sv.refreshtoken);//Replace this

        // Create Gmail service
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, authorize)
                .setApplicationName(GmailAPI.APPLICATION_NAME).build();

        return service;
    }

    private static String getAccessToken() {

        String accessToken = "";

        try {
            SecretVar sv = BeanTool.getBean(SecretVar.class);
            sv.refresh();
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("grant_type", "refresh_token");
            params.put("client_id", sv.clientid); //Replace this
            params.put("client_secret", sv.clientsecret);
            params.put("refresh_token", sv.refreshtoken); //Replace this

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

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return accessToken;
    }

}