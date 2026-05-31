-- Populate sample_thumbnail from all image shards
TRUNCATE TABLE sample_thumbnail;
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_00
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_00 i2 WHERE i2.sample_id = images_00.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_01
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_01 i2 WHERE i2.sample_id = images_01.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_02
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_02 i2 WHERE i2.sample_id = images_02.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_03
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_03 i2 WHERE i2.sample_id = images_03.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_04
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_04 i2 WHERE i2.sample_id = images_04.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_05
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_05 i2 WHERE i2.sample_id = images_05.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_06
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_06 i2 WHERE i2.sample_id = images_06.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_07
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_07 i2 WHERE i2.sample_id = images_07.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_08
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_08 i2 WHERE i2.sample_id = images_08.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_09
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_09 i2 WHERE i2.sample_id = images_09.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_0a
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_0a i2 WHERE i2.sample_id = images_0a.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_0b
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_0b i2 WHERE i2.sample_id = images_0b.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_0c
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_0c i2 WHERE i2.sample_id = images_0c.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_0d
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_0d i2 WHERE i2.sample_id = images_0d.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_0e
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_0e i2 WHERE i2.sample_id = images_0e.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_0f
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_0f i2 WHERE i2.sample_id = images_0f.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_10
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_10 i2 WHERE i2.sample_id = images_10.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_11
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_11 i2 WHERE i2.sample_id = images_11.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_12
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_12 i2 WHERE i2.sample_id = images_12.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_13
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_13 i2 WHERE i2.sample_id = images_13.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_14
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_14 i2 WHERE i2.sample_id = images_14.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_15
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_15 i2 WHERE i2.sample_id = images_15.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_16
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_16 i2 WHERE i2.sample_id = images_16.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_17
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_17 i2 WHERE i2.sample_id = images_17.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_18
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_18 i2 WHERE i2.sample_id = images_18.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_19
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_19 i2 WHERE i2.sample_id = images_19.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_1a
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_1a i2 WHERE i2.sample_id = images_1a.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_1b
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_1b i2 WHERE i2.sample_id = images_1b.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_1c
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_1c i2 WHERE i2.sample_id = images_1c.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_1d
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_1d i2 WHERE i2.sample_id = images_1d.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_1e
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_1e i2 WHERE i2.sample_id = images_1e.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_1f
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_1f i2 WHERE i2.sample_id = images_1f.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_20
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_20 i2 WHERE i2.sample_id = images_20.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_21
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_21 i2 WHERE i2.sample_id = images_21.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_22
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_22 i2 WHERE i2.sample_id = images_22.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_23
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_23 i2 WHERE i2.sample_id = images_23.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_24
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_24 i2 WHERE i2.sample_id = images_24.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_25
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_25 i2 WHERE i2.sample_id = images_25.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_26
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_26 i2 WHERE i2.sample_id = images_26.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_27
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_27 i2 WHERE i2.sample_id = images_27.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_28
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_28 i2 WHERE i2.sample_id = images_28.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_29
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_29 i2 WHERE i2.sample_id = images_29.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_2a
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_2a i2 WHERE i2.sample_id = images_2a.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_2b
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_2b i2 WHERE i2.sample_id = images_2b.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_2c
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_2c i2 WHERE i2.sample_id = images_2c.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_2d
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_2d i2 WHERE i2.sample_id = images_2d.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_2e
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_2e i2 WHERE i2.sample_id = images_2e.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_2f
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_2f i2 WHERE i2.sample_id = images_2f.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_30
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_30 i2 WHERE i2.sample_id = images_30.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_31
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_31 i2 WHERE i2.sample_id = images_31.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_32
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_32 i2 WHERE i2.sample_id = images_32.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_33
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_33 i2 WHERE i2.sample_id = images_33.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_34
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_34 i2 WHERE i2.sample_id = images_34.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_35
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_35 i2 WHERE i2.sample_id = images_35.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_36
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_36 i2 WHERE i2.sample_id = images_36.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_37
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_37 i2 WHERE i2.sample_id = images_37.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_38
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_38 i2 WHERE i2.sample_id = images_38.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_39
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_39 i2 WHERE i2.sample_id = images_39.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_3a
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_3a i2 WHERE i2.sample_id = images_3a.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_3b
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_3b i2 WHERE i2.sample_id = images_3b.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_3c
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_3c i2 WHERE i2.sample_id = images_3c.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_3d
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_3d i2 WHERE i2.sample_id = images_3d.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_3e
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_3e i2 WHERE i2.sample_id = images_3e.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_3f
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_3f i2 WHERE i2.sample_id = images_3f.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_40
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_40 i2 WHERE i2.sample_id = images_40.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_41
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_41 i2 WHERE i2.sample_id = images_41.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_42
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_42 i2 WHERE i2.sample_id = images_42.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_43
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_43 i2 WHERE i2.sample_id = images_43.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_44
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_44 i2 WHERE i2.sample_id = images_44.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_45
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_45 i2 WHERE i2.sample_id = images_45.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_46
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_46 i2 WHERE i2.sample_id = images_46.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_47
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_47 i2 WHERE i2.sample_id = images_47.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_48
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_48 i2 WHERE i2.sample_id = images_48.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_49
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_49 i2 WHERE i2.sample_id = images_49.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_4a
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_4a i2 WHERE i2.sample_id = images_4a.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_4b
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_4b i2 WHERE i2.sample_id = images_4b.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_4c
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_4c i2 WHERE i2.sample_id = images_4c.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_4d
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_4d i2 WHERE i2.sample_id = images_4d.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_4e
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_4e i2 WHERE i2.sample_id = images_4e.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_4f
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_4f i2 WHERE i2.sample_id = images_4f.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_50
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_50 i2 WHERE i2.sample_id = images_50.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_51
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_51 i2 WHERE i2.sample_id = images_51.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_52
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_52 i2 WHERE i2.sample_id = images_52.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_53
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_53 i2 WHERE i2.sample_id = images_53.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_54
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_54 i2 WHERE i2.sample_id = images_54.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_55
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_55 i2 WHERE i2.sample_id = images_55.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_56
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_56 i2 WHERE i2.sample_id = images_56.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_57
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_57 i2 WHERE i2.sample_id = images_57.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_58
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_58 i2 WHERE i2.sample_id = images_58.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_59
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_59 i2 WHERE i2.sample_id = images_59.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_5a
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_5a i2 WHERE i2.sample_id = images_5a.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_5b
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_5b i2 WHERE i2.sample_id = images_5b.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_5c
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_5c i2 WHERE i2.sample_id = images_5c.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_5d
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_5d i2 WHERE i2.sample_id = images_5d.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_5e
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_5e i2 WHERE i2.sample_id = images_5e.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_5f
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_5f i2 WHERE i2.sample_id = images_5f.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_60
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_60 i2 WHERE i2.sample_id = images_60.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_61
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_61 i2 WHERE i2.sample_id = images_61.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_62
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_62 i2 WHERE i2.sample_id = images_62.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_63
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_63 i2 WHERE i2.sample_id = images_63.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_64
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_64 i2 WHERE i2.sample_id = images_64.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_65
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_65 i2 WHERE i2.sample_id = images_65.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_66
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_66 i2 WHERE i2.sample_id = images_66.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_67
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_67 i2 WHERE i2.sample_id = images_67.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_68
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_68 i2 WHERE i2.sample_id = images_68.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_69
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_69 i2 WHERE i2.sample_id = images_69.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_6a
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_6a i2 WHERE i2.sample_id = images_6a.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_6b
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_6b i2 WHERE i2.sample_id = images_6b.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_6c
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_6c i2 WHERE i2.sample_id = images_6c.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_6d
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_6d i2 WHERE i2.sample_id = images_6d.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_6e
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_6e i2 WHERE i2.sample_id = images_6e.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_6f
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_6f i2 WHERE i2.sample_id = images_6f.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_70
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_70 i2 WHERE i2.sample_id = images_70.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_71
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_71 i2 WHERE i2.sample_id = images_71.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_72
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_72 i2 WHERE i2.sample_id = images_72.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_73
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_73 i2 WHERE i2.sample_id = images_73.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_74
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_74 i2 WHERE i2.sample_id = images_74.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_75
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_75 i2 WHERE i2.sample_id = images_75.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_76
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_76 i2 WHERE i2.sample_id = images_76.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_77
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_77 i2 WHERE i2.sample_id = images_77.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_78
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_78 i2 WHERE i2.sample_id = images_78.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_79
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_79 i2 WHERE i2.sample_id = images_79.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_7a
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_7a i2 WHERE i2.sample_id = images_7a.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_7b
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_7b i2 WHERE i2.sample_id = images_7b.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_7c
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_7c i2 WHERE i2.sample_id = images_7c.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_7d
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_7d i2 WHERE i2.sample_id = images_7d.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_7e
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_7e i2 WHERE i2.sample_id = images_7e.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_7f
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_7f i2 WHERE i2.sample_id = images_7f.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_80
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_80 i2 WHERE i2.sample_id = images_80.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_81
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_81 i2 WHERE i2.sample_id = images_81.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_82
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_82 i2 WHERE i2.sample_id = images_82.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_83
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_83 i2 WHERE i2.sample_id = images_83.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_84
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_84 i2 WHERE i2.sample_id = images_84.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_85
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_85 i2 WHERE i2.sample_id = images_85.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_86
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_86 i2 WHERE i2.sample_id = images_86.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_87
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_87 i2 WHERE i2.sample_id = images_87.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_88
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_88 i2 WHERE i2.sample_id = images_88.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_89
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_89 i2 WHERE i2.sample_id = images_89.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_8a
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_8a i2 WHERE i2.sample_id = images_8a.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_8b
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_8b i2 WHERE i2.sample_id = images_8b.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_8c
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_8c i2 WHERE i2.sample_id = images_8c.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_8d
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_8d i2 WHERE i2.sample_id = images_8d.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_8e
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_8e i2 WHERE i2.sample_id = images_8e.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_8f
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_8f i2 WHERE i2.sample_id = images_8f.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_90
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_90 i2 WHERE i2.sample_id = images_90.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_91
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_91 i2 WHERE i2.sample_id = images_91.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_92
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_92 i2 WHERE i2.sample_id = images_92.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_93
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_93 i2 WHERE i2.sample_id = images_93.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_94
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_94 i2 WHERE i2.sample_id = images_94.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_95
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_95 i2 WHERE i2.sample_id = images_95.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_96
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_96 i2 WHERE i2.sample_id = images_96.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_97
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_97 i2 WHERE i2.sample_id = images_97.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_98
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_98 i2 WHERE i2.sample_id = images_98.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_99
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_99 i2 WHERE i2.sample_id = images_99.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_9a
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_9a i2 WHERE i2.sample_id = images_9a.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_9b
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_9b i2 WHERE i2.sample_id = images_9b.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_9c
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_9c i2 WHERE i2.sample_id = images_9c.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_9d
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_9d i2 WHERE i2.sample_id = images_9d.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_9e
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_9e i2 WHERE i2.sample_id = images_9e.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_9f
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_9f i2 WHERE i2.sample_id = images_9f.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_a0
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_a0 i2 WHERE i2.sample_id = images_a0.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_a1
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_a1 i2 WHERE i2.sample_id = images_a1.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_a2
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_a2 i2 WHERE i2.sample_id = images_a2.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_a3
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_a3 i2 WHERE i2.sample_id = images_a3.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_a4
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_a4 i2 WHERE i2.sample_id = images_a4.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_a5
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_a5 i2 WHERE i2.sample_id = images_a5.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_a6
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_a6 i2 WHERE i2.sample_id = images_a6.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_a7
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_a7 i2 WHERE i2.sample_id = images_a7.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_a8
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_a8 i2 WHERE i2.sample_id = images_a8.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_a9
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_a9 i2 WHERE i2.sample_id = images_a9.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_aa
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_aa i2 WHERE i2.sample_id = images_aa.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_ab
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_ab i2 WHERE i2.sample_id = images_ab.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_ac
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_ac i2 WHERE i2.sample_id = images_ac.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_ad
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_ad i2 WHERE i2.sample_id = images_ad.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_ae
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_ae i2 WHERE i2.sample_id = images_ae.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_af
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_af i2 WHERE i2.sample_id = images_af.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_b0
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_b0 i2 WHERE i2.sample_id = images_b0.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_b1
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_b1 i2 WHERE i2.sample_id = images_b1.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_b2
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_b2 i2 WHERE i2.sample_id = images_b2.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_b3
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_b3 i2 WHERE i2.sample_id = images_b3.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_b4
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_b4 i2 WHERE i2.sample_id = images_b4.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_b5
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_b5 i2 WHERE i2.sample_id = images_b5.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_b6
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_b6 i2 WHERE i2.sample_id = images_b6.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_b7
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_b7 i2 WHERE i2.sample_id = images_b7.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_b8
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_b8 i2 WHERE i2.sample_id = images_b8.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_b9
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_b9 i2 WHERE i2.sample_id = images_b9.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_ba
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_ba i2 WHERE i2.sample_id = images_ba.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_bb
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_bb i2 WHERE i2.sample_id = images_bb.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_bc
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_bc i2 WHERE i2.sample_id = images_bc.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_bd
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_bd i2 WHERE i2.sample_id = images_bd.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_be
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_be i2 WHERE i2.sample_id = images_be.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_bf
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_bf i2 WHERE i2.sample_id = images_bf.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_c0
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_c0 i2 WHERE i2.sample_id = images_c0.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_c1
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_c1 i2 WHERE i2.sample_id = images_c1.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_c2
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_c2 i2 WHERE i2.sample_id = images_c2.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_c3
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_c3 i2 WHERE i2.sample_id = images_c3.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_c4
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_c4 i2 WHERE i2.sample_id = images_c4.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_c5
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_c5 i2 WHERE i2.sample_id = images_c5.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_c6
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_c6 i2 WHERE i2.sample_id = images_c6.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_c7
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_c7 i2 WHERE i2.sample_id = images_c7.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_c8
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_c8 i2 WHERE i2.sample_id = images_c8.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_c9
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_c9 i2 WHERE i2.sample_id = images_c9.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_ca
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_ca i2 WHERE i2.sample_id = images_ca.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_cb
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_cb i2 WHERE i2.sample_id = images_cb.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_cc
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_cc i2 WHERE i2.sample_id = images_cc.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_cd
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_cd i2 WHERE i2.sample_id = images_cd.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_ce
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_ce i2 WHERE i2.sample_id = images_ce.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_cf
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_cf i2 WHERE i2.sample_id = images_cf.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_d0
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_d0 i2 WHERE i2.sample_id = images_d0.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_d1
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_d1 i2 WHERE i2.sample_id = images_d1.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_d2
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_d2 i2 WHERE i2.sample_id = images_d2.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_d3
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_d3 i2 WHERE i2.sample_id = images_d3.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_d4
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_d4 i2 WHERE i2.sample_id = images_d4.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_d5
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_d5 i2 WHERE i2.sample_id = images_d5.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_d6
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_d6 i2 WHERE i2.sample_id = images_d6.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_d7
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_d7 i2 WHERE i2.sample_id = images_d7.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_d8
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_d8 i2 WHERE i2.sample_id = images_d8.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_d9
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_d9 i2 WHERE i2.sample_id = images_d9.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_da
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_da i2 WHERE i2.sample_id = images_da.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_db
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_db i2 WHERE i2.sample_id = images_db.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_dc
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_dc i2 WHERE i2.sample_id = images_dc.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_dd
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_dd i2 WHERE i2.sample_id = images_dd.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_de
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_de i2 WHERE i2.sample_id = images_de.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_df
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_df i2 WHERE i2.sample_id = images_df.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_e0
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_e0 i2 WHERE i2.sample_id = images_e0.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_e1
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_e1 i2 WHERE i2.sample_id = images_e1.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_e2
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_e2 i2 WHERE i2.sample_id = images_e2.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_e3
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_e3 i2 WHERE i2.sample_id = images_e3.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_e4
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_e4 i2 WHERE i2.sample_id = images_e4.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_e5
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_e5 i2 WHERE i2.sample_id = images_e5.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_e6
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_e6 i2 WHERE i2.sample_id = images_e6.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_e7
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_e7 i2 WHERE i2.sample_id = images_e7.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_e8
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_e8 i2 WHERE i2.sample_id = images_e8.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_e9
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_e9 i2 WHERE i2.sample_id = images_e9.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_ea
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_ea i2 WHERE i2.sample_id = images_ea.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_eb
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_eb i2 WHERE i2.sample_id = images_eb.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_ec
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_ec i2 WHERE i2.sample_id = images_ec.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_ed
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_ed i2 WHERE i2.sample_id = images_ed.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_ee
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_ee i2 WHERE i2.sample_id = images_ee.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_ef
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_ef i2 WHERE i2.sample_id = images_ef.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_f0
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_f0 i2 WHERE i2.sample_id = images_f0.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_f1
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_f1 i2 WHERE i2.sample_id = images_f1.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_f2
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_f2 i2 WHERE i2.sample_id = images_f2.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_f3
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_f3 i2 WHERE i2.sample_id = images_f3.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_f4
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_f4 i2 WHERE i2.sample_id = images_f4.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_f5
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_f5 i2 WHERE i2.sample_id = images_f5.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_f6
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_f6 i2 WHERE i2.sample_id = images_f6.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_f7
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_f7 i2 WHERE i2.sample_id = images_f7.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_f8
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_f8 i2 WHERE i2.sample_id = images_f8.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_f9
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_f9 i2 WHERE i2.sample_id = images_f9.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_fa
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_fa i2 WHERE i2.sample_id = images_fa.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_fb
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_fb i2 WHERE i2.sample_id = images_fb.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_fc
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_fc i2 WHERE i2.sample_id = images_fc.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_fd
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_fd i2 WHERE i2.sample_id = images_fd.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_fe
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_fe i2 WHERE i2.sample_id = images_fe.sample_id);
INSERT IGNORE INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name, create_time, update_time)
SELECT sample_id, id, thumbnail_path, hash, file_name, NOW(), NOW()
FROM images_ff
WHERE sample_id IS NOT NULL
  AND id = (SELECT MIN(i2.id) FROM images_ff i2 WHERE i2.sample_id = images_ff.sample_id);

