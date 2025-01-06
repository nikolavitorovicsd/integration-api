package com.mercans.integration_api.jpa.repository;

import com.mercans.integration_api.jpa.JsonResponseEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JsonResponseRepository extends JpaRepository<JsonResponseEntity, UUID> {}
