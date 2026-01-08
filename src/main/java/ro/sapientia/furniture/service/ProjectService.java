package ro.sapientia.furniture.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ro.sapientia.furniture.dto.request.CreateProjectRequest;
import ro.sapientia.furniture.dto.request.UpdateProjectRequest;
import ro.sapientia.furniture.dto.response.CreateProjectResponse;
import ro.sapientia.furniture.dto.response.FurnitureBodyResponse;
import ro.sapientia.furniture.dto.response.ProjectDetailsResponse;
import ro.sapientia.furniture.dto.response.ProjectListItemResponse;
import ro.sapientia.furniture.dto.response.ProjectVersionResponse;
import ro.sapientia.furniture.exception.EntityNotFoundException;
import ro.sapientia.furniture.dto.response.UpdateProjectResponse;
import ro.sapientia.furniture.exception.ServiceUnavailableException;
import ro.sapientia.furniture.exception.ResourceNotFoundException;
import ro.sapientia.furniture.model.FurnitureBody;

import ro.sapientia.furniture.model.Project;
import ro.sapientia.furniture.model.ProjectVersion;
import ro.sapientia.furniture.repository.ProjectRepository;
import ro.sapientia.furniture.repository.ProjectVersionRepository;

@Service
public class ProjectService {

    private static final int PAGE_SIZE = 10;

    private final ProjectRepository projectRepository;
    private final ProjectVersionRepository projectVersionRepository;

    public ProjectService(ProjectRepository projectRepository, ProjectVersionRepository projectVersionRepository) {
        this.projectRepository = projectRepository;
        this.projectVersionRepository = projectVersionRepository;
    }

    // --------------------
    // CREATE PROJECT
    // --------------------
    @Transactional
        public CreateProjectResponse createProject(CreateProjectRequest request) {
        LocalDateTime now = LocalDateTime.now();

        // 1. Project
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setCreatedAt(now);
        project.setUpdatedAt(now);
        project.setDeletedAt(null);

        // 2. Initial version
        ProjectVersion version = new ProjectVersion();
        version.setVersionNumber(1);
        version.setSavedAt(now);
        version.setVersionNote("Initial version");
        version.setProject(project);
        version.setName(project.getName());        // <= fontos
        version.setDescription(project.getDescription()); // ha van

        project.getVersions().add(version);

        // 3. OPTIONAL Furniture bodies
        if (request.getBodies() != null && !request.getBodies().isEmpty()) {
                request.getBodies().forEach(bodyReq -> {
                FurnitureBody body = new FurnitureBody();
                body.setWidth(bodyReq.getWidth());
                body.setHeigth(bodyReq.getHeigth());
                body.setDepth(bodyReq.getDepth());
                body.setVersion(version);

                version.getBodies().add(body);
                });
        }

        Project savedProject = projectRepository.save(project);

        // 4. Response mapping
        List<ProjectVersionResponse> versionResponses =
        savedProject.getVersions().stream()
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
                                .toList(),
                        v.getName(),
                        v.getDescription()
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

    // --------------------
    // GET PROJECT BY ID
    // --------------------

   @Transactional(readOnly = true)
    public ProjectDetailsResponse getProjectById(Long id) {

        Project project = projectRepository
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Project not found with id: " + id)
                );

        List<ProjectVersionResponse> versionResponses =
                project.getVersions().stream()
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
                                        .toList(),
                                v.getName(),
                                v.getDescription()
                        ))
                        .toList();

        return new ProjectDetailsResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                project.getDeletedAt(),
                versionResponses
        );
    }

    // --------------------
    // UPDATE PROJECT
    // --------------------
    @Transactional
    public UpdateProjectResponse updateProject(Long id, UpdateProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // 1. Handle Versioning before updating the main project
        createSnapshot(project);

        // 2. Update the actual project
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setUpdatedAt(LocalDateTime.now());

        Project updatedProject = projectRepository.save(project);

        return new UpdateProjectResponse(
                updatedProject.getId(),
                updatedProject.getName(),
                updatedProject.getDescription(),
                updatedProject.getUpdatedAt()
        );
    }

    private void createSnapshot(Project project) {
        List<ProjectVersion> versions = projectVersionRepository
                .findByProjectIdOrderByVersionNumberAsc(project.getId());

        // 3. Keep only the last 10: Delete the oldest if we're at the limit
        if (versions.size() >= 10) {
            projectVersionRepository.delete(versions.get(0));
        }

        // 4. Determine next version number
        int nextVersionNumber = versions.isEmpty() ? 1 : 
                versions.get(versions.size() - 1).getVersionNumber() + 1;

        // 5. Create new version entry
        ProjectVersion newVersion = new ProjectVersion();
        newVersion.setProject(project);
        newVersion.setVersionNumber(nextVersionNumber);
        newVersion.setSavedAt(LocalDateTime.now());
        newVersion.setVersionNote("Manual update to: " + project.getName());
        
        /* CRITICAL NOTE: 
           If you want to RESTORE the name and description later, 
           your ProjectVersion entity needs fields to store them! 
           Currently, your ProjectVersion only has 'versionNote'. 
           I recommend adding 'name' and 'description' columns to ProjectVersion.
        */

        newVersion.setName(project.getName());
        newVersion.setDescription(project.getDescription());

        projectVersionRepository.save(newVersion);
    }

    public List<ProjectVersionResponse> getProjectVersions(Long projectId) {
        // Verify project exists first
        if (!projectRepository.existsById(projectId)) {
            throw new RuntimeException("Project not found");
        }

        // Fetch versions using the repository method we'll define below
        return projectVersionRepository.findByProjectIdOrderByVersionNumberDesc(projectId)
                .stream()
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
                                .collect(Collectors.toList()),
                        v.getName(),
                        v.getDescription()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public Project restoreVersion(Long projectId, Long versionId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
                
        ProjectVersion version = projectVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Version not found"));

        // 1. Snapshot the CURRENT state as a new version before restoring
        createSnapshot(project); 

        // 2. Overwrite project fields with version data
        project.setName(version.getName());
        project.setDescription(version.getDescription());
        project.setUpdatedAt(LocalDateTime.now());

        return projectRepository.save(project);
    }

    // --------------------
    // DELETE PROJECT
    // --------------------
    @Transactional
    public Boolean deleteProject(Long projectId) {
        try {
            Project project = projectRepository.findById(projectId).orElseThrow(() -> { return new ResourceNotFoundException("Project not found"); });

            project.setDeletedAt(LocalDateTime.now());
            projectRepository.save(project);

            return true;
        } catch (DataAccessException ex) {
            throw new ServiceUnavailableException("Database error");
        }
    }
}
