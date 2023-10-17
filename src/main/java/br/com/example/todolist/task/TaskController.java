package br.com.example.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.example.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {
  
  @Autowired
  ITaskRepository repository;

  @PostMapping("/")
  public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
    UUID idUser = (UUID) request.getAttribute("idUser");
    taskModel.setIdUser(idUser);

    LocalDateTime currentDate = LocalDateTime.now();

    if(currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de início / data de término deve ser maior do que a data atual");
    }

    if(taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de início deve ser menor do que a data de término");
    }

    TaskModel taskCreated = repository.save(taskModel);
    return ResponseEntity.status(HttpStatus.OK).body(taskCreated);
  }

  @GetMapping("/")
  public List<TaskModel> list(HttpServletRequest request) {
    UUID idUser = (UUID) request.getAttribute("idUser");
    List<TaskModel> tasks = repository.findByIdUser(idUser);
    return tasks;
  }

  @PutMapping("/{id}")
  public ResponseEntity<Object> update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id) {
    UUID idUser = (UUID) request.getAttribute("idUser");

    TaskModel task = repository.findById(id).orElse(null);

    if(task == null) {
      return ResponseEntity.badRequest().body("Tarefa não encontrada");
    }

    if(!task.getIdUser().equals(idUser)) {
      return ResponseEntity.badRequest().body("Usuário não tem permissão para alterar essa tarefa");
    }

    Utils.copyNonNullProperties(taskModel, task);
    return ResponseEntity.ok().body(task);
  }
}
