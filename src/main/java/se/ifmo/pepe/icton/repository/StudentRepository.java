package se.ifmo.pepe.icton.repository;

import org.springframework.data.repository.CrudRepository;
import se.ifmo.pepe.icton.model.Student;

public interface StudentRepository extends CrudRepository<Student, Long> {
}
