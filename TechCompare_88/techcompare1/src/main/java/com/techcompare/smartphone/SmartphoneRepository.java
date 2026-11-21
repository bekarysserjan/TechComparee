package com.techcompare.smartphone;

import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for smartphones. Spring Data implements this interface automatically. */
public interface SmartphoneRepository extends JpaRepository<Smartphone, Integer> {
}
