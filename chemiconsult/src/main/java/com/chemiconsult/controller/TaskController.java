package com.chemiconsult.controller;

import com.chemiconsult.enums.TaskStatusEnum;
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

    // PUT /api/task/{id} â€” ediciÃ³n completa (tÃ­tulo, descripciÃ³n, asignado)
    @PutMapping("/{id}")
    public ResponseEntity<TaskTO> updateTask(@PathVariable Long id, @RequestBody TaskTO task) {
        log.info("Actualizando tarea con ID: {}", id);
        return ResponseEntity.ok(taskService.updateTask(id, task));
    }

    // PUT /api/task/{id}/status
    @PutMapping("/{id}/status")
    public ResponseEntity<TaskTO> updateStatus(@PathVariable Long id, @RequestParam TaskStatusEnum status) {
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

    // GET /api/task/archived
    @GetMapping("/archived")
    public ResponseEntity<List<TaskTO>> getArchivedTasks() {
        log.info("Obteniendo tareas archivadas");
        return ResponseEntity.ok(taskService.getArchivedTasks());
    }

    // PUT /api/task/{id}/archive
    @PutMapping("/{id}/archive")
    public ResponseEntity<TaskTO> archiveTask(@PathVariable Long id) {
        log.info("Archivando tarea con ID: {}", id);
        return ResponseEntity.ok(taskService.archiveTask(id));
    }

    // PUT /api/task/archive-all-done
    @PutMapping("/archive-all-done")
    public ResponseEntity<Void> archiveAllDone() {
        int count = taskService.archiveAllDone();
        log.info("Archivadas {} tareas completadas", count);
        return ResponseEntity.noContent().build();
    }

    // PUT /api/task/{id}/restore
    @PutMapping("/{id}/restore")
    public ResponseEntity<TaskTO> restoreTask(@PathVariable Long id) {
        log.info("Restaurando tarea con ID: {}", id);
        return ResponseEntity.ok(taskService.restoreTask(id));
    }

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }
}