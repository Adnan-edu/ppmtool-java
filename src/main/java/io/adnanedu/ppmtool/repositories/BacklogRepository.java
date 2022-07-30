package io.adnanedu.ppmtool.repositories;

import io.adnanedu.ppmtool.domain.Backlog;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BacklogRepository extends CrudRepository<Backlog, Long> {

    Backlog findBacklogByProjectIdentifier(String Identifier);
}
