package ro.sapientia.furniture.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder; // 🔥 ÚJ IMPORT
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ro.sapientia.furniture.dto.request.CreateProjectRequest;
import ro.sapientia.furniture.dto.request.UpdateProjectRequest;
import ro.sapientia.furniture.dto.request.CreateFurnitureBodyRequest;
import ro.sapientia.furniture.dto.response.CreateProjectResponse;
import ro.sapientia.furniture.dto.response.FurnitureBodyResponse;
import ro.sapientia.furniture.dto.response.ProjectDetailsResponse;
import ro.sapientia.furniture.dto.response.ProjectListItemResponse;
import ro.sapientia.furniture.dto.response.ProjectVersionResponse;
import ro.sapientia.furniture.dto.response.UpdateProjectResponse;
import ro.sapientia.furniture.exception.EntityNotFoundException;
import ro.sapientia.furniture.exception.ServiceUnavailableException;
import ro.sapientia.furniture.exception.ResourceNotFoundException;
import ro.sapientia.furniture.model.AppUser; // 🔥 ÚJ IMPORT
import ro.sapientia.furniture.model.FurnitureBody;
import ro.sapientia.furniture.model.Project;
import ro.sapientia.furniture.model.ProjectVersion;
import ro.sapientia.furniture.repository.AppUserRepository; // 🔥 ÚJ IMPORT
import ro.sapientia.furniture.repository.ProjectRepository;
import ro.sapientia.furniture.repository.ProjectVersionRepository;

@Service
public class ProjectService {

    private static final int PAGE_SIZE = 10;

    private final ProjectRepository projectRepository;
    private final ProjectVersionRepository projectVersionRepository;
    private final AppUserRepository appUserRepository; // 🔥 ÚJ DEPENDENCY

    // 🔥 Konstruktor bővítése az AppUserRepository-val
    public ProjectService(ProjectRepository projectRepository, 
                          ProjectVersionRepository projectVersionRepository,
                          AppUserRepository appUserRepository) {
        this.projectRepository = projectRepository;
        this.projectVersionRepository = projectVersionRepository;
        this.appUserRepository = appUserRepository;
    }

