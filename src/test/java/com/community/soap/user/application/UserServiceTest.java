package com.community.soap.user.application;


import com.community.soap.common.snowflake.Snowflake;
import com.community.soap.user.application.request.SignInRequest;
import com.community.soap.user.application.request.SignUpRequest;
import com.community.soap.user.application.response.MyPageResponse;
import com.community.soap.user.application.response.SignInResponse;
import com.community.soap.user.application.response.SignUpResponse;
import com.community.soap.user.domain.entity.User;
import com.community.soap.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Snowflake snowflake;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("signup()")
    class SignupTest {

        @Test
        @DisplayName("정상 가입 시 저장되고 응답을 반환한다")
        void signup_success() {
            // given
            long generatedId = 1234L;
            given(snowflake.nextId()).willReturn(generatedId);

            SignUpRequest req = new SignUpRequest(
                    "foo@bar.com",
                    "P@ssw0rd!",
                    "닉네임"
            );

            // 저장되는 엔티티를 검증하기 위해 캡쳐
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            given(userRepository.save(userCaptor.capture()))
                    .willAnswer(inv -> inv.getArgument(0));

            // when
            SignUpResponse res = userService.signup(req);

            // then
            User saved = userCaptor.getValue();
            assertThat(saved.getUserId()).isEqualTo(generatedId);
            assertThat(saved.getEmail()).isEqualTo("foo@bar.com");
            assertThat(saved.getPassword()).isEqualTo("P@ssw0rd!");

            assertThat(res).isNotNull();
            assertThat(res.userId()).isEqualTo(generatedId);

            then(userRepository).should(times(1)).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("signIn()")
    class SignInTest {

        @Test
        @DisplayName("이메일/비밀번호가 맞으면 성공")
        void signIn_success() {
            // given
            User user = User.register(100L, "foo@bar.com", "P@ssw0rd!");
            given(userRepository.findByEmail("foo@bar.com")).willReturn(Optional.of(user));

            SignInRequest req = new SignInRequest("foo@bar.com", "P@ssw0rd!");

            // when
            SignInResponse res = userService.signIn(req);

            // then
            assertThat(res).isNotNull();
            assertThat(res.email()).isEqualTo("foo@bar.com");
        }

        @Test
        @DisplayName("비밀번호가 틀리면 예외")
        void signIn_wrongPassword() {
            // given
            User user = User.register(100L, "foo@bar.com", "correct!");
            given(userRepository.findByEmail("foo@bar.com")).willReturn(Optional.of(user));

            SignInRequest req = new SignInRequest("foo@bar.com", "wrong!");

            // when & then
            assertThatThrownBy(() -> userService.signIn(req))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Passwords don't match");
        }

        @Test
        @DisplayName("이메일을 못 찾으면 예외")
        void signIn_emailNotFound() {
            // given
            given(userRepository.findByEmail("no@no.com")).willReturn(Optional.empty());
            SignInRequest req = new SignInRequest("no@no.com", "any");

            // when & then
            assertThatThrownBy(() -> userService.signIn(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User with email no@no.com not found");
        }
    }

    @Nested
    @DisplayName("myPage()")
    class MyPageTest {

        @Test
        @DisplayName("사용자 ID로 조회 성공")
        void myPage_success() {
            // given
            User user = User.register(777L, "me@site.com", "pw");
            given(userRepository.findByUserId(777L)).willReturn(Optional.of(user));

            // when
            MyPageResponse res = userService.myPage(777L);

            // then
            assertThat(res).isNotNull();
            assertThat(res.email()).isEqualTo("me@site.com");
        }

        @Test
        @DisplayName("사용자 ID를 못 찾으면 예외")
        void myPage_notFound() {
            // given
            given(userRepository.findByUserId(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.myPage(1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User with id 1 not found");
        }
    }

    @Nested
    @DisplayName("deleteUser()")
    class DeleteUserTest {

        @Test
        @DisplayName("엔티티의 delete(userId)가 호출된다")
        void deleteUser_callsDomainDelete() {
            // given
            // delete(userId) 동작을 눈으로 확인하기 어려우면, 스파이 또는 가짜 엔티티를 써서 플래그를 세우는 방법이 좋음
            User user = Mockito.spy(User.register(55L, "x@y.com", "pw"));
            given(userRepository.findByUserId(55L)).willReturn(Optional.of(user));

            // when
            userService.deleteUser(55L);

            // then
            // 도메인 메서드가 호출됐는지 확인
            Mockito.verify(user, times(1)).delete(55L);
        }
    }
}