package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    @NotEmpty // 문제점1) 요청시 엔티티로 받게 되면, 엔티티에 프레젠테이션 계층을 위한 검증 로직이 들어가게 된다. (문제1. 특정 API에서는 필요하지만, 다른 특정 API 에서는 필요 없을수도 있다. | 문제2. name -> username 으로 변경했다고 가정해보자. 그러면 API 스펙 자체가 username으로 바꿔어 버리게 된다. 즉, 엔티티를 수정했다고 해서 API 스펙이 바뀌어 버리면 안된다. 따라서 엔티티를 API 요청 바인딩 용으로 사용하거나 응답용으로 사용하지 말고, API 스펙에 맞춘 별도의 DTO 를 만들어서 사용해야 한다. ) (실무에서 엔티티는 여러곳에서 사용된다. 따라서 바뀔 확률이 높다. 그런데 이거를 바꿨다고 해서, API 스펙이 바뀌어 버리게 된다면, 그것은 큰 문제가 발생할 수 있다.)
    private String name;

    @Embedded
    private Address address;

    @JsonIgnore
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();
}
