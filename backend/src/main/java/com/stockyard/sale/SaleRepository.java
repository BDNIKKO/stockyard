package com.stockyard.sale;

import com.stockyard.dashboard.DashboardDtos.ChannelStat;
import com.stockyard.dashboard.DashboardDtos.Totals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long>, JpaSpecificationExecutor<Sale> {
    boolean existsByChannelId(Long channelId);

    @Query("""
            select new com.stockyard.dashboard.DashboardDtos$Totals(
                sum(s.quantity * s.unitPrice), sum(s.quantity), sum(s.quantity * s.unitCost))
            from Sale s
            where s.maker.id = :makerId and s.soldAt >= :from and s.soldAt < :to""")
    Totals totals(@Param("makerId") Long makerId, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

    @Query("""
            select new com.stockyard.dashboard.DashboardDtos$ChannelStat(
                c.id, c.name, sum(s.quantity * s.unitPrice), sum(s.quantity), sum(s.quantity * s.unitCost))
            from Sale s join s.channel c
            where s.maker.id = :makerId and s.soldAt >= :from and s.soldAt < :to
            group by c.id, c.name
            order by sum(s.quantity * s.unitPrice) desc""")
    List<ChannelStat> byChannel(@Param("makerId") Long makerId, @Param("from") OffsetDateTime from,
                                @Param("to") OffsetDateTime to);
}
