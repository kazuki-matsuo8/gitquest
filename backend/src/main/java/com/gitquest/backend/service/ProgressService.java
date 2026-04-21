package com.gitquest.backend.service;

import com.gitquest.backend.dto.progress.ProgressResponse;
import com.gitquest.backend.entity.Mission;
import com.gitquest.backend.entity.User;
import com.gitquest.backend.entity.UserProgress;
import com.gitquest.backend.repository.MissionRepository;
import com.gitquest.backend.repository.UserProgressRepository;
import com.gitquest.backend.repository.UserRepository;
import com.gitquest.backend.security.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProgressService {

    private final UserProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final MissionRepository missionRepository;

    public ProgressService(
            UserProgressRepository progressRepository,
            UserRepository userRepository,
            MissionRepository missionRepository
    ) {
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.missionRepository = missionRepository;
    }

    // ログイン中ユーザーの進捗一覧
    @Transactional(readOnly = true)
    public List<ProgressResponse> getMyProgress() {
        User user = getCurrentUser();
        return progressRepository.findByUserId(user.getId())
                .stream()
                .map(ProgressResponse::from)
                .toList();
    }

    // ミッションを「開始中」にする
    @Transactional
    public ProgressResponse start(UUID missionId) {
        User user = getCurrentUser();
        Mission mission = getMission(missionId);

        UserProgress progress = progressRepository
                .findByUserIdAndMissionId(user.getId(), missionId)
                .orElseGet(() -> {
                    UserProgress p = new UserProgress();
                    p.setUser(user);
                    p.setMission(mission);
                    return p;
                });

        progress.setStatus(UserProgress.Status.IN_PROGRESS);
        return ProgressResponse.from(progressRepository.save(progress));
    }

    // ミッションを「完了」にする
    @Transactional
    public ProgressResponse complete(UUID missionId) {
        User user = getCurrentUser();
        Mission mission = getMission(missionId);

        UserProgress progress = progressRepository
                .findByUserIdAndMissionId(user.getId(), missionId)
                .orElseGet(() -> {
                    UserProgress p = new UserProgress();
                    p.setUser(user);
                    p.setMission(mission);
                    return p;
                });

        progress.setStatus(UserProgress.Status.COMPLETED);
        progress.setCompletedAt(OffsetDateTime.now());
        return ProgressResponse.from(progressRepository.save(progress));
    }

    private User getCurrentUser() {
        String email = SecurityUtil.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("ログインユーザーが見つかりません"));
    }

    private Mission getMission(UUID missionId) {
        return missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("ミッションが見つかりません"));
    }
}
