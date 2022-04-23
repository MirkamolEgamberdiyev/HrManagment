package pdp.uz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pdp.uz.entity.Salary;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;


public interface SalaryRepository extends JpaRepository<Salary, Long> {

    List<Salary> findAllByUpdatedAtBetweenAndEmployeeId(Timestamp fromDate, Timestamp toDate, UUID id);

    List<Salary> findAllByVerifyingCode(String emailCode);
}
