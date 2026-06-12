package team.startup.gwangjutalentfestival.domain.apply.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 공연 신청 데이터를 저장하는 엔티티.
 * 업로드된 영상의 S3 key를 보관하여 다운로드 시점마다 Presigned URL을 재발급할 수 있게 한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "apply")
@Builder
public class ApplyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "video_key", nullable = false)
    private String videoKey;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;
}
