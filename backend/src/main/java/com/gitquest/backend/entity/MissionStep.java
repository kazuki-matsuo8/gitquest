package com.gitquest.backend.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "mission_steps")
public class MissionStep {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(nullable = false, length = 200)
    private String description;

    @Column(name = "expected_command", nullable = false, length = 200)
    private String expectedCommand;

    public UUID getId() { return id; }
    public Mission getMission() { return mission; }
    public int getOrderIndex() { return orderIndex; }
    public String getDescription() { return description; }
    public String getExpectedCommand() { return expectedCommand; }
}
