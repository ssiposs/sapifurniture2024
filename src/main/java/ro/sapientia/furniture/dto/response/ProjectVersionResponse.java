package ro.sapientia.furniture.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class ProjectVersionResponse {

    private Long id;
    private int versionNumber;
    private LocalDateTime savedAt;
    private String versionNote;
    private List<FurnitureBodyResponse> bodies;

    // Constructors
    public ProjectVersionResponse() {
    }

    public ProjectVersionResponse(Long id, int versionNumber, LocalDateTime savedAt, String versionNote, List<FurnitureBodyResponse> bodies) {
        this.id = id;
        this.versionNumber = versionNumber;
        this.savedAt = savedAt;
        this.versionNote = versionNote;
        this.bodies = bodies;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public LocalDateTime getSavedAt() {
        return savedAt;
    }

    public void setSavedAt(LocalDateTime savedAt) {
        this.savedAt = savedAt;
    }

    public String getVersionNote() {
        return versionNote;
    }

    public void setVersionNote(String versionNote) {
        this.versionNote = versionNote;
    }

    public List<FurnitureBodyResponse> getBodies() {
        return bodies;
    }

    public void setBodies(List<FurnitureBodyResponse> bodies) {
        this.bodies = bodies;
    }

    @Override
    public String toString() {
        return "ProjectVersionResponse{" +
                "id=" + id +
                ", versionNumber=" + versionNumber +
                ", savedAt=" + savedAt +
                ", versionNote='" + versionNote + '\'' +
                ", bodies=" + bodies +
                '}';
    }
}
