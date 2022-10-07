package internship.lhind.repository;

import internship.lhind.model.entity.MenuType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuTypeRepository extends JpaRepository<MenuType, Integer> {
    MenuType findMenuTypeByMenuType(String menuType);
}
