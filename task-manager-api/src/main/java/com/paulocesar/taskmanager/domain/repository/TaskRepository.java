package com.paulocesar.taskmanager.domain.repository;

import com.paulocesar.taskmanager.domain.entity.Task;
import com.paulocesar.taskmanager.domain.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByOwnerId(Long ownerId, Pageable pageable);

    Page<Task> findByOwnerIdAndStatus(Long ownerId, TaskStatus status, Pageable pageable);

    @Query("SELECT t FROM Task t JOIN FETCH t.owner WHERE t.id = :id")
    java.util.Optional<Task> findByIdWithOwner(@Param("id") Long id);

    long countByOwnerIdAndStatus(Long ownerId, TaskStatus status);
}
