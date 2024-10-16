package uni.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uni.backend.domain.MainCategory;

public interface MainCategoryRepository extends JpaRepository<MainCategory, Long> {
}