    // 🔥 SEGÉD METÓDUS: A bejelentkezett felhasználó lekérése
    private AppUser getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    // --------------------
    // CREATE PROJECT
    // --------------------
    @Transactional
    public CreateProjectResponse createProject(CreateProjectRequest request) {
        LocalDateTime now = LocalDateTime.now();
        AppUser currentUser = getCurrentUser(); // 🔥 Tulajdonos lekérése

        // 1. Project
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setCreatedAt(now);
        project.setUpdatedAt(now);
        project.setDeletedAt(null);
        project.setOwner(currentUser); // 🔥 Tulajdonos beállítása!

        // 2. Initial version
        ProjectVersion version = new ProjectVersion();
        version.setVersionNumber(1);
        version.setSavedAt(now);
        version.setVersionNote("Initial version");
        version.setProject(project);
        version.setName(project.getName());
        version.setDescription(project.getDescription());

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
        List<ProjectVersionResponse> versionResponses = savedProject.getVersions().stream()
                .map(this::mapVersionToResponse) // Kiszerveztem a mappinget a tisztább kódért (lásd lent)
                .collect(Collectors.toList());

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
            AppUser currentUser = getCurrentUser(); // 🔥

            Pageable pageable = PageRequest.of(
                    page,
                    PAGE_SIZE,
                    Sort.by("createdAt").descending()
            );

            // 🔥 CSAK a bejelentkezett felhasználó projektjeit kérjük le
            Page<Project> projects = projectRepository.findByOwnerAndDeletedAtIsNull(currentUser, pageable);

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
        AppUser currentUser = getCurrentUser(); // 🔥

        // 🔥 Biztonsági ellenőrzés: csak akkor adjuk vissza, ha az övé
        Project project = projectRepository
                .findByIdAndOwnerAndDeletedAtIsNull(id, currentUser)
                .orElseThrow(() ->
                        new EntityNotFoundException("Project not found with id: " + id)
                );

        List<ProjectVersionResponse> versionResponses = project.getVersions().stream()
                .map(this::mapVersionToResponse)
                .collect(Collectors.toList());

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
        AppUser currentUser = getCurrentUser(); // 🔥

        // 🔥 Csak saját projektet módosíthat
        Project project = projectRepository.findByIdAndOwnerAndDeletedAtIsNull(id, currentUser)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        // 1. Create new version WITH bodies
        createSnapshotWithBodies(project, request.getName(), request.getDescription(), request.getBodies());

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

    // --------------------
    // RESTORE VERSION
    // --------------------
    @Transactional
    public Project restoreVersion(Long projectId, Long versionId) {
        AppUser currentUser = getCurrentUser(); // 🔥

        // 🔥 Biztonsági check
        Project project = projectRepository.findByIdAndOwnerAndDeletedAtIsNull(projectId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // A verzió ellenőrzésénél is érdemes figyelni, hogy a verzió tényleg ehhez a projekthez tartozik-e
        ProjectVersion version = projectVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Version not found"));
        
        if (!version.getProject().getId().equals(project.getId())) {
             throw new ResourceNotFoundException("Version does not belong to this project");
        }

        version.getBodies().size(); // Force load

        List<CreateFurnitureBodyRequest> restoredBodies = version.getBodies().stream()
                .map(body -> {
                    CreateFurnitureBodyRequest req = new CreateFurnitureBodyRequest();
                    req.setWidth(body.getWidth());
                    req.setHeigth(body.getHeigth());
                    req.setDepth(body.getDepth());
                    return req;
                })
                .collect(Collectors.toList());

        createSnapshotWithBodies(project, version.getName(), version.getDescription(), restoredBodies);

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
            AppUser currentUser = getCurrentUser(); // 🔥
            
            // 🔥 Csak sajátot törölhet
            Project project = projectRepository.findByIdAndOwnerAndDeletedAtIsNull(projectId, currentUser)
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

            project.setDeletedAt(LocalDateTime.now());
            projectRepository.save(project);

            return true;
        } catch (DataAccessException ex) {
            throw new ServiceUnavailableException("Database error");
        }
    }
    
    // --------------------
    // GET VERSIONS
    // --------------------
    public List<ProjectVersionResponse> getProjectVersions(Long projectId) {
        AppUser currentUser = getCurrentUser(); // 🔥

        // Verify project exists AND belongs to user
        if (projectRepository.findByIdAndOwnerAndDeletedAtIsNull(projectId, currentUser).isEmpty()) {
            throw new RuntimeException("Project not found");
        }

        return projectVersionRepository.findByProjectIdOrderByVersionNumberDesc(projectId)
                .stream()
                .map(this::mapVersionToResponse)
                .collect(Collectors.toList());
    }


    // --- PRIVÁT SEGÉD METÓDUSOK ---

    private void createSnapshotWithBodies(Project project, String name, String description, List<CreateFurnitureBodyRequest> bodies) {
        List<ProjectVersion> versions = projectVersionRepository
                .findByProjectIdOrderByVersionNumberAsc(project.getId());

        if (versions.size() >= 10) {
            ProjectVersion oldestVersion = versions.get(0);
            project.getVersions().remove(oldestVersion);
            projectVersionRepository.delete(oldestVersion);
            projectVersionRepository.flush();
        }

        int nextVersionNumber = versions.isEmpty() ? 1 :
                versions.get(versions.size() - 1).getVersionNumber() + 1;

        ProjectVersion newVersion = new ProjectVersion();
        newVersion.setProject(project);
        newVersion.setVersionNumber(nextVersionNumber);
        newVersion.setSavedAt(LocalDateTime.now());
        newVersion.setVersionNote("Update: " + name);
        newVersion.setName(name);
        newVersion.setDescription(description);

        if (bodies != null && !bodies.isEmpty()) {
            bodies.forEach(bodyReq -> {
                FurnitureBody body = new FurnitureBody();
                body.setWidth(bodyReq.getWidth());
                body.setHeigth(bodyReq.getHeigth());
                body.setDepth(bodyReq.getDepth());
                body.setVersion(newVersion);
                newVersion.getBodies().add(body);
            });
        } else if (!versions.isEmpty()) {
            ProjectVersion latestVersion = versions.get(versions.size() - 1);
            latestVersion.getBodies().forEach(existingBody -> {
                FurnitureBody bodyCopy = new FurnitureBody();
                bodyCopy.setWidth(existingBody.getWidth());
                bodyCopy.setHeigth(existingBody.getHeigth());
                bodyCopy.setDepth(existingBody.getDepth());
                bodyCopy.setVersion(newVersion);
                newVersion.getBodies().add(bodyCopy);
            });
        }

        project.getVersions().add(newVersion);
        projectVersionRepository.save(newVersion);
    }

    // Helper a response mappinghez, hogy ne ismételjük a kódot
    private ProjectVersionResponse mapVersionToResponse(ProjectVersion v) {
        return new ProjectVersionResponse(
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
        );
    }
}