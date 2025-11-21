package com.example.knu_connect.domain.mentor.specification;

import com.example.knu_connect.domain.user.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class MentorSpecification {

    public static Specification<User> isMentor() {
        return (root, query, cb) -> cb.isTrue(root.get("mentor"));
    }

    public static Specification<User> hasCareer(String career) {
        return (root, query, cb) ->
                career == null || career.isBlank()
                        ? null
                        : cb.equal(root.get("career"), career);
    }

    public static Specification<User> hasInterest(String interest) {
        return (root, query, cb) ->
                interest == null || interest.isBlank()
                        ? null
                        : cb.equal(root.get("interest"), interest);
    }

    public static Specification<User> containsKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }

            String pattern = "%" + keyword + "%";

            return cb.or(
                    cb.like(root.get("name"), pattern),
                    cb.like(root.get("introduction"), pattern)
            );
        };
    }
}
