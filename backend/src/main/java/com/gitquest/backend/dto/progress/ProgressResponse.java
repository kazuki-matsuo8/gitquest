package com.gitquest.backend.dto.progress;

import com.gitquest.backend.entity.UserProgress;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProgressResponse(
        UUID id,
        UUID missionId,
        String missionTitle,
        int missionLevel,
        String status,
        OffsetDateTime completedAt,
        OffsetDateTime updatedAt
) {
    public static ProgressResponse from(UserProgress progress) {
        return new ProgressResponse(
                progress.getId(),
                progress.getMission().getId(),
                progress.getMission().getTitle(),
                progress.getMission().getLevel(),
                progress.getStatus().name(),
                progress.getCompletedAt(),
                progress.getUpdatedAt()
        );
    }
}
