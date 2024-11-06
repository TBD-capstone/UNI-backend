package uni.backend.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import uni.backend.domain.Ad;

public interface AdRepository extends JpaRepository<Ad, Integer> {

    Optional<Ad> findByAdvertiser(String advertiser);
}
