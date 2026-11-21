package com.techcompare.laptop;

import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for laptops. Spring Data implements this interface automatically. */
public interface LaptopRepository extends JpaRepository<Laptop, Integer> {
}
