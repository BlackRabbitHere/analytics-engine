package com.analytics.analytics_engine.repository;

import com.analytics.analytics_engine.model.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeadRepository extends JpaRepository<Lead,Long> {


}
