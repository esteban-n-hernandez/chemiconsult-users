package com.chemiconsult.to;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class TaskTO {

    private Long id;
    private String title;
    private String description;
    private String status;
    private Long userId;
    private String userName;
    private boolean archived;
    private LocalDate completedDate;
    private LocalDate archivedDate;
    private LocalDate dueDate;

}
