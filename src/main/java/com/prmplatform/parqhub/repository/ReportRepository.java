package com.prmplatform.parqhub.repository;

import com.prmplatform.parqhub.model.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    Page<Report> findByType(Report.ReportType type, Pageable pageable);
}
