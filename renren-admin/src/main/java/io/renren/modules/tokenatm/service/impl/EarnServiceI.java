package io.renren.modules.tokenatm.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.opencsv.CSVWriter;
import io.renren.modules.tokenatm.entity.RequestEntity;
import io.renren.modules.tokenatm.entity.SpendLogEntity;
import io.renren.modules.tokenatm.entity.TokenCountEntity;
import io.renren.modules.tokenatm.exceptions.BadRequestException;
import io.renren.modules.tokenatm.exceptions.InternalServerException;
import io.renren.modules.tokenatm.service.Beans.Assignment;
import io.renren.modules.tokenatm.service.Beans.AssignmentStatus;
import io.renren.modules.tokenatm.service.Beans.Student;
import io.renren.modules.tokenatm.service.*;
import io.renren.modules.tokenatm.service.Response.*;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

@Service("EarnService")
public class EarnServiceI implements EarnService {

    @Autowired
    EmailService emailService;

    //Canvas API settings
    //TODO: The API Endpoint and Bearer token is only used for testing. Please change to UCI endpoint and actual tokens in prod


    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    public static final int PER_PAGE = 100;

    private static final Logger LOGGER = LoggerFactory.getLogger(EarnService.class);


    //List of surveys

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private LogRepository logRepository;


    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private ConfigRepository configRepository;
    @Autowired
    private QualtricsService qualtricsService;

    //Token earning deadlines
    private static final List<Date> survey_deadlines = new ArrayList<>();
    private static Date module_deadline;

    static {
        //Set deadlines for surveys
        List<int[]> deadline_time_list = Arrays.asList(
                new int[]{2022, 10, 14, 23, 45}
        );
        for (int[] deadline : deadline_time_list) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(deadline[0], deadline[1], deadline[2], deadline[3], deadline[4]);
            survey_deadlines.add(calendar.getTime());
        }

