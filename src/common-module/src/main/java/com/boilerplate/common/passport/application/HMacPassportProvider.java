package com.boilerplate.common.passport.application;

import com.boilerplate.common.passport.dto.Passport;
import com.boilerplate.common.passport.dto.UserInfo;
import com.boilerplate.common.passport.exception.InvalidPassportException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class HMacPassportProvider implements PassportProvider {
    private final String HMacAlgo;
    private final String secretKey;
    private final ObjectMapper objectMapper;

    public HMacPassportProvider(@Value("${passport.algorithm}") final String HMacAlgo,
                                @Value("${passport.key}") final String secretKey,
                                final ObjectMapper objectMapper) {
        this.HMacAlgo = HMacAlgo;
        this.secretKey = secretKey;
        this.objectMapper = objectMapper;
    }

    @Override
    public String generatePassport(UserInfo userInfo) {
        String message;
        try {
            String userInfoStr = objectMapper.writeValueAsString(userInfo);
            String hashStr = createHMAC(userInfoStr);

            Passport passport = new Passport(userInfo, hashStr);
            String passportStr = objectMapper.writeValueAsString(passport);
            message = Base64.getEncoder().encodeToString(passportStr.getBytes());

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return message;
    }

    @Override
    public UserInfo getMemberInfo(String message) {
        UserInfo memberInfo;
        try {
            String passportStr = new String(Base64.getDecoder().decode(message));
            String infoStr = objectMapper.readTree(passportStr).get("userInfo").toString();
            memberInfo = objectMapper.readValue(infoStr, UserInfo.class);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return memberInfo;
    }

    @Override
    public boolean validatePassport(String message) {
        String infoEncodeStr;
        String hashStr;
        try {
            String passportStr = new String(Base64.getDecoder().decode(message));
            String infoStr = objectMapper.readTree(passportStr).get("userInfo").toString();
            infoEncodeStr = createHMAC(infoStr);
            hashStr = objectMapper.readTree(passportStr).get("userInfoIntegrity").asText();

            if (!hashStr.equals(infoEncodeStr)) throw new InvalidPassportException("잘못된 패스포트");

        } catch (Exception e) {
            throw new InvalidPassportException("잘못된 패스포트");
        }

        return true;
    }

    private String createHMAC(String message) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), HMacAlgo);
        Mac mac = null;
        try {
            mac = Mac.getInstance(HMacAlgo);
            mac.init(secretKeySpec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Base64.getEncoder().encodeToString(mac.doFinal(message.getBytes()));
    }
}
