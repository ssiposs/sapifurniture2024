package ro.sapientia.furniture.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ro.sapientia.furniture.dto.response.CreateProjectResponse;
import ro.sapientia.furniture.dto.response.FurnitureBodyResponse;
import ro.sapientia.furniture.dto.response.ProjectListItemResponse;
import ro.sapientia.furniture.dto.response.ProjectVersionResponse;
import ro.sapientia.furniture.exception.ServiceUnavailableException;
import ro.sapientia.furniture.model.Project;
import ro.sapientia.furniture.model.ProjectVersion;
import ro.sapientia.furniture.repository.ProjectRepository;

@Service
public class ProjectService {

    private static final int PAGE_SIZE = 10;

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    // --------------------
    // CREATE PROJECT
    // --------------------
    @Transactional
    public CreateProjectResponse createProject(String name, String description) {
        LocalDateTime now = LocalDateTime.now();

        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setCreatedAt(now);
        project.setUpdatedAt(now);
        project.setDeletedAt(null);

        ProjectVersion version = new ProjectVersion();
        version.setVersionNumber(1);
        version.setSavedAt(now);
        version.setVersionNote("Initial version");
        version.setProject(project);

        project.getVersions().add(version);

        Project savedProject = projectRepository.save(project);

        List<ProjectVersionResponse> versionResponses = savedProject.getVersions().stream()
                .map(v -> new ProjectVersionResponse(
                        v.getId(),
                        v.getVersionNumber(),
                        v.getSavedAt(),
                        v.getVersionNote(),
                        v.getBodies().stream()
                                .map(b -> new FurnitureBodyResponse(
                                        b.getId(),
                                        b.getWidth(),
                                        b.getHeigth(),
                                        b.getDepth()
                                ))
                                .toList()
                ))
                .toList();

        return new CreateProjectResponse(
                savedProject.getId(),
                savedProject.getName(),
                savedProject.getDescription(),
                savedProject.getCreatedAt(),
                savedProject.getUpdatedAt(),
                savedProject.getDeletedAt(),
                versionResponses
        );
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

            Page<Project> projects = projectRepository.findByDeletedAtIsNull(pageable);

            return projects.map(p -> new ProjectListItemResponse(
                    p.getId(),
                    p.getName(),
                    p.getDescription(),
                    p.getCreatedAt()
            ));

        } catch (DataAccessException ex) {
            throw new ServiceUnavailableException("Database error");
        }
    }
}