        //Set deadline for Module 1
        Calendar module_cal = Calendar.getInstance();
        module_cal.set(2022, 9, 26);
        module_deadline = module_cal.getTime();
    }

    /**
     * Fetch grades of all quizzes that is required to earn tokens
     *
     * @return Map of grades, key is student id, value is grade of that student for this assignment
     * @throws IOException
     * @throws JSONException
     */
    @Override
    public Map<String, Map<String, Double>> getStudentTokenGrades() throws IOException, JSONException {
        Map<String, Map<String, Double>> studentGroupScores = new HashMap<>();
        List<String> tokenQuizzes = getTokenQuizIds();
        for (String quiz : tokenQuizzes) {
            String quizId = quiz.split("/")[0];
            String quizGroup = quiz.split("/")[1];
            Map<String, Double> quizScores = getStudentQuizScores(quizId);
            quizScores.entrySet().forEach(e -> {
                String userId = e.getKey();
                Double score = e.getValue();
                Map<String, Double> groupScores = studentGroupScores.getOrDefault(userId, new HashMap<>());
                groupScores.put(quizGroup, groupScores.getOrDefault(quizGroup, 0.0) + score);
                groupScores.put(quizGroup + "_count", groupScores.getOrDefault(quizGroup + "_count" , 0.0) + 1);
                studentGroupScores.put(userId, groupScores);
            });
        }

        Map<String, Map<String, Double>> groupStudentScores = new HashMap<>();
        studentGroupScores.entrySet().forEach(e -> {
            String userId = e.getKey();
            Map<String, Double> groupScores = e.getValue();
            groupScores.entrySet().forEach(g -> {
                String group = g.getKey();
                if (!group.endsWith("_count")){
                    double totalScore = g.getValue();
                    double count = groupScores.getOrDefault(group + "_count", 0.0);
                    double averageScore = count > 0 ? totalScore / count : 0;
                    // add data into groupStudentScores
                    Map<String, Double> studentScores = groupStudentScores.getOrDefault(group, new HashMap<>());
                    studentScores.put(userId, averageScore);
                    groupStudentScores.put(group, studentScores);
                }
            });
        });
        return groupStudentScores;
    }

    public void init() throws JSONException, IOException {
        tokenRepository.deleteAll();
        logRepository.deleteAll();
        Map<String, Student> studentMap = getStudents();
        studentMap.entrySet().stream().forEach(e -> {
            Student student = e.getValue();
            TokenCountEntity entity = getEntityFromStudent(student);
            entity.setToken_count(0);
            tokenRepository.save(entity);
        });
    }

    private TokenCountEntity getEntityFromStudent(Student student) {
        TokenCountEntity entity = new TokenCountEntity();
        entity.setUser_id(student.getId());
        entity.setUser_name(student.getName());
        entity.setUser_email(student.getEmail());
        entity.setTimestamp(new Date());
        return entity;
    }

    private void updateTokenEntity(Map<String, Student> studentMap, String user_id, int add_count, String source) {
        Student student = studentMap.getOrDefault(user_id, null);
        if (student == null) {
            LOGGER.error("Error: Student " + user_id + " does not exist in enrollment list");
            return;
        }
        Optional<TokenCountEntity> optional = tokenRepository.findById(user_id);
        TokenCountEntity entity = null;
        if (optional.isPresent()) {
            entity = optional.get();
            entity.setToken_count(entity.getToken_count() + add_count);
        } else {
            entity = getEntityFromStudent(student);
            entity.setToken_count(add_count);
        }
        tokenRepository.save(entity);

        //Generate token use log
        logRepository.save(createLog(user_id, student.getName(), add_count >= 0 ? "earn" : "spend", add_count, source,""));
    }

    private SpendLogEntity createLog(String user_id, String user_name, String type, Integer token_count, String source, String note) {
        SpendLogEntity n = new SpendLogEntity();
        n.setUser_id(user_id);
        n.setUser_name(user_name);
        n.setType(type);
        n.setTokenCount(token_count);
        n.setSourcee(source);
        n.setTimestamp(new Date());
        n.setNote(note);
        return n;
    }

    public Iterable<TokenCountEntity> manualSyncTokens() throws JSONException, IOException {
        logRepository.save(createLog("", "", "system", 0, "manualSyncTokens(start)",""));
        Iterable<SpendLogEntity> originalLogs = logRepository.findAll();
        List<String> tokenSurveyIds = configRepository.findByType("qualtricsSurveyId");
        init();
        syncModule();
        for (String surveyId : tokenSurveyIds) {
            syncSurvey(surveyId);
        }
        syncLog(originalLogs);
        logRepository.save(createLog("", "", "system", 0, "manualSyncTokens(end)",""));
        return tokenRepository.findAll();
    }

    private void syncSurvey(String surveyId) {
        System.out.println("Fetching Qualtrics Survey " + surveyId);
        Map<String, Student> studentMap = null;
        Set<String> usersToUpdate = new HashSet<>();//List of user_ids that should +1 token
        Set<String> completed_emails = new HashSet<>();
        try {
            completed_emails = qualtricsService.getSurveyCompletions(surveyId);
            studentMap = getStudents();
        } catch (InternalServerException | IOException | JSONException e) {
            e.printStackTrace();
        }
        completed_emails.add("canapitest+4@gmail.com"); // fake_data
        completed_emails.add("canapitest+5@gmail.com"); // fake_data
        completed_emails.add("canapitest+6@gmail.com"); // fake_data
        completed_emails.add("canapitest+7@gmail.com"); // fake_data
        for (Map.Entry<String, Student> entry : studentMap.entrySet()) {
            Student student = entry.getValue();
            if (completed_emails.contains(student.getEmail())) {
                usersToUpdate.add(student.getId());
            }
        }

        for (String userId : usersToUpdate) {
            updateTokenEntity(studentMap, userId, 1, "Qualtrics Survey: " + surveyId);
        }
    }

    private void syncModule() {
        List<String> groupBarCredit = configRepository.findByType("tokenQuiz(group/bar/credit)");
        try {
            Map<String, Student> studentMap = getStudents();
            Map<String, Map<String, Double>> groupStudentScores = getStudentTokenGrades();
            System.out.println("Running Module 1");

            if (groupStudentScores != null && studentMap != null) {
                for (String barGroup :groupBarCredit) {
                    String group = barGroup.split("/")[0];
                    Double bar = Double.valueOf(barGroup.split("/")[1]);
                    Integer credit = Integer.valueOf(barGroup.split("/")[2]);
                    Map<String, Double> studentScores = groupStudentScores.get(group);
                    if (studentScores != null) {
                        for (Map.Entry<String, Double> entry : studentScores.entrySet()) {
                            String userId = entry.getKey();
                            Double score = entry.getValue();
                            if(score >= bar) {
                                updateTokenEntity(studentMap, userId, credit, "group_"+group);
                            }
                        }
                    }
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void syncLog(Iterable<SpendLogEntity> originalLogs) {
        try {
            Map<String, Student> studentMap = getStudents();
            for (SpendLogEntity log : originalLogs) {
                String user_id = String.valueOf(log.getUserId());
                Integer token_count = log.getTokenCount();
                if (log.getType().startsWith("spend")) {
                    updateTokenEntity(studentMap, user_id, -token_count, log.getSource());
                }else if (log.getType().startsWith("system")) {
                    logRepository.save(log);
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Async
    @Override
    public void syncTokensOnDeadline() throws JSONException, IOException {
        init();
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        TaskScheduler scheduler = new ConcurrentTaskScheduler(executorService);
        List<String> tokenSurveyIds = configRepository.findByType("qualtricsSurveyId");

        //Schedule Module 1
        scheduler.schedule(() -> syncModule(), module_deadline);

        for (int i = 0; i < tokenSurveyIds.size(); i++) {
            String surveyId = tokenSurveyIds.get(i);
            Date deadline = survey_deadlines.get(i);
            scheduler.schedule(() -> syncSurvey(surveyId), deadline);
        }
    }

    @Override
    public Iterable<TokenCountEntity> getAllStudentTokenCounts() {
        return tokenRepository.findAll();
    }

    @Override
    public Optional<TokenCountEntity> getStudentTokenCount(String user_id) {
        return tokenRepository.findById(user_id);
    }

    private String apiProcess(URL url, String body) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json");
        builder.addHeader("Authorization", "Bearer " + getBearerToken());
        if (body.length() > 0) {
            builder.post(RequestBody.create(body, JSON));
        }
        Request request = builder.build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
    private Response apiProcess(String method, URL url, RequestBody body, String __) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json");
        builder.addHeader("Authorization", "Bearer " + getBearerToken());
        if (method != "GET") {
            builder.method(method, body);
        }
        Request request = builder.build();
        Response response = client.newCall(request).execute();
        return response;
    }

    private Integer apiProcess(String method, URL url, RequestBody body) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json");
        builder.addHeader("Authorization", "Bearer " + getBearerToken());
        builder.method(method, body);
        Request request = builder.build();
        try (Response response = client.newCall(request).execute()) {
            return response.code();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 400;
    }

    private String getCanvasApiEndpoint() {
        return configRepository.findByType("CANVAS_API_ENDPOINT").get(0);
    }

    private String getCourseID() {
        return configRepository.findByType("CANVAS_COURSE_ID").get(0);
    }

    private List<String> getTokenQuizIds() {
        return configRepository.findByType("tokenQuiz(quizId/group)");
    }

    private List<String> getInstructorEmails() {
        return configRepository.findByType("INSTRUCTOR_EMAILS");
    }

    private String getBearerToken() {
        return configRepository.findByType("CANVAS_BEARER_TOKEN").get(0);
    }

    private Map<String, String> getResubmissionsMap() {
        List<String> resubmissions = configRepository.findByType("resubmissionsMap(ResubmissionId/AssignmentId)");
        Map<String, String> resubmissionsMap = new HashMap<>();
        for (String resubmission : resubmissions) {
            String[] parts = resubmission.split("/");
            resubmissionsMap.put(parts[0], parts[1]);
        }
        return resubmissionsMap;
    }

    private Map<String, Student> getStudents() throws IOException, JSONException {
        int page = 1;
        Map<String, Student> studentMap = new HashMap<>();
        while (true) {
            URL url = UriComponentsBuilder
                    .fromUriString(getCanvasApiEndpoint() + "/courses/" + getCourseID() + "/users")
                    .queryParam("page", page)
                    .queryParam("per_page", PER_PAGE)
                    .build().toUri().toURL();

            String response = apiProcess(url, "");
            JSONArray result = new JSONArray(response);
            for (int i = 0; i < result.length(); i++) {
                String id = ((JSONObject) result.get(i)).get("id").toString();
                String name = ((JSONObject) result.get(i)).get("name").toString();
                String email = ((JSONObject) result.get(i)).get("email").toString();
                studentMap.put(id, new Student(id, name, email));
            }
            if (result.length() < PER_PAGE)
                break;
            page++;
        }
        return studentMap;
    }

    @Override
    public HashMap<Object, Object> getStudentGrades() throws IOException, JSONException {
        Map<String, Student> students = getStudents();
        String users_id = students.entrySet().stream().map(e -> "&student_ids%5B%5D=" + e.getValue().getId()).collect(Collectors.joining(""));
        int page = 1;
        HashMap<Object, Object> students_data = new HashMap<>();

        while (true) {
            URL url = new URL(getCanvasApiEndpoint() + "/courses/" + getCourseID() + "/students/submissions?exclude_response_fields%5B%5D=preview_url&grouped=1&response_fields%5B%5D=assignment_id&response_fields%5B%5D=attachments&response_fields%5B%5D=attempt&response_fields%5B%5D=cached_due_date&response_fields%5B%5D=entered_grade&response_fields%5B%5D=entered_score&response_fields%5B%5D=excused&response_fields%5B%5D=grade&response_fields%5B%5D=grade_matches_current_submission&response_fields%5B%5D=grading_period_id&response_fields%5B%5D=id&response_fields%5B%5D=late&response_fields%5B%5D=late_policy_status&response_fields%5B%5D=missing&response_fields%5B%5D=points_deducted&response_fields%5B%5D=posted_at&response_fields%5B%5D=redo_request&response_fields%5B%5D=score&response_fields%5B%5D=seconds_late&response_fields%5B%5D=submission_type&response_fields%5B%5D=submitted_at&response_fields%5B%5D=url&response_fields%5B%5D=user_id&response_fields%5B%5D=workflow_state&student_ids%5B%5D="
                    + users_id + "&page=" + page + "&per_page=" + PER_PAGE);
            String response = apiProcess(url, "");
            JSONArray result = new JSONArray(response);

            for (int i = 0; i < result.length(); i++) {
                ArrayList<String> grades = new ArrayList<>();
                for (int j = 0; j < ((JSONArray) ((JSONObject) result.get(i)).get("submissions")).length(); j++) {
                    String assignment_id = ((JSONObject) ((JSONArray) ((JSONObject) result.get(i)).get("submissions")).get(j)).get("assignment_id").toString();
                    String score = ((JSONObject) ((JSONArray) ((JSONObject) result.get(i)).get("submissions")).get(j)).get("score").toString();
                    grades.add(score + "(" + assignment_id + ")");
                }
                String user_id = ((JSONObject) result.get(i)).get("user_id").toString();
                students_data.put("(" + user_id + ")", grades);
            }
            if (result.length() < PER_PAGE)
                break;
            page++;
        }
        return students_data;
    }


    @Override
    public Map<String, Object> getCourseData() throws IOException, JSONException {
        int page = 1;
        Map<String, Object> result = new HashMap<>();
        ArrayList<HashMap<Object, Object>> course_data = new ArrayList<>();
        while (true) {
            URL url = new URL(getCanvasApiEndpoint() + "/courses/" + getCourseID() + "/assignment_groups?exclude_assignment_submission_types%5B%5D=wiki_page&exclude_response_fields%5B%5D=description&exclude_response_fields%5B%5D=in_closed_grading_period&exclude_response_fields%5B%5D=needs_grading_count&exclude_response_fields%5B%5D=rubric&include%5B%5D=assignment_group_id&include%5B%5D=assignment_visibility&include%5B%5D=assignments&include%5B%5D=grades_published&include%5B%5D=post_manually&include%5B%5D=module_ids&override_assignment_dates=false"
                    + "&page=" + page + "&per_page=" + PER_PAGE);
            JSONArray response = new JSONArray(apiProcess(url, ""));
            for (int i = 0; i < response.length(); i++) {
                for (int j = 0; j < ((JSONArray) ((JSONObject) response.get(i)).get("assignments")).length(); j++) {
                    HashMap<Object, Object> item = new HashMap<>();
                    String assignment_id = ((JSONObject) ((JSONArray) ((JSONObject) response.get(i)).get("assignments")).get(j)).get("id").toString();
                    String assignment_name = ((JSONObject) ((JSONArray) ((JSONObject) response.get(i)).get("assignments")).get(j)).get("name").toString();
                    item.put("assignment_id", assignment_id);
                    item.put("assignment_name", assignment_name);
                    course_data.add(item);
                }
            }
            if (response.length() < PER_PAGE)
                break;
            page++;
        }
        result.put("result", course_data);
        return result;
    }

    private void sendNotificationEmail(Student student, Assignment assignment, int cost) {
        String message = String.format("On %s %s (ID: %s) successfully requested to use %d tokens for resubmission of %s (ID: %s)",
                new Date(), student.getName(), student.getId(), cost, assignment.getName(), assignment.getId());
        List<String> INSTRUCTOR_EMAILS = configRepository.findByType("INSTRUCTOR_EMAILS");
        for (String email : INSTRUCTOR_EMAILS) {
            emailService.sendSimpleMessage(email, "Usage Update in Token ATM", message);
        }
    }

    @Override
    public UseTokenResponse request_token_use(String user_id, String assignment, Integer cost) throws IOException, BadRequestException, JSONException {
        Optional<TokenCountEntity> optional = tokenRepository.findById(user_id);
        if (!optional.isPresent()) {
            LOGGER.error("Error: Student " + user_id + " is not in current database");
            return new UseTokenResponse("failed","Student " + user_id + " is not in current database", -1);
        }
        TokenCountEntity entity = optional.get();
        Integer token_amount = entity.getToken_count();

        if (token_amount < cost) {
            LOGGER.error("Error: Student " + user_id + " doesn't have enough sufficient token");
            return new UseTokenResponse("failed", "Insufficient token amount", token_amount);
        }

        Map<String, Student> studentMap = getStudents();

        // Create a new request in the database with a 'pending' status
        RequestEntity request = new RequestEntity();
        request.setStudentId(user_id);
        request.setAssignmentId(assignment);
        request.setTokenCount(cost);
        request.setStudentName(studentMap.get(user_id).getName());
        request.setStatus("Pending");
        requestRepository.save(request);

        // create a log
        logRepository.save(createLog(user_id, studentMap.get(user_id).getName(), "spend(pending)", cost, assignment, null));

        // Update the token count in the database
        token_amount -= cost;
        entity.setToken_count(token_amount);
        entity.setTimestamp(new Date());
        tokenRepository.save(entity);

        return new UseTokenResponse("success", "Request submitted for approval", token_amount);

    }

    @Override
    public UseTokenResponse approve_token_use(RequestEntity request) throws IOException, BadRequestException, JSONException {
        Date current_time = new Date();
        String title = "Resubmission";
        Date due =  new Date(current_time.getTime() + 24*60*60*1000);

        if (!request.isApproved()) {
            Map<String, String> resubmissionsMap = getResubmissionsMap();
            String assignment_id = resubmissionsMap.get(request.getAssignmentId());
            URL url = UriComponentsBuilder
                    .fromUriString(getCanvasApiEndpoint() + "/courses/" + getCourseID() + "/assignments/" + assignment_id + "/overrides")
                    .build().toUri().toURL();
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("assignment_override[student_ids][]", request.getStudentId())
                    .addFormDataPart("assignment_override[title]", title)
                    .addFormDataPart("assignment_override[lock_at]", due.toString())
                    .build();

            Response response = apiProcess("POST", url, body, "");
            if (response.code() == 201) {
                String str_response = response.body().string();
                String resubmissionId = new Gson().fromJson(str_response, JsonObject.class).get("id").getAsString();

                Map<String, Student> studentMap = getStudents();
                logRepository.save(createLog(request.getStudentId(), studentMap.get(request.getStudentId()).getName(), "spend(approved)", request.getTokenCount(), request.getAssignmentId(), resubmissionId));
                // sendNotificationEmail(studentMap.get(user_id), resubmission, cost);
                return new UseTokenResponse("success", "", request.getTokenCount());
            } else {
                return new UseTokenResponse("failed", "Request has already been approved", 0);
            }
        }
        if (request.getStatus().equals("Cancelled")) {
            LOGGER.error("Error: Request has already been cancelled");
            return new UseTokenResponse("failed", "Request has already been cancelled", 0);
        } else if (request.getStatus().equals("Rejected")) {
            LOGGER.error("Error: Request has already been rejected");
            return new UseTokenResponse("failed", "Request has already been rejected", 0);
        } else if (request.getStatus().equals("Approved")) {
            LOGGER.error("Error: Request has already been approved");
            return new UseTokenResponse("failed", "Request has already been approved", 0);
        }else {
            LOGGER.error("Unknown Error");
            return new UseTokenResponse("failed", "Unknown Error.", 0);
        }
    }

    @Override
    public UseTokenResponse approve_all_token_use(List<RequestEntity> requests) throws IOException, BadRequestException, JSONException {
        Date current_time = new Date();
        String title = "Resubmission";
        Date due =  new Date(current_time.getTime() + 24*60*60*1000);
        Map<String, Student> studentMap = getStudents();
        Map<String, List<String>> requestsByAssignment = new HashMap<>();
        List<String> approvedRequests = new ArrayList<>();
        for (RequestEntity request : requests) {
            if (requestsByAssignment.containsKey(request.getAssignmentId())) {
                requestsByAssignment.get(request.getAssignmentId()).add(request.getStudentId());
            } else {
                List<String> studentIds = new ArrayList<>();
                studentIds.add(request.getStudentId());
                requestsByAssignment.put(request.getAssignmentId(), studentIds);
            }
        }

        for (Map.Entry<String, List<String>> entry : requestsByAssignment.entrySet()) {
            String assignmentId = entry.getKey();
            List<String> studentIds = entry.getValue();

            Map<String, String> resubmissionsMap = getResubmissionsMap();
            String assignment_id = resubmissionsMap.get(assignmentId);
            URL url = UriComponentsBuilder
                    .fromUriString(getCanvasApiEndpoint() + "/courses/" + getCourseID() + "/assignments/" + assignment_id + "/overrides")
                    .build().toUri().toURL();
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("assignment_override[title]", title)
                    .addFormDataPart("assignment_override[lock_at]", due.toString());

            // Add each student ID to the request body
            for (String studentId : studentIds) {
                builder.addFormDataPart("assignment_override[student_ids][]", studentId);
            }

            RequestBody body = builder.build();
            Response response = apiProcess("POST", url, body, "");
            if (response.code() == 201) {
                String str_response = response.body().string();
                String resubmissionId = new Gson().fromJson(str_response, JsonObject.class).get("id").getAsString();
                for (String studentId : studentIds) {
                    logRepository.save(createLog(studentId, studentMap.get(studentId).getName(), "spend(approved)", studentMap.get(studentId).getTokenCount(), assignmentId, resubmissionId));
                }
                approvedRequests.add(assignmentId);
            } else {
                ObjectMapper mapper = new ObjectMapper();
                String str_response = response.body().string();
                String errorMessage = mapper.readTree(str_response)
                        .path("errors")
                        .path("assignment_override_students")
                        .get(0)
                        .path("message")
                        .asText();
                return new UseTokenResponse("failed", errorMessage, 1);
            }
        }
        for (RequestEntity request : requests) {
            if (approvedRequests.contains(request.getAssignmentId())) {
                request.setStatus("Approved");
                requestRepository.save(request);
            }
        }

        return new UseTokenResponse("success", "", 1);
    }

    @Override
    public RejectTokenResponse reject_token_use(RequestEntity request) throws JSONException, IOException {
        // Find the request for the given student ID and assignment
        Optional<RequestEntity> optionalRequest = requestRepository.findById(request.getId());
        RequestEntity dbRequest = optionalRequest.get();

        // check request id
        if (!optionalRequest.isPresent()) {
            LOGGER.error("Error: Request not found for id " + request.getId());
            return new RejectTokenResponse("failed", "Request not found for id " + request.getId(), 0);
        }
        // check student id
        Optional<TokenCountEntity> optionalTokenCount = tokenRepository.findById(dbRequest.getStudentId());
        if (!optionalTokenCount.isPresent()) {
            LOGGER.error("Error: Student " + dbRequest.getStudentId() + " is not in current database");
            return new RejectTokenResponse("failed", "Student " + dbRequest.getStudentId() + " is not in current database", 0);
        }
        // Check if the request is already cancelled or rejected
        if (dbRequest.getStatus().equals("Cancelled")) {
            LOGGER.error("Error: Request has already been cancelled");
            return new RejectTokenResponse("failed", "Request has already been cancelled", 0);
        } else if (dbRequest.getStatus().equals("Rejected")) {
            LOGGER.error("Error: Request has already been rejected");
            return new RejectTokenResponse("failed", "Request has already been rejected", 0);
        } else if (dbRequest.getStatus().equals("Approved")) {
            LOGGER.error("Error: Request has already been approved");
            return new RejectTokenResponse("failed", "Request has already been approved", 0);
        }

        // Update the request status to 'Rejected'
        dbRequest.setStatus("Rejected");
        requestRepository.save(dbRequest);

        // Update the token count in the database
        TokenCountEntity tokenCount = optionalTokenCount.get();
        Integer token_amount = tokenCount.getToken_count();
        token_amount += dbRequest.getTokenCount();
        tokenCount.setToken_count(token_amount);
        tokenRepository.save(tokenCount);

        // create a log
        Map<String, Student> studentMap = getStudents();
        logRepository.save(createLog(dbRequest.getStudentId(), studentMap.get(dbRequest.getStudentId()).getName(), "spend(rejected)", dbRequest.getTokenCount(), dbRequest.getAssignmentId(), null));

        return new RejectTokenResponse("success", "Request rejected and token count updated", token_amount);
    }

    @Override
    public CancelTokenResponse cancel_token_use(String user_id, String assignment, Integer cost) throws IOException, BadRequestException, JSONException {
        // Check if user exists
        Map<String, Student> studentMap = getStudents();
        // TODO error handling - user_id
        String studentName = studentMap.get(user_id).getName();
        Optional<TokenCountEntity> optionalTokenCount = tokenRepository.findById(user_id);
        if (!optionalTokenCount.isPresent()) {
            LOGGER.error("Error: Student " + user_id + " is not in current database");
            logRepository.save(createLog("", "", "system", 0, "Failed to cancel request - " + studentName + "(" + user_id + ")",""));
            return new CancelTokenResponse("failed", "Student " + user_id + " is not in current database", 0);
        }
        // Find the request for the given student ID and assignment
        List<RequestEntity> optionalRequest = requestRepository.findByStudentIdAndAssignmentIdOrderByIdDesc(user_id, assignment);
        if (optionalRequest.isEmpty()) {
            LOGGER.error("Error: Request not found for student " + user_id + " and assignment " + assignment);
            logRepository.save(createLog("", "", "system", 0, "Failed to cancel request - " + studentName + "(" + user_id + ")",""));
            return new CancelTokenResponse("failed", "Request not found for student " + user_id + " and assignment " + assignment, 0);
        }
        RequestEntity request = optionalRequest.get(0);

        // Check if the request is already cancelled or rejected
        if (request.getStatus().equals("Cancelled")) {
            LOGGER.error("Error: Request has already been cancelled");
            logRepository.save(createLog("", "", "system", 0, "Failed to cancel request - " + studentName + "(" + user_id + ")",""));
            return new CancelTokenResponse("failed", "Request has already been cancelled", 0);
        } else if (request.getStatus().equals("Rejected")) {
            LOGGER.error("Error: Request has already been rejected");
            logRepository.save(createLog("", "", "system", 0, "Failed to cancel request - " + studentName + "(" + user_id + ")",""));
            return new CancelTokenResponse("failed", "Request has already been rejected", 0);
        }
        Date current_time = new Date();
        if (request.getStatus().equals("Approved")) {
            List<SpendLogEntity> optional2 = logRepository.findByUserIdAssignmentIdType(user_id, assignment, "spend(approved)");
            SpendLogEntity entity2 = optional2.get(0);
            String resubmission_id = entity2.getNote();
            Map<String, String> resubmissionsMap = getResubmissionsMap();
            String assignment_id = resubmissionsMap.get(assignment);
            Date approvedDate = entity2.getTimestamp();
            Date expiredDate = new Date(approvedDate.getTime() + 24 * 3600 * 1000);

            //
            if (current_time.after(expiredDate)) {
                logRepository.save(createLog("", "", "system", 0, "Failed to cancel request - " + studentName + "(" + user_id + ")",resubmission_id));
                return new CancelTokenResponse("failed", "You can only cancel it within 24 hours", 0);
            }

            // Get user list from a specific resubmissionId
            URL url = UriComponentsBuilder
                    .fromUriString(getCanvasApiEndpoint() + "/courses/" + getCourseID() + "/assignments/" + assignment_id + "/overrides/" + resubmission_id)
                    .build().toUri().toURL();
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("", "");
            Response response = apiProcess("GET", url, builder.build(), "");
            String str_response = response.body().string();
            if (response.code() != 200) {
                logRepository.save(createLog("", "", "system", 0, "Failed to cancel request - " + studentName + "(" + user_id + ")",resubmission_id));
                return new CancelTokenResponse("failed", new ObjectMapper().readTree(str_response).path("errors").get(0).path("message").asText(), 0);
            }

            JSONArray student_ids = (JSONArray) new JSONObject(str_response).get("student_ids");

            // remove target user and update the resubmission
            Integer responseStatus = null;
            if (student_ids != null && student_ids.length() > 1) {
                for (int i = 0; i < student_ids.length(); i++) {
                    String student_id = student_ids.get(i).toString();
                    if (!user_id.equals(student_id)) {
                        builder.addFormDataPart("assignment_override[student_ids][]", student_id);
                    }
                }
                builder.addFormDataPart("assignment_override[title]", "Resubmission");
                builder.addFormDataPart("assignment_override[lock_at]", new JSONObject(str_response).get("lock_at").toString());
                responseStatus = apiProcess("PUT", url, builder.build());
            } else {
                responseStatus = apiProcess("DELETE", url, builder.build());
            }
            switch (responseStatus) {
                case 200:
                    // Update the request status to 'Cancelled'
                    logRepository.save(createLog("", "", "system", 0, "Revoked request - " + studentName + "(" + user_id + ")",resubmission_id));
                    request.setStatus("Revoked");
                    break;
                case 400:
                    logRepository.save(createLog("", "", "system", 0, "Failed to cancel request - " + studentName + "(" + user_id + ")",resubmission_id));
                    return new CancelTokenResponse("failed", "Student already requested resubmission", 0);
                default:
                    logRepository.save(createLog("", "", "system", 0, "Failed to cancel request - " + studentName + "(" + user_id + ")",resubmission_id));
                    return new CancelTokenResponse("failed", "Unable to update tokens", 0);
            }
        } else {
            logRepository.save(createLog("", "", "system", 0, "Cancelled request - " + studentName + "(" + user_id + ")",""));
            // Update the request status to 'Cancelled'
            request.setStatus("Cancelled");
        }
        requestRepository.save(request);

        // Update the token count in the database
        TokenCountEntity tokenCount = optionalTokenCount.get();
        Integer token_amount = tokenCount.getToken_count();
        token_amount += request.getTokenCount();
        tokenCount.setTimestamp(current_time);
        tokenCount.setToken_count(token_amount);
        tokenRepository.save(tokenCount);

        // create a log
        logRepository.save(createLog(user_id, studentMap.get(user_id).getName(), "spend(cancel)", request.getTokenCount(), assignment, null));
        return new CancelTokenResponse("success", "Request cancelled and token count updated", token_amount);
    }

    /**
     * Fetch grades of all students for a specific quiz
     *
     * @param quizId Quiz ID, can be looked up using List Assignments API
     * @return Map of quiz scores, key is student id, value is score of the quiz for this student
     * @throws IOException
     * @throws JSONException
     */
    private Map<String, Double> getStudentQuizScores(String quizId) throws IOException, JSONException {
        int page = 1;
        Map<String, Student> students = getStudents();
        Map<String, Double> quizScores = new HashMap<>();

        while (true) {
            String users_id = students.entrySet().stream().map(e -> "&student_ids%5B%5D=" + e.getValue().getId()).collect(Collectors.joining(""));
            URL url = new URL(getCanvasApiEndpoint() + "/courses/" + getCourseID() + "/quizzes/" + quizId +
                    "/submissions?exclude_response_fields%5B%5D=preview_url&grouped=1&response_fields%5B%5D=assignment_id&response_fields%5B%5D=attachments&response_fields%5B%5D=attempt&response_fields%5B%5D=cached_due_date&response_fields%5B%5D=entered_grade&response_fields%5B%5D=entered_score&response_fields%5B%5D=excused&response_fields%5B%5D=grade&response_fields%5B%5D=grade_matches_current_submission&response_fields%5B%5D=grading_period_id&response_fields%5B%5D=id&response_fields%5B%5D=late&response_fields%5B%5D=late_policy_status&response_fields%5B%5D=missing&response_fields%5B%5D=points_deducted&response_fields%5B%5D=posted_at&response_fields%5B%5D=redo_request&response_fields%5B%5D=score&response_fields%5B%5D=seconds_late&response_fields%5B%5D=submission_type&response_fields%5B%5D=submitted_at&response_fields%5B%5D=url&response_fields%5B%5D=user_id&response_fields%5B%5D=workflow_state&student_ids%5B%5D="
                    + users_id + "&page=" + page + "&per_page=" + PER_PAGE);
            String response = apiProcess(url, "");
            JSONObject resultObj = new JSONObject(response);
            JSONArray result = resultObj.getJSONArray("quiz_submissions");

            for (int i = 0; i < result.length(); i++) {
                JSONObject jsonObject = result.getJSONObject(i);
                double kept_score = jsonObject.getDouble("kept_score"), max_score = jsonObject.getDouble("quiz_points_possible");
                double percentage_score = kept_score / max_score * 100;
                String studentId = String.valueOf(jsonObject.getInt("user_id"));
                quizScores.put(studentId, percentage_score);
            }
            if (result.length() < PER_PAGE)
                break;
            page++;
        }
        return quizScores;
    }

    @Override
    public List<AssignmentStatus> getAssignmentStatuses(String user_id) {
        LOGGER.info("Fetching assignment statuses for " + user_id);
        List<AssignmentStatus> assignmentStatuses = new ArrayList<>();
        Map<String, String> resubmissionsMap = getResubmissionsMap();
        try {
            assignmentStatuses = getAssignmentsStatusForStudent(user_id, resubmissionsMap);
        } catch (IOException | JSONException ex) {
            ex.printStackTrace();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return assignmentStatuses;
    }

    /**
     * List assignment submissions for a specific student
     *
     * @param user_id
     * @param assignmentId
     * @return
     */
    private AssignmentStatus getAssignmentStatusForStudent(String user_id, String assignmentId, String resubmissionId) throws IOException, JSONException {
        int page = 1;
        Assignment assignment = fetchAssignment(assignmentId);
        Assignment resubmission = fetchAssignment(resubmissionId);

        while (true) {
            URL url = UriComponentsBuilder
                    .fromUriString(getCanvasApiEndpoint() + "/courses/" + getCourseID() + "/assignments/" + assignmentId + "/submissions")
                    .queryParam("page", page)
                    .queryParam("per_page", PER_PAGE)
                    .build().toUri().toURL();
            String response = apiProcess(url, "");
            JSONArray resultArray = new JSONArray(response);
            for (int i = 0; i < resultArray.length(); i++) {
                JSONObject submissionObj = resultArray.getJSONObject(i);
                String submissionUserId = submissionObj.get("user_id").toString();
                Double score = null;
                if (submissionUserId.equals(user_id)) {
                    score = submissionObj.isNull("score") ? null : submissionObj.getDouble("score");
                    //Doesn't have a grade yet or can't fetch grade
                    if (score == null) {
                        return new AssignmentStatus(assignment.getName(), assignment.getId(), resubmission.getId(), assignment.getDueDate(), 0.0, assignment.getMaxPoints(), "Not graded yet", -1);
                    }
                    //Grades released
                    int tokens_required = (int) (assignment.getMaxPoints() - score);
                    if (!resubmission.getDueDate().equals("No Due Date")
                            && Instant.now().isAfter(Instant.parse(resubmission.getDueDate()))) {
                        return new AssignmentStatus(assignment.getName(),
                                assignment.getId(),
                                resubmission.getId(),
                                resubmission.getDueDate(),
                                score,
                                assignment.getMaxPoints(),
                                "overdue",
                                -1);
                    }
                    String status = "none";
                    List<SpendLogEntity> data = logRepository.findByUserIdAssignmentId(user_id, assignmentId);

                    if (data.size() > 0) {
                        String latest_status = data.get(data.size() - 1).getType();
                        if (latest_status.equals("spend(approved)")) {
                            status = "Approved";
                        } else if (latest_status.equals("spend(pending)")) {
                            status = "Pending";
                        }
                    }
                    return new AssignmentStatus(assignment.getName(),
                            assignment.getId(),
                            resubmission.getId(),
                            resubmission.getDueDate(),
                            score,
                            assignment.getMaxPoints(),
                            status,
                            tokens_required);
                }
            }
            if (resultArray.length() < PER_PAGE)
                break;
            page++;
        }
        return new AssignmentStatus(assignment.getName(),
                assignment.getId(),
                resubmission.getId(),
                resubmission.getDueDate(),
                0.0,
                assignment.getMaxPoints(),
                "N/A",
                -1);
    }

    private List<AssignmentStatus> getAssignmentsStatusForStudent(String user_id, Map<String, String> resubmissionsMap) throws IOException, JSONException, URISyntaxException {
        List<AssignmentStatus> assignmentStatuses = new ArrayList<>();
        ArrayList<String> queries = new ArrayList<>();
        queries.add("student_ids[]=" + user_id);
        queries.add("include[]=assignment");
        ArrayList<String> resubmissionIds = new ArrayList<>();

        for (Map.Entry<String, String> entry : resubmissionsMap.entrySet()) {
            String assignmentId = entry.getKey();
            String resubmissionId = entry.getValue();
            resubmissionIds.add(resubmissionId);
            queries.add("assignment_ids[]=" + assignmentId);
        }
        URL url = UriComponentsBuilder.fromUriString(getCanvasApiEndpoint() + "/courses/" + getCourseID() + "/students/submissions")
                .queryParam(String.join("&", queries))
                .build().toUri().toURL();

        String response = apiProcess(url, "");
        JSONArray resultArray = new JSONArray(response);
        Map<String, Map<String, String>> resubmissionData = fetchResubmissions(resubmissionsMap);

        for (int i = 0; i < resultArray.length(); i++) {
            JSONObject submissionObj = resultArray.getJSONObject(i);
            JSONObject assignmentObj = submissionObj.getJSONObject("assignment");
            String assignmentId = assignmentObj.get("id").toString();
            Double score = submissionObj.isNull("score") ? null : submissionObj.getDouble("score");
            String assignmentName = assignmentObj.get("name").toString();
            String assignmentDue = assignmentObj.get("due_at").toString();
            Double assignmentMaxScore = assignmentObj.getDouble("points_possible");
            String resubmissionId = resubmissionsMap.get(assignmentId);
            String resubmissionDue = resubmissionData.get(resubmissionId).get("due_at");

            //Doesn't have a grade yet or can't fetch grade
            if (score == null) {
                assignmentStatuses.add(new AssignmentStatus(assignmentName, assignmentId, resubmissionId, assignmentDue, 0.0, assignmentMaxScore, "Not graded yet", -1));
            } else {
                //Grades released
                int tokens_required = (int) (assignmentMaxScore - score);
                String status = "none";
                if (!resubmissionDue.equals("No Due Date") && Instant.now().isAfter(Instant.parse(resubmissionDue))) {
                    assignmentStatuses.add(new AssignmentStatus(assignmentName, assignmentId, resubmissionId, resubmissionDue, score, assignmentMaxScore, "overdue", -1));
                } else {
                    List<SpendLogEntity> data = logRepository.findByUserIdAssignmentId(user_id, assignmentId);

                    if (!data.isEmpty()) {
                        String latestStatus = data.get(data.size() - 1).getType();
                        if (latestStatus.equals("spend(approved)")) {
                            status = "Approved";
                        } else if (latestStatus.equals("spend(pending)")) {
                            status = "Pending";
                        }
                    }
                    assignmentStatuses.add(new AssignmentStatus(assignmentName, assignmentId, resubmissionId, resubmissionDue, score, assignmentMaxScore, status, tokens_required));
                }
            }
        }
        return assignmentStatuses;
    }


    private Assignment fetchAssignment(String assignmentId) throws IOException, JSONException {
        URL url = UriComponentsBuilder.fromUriString(getCanvasApiEndpoint() + "/courses/" + getCourseID() + "/assignments/" + assignmentId)
                .build().toUri().toURL();
        String response = apiProcess(url, "");
        JSONObject responseObj = new JSONObject(response);
        String dueAt = responseObj.get("lock_at").toString();
        if (dueAt == null || dueAt.equals("null")) {
            dueAt = "No Due Date";
        }
        double pointsPossible = responseObj.getDouble("points_possible");
        String name = responseObj.get("name").toString();
        return new Assignment(assignmentId, name, dueAt, pointsPossible);
    }

    private Map<String, Map<String, String>> fetchResubmissions(Map<String, String> resubmissionsMap) throws IOException, JSONException, URISyntaxException {
        Map<String, Map<String, String>> resubmissionsIdMap = new HashMap<>();

        for (String assignmentId : resubmissionsMap.values()) {
            resubmissionsIdMap.put(assignmentId, new HashMap<>());
        }
        String query = "assignment_ids[]=" + String.join("&assignment_ids[]=", resubmissionsMap.values());
        URL url = new URL(getCanvasApiEndpoint() + "/courses/" + getCourseID() + "/assignments?" + query);

        String response = apiProcess(url, "");
        JSONArray resultArray = new JSONArray(response);

        for (int i = 0; i < resultArray.length(); i++) {
            JSONObject submissionObj = resultArray.getJSONObject(i);
            String ResubmissionId = submissionObj.get("id").toString();
            Map<String, String> item = resubmissionsIdMap.get(ResubmissionId);
            item.put("id", ResubmissionId);
            item.put("due_at", submissionObj.get("lock_at").toString());
        }

        return  resubmissionsIdMap;
    }

    @Override
    public RequestUserIdResponse getUserIdFromEmail(String email) throws JSONException, IOException {
        Map<String, Student> studentMap = getStudents();
        for (Map.Entry<String, Student> entry : studentMap.entrySet()) {
            if (entry.getValue().getEmail().equals(email))
                return new RequestUserIdResponse(entry.getKey());
        }
        return new RequestUserIdResponse("-1");
    }

    @Override
    public UpdateTokenResponse updateToken(String user_id, Integer tokenNum) throws JSONException, IOException {
        Optional<TokenCountEntity> optional = tokenRepository.findById(user_id);
        if (optional.isPresent()) {
            TokenCountEntity entity = optional.get();
            Integer oriTokenNum = entity.getToken_count();
            entity.setToken_count(tokenNum);
            tokenRepository.save(entity);

            //Save manual update log
            Map<String, Student> students = getStudents();
            Student student = students.get(user_id);
            logRepository.save(createLog(user_id, student.getName(), "system", tokenNum, "Manual Update Token Amount from "+ oriTokenNum +" to " + tokenNum,""));
            return new UpdateTokenResponse("complete", tokenNum);
        } else {
            LOGGER.error("Error: Student " + user_id + " does not exist in database");
            return new UpdateTokenResponse("failed", -1);
        }
    }

    @Override
    public ResponseEntity<?> exportData() {
        Iterable<SpendLogEntity> logs = logRepository.findAll();
        Iterable<TokenCountEntity> tokens = tokenRepository.findAll();

        try {
            File file = File.createTempFile("export", ".csv");
            FileWriter fileWriter = new FileWriter(file);
            CSVWriter csvWriter = new CSVWriter(fileWriter);

            csvWriter.writeNext(new String[]{"Logs"});
            for (SpendLogEntity log : logs) {
                csvWriter.writeNext(new String[] {String.valueOf(log.getId()), log.getSource(), String.valueOf(log.getTimestamp()), String.valueOf(log.getTokenCount()), log.getType(), log.getUserId(), log.getUser_name() });
            }
            csvWriter.writeNext(new String[]{"Tokens"});
            for (TokenCountEntity token : tokens) {
                csvWriter.writeNext(new String[] {token.getUser_id(), token.getUser_name(), token.getUser_email(), String.valueOf(token.getToken_count()), String.valueOf(token.getTimestamp())});
            }
            csvWriter.close();
            fileWriter.close();

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_TYPE, "text/csv");
            headers.setContentDispositionFormData("attachment", "export.csv");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            ResponseEntity<Object> response = new ResponseEntity<Object>(new FileSystemResource(file), headers, HttpStatus.OK);
            return response;
        } catch (IOException e) {
            // Handle the exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error exporting data");
        }
    }
}