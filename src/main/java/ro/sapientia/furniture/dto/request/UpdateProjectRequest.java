package ro.sapientia.furniture.dto.request;

import javax.validation.constraints.NotBlank;

public class UpdateProjectRequest {
    @NotBlank
    private String name;
    private String description;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}