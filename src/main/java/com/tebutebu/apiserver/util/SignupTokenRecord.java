package com.tebutebu.apiserver.util;

public record SignupTokenRecord(
        String provider,
        String providerId,
        String email
) {}
