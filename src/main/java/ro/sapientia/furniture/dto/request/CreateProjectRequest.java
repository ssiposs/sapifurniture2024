package ro.sapientia.furniture.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class CreateProjectRequest {

    @NotBlank(message = "Project name is required.")
    @Size(max = 255, message = "Project name must be less than 255 characters.")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
