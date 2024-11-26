package uni.backend.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uni.backend.domain.Qna;
import uni.backend.domain.Reply;
import uni.backend.domain.ReplyLikes;
import uni.backend.domain.User;
import uni.backend.domain.dto.ReplyResponse;
import uni.backend.repository.QnaRepository;
import uni.backend.repository.ReplyLikeRepository;
import uni.backend.repository.ReplyRepository;
import uni.backend.repository.UserRepository;

@Service
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final QnaRepository qnaRepository;
    private final UserRepository userRepository;
    private final ReplyLikeRepository replyLikeRepository;

    public ReplyService(ReplyRepository replyRepository, QnaRepository qnaRepository,
        UserRepository userRepository, ReplyLikeRepository replyLikeRepository) {
        this.replyRepository = replyRepository;
        this.qnaRepository = qnaRepository;
        this.userRepository = userRepository;
        this.replyLikeRepository = replyLikeRepository;
    }

    // 새로운 Reply 작성
    // 새로운 Reply 작성 및 작성자의 프로필 이미지 포함
    @Transactional
    public ReplyResponse createReply(Integer qnaId, Integer commenterId, String content) {
        // QnA 확인
        Qna qna = qnaRepository.findById(qnaId)
            .orElseThrow(() -> new IllegalArgumentException("QnA를 찾을 수 없습니다. ID: " + qnaId));

        // 댓글 작성자 확인
        User commenter = userRepository.findById(commenterId)
            .orElseThrow(
                () -> new IllegalArgumentException("댓글 작성자를 찾을 수 없습니다. ID: " + commenterId));

        // 답글 생성
        Reply reply = new Reply(qna, commenter, content);
        replyRepository.save(reply);

        // 작성자의 프로필 이미지 및 기타 정보
        String imgProf = commenter.getProfile().getImgProf(); // 작성자의 프로필 이미지
        String commenterName = commenter.getName();

        // ReplyResponse 생성 및 반환
        return new ReplyResponse(
            reply.getReplyId(),
            commenterId,
            commenterName,
            reply.getContent(),
            qnaId,
            imgProf,
            reply.getDeleted(),
            reply.getDeleted() ? "삭제된 댓글입니다." : null,
            reply.getLikes()
        );
    }

    // 좋아요 증가
//  @Transactional
//  public Reply likeReply(Integer replyId) {
//    Reply reply = replyRepository.findById(replyId)
//        .orElseThrow(() -> new IllegalArgumentException("대댓글을 찾을 수 없습니다. ID: " + replyId));
//    reply.increaseLikes(); // 엔티티 메서드 호출로 상태 변경
//    return replyRepository.save(reply); // 상태 변경 후 저장
//  }

    // 좋아요 감소
//  @Transactional
//  public Reply unlikeReply(Integer replyId) {
//    Reply reply = replyRepository.findById(replyId)
//        .orElseThrow(() -> new IllegalArgumentException("대댓글을 찾을 수 없습니다. ID: " + replyId));
//    reply.decreaseLikes(); // 엔티티 메서드 호출로 상태 변경
//    return replyRepository.save(reply); // 상태 변경 후 저장
//  }

    // 대댓글 삭제
    @Transactional
    public Reply deleteReply(Integer replyId) {
        Reply reply = replyRepository.findById(replyId)
            .orElseThrow(() -> new IllegalArgumentException("대댓글을 찾을 수 없습니다. ID: " + replyId));
        reply.softDelete(); // 소프트 삭제
        return replyRepository.save(reply); // 상태 변경 후 저장
    }

    // 대댓글 본문 수정
    @Transactional
    public Reply updateReplyContent(Integer replyId, String newContent) {
        Reply reply = replyRepository.findById(replyId)
            .orElseThrow(() -> new IllegalArgumentException("대댓글을 찾을 수 없습니다. ID: " + replyId));
        reply.updateContent(newContent); // 엔티티 메서드 호출로 상태 변경
        return replyRepository.save(reply); // 상태 변경 후 저장
    }

    // 대댓글 세부 정보 조회
    @Transactional(readOnly = true)
    public Reply getReply(Integer replyId) {
        return replyRepository.findById(replyId)
            .orElseThrow(() -> new IllegalArgumentException("대댓글을 찾을 수 없습니다. ID: " + replyId));
    }


    //대댓글 좋아요
    @Transactional
    public void toggleLike(Integer replyId, User user) {
        // replyId로 대댓글 조회
        Reply reply = replyRepository.findById(replyId)
            .orElseThrow(() -> new IllegalArgumentException("대댓글을 찾을 수 없습니다. ID: " + replyId));

        // 이미 좋아요가 눌러졌는지 확인
        Optional<ReplyLikes> existingLike = replyLikeRepository.findByUserAndReply(user, reply);

        if (existingLike.isPresent()) {
            // 좋아요 취소
            replyLikeRepository.delete(existingLike.get());
            reply.decreaseLikes(); // 좋아요 수 감소
        } else {
            // 좋아요 추가
            ReplyLikes like = new ReplyLikes();
            like.setUser(user);
            like.setReply(reply);
            replyLikeRepository.save(like);
            reply.increaseLikes(); // 좋아요 수 증가
        }

        replyRepository.save(reply); // 업데이트된 대댓글 저장
    }

}
