package com.chemiconsult.service;

import com.chemiconsult.brevo.service.BrevoEmailService;
import com.chemiconsult.entity.TaskDE;
import com.chemiconsult.entity.UserDE;
import com.chemiconsult.enums.TaskStatusEnum;
import com.chemiconsult.mapper.TaskMapper;
import com.chemiconsult.repository.TaskRepository;
import com.chemiconsult.repository.UserRepository;
import com.chemiconsult.to.TaskTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final BrevoEmailService emailService;

    public List<TaskTO> getAllTasks() {
        return TaskMapper.mapTaskEntityToTO(taskRepository.findAll());
    }

    public TaskTO createTask(TaskTO task) {
        TaskDE entity = TaskMapper.mapTaskTOToEntity(task);
        if (task.getUserId() != null) {
            UserDE user = userRepository.findById(task.getUserId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + task.getUserId()));
            entity.setUser(user);
        }
        TaskDE creada = taskRepository.save(entity);

        if (creada.getUser() != null) {
            notificarAsignacion(creada, "Nueva tarea asignada");
        }

        return TaskMapper.mapTaskEntityToTO(creada);
    }

    // Edición completa: título, descripción y asignado. Notifica por mail
    // solo si el usuario asignado cambió respecto al que tenía antes.
    public TaskTO updateTask(Long id, TaskTO to) {
        TaskDE tarea = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada con ID: " + id));

        Long userAnteriorId = tarea.getUser() != null ? tarea.getUser().getId() : null;

        tarea.setTitle(to.getTitle());
        tarea.setDescription(to.getDescription());

        if (to.getUserId() != null) {
            UserDE nuevoUser = userRepository.findById(to.getUserId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + to.getUserId()));
            tarea.setUser(nuevoUser);
        } else {
            tarea.setUser(null);
        }

        TaskDE actualizada = taskRepository.save(tarea);

        boolean cambioAsignado = !Objects.equals(userAnteriorId, to.getUserId());
        if (cambioAsignado && actualizada.getUser() != null) {
            notificarAsignacion(actualizada, "Tarea reasignada a vos");
        }

        return TaskMapper.mapTaskEntityToTO(actualizada);
    }

    public TaskTO updateStatus(Long id, TaskStatusEnum status) {
        TaskDE task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada con ID: " + id));
        TaskMapper.applyStatus(task, status);
        TaskDE actualizada = taskRepository.save(task);
        return TaskMapper.mapTaskEntityToTO(actualizada);
    }

    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new RuntimeException("Tarea no encontrada con ID: " + id);
        }
        taskRepository.deleteById(id);
    }

    private void notificarAsignacion(TaskDE tarea, String asunto) {
        String html = """
                <div style="font-family: -apple-system, sans-serif; max-width: 480px; padding: 24px; border: 1px solid #e2e8f0; border-radius: 12px;">
                    <h2 style="color:#5EA504; margin: 0 0 16px 0; font-size: 18px;">%s</h2>

                    <div style="margin-bottom: 14px;">
                        <p style="margin: 0 0 4px 0; font-size: 12px; font-weight: 600; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.5px;">Título</p>
                        <p style="margin: 0; font-size: 15px; color: #0f172a; font-weight: 600;">%s</p>
                    </div>

                    <div style="margin-bottom: 14px;">
                        <p style="margin: 0 0 4px 0; font-size: 12px; font-weight: 600; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.5px;">Descripción</p>
                        <p style="margin: 0; font-size: 14px; color: #334155; line-height: 1.5;">%s</p>
                    </div>

                    <hr style="border: none; border-top: 1px solid #f1f5f9; margin: 20px 0 12px 0;">
                    <p style="margin: 0; font-size: 12px; color: #94a3b8;">Chemiconsult — Sistema de gestión</p>
                </div>
                """.formatted(
                asunto,
                tarea.getTitle(),
                tarea.getDescription() != null && !tarea.getDescription().isBlank()
                        ? tarea.getDescription()
                        : "<em style=\"color:#94a3b8;\">Sin descripción adicional</em>"
        );

        emailService.enviarMail(
                tarea.getUser().getEmail(),
                tarea.getUser().getUsername(),
                asunto,
                html
        );
    }

    @Autowired
    public TaskService(TaskRepository taskRepository, UserRepository userRepository, BrevoEmailService emailService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }
}