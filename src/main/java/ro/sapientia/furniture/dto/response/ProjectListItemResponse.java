package ro.sapientia.furniture.dto.response;

import java.time.LocalDateTime;

public class ProjectListItemResponse {

    private Long id;
    private String name;
    private LocalDateTime createdAt;

    public ProjectListItemResponse(Long id, String name, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
