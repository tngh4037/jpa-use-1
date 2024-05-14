package jpabook.jpashop.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    // ========= //
    // == 등록 == //
    // ========= //

    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) { // 문제점1 해결) 별도의 DTO(Data-Transfer-Object) 사용 -> 이제 엔티티가 변경되더라도 API 스펙에는 변경 X ( DTO를 생성하고 값을 넣고 하는 수고로움이 있지만, 엔티티를 직접 사용하는 것 보다는 여러 장점이 있다. )
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @Data
    static class CreateMemberRequest {
        @NotEmpty
        private String name;
    }

    @Data
    static class CreateMemberResponse {
        private Long id; // 등록된 id 값

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }

    // ========= //
    // == 수정 == //
    // ========= //

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id,
                                               @RequestBody @Valid UpdateMemberRequest request) { // 참고) 수정용 요청DTO 와 응답DTO를 별도로 설계
        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

    @Data
    static class UpdateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    // ========= //
    // == 조회 == //
    // ========= //

    @GetMapping("/api/v1/members")
    public List<Member> memberV1() { // 문제점2) 응답시 엔티티를 반환하게 되면, 엔티티에 있는 정보들이 모두 노출된다. ( 참고로 만약 회원 데이터만 조회하는 API인데 orders 정보 등도 노출될 수 있음. 따라서 노출하고 싶지 않은 필드 정보는 @JsonIgnore 를 적용해줄 수 있다. -> 문제는, 회원과 관련된 다른 API 에서는 해당 정보를 보고자 하는 경우가 있을 것이다. 그러면 엔티티 내부가 점점 지저분해진다. 이 경우도 마찬가지로, 결국 엔티티에 프레젠테이션 계층을 위한 코드가 들어가게 되고, 결론적으로는 좋지 않다. )
        return memberService.findMembers(); // 문제점3) 컬렉션을 직접 반환하면 API 스펙을 유연하게 변경하기 어렵다. ( 별도의 Result 클래스 생성으로 해결 )
        /*
        결과가 아래와 같이 array가 응답된다. 그런데 만약 응답에 count 정보를 넣어 달라고 하면 ? 지금 구조로는 json 스펙이 깨지기 때문에 안된다. ( 또한, array 를 직접 바로 반환하면 API 스펙이 딱 굳어져 버린다. 유연성이 떨어지고 확장 불가 )
        [
            {
                "id": 1,
                "name": "member1",
                "address": {
                    "city": "서울",
                    "street": "1",
                    "zipcode": "1"
                }
            },
            {
                "id": 2,
                "name": "member2",
                "address": {
                    "city": "부산",
                    "street": "2",
                    "zipcode": ""
                }
            }
        ]

         그래서 아래와 같이 기본적인 스펙을 감싸주는 객체를 만들어서 반환하는게 더 좋은 설계다. (스펙 확장에 용이)
         {
            "count": 4,
            "data": [
                ...
            ]
         }
         */
    }

    @GetMapping("/api/v2/members")
    public Result memberV2() {
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());

        return new Result(collect);
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }
}
