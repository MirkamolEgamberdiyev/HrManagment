package pdp.uz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pdp.uz.entity.Role;
import pdp.uz.entity.enums.RoleName;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    Role findByRoleName(RoleName roleHrManager);
}
