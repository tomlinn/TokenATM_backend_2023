package io.renren.modules.tokenatm.controller;

import io.renren.modules.tokenatm.entity.TokenCountEntity;
import io.renren.modules.tokenatm.exceptions.BadRequestException;
import io.renren.modules.tokenatm.exceptions.InternalServerException;
import io.renren.modules.tokenatm.service.Beans.AssignmentStatus;
import io.renren.modules.tokenatm.service.EarnService;
import io.renren.modules.tokenatm.service.Request.RequestUserIdBody;
import io.renren.modules.tokenatm.service.Request.UseTokenBody;
import io.renren.modules.tokenatm.service.Response.CancelTokenResponse;
import io.renren.modules.tokenatm.service.Response.RequestUserIdResponse;
import io.renren.modules.tokenatm.service.Response.UpdateTokenResponse;
import io.renren.modules.tokenatm.service.Response.UseTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.json.JSONException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class EarnController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EarnService.class);

    @Autowired
    EarnService earnService;

    @GetMapping(path="/assignment_status/{user_id}")
    public @ResponseBody List<AssignmentStatus> getAssignmentStatuses(@PathVariable String user_id) throws JSONException, IOException {
        return earnService.getAssignmentStatuses(user_id);
    }

    @GetMapping(path="/sync")
    public @ResponseBody Iterable<TokenCountEntity> manualSync() throws JSONException, IOException {
        return earnService.manualSyncTokens();
    }


    @GetMapping(path="/tokens/{user_id}")
    public @ResponseBody Integer getTokenForStudent(@PathVariable String user_id) throws InternalServerException {
        Optional<TokenCountEntity> tokenCountEntityOptional = earnService.getStudentTokenCount(user_id);
        if (!tokenCountEntityOptional.isPresent()) {
            throw new InternalServerException("Token count not found for user ID: " + user_id);
        }
        try {
            return tokenCountEntityOptional.get().getToken_count();
        } catch (NumberFormatException e) {
            throw new InternalServerException("Token count is not an integer for user ID: " + user_id);
        }
    }

    @GetMapping("/grades")
    public HashMap<Object, Object> getStudentsData(
    )  throws InternalServerException {
        try {
            return earnService.getStudentGrades();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            throw new InternalServerException();
        }
    }

    @GetMapping("/students")
    public Iterable<TokenCountEntity> getStudents(
    ) {
        return earnService.getAllStudentTokenCounts();
    }

    @GetMapping("/token_grades")
    public Map<String, Map<String, Double>> getTokenGrades(
    ) throws InternalServerException {
        try {
            return earnService.getStudentTokenGrades();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            throw new InternalServerException();
        }
    }

    @GetMapping("/courses")
    public Map<String, Object> getCourseData(
    ) throws InternalServerException {
        try {
            return earnService.getCourseData();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            throw new InternalServerException();
        }
    }

    /**
     * Body: {
     *     "assignment_id": string,
     *     "cost": int
     * }
     */
    @PostMapping(path="/request")
    public @ResponseBody UseTokenResponse useToken(@RequestBody UseTokenBody body) throws IOException, BadRequestException, JSONException {
        return earnService.request_token_use(body.getStudentId(), body.getAssignmentId(), body.getAssignmentName(), body.getTokenCount());
    }

    @PostMapping(path="/cancel")
    public @ResponseBody CancelTokenResponse cancelToken(@RequestBody UseTokenBody body) throws IOException, BadRequestException, JSONException {
        return earnService.cancel_token_use(body.getStudentId(), body.getAssignmentId(), body.getAssignmentName(), body.getTokenCount());
    }

    @PostMapping("/update")
    public @ResponseBody UpdateTokenResponse updateToken(@RequestBody UseTokenBody body) throws JSONException, IOException {
        return earnService.updateToken(body.getStudentId(), body.getTokenCount());
    }


    @PostMapping("/userid")
    public @ResponseBody RequestUserIdResponse getUserId(@RequestBody RequestUserIdBody body) throws JSONException, IOException {
        return earnService.getUserIdFromEmail(body.getEmail());
    }

    @GetMapping("/exportall")
    public ResponseEntity<?> exportData() {
        return earnService.exportData();
    }
}