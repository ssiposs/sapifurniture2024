package ro.sapientia.furniture.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ro.sapientia.furniture.model.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Project findProjectById(Long id);

}
