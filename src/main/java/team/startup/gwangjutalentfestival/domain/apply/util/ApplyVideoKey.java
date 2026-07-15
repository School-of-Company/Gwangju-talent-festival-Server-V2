
package team.startup.gwangjutalentfestival.domain.apply.util;

import java.util.UUID;

/**
 * 신청 영상의 S3 객체 key 생성·검증 유틸.
 * key는 항상 {@code videos/{uuid}.mp4} 형식이며, 클라이언트가 임의 경로를 지정하지 못하도록 검증한다.
 */
public final class ApplyVideoKey {

    public static final String PATTERN = "^videos/[a-zA-Z0-9_-]+\\.mp4$";
    private static final String PREFIX = "videos/";
    private static final String SUFFIX = ".mp4";

    private ApplyVideoKey() {
    }

    /**
     * 새 영상 key를 생성한다.
     *
     * @return {@code videos/{uuid}.mp4} 형식의 key
     */
    public static String generate() {
        return PREFIX + UUID.randomUUID() + SUFFIX;
    }

    /**
     * key가 허용된 형식을 벗어났는지 검사한다.
     *
     * @param key 검사할 key
     * @return null이거나 형식에 맞지 않으면 {@code true}
     */
    public static boolean isInvalid(String key) {
        return key == null || !key.matches(PATTERN);
    }
}
