package org.example;

import com.docusign.esign.api.EnvelopesApi;
import com.docusign.esign.client.ApiClient;
import com.docusign.esign.client.auth.OAuth.OAuthToken;
import com.docusign.esign.client.auth.OAuth.UserInfo;
import com.docusign.esign.client.ApiException;
import com.docusign.esign.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;

public class DSEnvelope {

    public String createEnvelope(String signerEmail, String signerName) throws IOException, ApiException {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        // Setup OAuth token
        // Get access token and accountId
        ApiClient apiClient = new ApiClient("https://demo.docusign.net/restapi");
        apiClient.setOAuthBasePath("account-d.docusign.com");
        ArrayList<String> scopes = new ArrayList<String>();
        scopes.add("signature");
        scopes.add("impersonation");
        byte[] privateKeyBytes = Files.readAllBytes(Paths.get(""));
        OAuthToken oAuthToken = apiClient.requestJWTUserToken(
                "integrationKey",
                "impersonatedUserGuid",
                scopes,
                privateKeyBytes,
                3600);
        String accessToken = oAuthToken.getAccessToken();
        UserInfo userInfo = apiClient.getUserInfo(accessToken);
        String accountId = userInfo.getAccounts().get(0).getAccountId();
        apiClient.addDefaultHeader("Authorization", "Bearer "+ accessToken);

        EnvelopesApi envelopesApi = new EnvelopesApi(apiClient);

        // Create envelopeDefinition object
        EnvelopeDefinition envelope = new EnvelopeDefinition();
        envelope.setEmailSubject("Please sign this document set");

        // Create tabs object
        SignHere signHere = new SignHere();
        signHere.setDocumentId("1");
        signHere.setPageNumber("1");
        signHere.setXPosition("191");
        signHere.setYPosition("148");
        Tabs tabs = new Tabs();
        tabs.setSignHereTabs(Arrays.asList(signHere));
        // Set recipients
        Signer signer = new Signer();
        signer.setEmail(signerEmail);
        signer.setName(signerName + " executed: " + timestamp.toString());
        signer.recipientId("1");
        signer.setTabs(tabs);

        Recipients recipients = new Recipients();
        recipients.setSigners(Arrays.asList(signer));
        envelope.setRecipients(recipients);

        // Add document
        Document document = new Document();
        document.setDocumentBase64("VGhhbmtzIGZvciByZXZpZXdpbmcgdGhpcyEKCldlJ2xsIG1vdmUgZm9yd2FyZCBhcyBzb29uIGFzIHdlIGhlYXIgYmFjay4=");
        document.setName("doc1.txt");
        document.setFileExtension("txt");
        document.setDocumentId("1");
        envelope.setDocuments(Arrays.asList(document));

        envelope.setStatus("sent");

        EnvelopeSummary results = envelopesApi.createEnvelope(accountId, envelope);
        return results.getEnvelopeId();

    }

}