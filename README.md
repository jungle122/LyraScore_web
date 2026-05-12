# LyraScore 云端谱仓

> 面向乐器学习者的「乐谱资产 + 系统化练习 + AI 助手 + 琴友社交 + 实用工具」一体化全栈应用。
> 数据库分析与设计实习课设作品，已完成全部 6 大功能模块 + 管理员后台 + 3 款实用工具。

GitHub：<https://github.com/jungle122/LyraScore_web>

---

## 功能速览

| 模块 | 关键能力 |
|---|---|
| **谱仓** | 多图上传（最多 12 张）、乐器/风格/状态筛选、关键字搜索、阅读工具条（单页/双页/大图/全屏） |
| **计划 + 日志** | 计划 N:M 关联乐谱、记录练习日志（时长/BPM/心得）；累计/按曲/近 7 日聚合统计（GROUP BY + SUM） |
| **歌单** | 多歌单 N:M 收藏、自定义曲目顺序、可收藏他人公开乐谱 |
| **社交** | 用户名搜索加好友（自引用 N:M）、AI 智能点歌台（自由文本 → 歌名+歌手）、字典化快捷短语 |
| **徽章** | 17 枚徽章 / 7 种触发条件，`@EventListener` 事件驱动自动评估、`INSERT IGNORE` 幂等 |
| **实用工具** | 吉他定音器（5 种调弦 + Web Audio）、节拍器（30-250 BPM）、找谱搜索（简繁转换 + 多站） |
| **AI 双场景** | 点歌文本清洗（艺名归一化）+ 练习总结教练点评；阿里云百炼 qwen-turbo，失败自动降级 |
| **管理员后台** | 数据大盘（9 项跨表统计）、用户冻结/解冻、字典 CRUD；`@RequireRole("admin")` RBAC |

---

## 技术栈

| 层 | 技术 |
|---|---|
| 前端 | Vue 3（Composition API）+ Element Plus + Vite + Pinia + Vue Router 4 + Axios + opencc-js + Web Audio API |
| 后端 | Java 17 + Spring Boot 3.2 + MyBatis 3（纯 XML mapper）+ jjwt + jBCrypt + Lombok + Spring RestClient |
| 数据库 | MySQL 8.0（utf8mb4），12 张 `t_` 前缀表，InnoDB 引擎 |
| 安全 | 手写 JWT `HandlerInterceptor` + `@RequireRole` 自定义注解 RBAC（不依赖 Spring Security） |
| AI | 阿里云百炼 DashScope `qwen-turbo`（OpenAI 兼容端点） |

---

## 快速启动（从头开始）

### 第 1 步：确认前置依赖

| 工具 | 最低版本 | 检查命令 |
|---|---|---|
| JDK | 17 | `java --version` |
| Maven | 3.8 | `mvn --version` |
| Node.js | 18 | `node --version` |
| MySQL | 8.0 | `mysqladmin ping -u root -p` |

### 第 2 步：创建数据库并建表

如果数据库还不存在：

```sql
CREATE DATABASE lyrascore DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```

执行建表脚本和种子数据（**Windows 下必须加 `--default-character-set=utf8mb4`**，否则中文注释会乱码）：

```bash
cd backend

# 建表（12 张表）
mysql --default-character-set=utf8mb4 -u root -p lyrascore < src/main/resources/db/schema.sql

# 导入种子数据（演示账号 + 快捷短语 + 17 枚徽章定义）
mysql --default-character-set=utf8mb4 -u root -p lyrascore < src/main/resources/db/seed.sql
```

验证建表成功：

```bash
mysql -u root -p -e "USE lyrascore; SHOW TABLES;"
```

应该看到 12 张表：`t_user`, `t_score`, `t_friendship`, `t_song_request`, `t_practice_plan`, `t_plan_items`, `t_practice_log`, `t_setlist`, `t_setlist_items`, `t_dictionary`, `t_badge`, `t_user_badge`。

