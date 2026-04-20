package com.gitquest.backend.dto.mission;

import com.gitquest.backend.entity.MissionStep;

import java.util.UUID;

public record MissionStepResponse(
        UUID id,
        int orderIndex,
        String description,
        String expectedCommand
) {
    public static MissionStepResponse from(MissionStep step) {
        return new MissionStepResponse(
                step.getId(),
                step.getOrderIndex(),
                step.getDescription(),
                step.getExpectedCommand()
        );
    }
}
