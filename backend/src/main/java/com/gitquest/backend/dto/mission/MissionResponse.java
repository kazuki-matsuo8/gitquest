package com.gitquest.backend.dto.mission;

import com.gitquest.backend.entity.Mission;

import java.util.List;
import java.util.UUID;

public record MissionResponse(
        UUID id,
        int level,
        int orderIndex,
        String title,
        String description,
        String hint,
        List<MissionStepResponse> steps
) {
    public static MissionResponse from(Mission mission) {
        return new MissionResponse(
                mission.getId(),
                mission.getLevel(),
                mission.getOrderIndex(),
                mission.getTitle(),
                mission.getDescription(),
                mission.getHint(),
                mission.getSteps().stream().map(MissionStepResponse::from).toList()
        );
    }
}
