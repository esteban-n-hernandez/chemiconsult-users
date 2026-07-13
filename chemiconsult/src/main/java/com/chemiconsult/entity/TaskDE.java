package com.chemiconsult.entity;

import com.chemiconsult.enums.TaskStatusEnum;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "TASK")
public class TaskDE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private UserDE user;

    @Column(nullable = false)
    private String title;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatusEnum status = TaskStatusEnum.TODO;

    @Column(name = "CREATED_DATE", nullable = false)
    private LocalDate createdDate;

    @Column(name = "COMPLETED_DATE")
    private LocalDate completedDate;

}
