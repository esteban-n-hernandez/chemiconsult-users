package com.chemiconsult.controller;

import com.chemiconsult.enums.TaskStatus;
import com.chemiconsult.service.TaskService;
import com.chemiconsult.to.TaskTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@RestController
@RequestMapping("/api/task")
public class TaskController {

    TaskService taskService;

    // GET /api/task
    @GetMapping
    public ResponseEntity<List<TaskTO>> getAllTasks() {
        log.info("Obteniendo todas las tareas");
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    // POST /api/task
    @PostMapping
    public ResponseEntity<TaskTO> createTask(@RequestBody TaskTO task) {
        log.info("Creando nueva tarea: {}", task);
        TaskTO creada = taskService.createTask(task);
        return ResponseEntity.status(201).body(creada);
    }

    // PUT /api/task/{id}/status
    @PutMapping("/{id}/status")
    public ResponseEntity<TaskTO> updateStatus(@PathVariable Long id, @RequestParam TaskStatus status) {
        log.info("Actualizando estado de la tarea con ID: {} a: {}", id, status);
        return ResponseEntity.ok(taskService.updateStatus(id, status));
    }

    // DELETE /api/task/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        log.info("Eliminando tarea con ID: {}", id);
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }


    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

}
