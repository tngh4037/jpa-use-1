package jpabook.jpashop.repository.order.simplequery;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderSimpleQueryDto {
    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;

    public OrderSimpleQueryDto(Long orderId, String name, LocalDateTime orderDate, OrderStatus orderStatus, Address address) { // 참고) OrderSimpleQueryDto(Order order) 로 넣으면 안된다. ( JPQL 작성할 때 new 명령어에 OrderSimpleQueryDto(o) 로 Order 엔티티 자체를 넣고, DTO 생성자로 엔티티 객체를 받으면 안되나? -> 안된다. JPA는 기본적으로 엔티티를 넘기면 식별자로 넣어버린다. 따라서, 번거로우나 다 넣어줘야 한다. (단, 값 타입은 상관없다. 값처럼 동작한다. ex) Address) )
        this.orderId = orderId;
        this.name = name;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;
    }
}
