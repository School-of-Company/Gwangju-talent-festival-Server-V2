# GitHub Reply Formats

Use these templates when posting inline replies in Step 6.
Always quote `comment_id` to prevent shell injection.
All replies must be written in Korean.

## VALID — fix succeeded

```
@<reviewer> <abc1234> 에서 반영했습니다. (근거: <출처>)
```

## VALID — fix failed

```
@<reviewer> 지적 사항이 타당합니다. 직접 수정이 필요하여 별도 처리하겠습니다.
```

## INVALID

```
@<reviewer> 해당 지적은 이 프로젝트의 컨벤션과 다릅니다.
근거: <출처> — "<규칙 인용>"
현재 코드가 규칙을 올바르게 따르고 있어 변경하지 않겠습니다.
```

## PARTIAL — accepted

```
@<reviewer> 부분적으로 타당하다고 판단하여 <abc1234> 에서 반영했습니다.
```

## PARTIAL — rejected

```
@<reviewer> 검토 결과 이 방향으로는 적용하지 않기로 결정했습니다.
```

## PARTIAL — pending

```
@<reviewer> 검토 중입니다. 추후 답변드리겠습니다.
```
