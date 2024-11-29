package uni.backend.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uni.backend.domain.Qna;
import uni.backend.domain.QnaLikes;
import uni.backend.domain.User;
import uni.backend.domain.dto.QnaResponse;
import uni.backend.domain.dto.QnaUserResponse;
import uni.backend.domain.dto.ReplyResponse;
import uni.backend.repository.QnaLikeRepository;
import uni.backend.repository.QnaRepository;

import java.util.List;
import uni.backend.repository.UserRepository;

@Service
public class QnaService {

    private final QnaRepository qnaRepository;
    private final UserRepository userRepository;
    private final QnaLikeRepository qnaLikeRepository;

    public QnaService(QnaRepository qnaRepository, UserRepository userRepository,
        QnaLikeRepository qnaLikeRepository) {
        this.qnaRepository = qnaRepository;
        this.userRepository = userRepository;
        this.qnaLikeRepository = qnaLikeRepository;
    }

    // 특정 유저의 Qna 목록 조회
    public List<Qna> getQnasByUserId(Integer userId) {
        return qnaRepository.findByProfileOwnerUserId(userId);
    }


    //qna 작성
    @Transactional
    public QnaResponse createQna(Integer userId, Integer commenterId, String content) {
        // QnA 소유자
        User profileOwner = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("프로필 주인을 찾을 수 없습니다. ID: " + userId));

        // QnA 작성자
        User commenter = userRepository.findById(commenterId)
            .orElseThrow(
                () -> new IllegalArgumentException("댓글 작성자를 찾을 수 없습니다. ID: " + commenterId));

        // QnA 생성
        Qna qna = new Qna();
        qna.setProfileOwner(profileOwner);
        qna.setCommenter(commenter);
        qna.setContent(content);
        Qna savedQna = qnaRepository.save(qna);

        // QnAResponse 생성
        return new QnaResponse(
            qna.getQnaId(),
            new QnaUserResponse(profileOwner.getUserId(), profileOwner.getName()),
            new QnaUserResponse(commenter.getUserId(), commenter.getName()),
            qna.getContent(),
            null,
            commenter.getProfile().getImgProf(), // QnA 작성자의 프로필 이미지
            qna.getDeleted(),
            qna.getDeleted() ? "삭제된 Qna입니다." : null,
            qna.getLikes()
        );
    }


    @Transactional
    public Qna toggleLike(Integer qnaId, User user) {
        Qna qna = qnaRepository.findById(qnaId)
            .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다. ID: " + qnaId));
        Optional<QnaLikes> existingLike = qnaLikeRepository.findByUserAndQna(user,
            qna); // 사용자와 Qna를 통한 좋아요 확인

        if (existingLike.isPresent()) {
            // 좋아요 취소
            qnaLikeRepository.delete(existingLike.get());
            qna.decreaseLikes(); // 좋아요 수 감소
        } else {
            // 좋아요 추가
            QnaLikes like = new QnaLikes();
            like.setUser(user);
            like.setQna(qna);
            qnaLikeRepository.save(like);
            qna.increaseLikes(); // 좋아요 수 증가
        }

        return qnaRepository.save(qna); // 업데이트된 Qna 반환
    }

    // 댓글 삭제
    @Transactional
    public Qna deleteQna(Integer qnaId) {
        Qna qna = qnaRepository.findById(qnaId)
            .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다. ID: " + qnaId));
        qna.delete();
        return qna;
    }

    public List<QnaResponse> getUserQnas(Integer userId) {
        List<Qna> userQnas = getQnasByUserId(userId);

        return userQnas.stream().map(qna -> {
            User profileOwner = qna.getProfileOwner();
            User commentAuthor = qna.getCommenter();

            QnaUserResponse ownerResponse = new QnaUserResponse(profileOwner.getUserId(),
                profileOwner.getName());
            QnaUserResponse commentAuthorResponse = new QnaUserResponse(commentAuthor.getUserId(),
                commentAuthor.getName());

            // 대댓글 리스트를 ReplyResponse 리스트로 변환
            List<ReplyResponse> replyResponses = qna.getReplies().stream().map(reply -> {
                return new ReplyResponse(
                    reply.getReplyId(),
                    reply.getCommenter().getUserId(),
                    reply.getCommenter().getName(),
                    reply.getContent(),
                    reply.getQna().getQnaId(),
                    reply.getCommenter().getProfile().getImgProf(),
                    reply.getDeleted(),
                    reply.getDeleted() ? "삭제된 대댓글입니다." : null,
                    reply.getLikes()
                );
            }).toList();

            return new QnaResponse(
                qna.getQnaId(),
                ownerResponse,
                commentAuthorResponse,
                qna.getBlindQna(),
                replyResponses, // 변환된 대댓글 리스트 추가
                commentAuthor.getProfile().getImgProf(), // 프로필 이미지
                qna.getDeleted(), // 삭제 여부
                qna.getDeleted() ? "삭제된 Qna입니다." : null, // 삭제된 경우 메시지
                qna.getLikes()
            );
        }).toList();
    }

    // 댓글 본문 수정
//  @Transactional
//  public Qna updateQnaContent(Integer qnaId, String newContent) {
//    Qna qna = qnaRepository.findById(qnaId)
//        .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다. ID: " + qnaId));
//    qna.updateContent(newContent);
//    return qna;
//  }
}
