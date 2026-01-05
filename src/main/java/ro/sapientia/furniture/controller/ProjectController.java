package ro.sapientia.furniture.controller;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ro.sapientia.furniture.dto.request.CreateProjectRequest;
import ro.sapientia.furniture.dto.response.CreateProjectResponse;
import ro.sapientia.furniture.model.Project;
import ro.sapientia.furniture.service.ProjectService;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<CreateProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request) {

        Project project = projectService.createProject(request.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new CreateProjectResponse(project.getId()));
    }
}
