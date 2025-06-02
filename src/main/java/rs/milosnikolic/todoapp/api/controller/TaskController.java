package rs.milosnikolic.todoapp.api.controller;

import org.springframework.web.bind.annotation.*;
import rs.milosnikolic.todoapp.api.model.Task;
import rs.milosnikolic.todoapp.api.service.TaskService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @GetMapping
    public List<Task> getAll() {
        return service.getAll();
    }

    @PostMapping
    public Task add(@RequestBody Task task) {
        return service.save(task);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PutMapping("/{id}/toggle")
    public Task toggle(@PathVariable Long id) {
        return service.toggle(id);
    }

    @PutMapping("/{id}")
    public Task updateTitle(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return service.updateTitle(id, body.get("title"));
    }
}
