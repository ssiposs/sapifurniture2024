package ro.sapientia.furniture.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ro.sapientia.furniture.model.Project;
import ro.sapientia.furniture.repository.ProjectRepository;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional
    public Project createProject(String name) {
        Project project = new Project();
        project.setName(name);
        project.setCreatedAt(LocalDateTime.now());

        return projectRepository.save(project);
    }
}
