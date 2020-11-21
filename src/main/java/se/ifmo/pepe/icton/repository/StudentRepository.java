package se.ifmo.pepe.icton.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import se.ifmo.pepe.icton.model.Lab;
import se.ifmo.pepe.icton.model.Student;

import java.util.Map;

public interface StudentRepository extends CrudRepository<Student, Long> {
}
