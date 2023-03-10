package teamproject.pocoapoco.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import teamproject.pocoapoco.controller.main.api.UserController;
import teamproject.pocoapoco.domain.dto.user.*;
import teamproject.pocoapoco.domain.entity.User;
import teamproject.pocoapoco.exception.AppException;
import teamproject.pocoapoco.exception.ErrorCode;
import teamproject.pocoapoco.fixture.UserEntityFixture;
import teamproject.pocoapoco.repository.UserRepository;
import teamproject.pocoapoco.service.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserService userService;

    @MockBean
    UserRepository userRepository;
    UserLoginRequest request = new UserLoginRequest("userId1234", "pass1234");
    @Nested
    @DisplayName("???????????? ?????????")
    public class JoinTest{

        UserJoinRequest userJoinRequest1 = new UserJoinRequest("?????????", "?????????", "????????????", "????????????", "????????? ?????????", true, false, false);
        UserJoinRequest userJoinRequest2 = new UserJoinRequest("?????????", "?????????11", "????????????11", "????????????11", "????????? ?????????123", true, false, true);
        UserJoinRequest userJoinRequest3 = new UserJoinRequest("?????????12", "?????????", "?????????????????????", "?????????????????????", "????????? ????????? ?????????", true, false, false);
        UserJoinRequest userJoinRequest4 = new UserJoinRequest("?????????12", "?????????", "?????????????????????", "?????????????????????123", "????????? ????????? ?????????", true, false, false);


        User user1 = UserEntityFixture.get(userJoinRequest1);


        @Test
        @WithMockUser
        @DisplayName("???????????? ??????")
        public void ?????????????????????1() throws Exception {

            // given

            UserJoinResponse userJoinResponse = UserJoinResponse.builder()
                    .userId("?????????")
                    .message("???????????? ???????????????.").build();

            given(userService.saveUser(any())).willReturn(userJoinResponse);

            // when
            mockMvc.perform(post("/api/v1/users/join")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(userJoinRequest1)))
                    //then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.userId").value("?????????"))
                    .andExpect(jsonPath("$.result.message").value("???????????? ???????????????."))
                    .andDo(print());

        }


        @Test
        @WithMockUser
        @DisplayName("???????????? ??????1 - ????????? ??????")
        public void ?????????????????????2() throws Exception {

            // given

            userService.saveUser(userJoinRequest1);

            given(userService.saveUser(any())).willThrow(new AppException(ErrorCode.DUPLICATED_USERID, ErrorCode.DUPLICATED_USERID.getMessage()));

            // when
            mockMvc.perform(post("/api/v1/users/join")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(userJoinRequest2)))
                    // then
                    .andExpect(status().isConflict())
                    .andExpect(content().string(ErrorCode.DUPLICATED_USERID.name() + " ?????? ???????????? ????????? ?????????."))
                    .andDo(print());

        }


        @Test
        @WithMockUser
        @DisplayName("???????????? ??????2 - ????????? ??????")
        public void ?????????????????????3() throws Exception {

            // given
            userService.saveUser(userJoinRequest1);
            given(userService.saveUser(any())).willThrow(new AppException(ErrorCode.DUPLICATED_USERNAME, ErrorCode.DUPLICATED_USERNAME.getMessage()));


            //when
            mockMvc.perform(post("/api/v1/users/join")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(userJoinRequest3)))
                    // then
                    .andExpect(status().isConflict())
                    .andExpect(content().string(ErrorCode.DUPLICATED_USERNAME.name() + " ?????? ???????????? ????????? ?????????."))
                    .andDo(print());

        }

        @Test
        @WithMockUser
        @DisplayName("???????????? ??????3 - ???????????? ?????? ??????")
        public void ?????????????????????4() throws Exception {

            // given
            given(userService.saveUser(any())).willThrow(new AppException(ErrorCode.NOT_MATCH_PASSWORD, ErrorCode.NOT_MATCH_PASSWORD.getMessage()));

            // when
            mockMvc.perform(post("/api/v1/users/join")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(userJoinRequest4)))
                    //then
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string(ErrorCode.NOT_MATCH_PASSWORD.name() + " ??????????????? ???????????? ????????????."))
                    .andDo(print());

        }

    }

    @Nested
    @DisplayName("????????? Test")
    class Login {

        @Test
        @WithMockUser
        @DisplayName("????????? ??????")
        void ??????????????????1() throws Exception {

            when(userService.login(any())).thenReturn(new UserLoginResponse("refreshToken","accessToken"));

            //when
            mockMvc.perform(post("/api/v1/users/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(request)))
                    //then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                    .andExpect(jsonPath("$.result.refreshToken").value("refreshToken"))
                    .andExpect(jsonPath("$.result.accessToken").value("accessToken"))
                    .andDo(print());

        }

        @Test
        @WithMockUser
        @DisplayName("????????? ??????1 - ?????? ????????? ??????")
        void ??????????????????2() throws Exception {

            //given
            given(userService.login(any())).willThrow(new AppException(ErrorCode.USERID_NOT_FOUND, ErrorCode.USERID_NOT_FOUND.getMessage()));

            //when
            mockMvc.perform(post("/api/v1/users/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(request)))
                    //then
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(ErrorCode.USERID_NOT_FOUND.name() + " " + ErrorCode.USERID_NOT_FOUND.getMessage()))
                    .andDo(print());
        }

        @Test
        @WithMockUser
        @DisplayName("????????? ??????2 - ???????????? ?????????")
        void ??????????????????3() throws Exception {

            //given
            given(userService.login(any())).willThrow(new AppException(ErrorCode.INVALID_PASSWORD, ErrorCode.INVALID_PASSWORD.getMessage()));

            //when
            mockMvc.perform(post("/api/v1/users/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(request)))
                    //then
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string(ErrorCode.INVALID_PASSWORD.name() + " " + ErrorCode.INVALID_PASSWORD.getMessage()))
                    .andDo(print());
        }
    }



    @Nested
    @DisplayName("????????? ?????? ??????")
    class GetProfile{

        UserProfileResponse userProfileResponse = UserProfileResponse.builder()
                .userName("?????????")
                .address("??????")
                .likeSoccer(true)
                .likeJogging(true)
                .likeTennis(false)
                .build();

        @Test
        @WithMockUser
        @DisplayName("????????? ?????? ?????? - ??? ?????????")
        void ???????????????1() throws Exception {

            //given
            given(userService.getUserInfoByUserName(any())).willReturn(userProfileResponse);

            //when
            mockMvc.perform(get("/api/v1/users/profile/my")
                            .with(csrf()))
                    //then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                    .andExpect(jsonPath("$.result.userName").value("?????????"))
                    .andExpect(jsonPath("$.result.likeSoccer").value(true))
                    .andDo(print());

        }


        @Test
        @WithMockUser
        @DisplayName("????????? ?????? ??????1 - ????????? ?????? ??????")
        void ???????????????2() throws Exception {

            //given
            given(userService.getUserInfoByUserName(any())).willThrow(new AppException(ErrorCode.USERID_NOT_FOUND, ErrorCode.USERID_NOT_FOUND.getMessage()));


            //when
            mockMvc.perform(get("/api/v1/users/profile/my")
                            .with(csrf()))
                    //then
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(ErrorCode.USERID_NOT_FOUND.name() + " " + ErrorCode.USERID_NOT_FOUND.getMessage()))
                    .andDo(print());

        }



    }


    @Nested
    @DisplayName("????????? ?????? ??????")
    class ReviseProfile{


        UserProfileRequest userProfileRequest1 = UserProfileRequest.builder()
                .userName("?????????")
                .address("??????")
                .password("????????????")
                .passwordConfirm("????????????")
                .likeSoccer(true)
                .likeJogging(false)
                .likeTennis(true)
                .build();

        UserProfileRequest userProfileRequest2 = UserProfileRequest.builder()
                .userName("?????????")
                .address("??????")
                .password("????????????")
                .passwordConfirm("????????????123")
                .likeSoccer(true)
                .likeJogging(false)
                .likeTennis(true)
                .build();

        UserProfileResponse userProfileResponse1 = UserProfileResponse.builder()
                .userName("?????????")
                .address("??????")
                .likeSoccer(true)
                .likeJogging(false)
                .likeTennis(true)
                .build();

        @Test
        @WithMockUser
        @DisplayName("????????? ?????? ??????")
        void ???????????????1() throws Exception {

            //given
            given(userService.updateUserInfoByUserName(any(), any()))
                    .willReturn(userProfileResponse1);



            // when
            mockMvc.perform(put("/api/v1/users/revise")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(userProfileRequest1)))
                    // then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.userName").value("?????????"))
                    .andExpect(jsonPath("$.result.address").value("??????"))
                    .andDo(print());
        }


        @Test
        @WithMockUser
        @DisplayName("????????? ?????? ??????1 - ???????????? ?????? ??????")
        void ???????????????2() throws Exception {
            // given: ????????????
            given(userService.updateUserInfoByUserName(any(), any()))
                    .willThrow(new AppException(ErrorCode.NOT_MATCH_PASSWORD, ErrorCode.NOT_MATCH_PASSWORD.getMessage()));




            //when
            mockMvc.perform(put("/api/v1/users/revise")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(userProfileRequest2)))
                    //then
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string(ErrorCode.NOT_MATCH_PASSWORD.name() + " ??????????????? ???????????? ????????????."))
                    .andDo(print());
        }


        @Test
        @WithMockUser
        @DisplayName("????????? ?????? ??????2 - ???????????? ?????? ??????")
        void ???????????????3() throws Exception {

            // given:
            given(userService.updateUserInfoByUserName(any(), any()))
                    .willThrow(new AppException(ErrorCode.USERID_NOT_FOUND, ErrorCode.USERID_NOT_FOUND.getMessage()));

            //when
            mockMvc.perform(put("/api/v1/users/revise")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(userProfileRequest2)))
                    //then
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(ErrorCode.USERID_NOT_FOUND.name() + " ???????????? ???????????? ????????????."))
                    .andDo(print());


        }



    }




}