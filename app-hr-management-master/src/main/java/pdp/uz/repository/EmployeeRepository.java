package pdp.uz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pdp.uz.entity.Employee;


import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    Optional<Employee> findByEmail(String email);

    List<Employee> findAllByCompanyIdAndEnabledTrue(Long id);

    boolean existsByEmail(String email);

    Optional<Employee> findByEmailAndEmailCode(String email, String emailCode);

    Optional<Employee> findByEmailAndEnabledTrue(String email);

    @Query(value = "select * from employee t\n" +
            "join company c on c.id = t.company_id\n" +
            "join employee_roles er on t.id = er.employee_id\n" +
            "where er.roles_id = 2 and company_id =:company_id", nativeQuery = true)
    Optional<Employee> findCompanyDirector(Long company_id);

    @Query(value = "select count(*) > 0\n" +
            "from employee_roles t\n" +
            "         join employee e on e.id = t.employee_id\n" +
            "where roles_id = 2\n" +
            "  and e.email =:email", nativeQuery = true)
    boolean isDirector(String email);

    @Query(nativeQuery = true, value = "select count(*)>0 from employee t\n" +
            "join employee_roles er on t.id = er.employee_id\n" +
            "where roles_id = 3 and email =:email")
    boolean isManager(String email);

    @Query(nativeQuery = true, value = "select count(*)>0 from employee t\n" +
            "join employee_roles er on t.id = er.employee_id\n" +
            "where roles_id = 4 and email =:email")
    boolean isWorker(String email);
}
