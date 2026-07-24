package team.startup.gwangjutalentfestival.domain.excel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.startup.gwangjutalentfestival.domain.excel.service.impl.DownloadJudgeSheetsServiceImpl;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgeCommentEntity;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgementEntity;
import team.startup.gwangjutalentfestival.domain.judge.repository.JudgeCommentRepository;
import team.startup.gwangjutalentfestival.domain.judge.repository.JudgementRepository;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.team.enums.TeamGenre;
import team.startup.gwangjutalentfestival.domain.team.enums.TeamStatus;
import team.startup.gwangjutalentfestival.domain.team.repository.TeamRepository;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.domain.user.repository.UserRepository;
import team.startup.gwangjutalentfestival.global.thirdparty.google.adapter.GoogleExcelAdapter;
import team.startup.gwangjutalentfestival.global.thirdparty.google.properties.GoogleExcelProperties;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DownloadJudgeSheetsServiceImplTest {

    @Mock private TeamRepository teamRepository;
    @Mock private JudgementRepository judgementRepository;
    @Mock private JudgeCommentRepository judgeCommentRepository;
    @Mock private UserRepository userRepository;
    @Mock private DownloadJudgingSummaryExcelService summaryExcelService;
    @Mock private GoogleExcelAdapter googleExcelAdapter;
    @Mock private GoogleExcelProperties googleExcelProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private DownloadJudgeSheetsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DownloadJudgeSheetsServiceImpl(
                teamRepository, judgementRepository, judgeCommentRepository, userRepository, summaryExcelService,
                googleExcelAdapter, googleExcelProperties);
        given(summaryExcelService.execute()).willReturn(new byte[]{1, 2, 3});
        given(googleExcelAdapter.exportJudgeTemplate()).willReturn(template());
        given(googleExcelProperties.judgeTemplatePage()).willReturn("개별 심사표");
    }

    @Test
    void 집계표와_심사위원별_개별표를_ZIP으로_생성하고_점수와_필기이미지를_넣는다() throws Exception {
        TeamEntity teamA = team(1L, 1);
        TeamEntity teamB = team(2L, 2);
        UserEntity judgeA = user(10L);
        UserEntity judgeB = user(20L);
        given(teamRepository.findAllByOrderByPerformOrderAsc()).willReturn(List.of(teamA, teamB));
        given(judgementRepository.findAllWithUserAndTeam()).willReturn(List.of(
                judgement(teamA, judgeA, 20, 15, 25),
                judgement(teamA, judgeB, 10, 10, 10)
        ));
        given(judgeCommentRepository.findAllWithUserAndTeam()).willReturn(List.of(
                comment(teamA, judgeA, strokes()),
                comment(teamB, judgeA, objectMapper.createArrayNode())
        ));
        given(userRepository.findAllByRoleOrderByIdAsc(Role.JUDGE)).willReturn(List.of(judgeA, judgeB));
        Map<String, byte[]> files = zipEntries(service.execute());

        assertThat(files).containsOnlyKeys("심사집계표.xlsx", "심사위원_A_개별심사표.xlsx", "심사위원_B_개별심사표.xlsx");
        assertThat(files.get("심사집계표.xlsx")).containsExactly(1, 2, 3);
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(files.get("심사위원_A_개별심사표.xlsx")))) {
            var sheet = workbook.getSheet("개별 심사표");
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("원본 제목");
            assertThat(sheet.getRow(2).getCell(0).getStringCellValue()).isEqualTo("심사순서");
            assertThat(sheet.getRow(2).getCell(1).getStringCellValue()).isEqualTo("심사위원 A");
            assertThat(sheet.getRow(2).getCell(2).getStringCellValue()).isEqualTo("코멘트");
            assertThat(sheet.getRow(3).getCell(0).getNumericCellValue()).isEqualTo(1);
            assertThat(sheet.getRow(3).getCell(1).getNumericCellValue()).isEqualTo(60);
            assertThat(sheet.getRow(4).getCell(0).getNumericCellValue()).isEqualTo(2);
            assertThat(sheet.getRow(4).getCell(1).getNumericCellValue()).isZero();
            assertThat(workbook.getAllPictures()).hasSize(1);
            assertThat(renderedImageHasColor(workbook.getAllPictures().getFirst(), Color.RED)).isTrue();
            assertThat(renderedImageHasColor(workbook.getAllPictures().getFirst(), new Color(0x12, 0x12, 0x12))).isTrue();
        }
    }

    @Test
    void 열두번째_행은_보존하고_아홉번째_팀은_열세번째_행부터_작성한다() throws Exception {
        List<TeamEntity> teams = java.util.stream.IntStream.rangeClosed(1, 9)
                .mapToObj(index -> team(index, index))
                .toList();
        UserEntity judge = user(10L);
        given(teamRepository.findAllByOrderByPerformOrderAsc()).willReturn(teams);
        given(judgementRepository.findAllWithUserAndTeam()).willReturn(List.of(judgement(teams.getFirst(), judge, 20, 15, 25)));
        given(judgeCommentRepository.findAllWithUserAndTeam()).willReturn(List.of());
        given(userRepository.findAllByRoleOrderByIdAsc(Role.JUDGE)).willReturn(List.of(judge));

        Map<String, byte[]> files = zipEntries(service.execute());

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(files.get("심사위원_A_개별심사표.xlsx")))) {
            var sheet = workbook.getSheet("개별 심사표");
            assertThat(sheet.getRow(11).getCell(0).getStringCellValue()).isEqualTo("12행 보존");
            assertThat(sheet.getRow(12).getCell(0).getNumericCellValue()).isEqualTo(9);
        }
    }

    private byte[] template() {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("개별 심사표");
            for (int rowIndex = 0; rowIndex < 14; rowIndex++) {
                var row = sheet.createRow(rowIndex);
                for (int column = 0; column < 4; column++) {
                    row.createCell(column);
                }
            }
            sheet.getRow(0).getCell(0).setCellValue("원본 제목");
            sheet.getRow(11).getCell(0).setCellValue("12행 보존");
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean renderedImageHasColor(PictureData picture, Color color) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(picture.getData()));
        int expected = color.getRGB() & 0x00ffffff;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) & 0x00ffffff) == expected) {
                    return true;
                }
            }
        }
        return false;
    }

    private Map<String, byte[]> zipEntries(byte[] zip) throws IOException {
        Map<String, byte[]> files = new java.util.HashMap<>();
        try (ZipInputStream input = new ZipInputStream(new ByteArrayInputStream(zip))) {
            ZipEntry entry;
            while ((entry = input.getNextEntry()) != null) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                input.transferTo(output);
                files.put(entry.getName(), output.toByteArray());
            }
        }
        return files;
    }

    private TeamEntity team(long id, int order) {
        return TeamEntity.builder()
                .id(id)
                .teamName("팀" + id)
                .school("광주고")
                .teamStatus(TeamStatus.PENDING)
                .teamGenre(TeamGenre.SING)
                .performOrder(order)
                .totalScore(0)
                .build();
    }

    private UserEntity user(long id) {
        return UserEntity.builder().id(id).role(Role.JUDGE).build();
    }

    private JudgementEntity judgement(TeamEntity team, UserEntity user, int first, int second, int third) {
        return JudgementEntity.builder()
                .team(team)
                .user(user)
                .completenessExpressionScore(first)
                .creativityCompositionScore(second)
                .stagePerformanceTeamworkScore(third)
                .build();
    }

    private JudgeCommentEntity comment(TeamEntity team, UserEntity user, ArrayNode strokes) {
        return JudgeCommentEntity.builder().team(team).user(user).strokes(strokes).build();
    }

    private ArrayNode strokes() {
        ArrayNode strokes = objectMapper.createArrayNode();
        var points = strokes.addObject().put("color", "#ff0000").putArray("points");
        points.addObject().put("x", 0.1).put("y", 0.2);
        points.addObject().put("x", 0.8).put("y", 0.7);
        points = strokes.addObject().put("color", "#121212").putArray("points");
        points.addObject().put("x", 0.2).put("y", 0.3);
        points.addObject().put("x", 0.7).put("y", 0.4);
        return strokes;
    }
}
