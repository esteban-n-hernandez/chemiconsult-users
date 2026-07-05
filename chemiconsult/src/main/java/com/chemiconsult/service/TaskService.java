package com.chemiconsult.service;

import com.chemiconsult.enums.TaskStatus;
import com.chemiconsult.mapper.TaskMapper;
import com.chemiconsult.repository.TaskRepository;
import com.chemiconsult.to.TaskTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    TaskRepository taskRepository;

    public List<TaskTO> getAllTasks() {
        return TaskMapper.mapTaskEntityToTO(taskRepository.findAll());
    }

    public TaskTO createTask(TaskTO task) {
        TaskMapper.mapTaskTOToEntity(task);
        return null;
    }

    public TaskTO updateStatus(Long id, TaskStatus status) {
        return null;
    }

    public void deleteTask(Long id) {
    }

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
}
