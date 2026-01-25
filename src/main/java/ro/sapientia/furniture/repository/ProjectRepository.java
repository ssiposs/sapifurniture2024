package ro.sapientia.furniture.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import ro.sapientia.furniture.model.AppUser;
import ro.sapientia.furniture.model.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    // --- EREDETI METÓDUSOK (Megmaradhatnak Admin funkciókhoz) ---
    // Ezek mindenkitől mindent visszaadnak, aki nincs törölve.
    Optional<Project> findByIdAndDeletedAtIsNull(Long id);
    Page<Project> findByDeletedAtIsNull(Pageable pageable);


    // --- ÚJ BIZTONSÁGOS METÓDUSOK (A ProjectService-hez) ---

    // 1. Listázás: Csak az adott tulajdonos (owner) projektjeit adja vissza
    // SQL: SELECT * FROM project WHERE owner_id = ? AND deleted_at IS NULL
    Page<Project> findByOwnerAndDeletedAtIsNull(AppUser owner, Pageable pageable);

    // 2. Keresés ID alapján: Csak akkor találja meg, ha az ID stimmel ÉS a tulajdonos is
    // SQL: SELECT * FROM project WHERE id = ? AND owner_id = ? AND deleted_at IS NULL
    Optional<Project> findByIdAndOwnerAndDeletedAtIsNull(Long id, AppUser owner);

}