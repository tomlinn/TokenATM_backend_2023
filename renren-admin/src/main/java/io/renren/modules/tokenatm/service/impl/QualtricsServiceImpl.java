package io.renren.modules.tokenatm.service.impl;

import io.renren.modules.tokenatm.exceptions.BadRequestException;
import io.renren.modules.tokenatm.exceptions.InternalServerException;
import io.renren.modules.tokenatm.service.ConfigRepository;
import io.renren.modules.tokenatm.service.QualtricsService;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

@Service("QualtricsService")
public class QualtricsServiceImpl implements QualtricsService {

    //Qualtrics API Settings
    //TODO: The API Endpoint and API key is only used for testing. Please change to UCI endpoint and actual keys in prod

    private static final String QualtricsBody = "{\"format\":\"json\",\"compress\":\"false\"}";

    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    private static final Logger LOGGER = LoggerFactory.getLogger(QualtricsServiceImpl.class);

    @Autowired
    private ConfigRepository configRepository;

    private class ExportResponse {
        public String getFileId() {
            return fileId;
        }

        public double getPercentComplete() {
            return percentComplete;
        }

        public String getStatus() {
            return status;
        }

        private String fileId;
        private double percentComplete;
        private String status;

        public ExportResponse(String fileId, double percentComplete, String status) {
            this.fileId = fileId;
            this.percentComplete = percentComplete;
            this.status = status;
        }
    }

    private String getQualtricsApiEndpoint() {
        return configRepository.findByType("QUALTRICS_API_ENDPOINT").get(0);
    }

    private String getApiKey() {
        return configRepository.findByType("QUALTRICS_API_KEY").get(0);
    }

    /**
     * Fetch completion status of required surveys
     * See https://api.qualtrics.com/6b00592b9c013-start-response-export for details of API
     *
     * @return
     * @throws IOException
     * @throws JSONException
     * @throws BadRequestException
     */
    @Override
    public Set<String> getSurveyCompletions(String surveyId) throws InternalServerException {
        try {
            URL url = UriComponentsBuilder
                    .fromUriString(getQualtricsApiEndpoint() + "/surveys/" + surveyId + "/export-responses")
                    .build().toUri().toURL();
            String response = apiProcess(url, QualtricsBody);
            JSONObject resultObj = new JSONObject(response).getJSONObject("result");
            String progressId = resultObj.getString("progressId");
            ExportResponse exportResponse = null;
            while (true) {
                exportResponse = getExportStatus(surveyId, progressId);
                LOGGER.info("Current status: " + exportResponse.status + ", Progress: " + exportResponse.getPercentComplete());
                if (exportResponse.getStatus().equals("complete")) {
                    //export success
                    return getSurveyCompletedEmailAddresses(surveyId, exportResponse.getFileId());
                } else if (exportResponse.getStatus().equals("failed")) {
                    //export failed
                    LOGGER.error("Failed to download survey export, progress = " + exportResponse.getPercentComplete() + "%");
                    throw new InternalServerException("Download of survey export failed");
                } else {
                    //still in progress
                    LOGGER.info("Download in progress, current completed: " + exportResponse.getPercentComplete() + "%");
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            throw new InternalServerException("Error when processing survey data");
        }

    }

    /**
     * Fetch the current exporting progress of given progressId, should iterate until percentage is 100.0
     *
     * @param progressId
     * @return
     * @throws IOException
     * @throws JSONException
     */
    private ExportResponse getExportStatus(String surveyId, String progressId) throws IOException, JSONException {
        URL url = UriComponentsBuilder
                .fromUriString(getQualtricsApiEndpoint() + "/surveys/" + surveyId + "/export-responses/" + progressId)
                .build().toUri().toURL();
        String response = apiProcess(url, "");
        //A more elegant way is to use the ObjectMapper, but initializing it is very costly
        JSONObject resultObj = new JSONObject(response).getJSONObject("result");
        return new ExportResponse(
                resultObj.getDouble("percentComplete") == 0
                        ? ""
                        : resultObj.getString("fileId"),
                resultObj.getDouble("percentComplete"),
                resultObj.getString("status"));
    }

    private Set<String> getSurveyCompletedEmailAddresses(String surveyId, String fileId) throws IOException, JSONException {
        LOGGER.info("FileId = " + fileId);
        Set<String> completedEmails = new HashSet<>();
        URL url = UriComponentsBuilder
                .fromUriString(getQualtricsApiEndpoint() + "/surveys/" + surveyId + "/export-responses/" + fileId + "/file")
                .build().toUri().toURL();
        String response = apiProcess(url, "");
        JSONArray responseList = new JSONObject(response).getJSONArray("responses");
        for (int i = 0; i < responseList.length(); i++) {
            JSONObject responseItem = responseList.getJSONObject(i).getJSONObject("values");
            String emailAddress = responseItem.getString("EmailAddress");
            completedEmails.add(emailAddress);
        }
        return completedEmails;
    }


    private String apiProcess(URL url, String body) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json");
        builder.addHeader("X-API-TOKEN", getApiKey());
        if (body.length() > 0) {
            builder.post(RequestBody.create(body, JSON));
        }
        Request request = builder.build();
        System.out.println("Qualtrics API:" + url);
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}
