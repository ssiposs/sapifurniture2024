package ro.sapientia.furniture.service;

import java.time.LocalDateTime;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ro.sapientia.furniture.dto.response.ProjectListItemResponse;
import ro.sapientia.furniture.exception.ServiceUnavailableException;
import ro.sapientia.furniture.model.Project;
import ro.sapientia.furniture.repository.ProjectRepository;

@Service
public class ProjectService {

    private static final int PAGE_SIZE = 10; // később lehet 100

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    // --------------------
    // CREATE PROJECT
    // --------------------
    @Transactional
    public Project createProject(String name) {

        Project project = new Project();
        project.setName(name);
        project.setCreatedAt(LocalDateTime.now());

        return projectRepository.save(project);
    }

    // --------------------
    // GET PROJECTS (paged)
    // --------------------
    public Page<ProjectListItemResponse> getProjects(int page) {

        try {
            Pageable pageable = PageRequest.of(
                    page,
                    PAGE_SIZE,
                    Sort.by("createdAt").descending()
            );

            Page<Project> projects =
                    projectRepository.findByDeletedAtIsNull(pageable);

            return projects.map(p ->
                    new ProjectListItemResponse(
                            p.getId(),
                            p.getName(),
                            p.getCreatedAt()
                    )
            );

        } catch (DataAccessException ex) {
            // DB hiba → 503
            throw new ServiceUnavailableException("Database error");
        }
    }
}