### 第 3 步：配置环境变量

**PowerShell**（每次启动前执行，或写入 `$PROFILE` 永久生效）：

```powershell
$env:DB_PASSWORD       = "你的MySQL密码"
$env:JWT_SECRET        = "至少32位的随机字符串"
$env:DASHSCOPE_API_KEY = "百炼API Key（可选）"
$env:AI_ENABLED        = "false"   # 没有百炼 key 就设 false，AI 功能自动降级，不影响使用
```

**git-bash**：

```bash
export DB_PASSWORD="你的MySQL密码"
export JWT_SECRET="至少32位的随机字符串"
export AI_ENABLED="false"
```

### 第 4 步：启动后端

打开**新的终端窗口**：

```bash
cd backend
mvn spring-boot:run
```

看到 `Started LyraScoreApplication` 和 `Tomcat started on port 8080 (http) with context path '/api'` 即启动成功。

后端监听：`http://localhost:8080/api`

### 第 5 步：启动前端

打开**另一个终端窗口**：

```bash
cd frontend
npm install        # 仅首次运行需要
npm run dev
```

看到 `VITE ready` 和 `Local: http://localhost:5200/` 即启动成功。

### 第 6 步：打开浏览器

访问 `http://localhost:5200`，开始使用。

---

## 默认演示账号

| 角色 | 用户名 | 密码 | 说明 |
|---|---|---|---|
| 普通用户 | `lyra` | `123456` | 主演示账号 |
| 普通用户 | `lyra2` | `123456` | 用于演示好友/点歌功能 |
| 管理员 | `admin` | `admin123` | 登录后自动跳转管理后台 |

> 手动注册的账号默认是普通用户。要升为管理员，在 MySQL 中执行：
> ```sql
> UPDATE t_user SET role = 'admin' WHERE username = '你的用户名';
> ```

---

## 验证各端口是否正常

```bash
# 后端 API
curl http://localhost:8080/api/auth/login -X POST -H "Content-Type: application/json" -d '{"username":"lyra","password":"123456"}'

# 预期返回：{"code":0,"message":"success","data":{"token":"...","userId":...}}
```

```bash
# 前端页面
curl http://localhost:5200

# 预期返回：HTML 页面内容
```

---

## 常见问题

### Q1：启动后端报 `Access denied for user 'root'`

MySQL 密码不对。检查 `$env:DB_PASSWORD` 是否正确设置，或者用默认值 `root` 试试。

### Q2：启动后端报 `Unknown database 'lyrascore'`

数据库还没创建。回到第 2 步，先执行 `CREATE DATABASE`。

### Q3：前端页面打开但接口报 401

检查后端是否在运行。两个终端都要开着。

### Q4：上传图片失败

确认 `backend/uploads/` 目录存在且有写入权限。首次上传时程序会自动创建 `uploads/scores/` 子目录。

### Q5：Windows 下执行 schema.sql 后表注释乱码

必须加 `--default-character-set=utf8mb4` 参数：
```bash
mysql --default-character-set=utf8mb4 -u root -p lyrascore < backend/src/main/resources/db/schema.sql
```

### Q6：端口 5200 被占用

项目之前遇到过 Hyper-V 保留端口段（5081-5180）。在 `frontend/vite.config.js` 中把端口改成其他值，如 `5210`。

### Q7：想用 AI 功能但没百炼 key

不配 key、`AI_ENABLED=false` 即可正常使用系统。点歌功能变为"待人工审核"状态，练习报告用本地模板生成。

---

## 目录结构

