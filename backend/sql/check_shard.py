import subprocess
hex_digits = '0123456789abcdef'
existing = set()
for a in hex_digits:
    for b in hex_digits:
        existing.add('images_' + a + b)

r = subprocess.run(['mysql', '-u', 'root', '-p123456', '--default-character-set=utf8mb4', '-N', '-e', 'SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA="photo_management" AND TABLE_NAME LIKE "images_%";'], capture_output=True, text=True)
db_tables = set(line.strip() for line in r.stdout.strip().split('\n') if line.strip())

missing = [t for t in sorted(existing) if t not in db_tables]
print(f'Total needed: {len(existing)}, Created: {len(db_tables)}, Missing: {len(missing)}')
if missing:
    sql_lines = ['USE photo_management;', '']
    for t in missing:
        sql = f'''CREATE TABLE IF NOT EXISTS {t} (
    id BIGINT NOT NULL AUTO_INCREMENT,
    gallery_id BIGINT DEFAULT NULL,
    sample_id BIGINT DEFAULT NULL,
    file_name VARCHAR(200) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    thumbnail_path VARCHAR(500) DEFAULT NULL,
    file_size BIGINT NOT NULL DEFAULT 0,
    file_type VARCHAR(50) DEFAULT NULL,
    width INT DEFAULT NULL,
    height INT DEFAULT NULL,
    hash VARCHAR(64) DEFAULT NULL,
    description VARCHAR(500) DEFAULT NULL,
    tags VARCHAR(500) DEFAULT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_gallery_id (gallery_id),
    KEY idx_sample_id (sample_id),
    KEY idx_hash (hash),
    KEY idx_create_by (create_by),
    KEY idx_create_time (create_time),
    KEY idx_gallery_sort (gallery_id, sort_order, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;'''
        sql_lines.append(sql)
        sql_lines.append('')

    with open('D:/temp/shard_fix.sql', 'w', encoding='utf-8') as f:
        f.write('\n'.join(sql_lines))
    print(f'Wrote {len(missing)} missing tables to D:/temp/shard_fix.sql')
else:
    print('All 256 tables exist!')
