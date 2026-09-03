package team.startup.gwangjutalentfestival.domain.excel.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import team.startup.gwangjutalentfestival.domain.excel.exception.JudgeSheetExportException;
import team.startup.gwangjutalentfestival.domain.excel.service.DownloadJudgeSheetsService;
import team.startup.gwangjutalentfestival.domain.excel.service.DownloadJudgingSummaryExcelService;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgeCommentEntity;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgeProfileEntity;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgementEntity;
import team.startup.gwangjutalentfestival.domain.judge.repository.JudgeCommentRepository;
import team.startup.gwangjutalentfestival.domain.judge.repository.JudgeProfileRepository;
import team.startup.gwangjutalentfestival.domain.judge.repository.JudgementRepository;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.team.repository.TeamRepository;
import team.startup.gwangjutalentfestival.domain.user.entity.UserEntity;
import team.startup.gwangjutalentfestival.domain.user.enums.Role;
import team.startup.gwangjutalentfestival.domain.user.repository.UserRepository;
import team.startup.gwangjutalentfestival.global.thirdparty.google.adapter.GoogleExcelAdapter;
import team.startup.gwangjutalentfestival.global.thirdparty.google.properties.GoogleExcelProperties;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static team.startup.gwangjutalentfestival.global.thirdparty.google.exception.GoogleSheetsConfigMissingException.require;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadJudgeSheetsServiceImpl implements DownloadJudgeSheetsService {

    private static final int IMAGE_WIDTH = 1000;
    private static final int IMAGE_HEIGHT = 500;
    private static final int IMAGE_PADDING = 10;
    private static final int COMMENT_CELL_WIDTH = 100;
    private static final int COMMENT_CELL_HEIGHT = 50;
    private static final int CELL_PADDING = 6;
    private static final int PROFILE_ROW = 1;
    private static final int HEADER_ROW = 2;
    private static final int FIRST_DATA_ROW = 3;
    private static final int AFFILIATION_COLUMN = 1;
    private static final int POSITION_COLUMN = 3;
    private static final int NAME_COLUMN = 5;
    private static final int PROFILE_COLUMN_SPAN = 2;
    private static final int TEAM_NAME_COLUMN = 1;
    private static final int COMPLETENESS_COLUMN = 2;
    private static final int CREATIVITY_COLUMN = 3;
    private static final int STAGE_COLUMN = 4;
    private static final int CALCULATED_SCORE_COLUMN = 5;
    private static final int COMMENT_COLUMN = 6;

    private final TeamRepository teamRepository;
    private final JudgementRepository judgementRepository;
    private final JudgeCommentRepository judgeCommentRepository;
    private final JudgeProfileRepository judgeProfileRepository;
    private final UserRepository userRepository;
    private final DownloadJudgingSummaryExcelService downloadJudgingSummaryExcelService;
    private final GoogleExcelAdapter googleExcelAdapter;
    private final GoogleExcelProperties googleExcelProperties;

    @Override
    public byte[] execute() {
        List<TeamEntity> teams = teamRepository.findAllByOrderByPerformOrderAsc();
        List<JudgementEntity> judgements = judgementRepository.findAllWithUserAndTeam();
        Map<JudgeTeamKey, JudgeCommentEntity> comments = commentMap(judgeCommentRepository.findAllWithUserAndTeam());
        Map<Long, JudgeProfileEntity> profiles = profileMap(judgeProfileRepository.findAllWithUser());
        byte[] judgeTemplate = googleExcelAdapter.exportJudgeTemplate();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(output)) {
            writeZipEntry(zip, "심사집계표.xlsx", downloadJudgingSummaryExcelService.execute());

            List<Long> judgeIds = userRepository.findAllByRoleOrderByIdAsc(Role.JUDGE).stream()
                    .map(UserEntity::getId)
                    .toList();
            for (int index = 0; index < judgeIds.size(); index++) {
                Long judgeId = judgeIds.get(index);
                writeZipEntry(zip, "심사위원_" + judgeLabel(index) + "_개별심사표.xlsx",
                        createJudgeSheet(
                                judgeTemplate, teams, judgements, comments, profiles.get(judgeId), judgeId));
            }
        } catch (IOException e) {
            log.error("심사표 ZIP 생성 실패 - message: {}", e.getMessage(), e);
            throw new JudgeSheetExportException();
        }
        return output.toByteArray();
    }

    private byte[] createJudgeSheet(
            byte[] template,
            List<TeamEntity> teams,
            List<JudgementEntity> judgements,
            Map<JudgeTeamKey, JudgeCommentEntity> comments,
            JudgeProfileEntity profile,
            Long judgeId) throws IOException {
        Map<Long, JudgementEntity> scoreMap = new HashMap<>();
        judgements.stream()
                .filter(judgement -> judgeId.equals(judgement.getUser().getId()))
                .forEach(judgement -> scoreMap.put(judgement.getTeam().getId(), judgement));

        String templatePage = require(googleExcelProperties.judgeTemplatePage(), "google.excel.judge-template-page");
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(template)); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.getSheet(templatePage);
            if (sheet == null) {
                log.error("개별 심사표 템플릿 탭을 찾을 수 없음 - page: {}", templatePage);
                throw new JudgeSheetExportException();
            }
            Row headerRow = row(sheet, HEADER_ROW);
            cell(headerRow, 0).setCellValue("심사번호");
            cell(headerRow, TEAM_NAME_COLUMN).setCellValue("팀명");
            cell(headerRow, COMPLETENESS_COLUMN).setCellValue("완성도·표현력");
            cell(headerRow, CREATIVITY_COLUMN).setCellValue("창의력·구성");
            cell(headerRow, STAGE_COLUMN).setCellValue("무대매너·퍼포먼스");
            cell(headerRow, CALCULATED_SCORE_COLUMN).setCellValue("총합 점수");
            cell(headerRow, COMMENT_COLUMN).setCellValue("심사 의견");
            sheet.setColumnWidth(COMMENT_COLUMN,
                    Math.round(COMMENT_CELL_WIDTH / Units.DEFAULT_CHARACTER_WIDTH * 256));

            Drawing<?> drawing = sheet.getDrawingPatriarch();
            if (drawing == null) {
                drawing = sheet.createDrawingPatriarch();
            }
            addProfileImages(workbook, drawing, sheet, profile);
            for (int index = 0; index < teams.size(); index++) {
                TeamEntity team = teams.get(index);
                JudgementEntity judgement = scoreMap.get(team.getId());
                int rowIndex = dataRow(index);
                Row row = row(sheet, rowIndex);
                row.setHeightInPoints((float) Units.pixelToPoints(COMMENT_CELL_HEIGHT));
                cell(row, 0).setCellValue(orZero(team.getPerformOrder()));
                cell(row, TEAM_NAME_COLUMN).setCellValue(team.getTeamName());
                int completeness = judgement == null ? 0 : orZero(judgement.getCompletenessExpressionScore());
                int creativity = judgement == null ? 0 : orZero(judgement.getCreativityCompositionScore());
                int stage = judgement == null ? 0 : orZero(judgement.getStagePerformanceTeamworkScore());
                cell(row, COMPLETENESS_COLUMN).setCellValue(completeness);
                cell(row, CREATIVITY_COLUMN).setCellValue(creativity);
                cell(row, STAGE_COLUMN).setCellValue(stage);
                cell(row, CALCULATED_SCORE_COLUMN).setCellValue(completeness + creativity + stage);

                JudgeCommentEntity comment = comments.get(new JudgeTeamKey(judgeId, team.getId()));
                if (comment != null && hasPoints(comment.getStrokes())) {
                    addImage(
                            workbook,
                            drawing,
                            sheet,
                            COMMENT_COLUMN,
                            1,
                            rowIndex,
                            CELL_PADDING,
                            renderStrokes(comment.getStrokes()));
                }
            }
            workbook.write(output);
            return output.toByteArray();
        }
    }

    private void addProfileImages(
            Workbook workbook,
            Drawing<?> drawing,
            Sheet sheet,
            JudgeProfileEntity profile) throws IOException {
        if (profile == null) {
            return;
        }
        addProfileImage(workbook, drawing, sheet, AFFILIATION_COLUMN, profile.getAffiliationStrokes());
        addProfileImage(workbook, drawing, sheet, POSITION_COLUMN, profile.getPositionStrokes());
        addProfileImage(workbook, drawing, sheet, NAME_COLUMN, profile.getNameStrokes());
    }

    private void addProfileImage(
            Workbook workbook,
            Drawing<?> drawing,
            Sheet sheet,
            int column,
            JsonNode strokes) throws IOException {
        if (!hasPoints(strokes)) {
            return;
        }
        addImage(
                workbook,
                drawing,
                sheet,
                column,
                PROFILE_COLUMN_SPAN,
                PROFILE_ROW,
                CELL_PADDING,
                renderStrokes(strokes));
    }

    /**
     * 이미지를 대상 영역 안쪽에 배치한다.
     * <p>영역은 {@code startColumn}부터 {@code columnSpan}개 열과 {@code rowIndex} 한 행이며,
     * 네 방향 모두 {@code paddingPx}만큼 안으로 들여 표 테두리에 획이 닿지 않게 한다.
     * 여백이 영역보다 크면 해당 축의 여백을 버린다.</p>
     */
    private void addImage(
            Workbook workbook,
            Drawing<?> drawing,
            Sheet sheet,
            int startColumn,
            int columnSpan,
            int rowIndex,
            int paddingPx,
            byte[] image) {
        int lastColumn = startColumn + columnSpan - 1;
        int areaWidth = 0;
        for (int column = startColumn; column <= lastColumn; column++) {
            areaWidth += Math.round(sheet.getColumnWidthInPixels(column));
        }
        int lastColumnWidth = Math.round(sheet.getColumnWidthInPixels(lastColumn));
        int rowHeight = Units.pointsToPixel(row(sheet, rowIndex).getHeightInPoints());
        int horizontalPadding = fitPadding(paddingPx, areaWidth);
        int verticalPadding = fitPadding(paddingPx, rowHeight);

        int pictureIndex = workbook.addPicture(image, Workbook.PICTURE_TYPE_PNG);
        ClientAnchor anchor = workbook.getCreationHelper().createClientAnchor();
        anchor.setCol1(startColumn);
        anchor.setDx1(Units.pixelToEMU(horizontalPadding));
        anchor.setCol2(lastColumn);
        anchor.setDx2(Units.pixelToEMU(lastColumnWidth - horizontalPadding));
        anchor.setRow1(rowIndex);
        anchor.setDy1(Units.pixelToEMU(verticalPadding));
        anchor.setRow2(rowIndex);
        anchor.setDy2(Units.pixelToEMU(rowHeight - verticalPadding));
        anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_DONT_RESIZE);
        drawing.createPicture(anchor, pictureIndex);
    }

    private int fitPadding(int paddingPx, int availablePx) {
        return availablePx > paddingPx * 2 ? paddingPx : 0;
    }

    private byte[] renderStrokes(JsonNode strokes) throws IOException {
        double scale = Math.min(
                (IMAGE_WIDTH - IMAGE_PADDING * 2d) / IMAGE_WIDTH,
                (IMAGE_HEIGHT - IMAGE_PADDING * 2d) / IMAGE_HEIGHT);
        double offsetX = (IMAGE_WIDTH - IMAGE_WIDTH * scale) / 2;
        double offsetY = (IMAGE_HEIGHT - IMAGE_HEIGHT * scale) / 2;
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
            graphics.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (JsonNode stroke : strokes) {
                JsonNode points = strokePoints(stroke);
                if (!points.isArray() || points.isEmpty()) {
                    continue;
                }
                graphics.setColor(parseColor(stroke.path("color").asText()));
                Path2D path = new Path2D.Double();
                boolean started = false;
                for (JsonNode point : points) {
                    if (!isPoint(point)) {
                        continue;
                    }
                    double x = offsetX + Math.clamp(point.path("x").asDouble(), 0, 1) * IMAGE_WIDTH * scale;
                    double y = offsetY + Math.clamp(point.path("y").asDouble(), 0, 1) * IMAGE_HEIGHT * scale;
                    if (started) {
                        path.lineTo(x, y);
                    } else {
                        path.moveTo(x, y);
                        path.lineTo(x, y);
                        started = true;
                    }
                }
                if (started) {
                    graphics.draw(path);
                }
            }
        } finally {
            graphics.dispose();
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }

    private boolean hasPoints(JsonNode strokes) {
        if (strokes == null || !strokes.isArray()) {
            return false;
        }
        for (JsonNode stroke : strokes) {
            for (JsonNode point : strokePoints(stroke)) {
                if (isPoint(point)) {
                    return true;
                }
            }
        }
        return false;
    }

    private JsonNode strokePoints(JsonNode stroke) {
        return stroke.isArray() ? stroke : stroke.path("points");
    }

    private boolean isPoint(JsonNode point) {
        return point.path("x").isNumber()
                && point.path("y").isNumber()
                && Double.isFinite(point.path("x").asDouble())
                && Double.isFinite(point.path("y").asDouble());
    }

    private Map<JudgeTeamKey, JudgeCommentEntity> commentMap(List<JudgeCommentEntity> comments) {
        Map<JudgeTeamKey, JudgeCommentEntity> result = new HashMap<>();
        comments.forEach(comment -> result.put(new JudgeTeamKey(comment.getUser().getId(), comment.getTeam().getId()), comment));
        return result;
    }

    private Map<Long, JudgeProfileEntity> profileMap(List<JudgeProfileEntity> profiles) {
        Map<Long, JudgeProfileEntity> result = new HashMap<>();
        profiles.forEach(profile -> result.put(profile.getUser().getId(), profile));
        return result;
    }

    private void writeZipEntry(ZipOutputStream zip, String name, byte[] content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content);
        zip.closeEntry();
    }

    private Color parseColor(String value) {
        try {
            return Color.decode(value);
        } catch (NumberFormatException e) {
            return Color.BLACK;
        }
    }

    private int orZero(Integer value) {
        return value == null ? 0 : value;
    }

    private Row row(Sheet sheet, int index) {
        Row row = sheet.getRow(index);
        return row == null ? sheet.createRow(index) : row;
    }

    private org.apache.poi.ss.usermodel.Cell cell(Row row, int index) {
        org.apache.poi.ss.usermodel.Cell cell = row.getCell(index);
        return cell == null ? row.createCell(index) : cell;
    }

    private int dataRow(int teamIndex) {
        return FIRST_DATA_ROW + teamIndex;
    }

    private String judgeLabel(int index) {
        return String.valueOf((char) ('A' + index));
    }

    private record JudgeTeamKey(Long judgeId, Long teamId) {
    }
}
