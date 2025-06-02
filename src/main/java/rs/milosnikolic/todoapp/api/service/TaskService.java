package rs.milosnikolic.todoapp.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.milosnikolic.todoapp.api.model.Task;
import rs.milosnikolic.todoapp.api.repository.TaskRepository;

import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository repo;

    public List<Task> getAll() {
        return repo.findAll();
    }

    public Task save(Task task) {
        return repo.save(task);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public Task toggle(long id) {
        Task task = repo.findById(id).orElseThrow();
        task.setCompleted(!task.isCompleted());
        return repo.save(task);
    }

    public Task updateTitle(Long id, String newTitle) {
        Task task = repo.findById(id).orElseThrow();
        task.setTitle(newTitle);
        return repo.save(task);
    }
}
