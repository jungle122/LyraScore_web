-- LyraScore 种子数据：快捷点歌短语
-- 可以反复执行（INSERT IGNORE 跳过已有 dict_type+dict_key 唯一键冲突）

INSERT IGNORE INTO t_dictionary (dict_type, dict_key, dict_value, sort_order, is_active) VALUES
  ('quick_dm', 'dm1', '大佬教教我',           10, 1),
  ('quick_dm', 'dm2', '这首给我一份谱呗',     20, 1),
  ('quick_dm', 'dm3', '催更催更',             30, 1),
  ('quick_dm', 'dm4', '弹这首！',             40, 1),
  ('quick_dm', 'dm5', '路过求一份指弹版',     50, 1);
