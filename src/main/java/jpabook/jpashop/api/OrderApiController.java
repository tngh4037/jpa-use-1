package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();

            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());
        }

        return all;
    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());

        // [ occurred N+1 ]
        // - order 조회 1번
        // - (주문마다 loop를 돌면서) member 조회, delivery 조회, orderItems 조회
        //                                                         ㄴ (주문상품마다 loop를 돌면서) item 조회

        return collect;
    }

    @Getter
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems; // 참고) 엔티티를 외부로 노출하지 말라는 것이 단순히 외부 껍데기만 DTO로 변환하라는 것이 아니라, 내부 모든 필드도 포함한다. ( 단, Address 와 같은 값 타입은 노출해도 된다. )

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(Collectors.toList());
        }
    }

    @Getter
    static class OrderItemDto {
        private String itemName; // 상품명
        private int orderPrice; // 주문 가격
        private int count; // 주문 수량

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }

    // [ fetch join 적용 ]
    // - (xxxToMany 관계) 컬렉션 페치 조인으로 인한 중복제거를 위해 distinct 처리
    // - but, 컬렉션 페치 조인은 페이징 불가 (warn 로그 발생)
    //   ㄴ DB에서 데이터 전체를 조회해서(=DB에서 애플리케이션으로 데이터 모두 전송), 메모리에서 페이징.. (위험)
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem(); // fetch join
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    // [ xxxToMany - 컬렉션 페치 조인 페이징 한계 돌파 ]
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                        @RequestParam(value = "limit", defaultValue = "100") int limit) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit); // (only xxxToOne) fetch join
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());

        // 1) (데이터 뻥튀기 문제가 없는) xxxToOne 관계에 한해서만 모두 fetch join 한다. (컬렉션은 지연로딩)
        // : order( only xxxToOne fetch join ) -> orderItem -> item(2) -> orderItem -> item(2)
        //                                     ㄴ LAZY

        // 1) 으로만 하면 N+1 문제가 발생한다. 이 문제를 완전히 해결할 수는 없지만 최적화는 할 수는 있다. (아래 참고)

        // 2) batch size 를 설정 (최적화)
        // application.yml : spring.jpa.properties.hibernate.default_batch_fetch_size: 100
        // : order( only xxxToOne fetch join ) -> orderItem -> item
        //                                     ㄴ LAZY
        //
        // (참고) orderItem 조회시 in 쿼리에, 1) 에서 조회된 orders 의 order_id 를 100개씩 적용
        // (참고) item 조회시 in 쿼리에, 이전에 조회된 orderItems 의 item_id 를 100개씩 적용

        // 결론) xxxToOne 관계는 페치 조인으로 쿼리 수를 최대한 줄이고, 나머지는 default_batch_fetch_size 로 최적화 하자.

        return result;
    }

    // jpa 에서 dto 직접 조회
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

}
