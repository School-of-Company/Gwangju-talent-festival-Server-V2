INSERT IGNORE INTO seat_lock (seat_key)
WITH RECURSIVE seat_numbers AS (
    SELECT 1 AS seat_number
    UNION ALL
    SELECT seat_number + 1
    FROM seat_numbers
    WHERE seat_number < 132
), sections AS (
    SELECT 'A' AS seat_section, 101 AS max_seat_number
    UNION ALL SELECT 'B', 132
    UNION ALL SELECT 'C', 101
    UNION ALL SELECT 'D', 89
    UNION ALL SELECT 'E', 96
    UNION ALL SELECT 'F', 89
)
SELECT CONCAT(seat_section, ':', seat_number)
FROM sections
JOIN seat_numbers ON seat_number <= max_seat_number;
