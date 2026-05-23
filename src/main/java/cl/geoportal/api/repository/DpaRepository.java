package cl.geoportal.api.repository;

import cl.geoportal.api.entity.DivisionPoliticaAdministrativa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DpaRepository extends JpaRepository<DivisionPoliticaAdministrativa, String> {
}
