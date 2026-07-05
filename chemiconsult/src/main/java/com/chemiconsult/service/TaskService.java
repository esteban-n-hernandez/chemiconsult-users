package com.chemiconsult.service;

import com.chemiconsult.entity.TaskDE;
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

    // ── Listar todas ──
    public List<TaskTO> getAllTasks() {
        return TaskMapper.mapTaskEntityToTO(taskRepository.findAll());
    }

    // ── Crear ──
    public TaskTO createTask(TaskTO task) {
        TaskDE entity = TaskMapper.mapTaskTOToEntity(task);
        TaskDE creada = taskRepository.save(entity);
        return TaskMapper.mapTaskEntityToTO(creada);
    }

    // ── Actualizar estado ──
    public TaskTO updateStatus(Long id, TaskStatus status) {
        TaskDE task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada con ID: " + id));
        TaskMapper.applyStatus(task, status);
        TaskDE actualizada = taskRepository.save(task);
        return TaskMapper.mapTaskEntityToTO(actualizada);
    }

    // ── Eliminar ──
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new RuntimeException("Tarea no encontrada con ID: " + id);
        }
        taskRepository.deleteById(id);
    }

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
}
