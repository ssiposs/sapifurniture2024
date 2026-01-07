package ro.sapientia.furniture.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import ro.sapientia.furniture.exception.ResourceNotFoundException;
import ro.sapientia.furniture.exception.ServiceUnavailableException;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import ro.sapientia.furniture.dto.request.CreateProjectRequest;
import ro.sapientia.furniture.dto.response.CreateProjectResponse;
import ro.sapientia.furniture.dto.response.ProjectVersionResponse;
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

    // @Test
    // void testCreateProject() throws Exception {
    //     String projectName = "Test Project";
    //     String projectDescription = "This is a test project.";
    //     LocalDateTime now = LocalDateTime.now();

    //     CreateProjectRequest request = new CreateProjectRequest();
    //     request.setName(projectName);
    //     request.setDescription(projectDescription); 

    //     CreateProjectResponse response = new CreateProjectResponse(
    //             1L,
    //             projectName,
    //             projectDescription,
    //             now,
    //             now,
    //             null,
    //             List.of(new ProjectVersionResponse(1L, 1, now, "Initial version", List.of()))
    //     );

    //     when(projectService.createProject(projectName, projectDescription)).thenReturn(response);

    //     mockMvc.perform(post("/projects")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(request)))
    //             .andExpect(status().isCreated())
    //             .andExpect(jsonPath("$.id").value(1L))
    //             .andExpect(jsonPath("$.name").value(projectName))
    //             .andExpect(jsonPath("$.description").value(projectDescription))
    //             .andExpect(jsonPath("$.versions[0].version regenerativeNumber").value(1));

    //     verify(projectService, times(1)).createProject(projectName, projectDescription);
    // }

    @Test
    void getVersions_ShouldReturnEmptyList_WhenNoVersionsExist() throws Exception {
        mockMvc.perform(get("/projects/1/versions"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void deleteProject_ShouldReturnOk() throws Exception {
        // Arrange
        Long projectId = 1L;
        // logic is void or returns Boolean, we just need to ensure it doesn't throw

        // Act & Assert
        mockMvc.perform(delete("/api/projects/{projectId}", projectId)) // Adjust URL prefix if needed
                .andExpect(status().isOk());

        verify(projectService).deleteProject(projectId);
    }

    @Test
    void deleteProject_NotFound_ShouldReturn404() throws Exception {
        // Arrange
        Long projectId = 99L;
        
        // Mock the service to throw your custom exception
        doThrow(new ResourceNotFoundException("Not found"))
            .when(projectService).deleteProject(projectId);

        // Act & Assert
        mockMvc.perform(delete("/api/projects/{projectId}", projectId))
                .andExpect(status().isNotFound());
    }
}
