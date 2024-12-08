package uni.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import uni.backend.domain.Qna;
import uni.backend.domain.Reply;
import uni.backend.domain.User;
import uni.backend.domain.dto.ReplyCreateRequest;
import uni.backend.domain.dto.ReplyCreateResponse;
import uni.backend.domain.dto.ReplyResponse;
import uni.backend.domain.dto.Response;
import uni.backend.service.ReplyService;
import uni.backend.repository.UserRepository;
import uni.backend.domain.Profile;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ReplyControllerTest {

    @Mock
    private ReplyService replyService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ReplyController replyController;

    private ReplyCreateRequest replyCreateRequest;
    private ReplyResponse replyResponse;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // ReplyCreateRequest 객체 초기화
        replyCreateRequest = ReplyCreateRequest.builder()
            .content("Sample reply content")
            .build();

        // ReplyResponse 객체 초기화
        replyResponse = ReplyResponse.builder()
            .replyId(1)
            .commenterId(1)
            .commenterName("Sample Commenter")
            .content("Sample content")
            .qnaId(1)
            .imgProf("imgProfile")
            .deleted(false)
            .deletedMessage(null)
            .likes(0L)
            .build();

        // User 객체 초기화
        user = new User();
        user.setUserId(1);
        user.setName("Sample User");
    }

    @Test
    @DisplayName("대댓글 작성")
    void testCreateReply() {
        // given
        Integer userId = 1;
        Integer qnaId = 1;
        Integer commenterId = 1;

        when(replyService.createReply(qnaId, commenterId,
            replyCreateRequest.getContent())).thenReturn(replyResponse);

        // when
        ResponseEntity<ReplyCreateResponse> response = replyController.createReply(userId, qnaId,
            commenterId, replyCreateRequest);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("대댓글이 작성되었습니다.", response.getBody().getMessage());
        verify(replyService, times(1)).createReply(qnaId, commenterId,
            replyCreateRequest.getContent()); // 서비스 메서드 호출 검증
    }

    @Test
    @DisplayName("대댓글 좋아요 토글")
    void testToggleLikeReply() {
        // given
        Integer replyId = 1;

        when(authentication.getName()).thenReturn("sample@example.com");
        when(userRepository.findByEmail("sample@example.com")).thenReturn(Optional.of(user));

        // when
        ResponseEntity<Response> response = replyController.toggleLikeReply(replyId,
            authentication);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("좋아요 상태가 변경되었습니다.", response.getBody().getMessage());
        verify(replyService, times(1)).toggleLike(replyId, user);
    }

    @Test
    @DisplayName("대댓글 삭제")
    void testDeleteReply() {
        // given
        Integer replyId = 1;

        // when
        ResponseEntity<Response> response = replyController.deleteReply(replyId);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("대댓글이 삭제되었습니다.", response.getBody().getMessage());
        verify(replyService, times(1)).deleteReply(replyId);
    }

    @Test
    @DisplayName("대댓글 정보 조회 - 삭제되지 않은 경우")
    void testGetReply() {
        // given
        Integer replyId = 1;
        Reply reply = new Reply();
        reply.setReplyId(replyId);
        reply.setCommenter(user);
        reply.setContent("Sample content");
        reply.setLikes(0L);
        reply.setDeleted(false);

        Qna qna = mock(Qna.class);
        when(qna.getQnaId()).thenReturn(1);
        reply.setQna(qna);

        Profile profile = mock(Profile.class);
        when(profile.getImgProf()).thenReturn("imgProfile");
        user.setProfile(profile);

        when(replyService.getReply(replyId)).thenReturn(reply);

        // when
        ResponseEntity<ReplyResponse> response = replyController.getReply(replyId);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(replyId, response.getBody().getReplyId());
        assertEquals("Sample content", response.getBody().getContent());
        assertEquals("imgProfile",
            response.getBody().getImgProf());
        verify(replyService, times(1)).getReply(replyId);
    }

    @Test
    @DisplayName("대댓글 정보 조회 - 삭제된 경우")
    void testGetReplyDeleted() {
        // given
        Integer replyId = 1;
        
        User user = mock(User.class);

        Profile profile = mock(Profile.class);
        when(profile.getImgProf()).thenReturn(
            "imgProfile");
        when(user.getProfile()).thenReturn(
            profile);

        Qna qna = mock(Qna.class);
        when(qna.getQnaId()).thenReturn(1);

        Reply reply = new Reply();
        reply.setReplyId(replyId);
        reply.setCommenter(user);
        reply.setContent("Sample content");
        reply.setLikes(0L);
        reply.setDeleted(true);
        reply.setQna(qna);

        when(replyService.getReply(replyId)).thenReturn(reply);

        // when
        ResponseEntity<ReplyResponse> response = replyController.getReply(replyId);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("삭제된 댓글입니다.",
            response.getBody().getContent());
        assertTrue(response.getBody().getDeleted());
        verify(replyService, times(1)).getReply(replyId);
    }
}