package rs.milosnikolic.todoapp.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.milosnikolic.todoapp.api.model.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
