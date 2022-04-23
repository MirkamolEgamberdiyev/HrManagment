package pdp.uz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pdp.uz.entity.Task;
import pdp.uz.entity.enums.TaskStatus;

import java.util.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;


public interface TaskRepository extends JpaRepository<Task, Long> {


    List<Task> findAllByDeadlineBeforeAndEmployee_EmailAndTaskStatus(Date deadline, String employee_email, TaskStatus taskStatus);

    List<Task> findAllByCompletedAtBetweenAndEmployee_Email(Timestamp fromDate, Timestamp toDate, String email);

    List<Task> findAllByCompletedAtBetweenAndTaskStatusAndEmployee_Email(Timestamp completedAt, Timestamp completedAt2, TaskStatus taskStatus, String employee_email);

    Optional<Task> findByTaskCode(String taskCode);
}
