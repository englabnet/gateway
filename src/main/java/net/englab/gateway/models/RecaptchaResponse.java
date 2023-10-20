package net.englab.gateway.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RecaptchaResponse(
    boolean success,
    double score,
    String action,
    @JsonProperty("challenge_ts")
    String challengeTs,
    String hostname

) { }
