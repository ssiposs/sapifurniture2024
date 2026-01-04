package ro.sapientia.furniture.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ro.sapientia.furniture.model.ProjectVersion;

public interface ProjectVersionRepository extends JpaRepository<ProjectVersion, Long> {

    ProjectVersion findProjectVersionById(Long id);

    List<ProjectVersion> findByProjectIdOrderByVersionNumberDesc(Long projectId);

}
