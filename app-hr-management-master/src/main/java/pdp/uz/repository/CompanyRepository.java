package pdp.uz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pdp.uz.entity.Company;


public interface CompanyRepository extends JpaRepository<Company, Integer> {

    @Query(nativeQuery = true, value = "select count(*)>0 from employee t\n" +
            "join company c on c.id = t.company_id\n" +
            "join employee_roles er on t.id = er.employee_id\n" +
            "where er.roles_id=2 and c.id =:companyId")
    boolean hasDirector(Long companyId);

    boolean existsById(Long companyId);

    Company getById(Long companyId);
}
