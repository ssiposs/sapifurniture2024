package ro.sapientia.furniture.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;

import ro.sapientia.furniture.exception.ResourceNotFoundException;
import ro.sapientia.furniture.exception.ServiceUnavailableException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ro.sapientia.furniture.dto.response.CreateProjectResponse;
import ro.sapientia.furniture.dto.response.ProjectVersionResponse;
import ro.sapientia.furniture.model.Project;
import ro.sapientia.furniture.model.ProjectVersion;
import ro.sapientia.furniture.repository.ProjectRepository;

public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // @Test
    // void testCreateProject() {
    //     String projectName = "Test Project";
    //     String projectDescription = "This is a test project.";
    //     LocalDateTime now = LocalDateTime.now();

    //     Project project = new Project();
    //     project.setId(1L);
    //     project.setName(projectName);
    //     project.setDescription(projectDescription);
    //     project.setCreatedAt(now);
    //     project.setUpdatedAt(now);

    //     ProjectVersion version = new ProjectVersion();
    //     version.setId(1L);
    //     version.setVersionNumber(1);
    //     version.setSavedAt(now);
    //     version.setVersionNote("Initial version");
    //     version.setProject(project);
    //     project.getVersions().add(version);

    //     when(projectRepository.save(any(Project.class))).thenReturn(project);

    //     CreateProjectResponse result = projectService.createProject(projectName, projectDescription);

    //     assertNotNull(result);
    //     assertEquals(1L, result.getId());
    //     assertEquals(projectName, result.getName());
    //     assertEquals(projectDescription, result.getDescription());
    //     assertNotNull(result.getCreatedAt());
    //     assertEquals(1, result.getVersions().size());
    //     assertEquals(1, result.getVersions().get(0).getVersionNumber());

    //     verify(projectRepository, times(1)).save(any(Project.class));
    // }

    @Test
    void deleteProject_Success_ShouldSoftDelete() {
        // Arrange
        Long projectId = 1L;
        Project mockProject = new Project();
        mockProject.setId(projectId);
        mockProject.setDeletedAt(null); // Initially active

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(mockProject));

        // Act
        Boolean result = projectService.deleteProject(projectId);

        // Assert
        assertTrue(result);
        assertNotNull(mockProject.getDeletedAt(), "deletedAt should be set to current timestamp");
        verify(projectRepository).save(mockProject); // Verify save was called
    }

    @Test
    void deleteProject_NotFound_ShouldThrowException() {
        // Arrange
        Long projectId = 99L;
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            projectService.deleteProject(projectId);
        });

        verify(projectRepository, never()).save(any());
    }

    @Test
    void deleteProject_DatabaseError_ShouldThrowServiceUnavailable() {
        // Arrange
        Long projectId = 1L;
        when(projectRepository.findById(projectId))
            .thenThrow(new DataIntegrityViolationException("DB Error")); // Simulating DataAccessException

        // Act & Assert
        assertThrows(ServiceUnavailableException.class, () -> {
            projectService.deleteProject(projectId);
        });
    }
}
