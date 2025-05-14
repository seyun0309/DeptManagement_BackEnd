//package sunjin.DeptManagement_BackEnd.domain.order.service;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.multipart.MultipartFile;
//import sunjin.DeptManagement_BackEnd.domain.department.domain.Department;
//import sunjin.DeptManagement_BackEnd.domain.member.domain.Member;
//import sunjin.DeptManagement_BackEnd.domain.member.repository.MemberRepository;
//import sunjin.DeptManagement_BackEnd.domain.order.domain.Order;
//import sunjin.DeptManagement_BackEnd.domain.order.dto.request.CreateOrderRequestDTO;
//import sunjin.DeptManagement_BackEnd.domain.order.repository.OrderRepository;
//import sunjin.DeptManagement_BackEnd.global.auth.service.JwtProvider;
//import sunjin.DeptManagement_BackEnd.global.enums.DeptType;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.Optional;
//
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.*;
//
//@SpringBootTest
//@Transactional
//class CommonOrderServiceTest {
//    @InjectMocks
//    CommonOrderService commonOrderService;
//
//    @Mock
//    OrderRepository orderRepository;
//
//    @Mock
//    MemberRepository memberRepository;
//
//    @Mock
//    JwtProvider jwtProvider;
//
//    @Test
//    @DisplayName("주문 성공")
//    public void 주문_성공() throws IOException {
//        // given
//        MultipartFile image = mock(MultipartFile.class);
//        given(image.isEmpty()).willReturn(false);
//        given(image.getOriginalFilename()).willReturn("receipt.jpg");
//
//        CreateOrderRequestDTO requestDTO = CreateOrderRequestDTO.builder()
//                .productType("식비")
//                .storeName("김밥천국")
//                .totalPrice(15000)
//                .build();
//
//        given(jwtProvider.extractIdFromTokenInHeader()).willReturn(String.valueOf(1L));
//
//        Member mockMember = Member.builder()
//                .id(1L)
//                .userName("김세윤")
//                .refreshToken("someToken")
//                .department(new Department(DeptType.DIGITAL_SCM))
//                .build();
//
//        given(memberRepository.findById(1L)).willReturn(Optional.of(mockMember));
//
//        doNothing().when(image).transferTo(any(File.class)); // 파일 저장이 실제 일어나지 않게 mocking
//
//        // when
//        commonOrderService.createOrder(image, requestDTO);
//
//        // then
//        verify(orderRepository, times(1)).save(any(Order.class));
//    }
//
//    @Test
//    @DisplayName("주문 실패-로그인 만료")
//    public void 주문_실패_로그인만료() {
//
//    }
//}