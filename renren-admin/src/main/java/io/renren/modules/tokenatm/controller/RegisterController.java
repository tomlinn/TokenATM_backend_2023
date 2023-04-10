package io.renren.modules.tokenatm.controller;

import io.renren.modules.tokenatm.entity.VerficationEntity;
import io.renren.modules.tokenatm.service.EmailService;
import io.renren.modules.tokenatm.service.Request.RequestVerificationBody;
import io.renren.modules.tokenatm.service.Request.VerificationBody;
import io.renren.modules.tokenatm.service.Response.RequestVerificationResponse;
import io.renren.modules.tokenatm.service.Response.VerificationResponse;
import io.renren.modules.tokenatm.service.VerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.Random;

@RestController
public class RegisterController {

    @Autowired
    EmailService emailService;

    @Autowired
    VerificationRepository verificationRepository;

    @PostMapping("/request_verification")
    public RequestVerificationResponse requestVerificationCode(@RequestBody RequestVerificationBody body) {
        String email = body.getEmail();
        String verification = getRandomVerification();
        Optional<VerficationEntity> optional = verificationRepository.findByEmail(email);
        if (optional.isPresent()) {
            VerficationEntity entity = optional.get();
            entity.setCode(verification);
            verificationRepository.save(entity);
        } else {
            verificationRepository.save(new VerficationEntity(email, verification));
        }
        emailService.sendSimpleMessage(body.getEmail(), "Your Token ATM Registration Code", verification);
        return new RequestVerificationResponse("success", "");
    }

    @PostMapping("/verify")
    public VerificationResponse verify(@RequestBody VerificationBody body) {
        String email = body.getEmail(), verification = body.getVerification();
        Optional<VerficationEntity> optional = verificationRepository.findByEmail(email);
        if (optional.isPresent()) {
            VerficationEntity entity = optional.get();
            if (entity.getCode().equals(verification)) {
                return new VerificationResponse("success", "");
            }
            return new VerificationResponse("failed", "verification code is wrong");
        } else {
            return new VerificationResponse("failed", "email is not recognized");
        }
    }

    public static String getRandomVerification() {
        Random random = new Random();
        int number = random.nextInt(999999);
        return String.format("%06d", number);
    }
}
