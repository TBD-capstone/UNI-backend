package uni.backend.domain;

import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // 신고 고유 ID

    @ManyToOne
    @JoinColumn(name = "reported_user_id", nullable = false)
    private User reportedUser; // 신고된 유저

    @ManyToOne
    @JoinColumn(name = "reporter_user_id", nullable = false)
    private User reporterUser; // 신고한 유저

    @Column(nullable = false)
    private ReportReason reason; // 신고 사유 (Enum)

    @Column(nullable = false)
    @Size(min = 10, message = "신고 사유는 최소 10자 이상이어야 합니다.")
    private String detailedReason; // 추가 상세 사유

    @Column(nullable = false)
    private ReportCategory category; // 신고 카테고리 (프로필, 채팅, Q&A, 리뷰 등)

    @Column(nullable = false)
    private LocalDateTime reportedAt; // 신고 날짜

    @Column(nullable = false)
    @Size(min = 5, message = "신고 제목은 최소 5자 이상이어야 합니다.")
    private String title; // 신고 제목 추가
}

