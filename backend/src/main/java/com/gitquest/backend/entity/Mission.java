package com.gitquest.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "missions")
public class Mission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private int level;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String hint;

    // ミッションのステップ一覧（順番通りに取得）
    @OneToMany(mappedBy = "mission", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("orderIndex ASC")
    private List<MissionStep> steps = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = OffsetDateTime.now(); }

    public UUID getId() { return id; }
    public int getLevel() { return level; }
    public int getOrderIndex() { return orderIndex; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getHint() { return hint; }
    public List<MissionStep> getSteps() { return steps; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