```
LyraScore/
├── backend/
│   ├── src/main/java/com/lyrascore/
│   │   ├── user/           # 用户注册/登录 + JWT 拦截器 + RBAC 注解
│   │   ├── score/          # 乐谱 CRUD + 多图上传
│   │   ├── plan/           # 练习计划 + 计划-乐谱 N:M 关联
│   │   ├── log/            # 练习日志 + 聚合统计 + AI 练习总结
│   │   ├── setlist/        # 歌单 + 歌单-乐谱 N:M 关联
│   │   ├── friend/         # 好友关系（自引用 N:M + CASE WHEN）
│   │   ├── song/           # AI 点歌请求
│   │   ├── badge/          # 徽章 + @EventListener 事件监听
│   │   ├── dictionary/     # 字典表（快捷短语）
│   │   ├── admin/          # 管理后台（数据大盘 + 用户管理 + 字典管理）
│   │   ├── ai/             # 阿里云百炼 qwen-turbo 调用
│   │   ├── common/         # R 统一响应 + BusinessException + GlobalExceptionHandler
│   │   └── config/         # WebMvcConfig（拦截器注册 + 静态资源映射）
│   └── src/main/resources/
│       ├── application.yml
│       ├── application-dev.yml    # 开发环境配置（数据源/MyBatis/JWT/AI/上传）
│       ├── mapper/                # 10 个 MyBatis XML 映射文件
│       └── db/
│           ├── schema.sql         # 12 张表 DDL
│           └── seed.sql           # 种子数据（账号+短语+徽章）
├── frontend/
│   └── src/
│       ├── views/          # 20 个页面组件（含 3 个 admin 页面）
│       ├── components/     # 共享组件（ScorePickerDialog）
│       ├── api/            # 8 个 API 调用模块
│       ├── router/         # 路由配置 + 三层守卫（auth/guest/admin）
│       ├── stores/         # Pinia 用户状态
│       ├── utils/          # Axios 封装（自动 token + 401 去重处理）
│       └── styles/         # 全局琥珀色主题
├── docs/
│   ├── PRD.md              # 产品需求文档 v5.0
│   ├── E-R 图.md           # E-R 图（Mermaid） + Chen 记法版本
│   ├── 开发计划.md          # 6 阶段 14 任务
│   ├── 技术栈.md            # 技术选型与理由
│   └── 实验报告.md          # 课程设计报告（6 章，需在 Word 中排版）
├── CLAUDE.md               # AI 助手上下文指南
├── .env.example            # 环境变量模板
└── README.md               # 本文件
```

---

## 关键技术决策

| # | 决策 | 原因 |
|---|---|---|
| 1 | 图片存本地，DB 存相对路径 | 部署迁移不改代码，不依赖外部对象存储 |
| 2 | JWT + HandlerInterceptor，不用 Spring Security | 代码约 100 行，逻辑透明可调试，适合教学 |
| 3 | `@RequireRole` 自定义注解 RBAC | 声明式权限控制，方法级精细管理 |
| 4 | 徽章用 `@EventListener`，不用 DB 触发器 | Java 可调试、可测试，IDE 可导航，触发器易被遗忘 |
| 5 | AI 失败降级，不阻塞主流程 | 点歌 status=2 待人工处理 / 练习报告回退本地模板 |
| 6 | 外键策略分级：CASCADE vs RESTRICT | 关联中间表用 CASCADE 自动清理；核心实体用 RESTRICT 防误删 |
| 7 | MyBatis XML mapper，不用 MyBatis-Plus | 保留手写 SQL 的教学价值，GROUP BY / CASE WHEN 等一目了然 |

---

## 文档索引

| 文档 | 内容 |
|---|---|
| `docs/PRD.md` | 产品需求文档 v5.0 |
| `docs/E-R 图.md` | E-R 图（Mermaid 关系图 + Chen 记法） |
| `docs/开发计划.md` | 6 阶段 14 任务与垂直切片演化路径 |
| `docs/技术栈.md` | 前后端技术栈完整说明 |
| `docs/实验报告.md` | 课程设计报告正文（6 章 Markdown） |
| `CLAUDE.md` | 给 AI 助手看的项目上下文与决策记录 |

---

本仓库为 2025-2026 学年《数据库分析与设计实习》课程作业，仅作教学演示用途。
