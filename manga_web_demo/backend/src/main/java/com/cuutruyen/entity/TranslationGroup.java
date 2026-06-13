package com.cuutruyen.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "TranslationGroups")
@Data
public class TranslationGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Integer groupId;

    @Column(unique = true, nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "leader_id", nullable = false)
    private User leader;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    // Số dư chung của nhóm (tăng khi có người mở khoá chapter)
    @Column(name = "balance", nullable = false)
    private Long balance = 0L;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status {
        PENDING, ACTIVE, REJECTED, BANNED
    }
}
