package com.mercans.integration_api.jpa.repository;

import com.mercans.integration_api.jpa.JsonResponseEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JsonResponseRepository extends JpaRepository<JsonResponseEntity, UUID> {}
