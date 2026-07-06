package com.chemiconsult.mapper;

import com.chemiconsult.entity.TaskDE;
import com.chemiconsult.enums.TaskStatus;
import com.chemiconsult.to.TaskTO;

import java.time.LocalDate;
import java.util.List;

public class TaskMapper {
    public static List<TaskTO> mapTaskEntityToTO(List<TaskDE> taskList) {
        return taskList.stream().map(task ->
                        TaskTO.builder()
                                .id(task.getId())
                                .title(task.getTitle())
                                .description(task.getDescription())
                                .status(task.getStatus() != null ? task.getStatus().name() : null)
                                .build())
                .toList();
    }

    public static TaskTO mapTaskEntityToTO(TaskDE task) {
        return TaskTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus() != null ? task.getStatus().name() : null)
                .build();
    }

    public static TaskDE mapTaskTOToEntity(TaskTO task) {
        TaskDE taskDE = new TaskDE();
        taskDE.setTitle(task.getTitle());
        taskDE.setDescription(task.getDescription());
        taskDE.setStatus(task.getStatus() == null ? TaskStatus.TODO : TaskStatus.valueOf(task.getStatus()));
        taskDE.setCreatedDate(LocalDate.now());
        taskDE.setCompletedDate(taskDE.getStatus() == TaskStatus.DONE ? LocalDate.now() : null);
        return taskDE;
    }

    public static TaskDE applyStatus(TaskDE task, TaskStatus status) {
        task.setStatus(status);
        task.setCompletedDate(status == TaskStatus.DONE ? LocalDate.now() : null);
        return task;
    }
}
