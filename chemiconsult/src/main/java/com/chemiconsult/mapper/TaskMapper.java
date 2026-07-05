package com.chemiconsult.mapper;

import com.chemiconsult.entity.TaskDE;
import com.chemiconsult.to.TaskTO;

import java.util.List;

public class TaskMapper {
    public static List<TaskTO> mapTaskEntityToTO(List<TaskDE> taskList) {
        return taskList.stream().map(task ->
                        TaskTO.builder()
                                .id(task.getId())
                                .title(task.getTitle())
                                .description(task.getDescription())
                                .build())
                .toList();
    }

    public static void mapTaskTOToEntity(TaskTO task) {
    }
}
