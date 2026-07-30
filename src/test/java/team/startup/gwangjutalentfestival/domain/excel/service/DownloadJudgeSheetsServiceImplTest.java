package team.startup.gwangjutalentfestival.domain.excel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.startup.gwangjutalentfestival.domain.excel.service.impl.DownloadJudgeSheetsServiceImpl;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgeCommentEntity;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgeProfileEntity;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgementEntity;
import team.startup.gwangjutalentfestival.domain.judge.repository.JudgeCommentRepository;
import team.startup.gwangjutalentfestival.domain.judge.repository.JudgeProfileRepository;
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
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.STEditAs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.awt.Color;
import java.awt.Rectangle;
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
    @Mock private JudgeProfileRepository judgeProfileRepository;
    @Mock private UserRepository userRepository;
    @Mock private DownloadJudgingSummaryExcelService summaryExcelService;
    @Mock private GoogleExcelAdapter googleExcelAdapter;
    @Mock private GoogleExcelProperties googleExcelProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private DownloadJudgeSheetsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DownloadJudgeSheetsServiceImpl(
                teamRepository, judgementRepository, judgeCommentRepository, judgeProfileRepository, userRepository,
                summaryExcelService, googleExcelAdapter, googleExcelProperties);
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
                comment(teamB, judgeA, objectMapper.createArrayNode()),
                comment(teamA, judgeB, strokes())
        ));
        given(userRepository.findAllByRoleOrderByIdAsc(Role.JUDGE)).willReturn(List.of(judgeA, judgeB));
        Map<String, byte[]> files = zipEntries(service.execute());

        assertThat(files).containsOnlyKeys("심사집계표.xlsx", "심사위원_A_개별심사표.xlsx", "심사위원_B_개별심사표.xlsx");
        assertThat(files.get("심사집계표.xlsx")).containsExactly(1, 2, 3);
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(files.get("심사위원_A_개별심사표.xlsx")))) {
            var sheet = workbook.getSheet("개별 심사표");
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("원본 제목");
            assertThat(sheet.getRow(2).getCell(0).getStringCellValue()).isEqualTo("소속/직위/이름");
            assertThat(sheet.getRow(3).getCell(0).getStringCellValue()).isEqualTo("심사순서");
            assertThat(sheet.getRow(3).getCell(1).getStringCellValue()).isEqualTo("팀명");
            assertThat(sheet.getRow(3).getCell(2).getStringCellValue()).isEqualTo("완성도·표현력");
            assertThat(sheet.getRow(3).getCell(3).getStringCellValue()).isEqualTo("창의력·구성");
            assertThat(sheet.getRow(3).getCell(4).getStringCellValue()).isEqualTo("무대매너·퍼포먼스");
            assertThat(sheet.getRow(3).getCell(5).getStringCellValue()).isEqualTo("총합 점수");
            assertThat(sheet.getRow(3).getCell(6).getStringCellValue()).isEqualTo("심사 의견");
            assertThat(sheet.getRow(4).getCell(0).getNumericCellValue()).isEqualTo(1);
            assertThat(sheet.getRow(4).getCell(1).getStringCellValue()).isEqualTo("팀1");
            assertThat(sheet.getRow(4).getCell(2).getNumericCellValue()).isEqualTo(20);
            assertThat(sheet.getRow(4).getCell(3).getNumericCellValue()).isEqualTo(15);
            assertThat(sheet.getRow(4).getCell(4).getNumericCellValue()).isEqualTo(25);
            assertThat(sheet.getRow(4).getCell(5).getNumericCellValue()).isEqualTo(60);
            assertThat(sheet.getRow(5).getCell(0).getNumericCellValue()).isEqualTo(2);
            assertThat(sheet.getRow(5).getCell(1).getStringCellValue()).isEqualTo("팀2");
            assertThat(sheet.getRow(5).getCell(2).getNumericCellValue()).isZero();
            assertThat(sheet.getRow(5).getCell(3).getNumericCellValue()).isZero();
            assertThat(sheet.getRow(5).getCell(4).getNumericCellValue()).isZero();
            assertThat(sheet.getRow(5).getCell(5).getNumericCellValue()).isZero();
            assertThat(workbook.getAllPictures()).hasSize(1);
            assertThat(renderedImageHasColor(workbook.getAllPictures().getFirst(), Color.RED)).isTrue();
            assertThat(renderedImageHasColor(workbook.getAllPictures().getFirst(), new Color(0x12, 0x12, 0x12))).isTrue();
            assertCommentPicture(workbook, 4);
        }
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(files.get("심사위원_B_개별심사표.xlsx")))) {
            assertCommentPicture(workbook, 4);
        }
    }

    @Test
    void 코멘트를_1000x500_PNG로_렌더링하고_G셀_100x50_안에_고정한다() throws Exception {
        List<TeamEntity> teams = java.util.stream.IntStream.rangeClosed(1, 6)
                .mapToObj(index -> team(index, index))
                .toList();
        UserEntity judge = user(10L);
        given(teamRepository.findAllByOrderByPerformOrderAsc()).willReturn(teams);
        given(judgementRepository.findAllWithUserAndTeam()).willReturn(List.of());
        given(judgeCommentRepository.findAllWithUserAndTeam()).willReturn(List.of(
                comment(teams.get(0), judge, stroke("#ff0000", 0.1, 0.5, 0.9, 0.5)),
                comment(teams.get(1), judge, stroke("#121212", 0.5, 0.1, 0.5, 0.9)),
                comment(teams.get(2), judge, stroke("#0000ff", 0.45, 0.45, 0.55, 0.55)),
                comment(teams.get(3), judge, stroke("#00ff00", 0, 0, 1, 1)),
                comment(teams.get(4), judge, strokes()),
                comment(teams.get(5), judge, emptyStroke())
        ));
        given(userRepository.findAllByRoleOrderByIdAsc(Role.JUDGE)).willReturn(List.of(judge));

        Map<String, byte[]> files = zipEntries(service.execute());

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(files.get("심사위원_A_개별심사표.xlsx")))) {
            var sheet = workbook.getSheet("개별 심사표");
            var pictures = sheet.getDrawingPatriarch().getShapes().stream()
                    .map(XSSFPicture.class::cast)
                    .toList();
            assertThat(pictures).hasSize(5);
            pictures.forEach(picture -> {
                BufferedImage image;
                try {
                    image = ImageIO.read(new ByteArrayInputStream(picture.getPictureData().getData()));
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
                assertThat(image.getWidth()).isEqualTo(1000);
                assertThat(image.getHeight()).isEqualTo(500);
            });

            Rectangle horizontal = renderedBounds(pictures.get(0).getPictureData());
            Rectangle vertical = renderedBounds(pictures.get(1).getPictureData());
            Rectangle center = renderedBounds(pictures.get(2).getPictureData());
            Rectangle corner = renderedBounds(pictures.get(3).getPictureData());
            assertThat(horizontal.width).isGreaterThan(horizontal.height);
            assertThat(horizontal.getCenterY()).isBetween(249d, 251d);
            assertThat(vertical.height).isGreaterThan(vertical.width);
            assertThat(vertical.getCenterX()).isBetween(499d, 501d);
            assertThat(center.getCenterX()).isBetween(499d, 501d);
            assertThat(center.getCenterY()).isBetween(249d, 251d);
            assertThat(corner.x).isGreaterThanOrEqualTo(18);
            assertThat(corner.y).isGreaterThanOrEqualTo(8);
            assertThat(corner.getMaxX()).isLessThanOrEqualTo(982);
            assertThat(corner.getMaxY()).isLessThanOrEqualTo(492);
            assertThat(renderedImageHasColor(pictures.get(4).getPictureData(), Color.RED)).isTrue();
            assertThat(renderedImageHasColor(pictures.get(4).getPictureData(), new Color(0x12, 0x12, 0x12))).isTrue();
            assertThat(pictures).extracting(picture -> picture.getClientAnchor().getRow1())
                    .containsExactly(4, 5, 6, 7, 8);
            assertThat(sheet.getRow(9).getHeightInPoints()).isEqualTo(37.5f);
        }
    }

    @Test
    void 심사위원별_소속_직위_이름을_각_병합셀에_삽입한다() throws Exception {
        UserEntity judgeA = user(10L);
        UserEntity judgeB = user(20L);
        given(teamRepository.findAllByOrderByPerformOrderAsc()).willReturn(List.of());
        given(judgementRepository.findAllWithUserAndTeam()).willReturn(List.of());
        given(judgeCommentRepository.findAllWithUserAndTeam()).willReturn(List.of());
        given(judgeProfileRepository.findAllWithUser()).willReturn(List.of(
                profile(judgeA,
                        path(0.1, 0.5, 0.9, 0.5),
                        path(0.1, 0.5, 0.9, 0.5),
                        path(0.5, 0.1, 0.5, 0.9)),
                profile(judgeB,
                        path(0.1, 0.5, 0.9, 0.5),
                        emptyPath(),
                        objectMapper.createArrayNode())
        ));
        given(userRepository.findAllByRoleOrderByIdAsc(Role.JUDGE)).willReturn(List.of(judgeA, judgeB));

        Map<String, byte[]> files = zipEntries(service.execute());

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(files.get("심사위원_A_개별심사표.xlsx")))) {
            var sheet = workbook.getSheet("개별 심사표");
            var pictures = sheet.getDrawingPatriarch().getShapes().stream()
                    .map(XSSFPicture.class::cast)
                    .toList();
            assertThat(pictures).hasSize(3);
            assertProfilePicture(sheet, pictures.get(0), 1);
            assertProfilePicture(sheet, pictures.get(1), 3);
            assertProfilePicture(sheet, pictures.get(2), 5);
            for (XSSFPicture picture : pictures) {
                assertThat(renderedImageHasColor(picture.getPictureData(), Color.BLACK)).isTrue();
            }
            assertThat(sheet.getDrawingPatriarch().getCTDrawing().getTwoCellAnchorList())
                    .allMatch(anchor -> anchor.getEditAs() == STEditAs.ONE_CELL);
        }
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(files.get("심사위원_B_개별심사표.xlsx")))) {
            var pictures = workbook.getSheet("개별 심사표").getDrawingPatriarch().getShapes();
            assertThat(pictures).hasSize(1);
            assertThat(renderedImageHasColor(
                    ((XSSFPicture) pictures.getFirst()).getPictureData(), Color.BLACK)).isTrue();
        }
    }

    @Test
    void 팀을_중간_빈_행_없이_연속으로_작성한다() throws Exception {
        List<TeamEntity> teams = java.util.stream.IntStream.rangeClosed(1, 10)
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
            assertThat(sheet.getRow(11).getCell(0).getNumericCellValue()).isEqualTo(8);
            assertThat(sheet.getRow(12).getCell(0).getNumericCellValue()).isEqualTo(9);
            assertThat(sheet.getRow(13).getCell(0).getNumericCellValue()).isEqualTo(10);
        }
    }

    private byte[] template() {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("개별 심사표");
            for (int rowIndex = 0; rowIndex < 15; rowIndex++) {
                var row = sheet.createRow(rowIndex);
                for (int column = 0; column < 7; column++) {
                    row.createCell(column);
                }
            }
            sheet.getRow(0).getCell(0).setCellValue("원본 제목");
            sheet.getRow(2).getCell(0).setCellValue("소속/직위/이름");
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 1, 2));
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 3, 4));
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 5, 6));
            sheet.getRow(12).getCell(0).setCellValue("13행 보존");
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

    private Rectangle renderedBounds(PictureData picture) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(picture.getData()));
        int minX = image.getWidth();
        int minY = image.getHeight();
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) & 0x00ffffff) != 0x00ffffff) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    private void assertCommentPicture(XSSFWorkbook workbook, int rowIndex) throws IOException {
        var sheet = workbook.getSheet("개별 심사표");
        assertThat(sheet.getColumnWidthInPixels(6)).isBetween(99.9f, 100.1f);
        assertThat(sheet.getRow(rowIndex).getHeightInPoints()).isEqualTo(37.5f);
        assertThat(sheet.getMergedRegions()).noneMatch(region -> region.isInRange(rowIndex, 6));

        XSSFPicture picture = (XSSFPicture) sheet.getDrawingPatriarch().getShapes().getFirst();
        ClientAnchor anchor = picture.getClientAnchor();
        assertThat(anchor.getCol1()).isEqualTo((short) 6);
        assertThat(anchor.getCol2()).isEqualTo((short) 6);
        assertThat(anchor.getRow1()).isEqualTo(rowIndex);
        assertThat(anchor.getRow2()).isEqualTo(rowIndex);
        assertThat(anchor.getDx1()).isZero();
        assertThat(anchor.getDy1()).isZero();
        assertThat(anchor.getDx2()).isEqualTo(Units.pixelToEMU(100));
        assertThat(anchor.getDy2()).isEqualTo(Units.pixelToEMU(50));
        XSSFDrawing drawing = sheet.getDrawingPatriarch();
        assertThat(drawing.getCTDrawing().getTwoCellAnchorArray(0).getEditAs()).isEqualTo(STEditAs.ONE_CELL);

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(picture.getPictureData().getData()));
        assertThat(image.getWidth()).isEqualTo(1000);
        assertThat(image.getHeight()).isEqualTo(500);
    }

    private void assertProfilePicture(
            org.apache.poi.ss.usermodel.Sheet sheet,
            XSSFPicture picture,
            int column) throws IOException {
        ClientAnchor anchor = picture.getClientAnchor();
        assertThat(anchor.getCol1()).isEqualTo((short) column);
        assertThat(anchor.getCol2()).isEqualTo((short) (column + 2));
        assertThat(anchor.getRow1()).isEqualTo(2);
        assertThat(anchor.getRow2()).isEqualTo(3);
        assertThat(anchor.getDx1()).isZero();
        assertThat(anchor.getDy1()).isZero();
        assertThat(anchor.getDx2()).isZero();
        assertThat(anchor.getDy2()).isZero();

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(picture.getPictureData().getData()));
        assertThat(image.getWidth()).isEqualTo(1000);
        assertThat(image.getHeight()).isEqualTo(500);
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

    private JudgeProfileEntity profile(
            UserEntity user,
            ArrayNode affiliation,
            ArrayNode position,
            ArrayNode name) {
        return JudgeProfileEntity.builder()
                .user(user)
                .affiliationStrokes(affiliation)
                .positionStrokes(position)
                .nameStrokes(name)
                .build();
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

    private ArrayNode stroke(String color, double x1, double y1, double x2, double y2) {
        ArrayNode strokes = objectMapper.createArrayNode();
        var points = strokes.addObject().put("color", color).putArray("points");
        points.addObject().put("x", x1).put("y", y1);
        points.addObject().put("x", x2).put("y", y2);
        return strokes;
    }

    private ArrayNode path(double x1, double y1, double x2, double y2) {
        ArrayNode strokes = objectMapper.createArrayNode();
        var points = strokes.addArray();
        points.addObject().put("x", x1).put("y", y1);
        points.addObject().put("x", x2).put("y", y2);
        return strokes;
    }

    private ArrayNode emptyStroke() {
        ArrayNode strokes = objectMapper.createArrayNode();
        strokes.addObject().put("color", "#000000").putArray("points");
        return strokes;
    }

    private ArrayNode emptyPath() {
        ArrayNode strokes = objectMapper.createArrayNode();
        strokes.addArray();
        return strokes;
    }
}
