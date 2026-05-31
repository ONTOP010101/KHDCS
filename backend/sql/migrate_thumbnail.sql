INSERT INTO sample_thumbnail (sample_id, image_id, thumbnail, hash, file_name)
SELECT t.sample_id, t.id, t.thumbnail_path, t.hash, t.file_name FROM (
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_00 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_01 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_02 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_03 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_04 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_05 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_06 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_07 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_08 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_09 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_0a WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_0b WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_0c WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_0d WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_0e WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_0f WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_10 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_11 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_12 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_13 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_14 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_15 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_16 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_17 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_18 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_19 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_1a WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_1b WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_1c WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_1d WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_1e WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_1f WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_20 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_21 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_22 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_23 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_24 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_25 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_26 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_27 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_28 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_29 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_2a WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_2b WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_2c WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_2d WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_2e WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_2f WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_30 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_31 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_32 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_33 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_34 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_35 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_36 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_37 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_38 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_39 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_3a WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_3b WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_3c WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_3d WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_3e WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_3f WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_40 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_41 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_42 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_43 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_44 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_45 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_46 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_47 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_48 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_49 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_4a WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_4b WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_4c WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_4d WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_4e WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_4f WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_50 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_51 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_52 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_53 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_54 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_55 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_56 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_57 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_58 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_59 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_5a WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_5b WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_5c WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_5d WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_5e WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_5f WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_60 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_61 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_62 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_63 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_64 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_65 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_66 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_67 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_68 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_69 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_6a WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_6b WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_6c WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_6d WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_6e WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_6f WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_70 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_71 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_72 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_73 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_74 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_75 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_76 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_77 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_78 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_79 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_7a WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_7b WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_7c WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_7d WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_7e WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_7f WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_80 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_81 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_82 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_83 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_84 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_85 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_86 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_87 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_88 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_89 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_8a WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_8b WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_8c WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_8d WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_8e WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_8f WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_90 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_91 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_92 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_93 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_94 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_95 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_96 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_97 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_98 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_99 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_9a WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_9b WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_9c WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_9d WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_9e WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_9f WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_a0 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_a1 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_a2 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_a3 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_a4 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_a5 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_a6 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_a7 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_a8 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_a9 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_aa WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_ab WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_ac WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_ad WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_ae WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_af WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_b0 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_b1 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_b2 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_b3 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_b4 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_b5 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_b6 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_b7 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_b8 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_b9 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_ba WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_bb WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_bc WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_bd WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_be WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_bf WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_c0 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_c1 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_c2 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_c3 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_c4 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_c5 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_c6 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_c7 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_c8 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_c9 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_ca WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_cb WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_cc WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_cd WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_ce WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_cf WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_d0 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_d1 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_d2 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_d3 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_d4 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_d5 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_d6 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_d7 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_d8 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_d9 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_da WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_db WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_dc WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_dd WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_de WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_df WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_e0 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_e1 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_e2 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_e3 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_e4 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_e5 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_e6 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_e7 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_e8 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_e9 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_ea WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_eb WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_ec WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_ed WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_ee WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_ef WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_f0 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_f1 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_f2 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_f3 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_f4 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_f5 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_f6 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_f7 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_f8 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_f9 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_fa WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_fb WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_fc WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_fd WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_fe WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, id, thumbnail_path, hash, file_name FROM images_ff WHERE sample_id IS NOT NULL
) t
INNER JOIN (
  SELECT sample_id, MIN(sort_order) as min_sort FROM (
SELECT sample_id, sort_order FROM images_00 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_01 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_02 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_03 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_04 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_05 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_06 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_07 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_08 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_09 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_0a WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_0b WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_0c WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_0d WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_0e WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_0f WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_10 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_11 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_12 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_13 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_14 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_15 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_16 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_17 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_18 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_19 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_1a WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_1b WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_1c WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_1d WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_1e WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_1f WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_20 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_21 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_22 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_23 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_24 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_25 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_26 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_27 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_28 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_29 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_2a WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_2b WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_2c WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_2d WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_2e WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_2f WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_30 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_31 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_32 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_33 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_34 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_35 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_36 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_37 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_38 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_39 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_3a WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_3b WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_3c WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_3d WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_3e WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_3f WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_40 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_41 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_42 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_43 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_44 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_45 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_46 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_47 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_48 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_49 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_4a WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_4b WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_4c WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_4d WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_4e WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_4f WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_50 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_51 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_52 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_53 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_54 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_55 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_56 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_57 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_58 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_59 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_5a WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_5b WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_5c WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_5d WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_5e WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_5f WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_60 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_61 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_62 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_63 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_64 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_65 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_66 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_67 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_68 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_69 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_6a WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_6b WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_6c WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_6d WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_6e WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_6f WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_70 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_71 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_72 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_73 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_74 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_75 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_76 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_77 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_78 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_79 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_7a WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_7b WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_7c WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_7d WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_7e WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_7f WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_80 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_81 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_82 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_83 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_84 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_85 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_86 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_87 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_88 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_89 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_8a WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_8b WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_8c WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_8d WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_8e WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_8f WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_90 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_91 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_92 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_93 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_94 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_95 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_96 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_97 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_98 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_99 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_9a WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_9b WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_9c WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_9d WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_9e WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_9f WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_a0 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_a1 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_a2 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_a3 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_a4 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_a5 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_a6 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_a7 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_a8 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_a9 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_aa WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_ab WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_ac WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_ad WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_ae WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_af WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_b0 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_b1 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_b2 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_b3 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_b4 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_b5 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_b6 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_b7 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_b8 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_b9 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_ba WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_bb WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_bc WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_bd WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_be WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_bf WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_c0 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_c1 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_c2 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_c3 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_c4 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_c5 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_c6 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_c7 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_c8 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_c9 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_ca WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_cb WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_cc WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_cd WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_ce WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_cf WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_d0 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_d1 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_d2 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_d3 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_d4 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_d5 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_d6 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_d7 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_d8 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_d9 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_da WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_db WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_dc WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_dd WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_de WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_df WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_e0 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_e1 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_e2 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_e3 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_e4 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_e5 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_e6 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_e7 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_e8 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_e9 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_ea WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_eb WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_ec WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_ed WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_ee WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_ef WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_f0 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_f1 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_f2 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_f3 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_f4 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_f5 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_f6 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_f7 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_f8 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_f9 WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_fa WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_fb WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_fc WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_fd WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_fe WHERE sample_id IS NOT NULL
 UNION ALL
SELECT sample_id, sort_order FROM images_ff WHERE sample_id IS NOT NULL
) m GROUP BY sample_id
) g ON t.sample_id = g.sample_id AND t.sort_order = g.min_sort
ON DUPLICATE KEY UPDATE image_id=t.id, thumbnail=t.thumbnail_path, hash=t.hash, file_name=t.file_name;
