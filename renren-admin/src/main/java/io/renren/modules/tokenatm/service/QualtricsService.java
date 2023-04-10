package io.renren.modules.tokenatm.service;

import io.renren.modules.tokenatm.exceptions.InternalServerException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.util.Set;

public interface QualtricsService {

    @Retryable(value = InternalServerException.class, maxAttempts = 10, backoff = @Backoff(delay = 1_000))
    Set<String> getSurveyCompletions(String surveyId) throws InternalServerException;

}
