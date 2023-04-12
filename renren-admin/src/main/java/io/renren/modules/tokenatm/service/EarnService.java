package io.renren.modules.tokenatm.service;

import io.renren.modules.tokenatm.entity.RequestEntity;
import io.renren.modules.tokenatm.entity.TokenCountEntity;
import io.renren.modules.tokenatm.exceptions.BadRequestException;
import io.renren.modules.tokenatm.service.Beans.AssignmentStatus;
import io.renren.modules.tokenatm.service.Response.*;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface EarnService {

    HashMap<Object, Object> getStudentGrades() throws IOException, JSONException;

    Map<String, Object> getCourseData() throws IOException, JSONException;

    Map<String, Map<String, Double>> getStudentTokenGrades() throws IOException, JSONException;

    Iterable<TokenCountEntity> getAllStudentTokenCounts();

    Optional<TokenCountEntity> getStudentTokenCount(String user_id);

    void syncTokensOnDeadline() throws JSONException, IOException;

    Iterable<TokenCountEntity> manualSyncTokens() throws JSONException, IOException;

    UseTokenResponse request_token_use(String user_id, String assignment_id, Integer cost) throws IOException, BadRequestException, JSONException;

    UseTokenResponse approve_token_use(RequestEntity request) throws IOException, BadRequestException, JSONException;
    UseTokenResponse approve_all_token_use(List<RequestEntity> request) throws IOException, BadRequestException, JSONException;

    CancelTokenResponse cancel_token_use(String user_id, String assignment_id, Integer cost) throws IOException, BadRequestException, JSONException;

    List<AssignmentStatus> getAssignmentStatuses(String user_id) throws JSONException, IOException;

    UpdateTokenResponse updateToken(String user_id, Integer tokenNum) throws JSONException, IOException;

    RequestUserIdResponse getUserIdFromEmail(String email) throws JSONException, IOException;

    RejectTokenResponse reject_token_use(RequestEntity request) throws JSONException, IOException;

    ResponseEntity<?> exportData();
}

