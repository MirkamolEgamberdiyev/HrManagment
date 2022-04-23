package pdp.uz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pdp.uz.entity.TourniquetHistory;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface TourniquetHistoryRepository extends JpaRepository<TourniquetHistory, UUID> {

    List<TourniquetHistory> findAllByExitedAtBetween(Timestamp fromDate, Timestamp toDate);

    Collection<? extends TourniquetHistory> findAllByEnteredAtBetween(Timestamp fromDate, Timestamp toDate);
}
