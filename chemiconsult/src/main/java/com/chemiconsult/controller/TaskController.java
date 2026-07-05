package com.chemiconsult.controller;

import com.chemiconsult.enums.TaskStatus;
import com.chemiconsult.service.TaskService;
import com.chemiconsult.to.TaskTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@RestController
@RequestMapping("/api/task")
public class TaskController {

    TaskService taskService;

    @GetMapping
    public List<TaskTO> getAllTasks() {
        return taskService.getAllTasks();
    }

    @PostMapping
    public TaskTO createTask(@RequestBody TaskTO task) {
        return taskService.createTask(task);
    }

    @PutMapping("/{id}/status")
    public TaskTO updateStatus(@PathVariable Long id, @RequestParam TaskStatus status) {
        return taskService.updateStatus(id, status);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }


    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

}
