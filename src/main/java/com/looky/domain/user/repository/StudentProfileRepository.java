package com.looky.domain.user.repository;

import com.looky.domain.user.entity.StudentProfile;
import com.looky.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    @Query("SELECT s.nickname FROM StudentProfile s WHERE s.user = :user")
    Optional<String> findNicknameByUser(@Param("user") User user);
}
