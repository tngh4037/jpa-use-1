package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findByName(String name); // spring data jpa 가 내부 규칙에 의해 JPQL (select m from Member m where m.name = :name) 을 대신 생성 해준다.
}
