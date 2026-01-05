package ro.sapientia.furniture.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ro.sapientia.furniture.dto.request.CreateProjectRequest;
import ro.sapientia.furniture.model.Project;
import ro.sapientia.furniture.service.ProjectService;

class ProjectControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController projectController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(projectController).build();
    }

    @Test
    void testCreateProject() throws Exception {
        String projectName = "Test Project";

        CreateProjectRequest request = new CreateProjectRequest();
        request.setName(projectName);

        Project savedProject = new Project();
        savedProject.setId(1L);
        savedProject.setName(projectName);
        savedProject.setCreatedAt(LocalDateTime.now());

        // Mock the service call
        when(projectService.createProject(projectName)).thenReturn(savedProject);

        // Perform POST request with JSON body
        mockMvc.perform(post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())  // 201 CREATED
                .andExpect(jsonPath("$.id").value(1L));

        // Verify service call
        verify(projectService, times(1)).createProject(projectName);
    }
}
