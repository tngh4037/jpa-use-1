package jpabook.jpashop.repository.order.simplequery;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

// repository 는 가급적 순수한 엔티티를 조회하는게 좋다. 엔티티로 조회해야 재사용성도 좋고 개발 생산성이 높아진다.
// 만약 정말 복잡한 쿼리들로 원하는 정보만 뽑아서 DTO로 바로 조회해야 할 때는, 별도로 패키지로 두고 관리하는 편이 낫다. ( 이 경우는 대부분 특정 API에만 특화된, 의존하는 것이므로, 이게 repository 에 있으면 재사용성도 떨어지고 용도가 애매해진다. )
@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {

    private final EntityManager em;
    
    public List<OrderSimpleQueryDto> findOrderDtos() {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d", OrderSimpleQueryDto.class).getResultList();
    }
}
