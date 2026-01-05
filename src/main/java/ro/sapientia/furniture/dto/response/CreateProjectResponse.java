package ro.sapientia.furniture.dto.response;

public class CreateProjectResponse {

    private Long id;

    public CreateProjectResponse(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
