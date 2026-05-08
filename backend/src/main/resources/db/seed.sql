-- LyraScore 种子数据：快捷点歌短语
-- 可以反复执行（INSERT IGNORE 跳过已有 dict_type+dict_key 唯一键冲突）

INSERT IGNORE INTO t_dictionary (dict_type, dict_key, dict_value, sort_order, is_active) VALUES
  ('quick_dm', 'dm1', '大佬教教我',           10, 1),
  ('quick_dm', 'dm2', '这首给我一份谱呗',     20, 1),
  ('quick_dm', 'dm3', '催更催更',             30, 1),
  ('quick_dm', 'dm4', '弹这首！',             40, 1),
  ('quick_dm', 'dm5', '路过求一份指弹版',     50, 1);


-- ============================================================
-- 徽章定义（任务 12）
-- condition_type:
--   first_log       —— 写下第一条练习日志
--   total_minutes   —— 累计练习达到 N 分钟
--   score_count     —— 上传乐谱达到 N 张
-- ============================================================
INSERT IGNORE INTO t_badge (name, icon_url, condition_type, condition_value, description) VALUES
  ('初出茅庐',     '🎵', 'first_log',           1,    '完成第一次练习记录'),
  ('节奏新手',     '🌱', 'total_minutes',       30,   '累计练习达到 30 分钟'),
  ('肝帝预备役',   '🔥', 'total_minutes',       60,   '累计练习达到 60 分钟'),
  ('小有所成',     '🌿', 'total_minutes',       300,  '累计练习达到 5 小时'),
  ('真正的肝帝',   '💪', 'total_minutes',       600,  '累计练习达到 600 分钟'),
  ('练习狂热',     '🌳', 'total_minutes',       1800, '累计练习达到 30 小时'),
  ('琴技大师',     '🌲', 'total_minutes',       6000, '累计练习达到 100 小时'),
  ('坚持就是胜利', '⏳', 'log_count',           10,   '完成 10 次练习记录'),
  ('百炼成钢',     '⚒️', 'log_count',           50,   '完成 50 次练习记录'),
  ('乐谱猎人',     '🎼', 'score_count',         5,    '上传乐谱达到 5 张'),
  ('谱仓守护者',   '📚', 'score_count',         10,   '上传乐谱达到 10 张'),
  ('谱海无涯',     '🗺️', 'score_count',         20,   '上传乐谱达到 20 张'),
  ('运筹帷幄',     '📋', 'plan_count',          1,    '创建第一个练习计划'),
  ('歌单达人',     '🎵', 'setlist_count',       3,    '创建 3 个歌单'),
  ('不再孤单',     '🤝', 'friend_count',        1,    '添加第一位好友'),
  ('知音难觅',     '👥', 'friend_count',        5,    '拥有 5 位琴友'),
  ('点歌之王',     '🎤', 'song_request_count',  1,    '给好友点了第一首歌');
