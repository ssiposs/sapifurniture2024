package ro.sapientia.furniture.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ro.sapientia.furniture.model.Project;
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

    @Test
    void testCreateProject() {
        String projectName = "Test Project";

        // Mockolt repository válasz
        Project savedProject = new Project();
        savedProject.setId(1L);
        savedProject.setName(projectName);
        savedProject.setCreatedAt(LocalDateTime.now());

        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

        // Metódus meghívása
        Project result = projectService.createProject(projectName);

        // Ellenőrzések
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(projectName, result.getName());
        assertNotNull(result.getCreatedAt());

        // Ellenőrizzük, hogy a repository mentés tényleg meghívódott
        verify(projectRepository, times(1)).save(any(Project.class));
    }
}
