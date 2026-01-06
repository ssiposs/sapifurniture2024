package ro.sapientia.furniture.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import ro.sapientia.furniture.model.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Project findProjectById(Long id);

    Page<Project> findByDeletedAtIsNull(Pageable pageable);
}
