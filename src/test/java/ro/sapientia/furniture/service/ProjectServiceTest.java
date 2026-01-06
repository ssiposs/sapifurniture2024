// package ro.sapientia.furniture.service;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;

// import java.time.LocalDateTime;
// import java.util.List;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;

// import ro.sapientia.furniture.dto.response.CreateProjectResponse;
// import ro.sapientia.furniture.dto.response.ProjectVersionResponse;
// import ro.sapientia.furniture.model.Project;
// import ro.sapientia.furniture.model.ProjectVersion;
// import ro.sapientia.furniture.repository.ProjectRepository;

// public class ProjectServiceTest {

//     @Mock
//     private ProjectRepository projectRepository;

//     @InjectMocks
//     private ProjectService projectService;

//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);
//     }

//     @Test
//     void testCreateProject() {
//         String projectName = "Test Project";
//         String projectDescription = "This is a test project.";
//         LocalDateTime now = LocalDateTime.now();

//         Project project = new Project();
//         project.setId(1L);
//         project.setName(projectName);
//         project.setDescription(projectDescription);
//         project.setCreatedAt(now);
//         project.setUpdatedAt(now);

//         ProjectVersion version = new ProjectVersion();
//         version.setId(1L);
//         version.setVersionNumber(1);
//         version.setSavedAt(now);
//         version.setVersionNote("Initial version");
//         version.setProject(project);
//         project.getVersions().add(version);

//         when(projectRepository.save(any(Project.class))).thenReturn(project);

//         CreateProjectResponse result = projectService.createProject(projectName, projectDescription);

//         assertNotNull(result);
//         assertEquals(1L, result.getId());
//         assertEquals(projectName, result.getName());
//         assertEquals(projectDescription, result.getDescription());
//         assertNotNull(result.getCreatedAt());
//         assertEquals(1, result.getVersions().size());
//         assertEquals(1, result.getVersions().get(0).getVersionNumber());

//         verify(projectRepository, times(1)).save(any(Project.class));
//     }
// }
