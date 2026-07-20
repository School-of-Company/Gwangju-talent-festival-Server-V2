package team.startup.gwangjutalentfestival.domain.excel.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import team.startup.gwangjutalentfestival.domain.excel.service.DownloadJudgeSheetsService;
import team.startup.gwangjutalentfestival.domain.excel.service.DownloadJudgingSummaryExcelService;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgeCommentEntity;
import team.startup.gwangjutalentfestival.domain.judge.entity.JudgementEntity;
import team.startup.gwangjutalentfestival.domain.judge.repository.JudgeCommentRepository;
import team.startup.gwangjutalentfestival.domain.judge.repository.JudgementRepository;
import team.startup.gwangjutalentfestival.domain.team.entity.TeamEntity;
import team.startup.gwangjutalentfestival.domain.team.repository.TeamRepository;
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

@Service
@RequiredArgsConstructor
public class DownloadJudgeSheetsServiceImpl implements DownloadJudgeSheetsService {

    private static final int COMMENT_WIDTH = 360;
    private static final int COMMENT_HEIGHT = 160;
    private static final int HEADER_ROW = 2;
    private static final int FIRST_DATA_ROW = 3;
    private static final int PRESERVED_ROW = 11;
    private static final int COMMENT_COLUMN = 2;

    private final TeamRepository teamRepository;
    private final JudgementRepository judgementRepository;
    private final JudgeCommentRepository judgeCommentRepository;
    private final DownloadJudgingSummaryExcelService downloadJudgingSummaryExcelService;
    private final GoogleExcelAdapter googleExcelAdapter;
    private final GoogleExcelProperties googleExcelProperties;

    @Override
    public byte[] execute() {
        List<TeamEntity> teams = teamRepository.findAllByOrderByPerformOrderAsc();
        List<JudgementEntity> judgements = judgementRepository.findAllWithUserAndTeam();
        Map<JudgeTeamKey, JudgeCommentEntity> comments = commentMap(judgeCommentRepository.findAllWithUserAndTeam());
        byte[] judgeTemplate = googleExcelAdapter.exportJudgeTemplate();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(output)) {
            writeZipEntry(zip, "심사집계표.xlsx", downloadJudgingSummaryExcelService.execute());

            List<Long> judgeIds = judgements.stream()
                    .map(judgement -> judgement.getUser().getId())
                    .distinct()
                    .sorted()
                    .toList();
            for (int index = 0; index < judgeIds.size(); index++) {
                Long judgeId = judgeIds.get(index);
                writeZipEntry(zip, "심사위원_" + judgeLabel(index) + "_개별심사표.xlsx",
                        createJudgeSheet(judgeTemplate, teams, judgements, comments, judgeId, judgeLabel(index)));
            }
        } catch (IOException e) {
            throw new IllegalStateException("심사표 ZIP을 생성할 수 없습니다.", e);
        }
        return output.toByteArray();
    }

    private byte[] createJudgeSheet(
            byte[] template,
            List<TeamEntity> teams,
            List<JudgementEntity> judgements,
            Map<JudgeTeamKey, JudgeCommentEntity> comments,
            Long judgeId,
            String judgeLabel) throws IOException {
        Map<Long, JudgementEntity> scoreMap = new HashMap<>();
        judgements.stream()
                .filter(judgement -> judgeId.equals(judgement.getUser().getId()))
                .forEach(judgement -> scoreMap.put(judgement.getTeam().getId(), judgement));

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(template)); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.getSheet(googleExcelProperties.judgeTemplatePage());
            if (sheet == null) {
                throw new IllegalStateException("개별 심사표 템플릿 탭을 찾을 수 없습니다.");
            }
            Row headerRow = row(sheet, HEADER_ROW);
            cell(headerRow, 0).setCellValue("심사순서");
            cell(headerRow, 1).setCellValue("심사위원 " + judgeLabel);
            cell(headerRow, COMMENT_COLUMN).setCellValue("코멘트");

            Drawing<?> drawing = sheet.getDrawingPatriarch();
            if (drawing == null) {
                drawing = sheet.createDrawingPatriarch();
            }
            for (int index = 0; index < teams.size(); index++) {
                TeamEntity team = teams.get(index);
                JudgementEntity judgement = scoreMap.get(team.getId());
                int rowIndex = dataRow(index);
                Row row = row(sheet, rowIndex);
                cell(row, 0).setCellValue(orZero(team.getPerformOrder()));
                int completeness = judgement == null ? 0 : orZero(judgement.getCompletenessExpressionScore());
                int creativity = judgement == null ? 0 : orZero(judgement.getCreativityCompositionScore());
                int stage = judgement == null ? 0 : orZero(judgement.getStagePerformanceTeamworkScore());
                cell(row, 1).setCellValue(completeness + creativity + stage);

                JudgeCommentEntity comment = comments.get(new JudgeTeamKey(judgeId, team.getId()));
                if (comment != null && comment.getStrokes() != null && comment.getStrokes().isArray() && !comment.getStrokes().isEmpty()) {
                    addCommentImage(workbook, drawing, rowIndex, renderComment(comment.getStrokes()));
                }
            }
            workbook.write(output);
            return output.toByteArray();
        }
    }

    private void addCommentImage(Workbook workbook, Drawing<?> drawing, int rowIndex, byte[] image) {
        int pictureIndex = workbook.addPicture(image, Workbook.PICTURE_TYPE_PNG);
        ClientAnchor anchor = workbook.getCreationHelper().createClientAnchor();
        anchor.setCol1(COMMENT_COLUMN);
        anchor.setCol2(COMMENT_COLUMN + 2);
        anchor.setRow1(rowIndex);
        anchor.setRow2(rowIndex + 1);
        drawing.createPicture(anchor, pictureIndex);
    }

    private byte[] renderComment(JsonNode strokes) throws IOException {
        BufferedImage image = new BufferedImage(COMMENT_WIDTH, COMMENT_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, COMMENT_WIDTH, COMMENT_HEIGHT);
            graphics.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (JsonNode stroke : strokes) {
                JsonNode points = stroke.path("points");
                if (!points.isArray() || points.isEmpty()) {
                    continue;
                }
                graphics.setColor(parseColor(stroke.path("color").asText()));
                Path2D path = new Path2D.Double();
                boolean started = false;
                for (JsonNode point : points) {
                    if (!point.has("x") || !point.has("y")) {
                        continue;
                    }
                    double x = Math.clamp(point.path("x").asDouble(), 0, 1) * (COMMENT_WIDTH - 1);
                    double y = Math.clamp(point.path("y").asDouble(), 0, 1) * (COMMENT_HEIGHT - 1);
                    if (started) {
                        path.lineTo(x, y);
                    } else {
                        path.moveTo(x, y);
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

    private Map<JudgeTeamKey, JudgeCommentEntity> commentMap(List<JudgeCommentEntity> comments) {
        Map<JudgeTeamKey, JudgeCommentEntity> result = new HashMap<>();
        comments.forEach(comment -> result.put(new JudgeTeamKey(comment.getUser().getId(), comment.getTeam().getId()), comment));
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
        int row = FIRST_DATA_ROW + teamIndex;
        return row >= PRESERVED_ROW ? row + 1 : row;
    }

    private String judgeLabel(int index) {
        return String.valueOf((char) ('A' + index));
    }

    private record JudgeTeamKey(Long judgeId, Long teamId) {
    }
}
