# 🎼 LyraScore 云端谱仓

> 面向乐器学习者的「乐谱资产 + 系统化练习 + AI 助手 + 琴友社交 + 实用工具」一体化全栈应用。
> 数据库分析与设计实习课设作品，已完成全部 6 大功能模块 + 管理员后台 + 3 款实用工具。

GitHub：<https://github.com/jungle122/LyraScore_web>

---

## ✨ 功能速览

| 模块 | 关键能力 |
|---|---|
| **🗂️ 谱仓** | 多图上传（最多 12 张）、乐器/风格/状态筛选、关键字搜索、阅读工具条（1 页 / 2 页 / 大图 / 全屏） |
| **📅 计划 + 日志** | 计划 N:M 关联乐谱、写日志记录时长/BPM/心得；累计时长 / 按曲分组 / 近 7 日趋势聚合统计 |
| **🎵 歌单** | 多歌单 N:M 收藏、自定义曲目顺序 |
| **👥 社交** | 用户名搜索加好友（自引用 N:M）、AI 智能点歌台（自由文本 → `(歌名, 歌手)`）、字典化快捷短语留言 |
| **🏆 徽章** | 17 张徽章 / 7 种触发条件，`@EventListener` 事件驱动自动发牌、`INSERT IGNORE` 幂等 |
| **🛠️ 实用工具** | 吉他定音器（5 种调弦预设 + Web Audio 变调）、节拍器（30–250 BPM）、找谱搜索（简繁转换 + 多站快搜） |
| **🤖 AI 双场景** | ① 点歌信息清洗（艺名归一化）② 练习总结教练点评；阿里云百炼 `qwen-turbo`，失败自动降级 |
| **👑 管理员后台** | 数据大盘（9 项跨表 COUNT / SUM）、用户冻结/解冻、字典 CRUD；`@RequireRole("admin")` RBAC 双层保护 |

---

## 🧰 技术栈

**前端**：Vue 3（Composition API）· Element Plus · Vite · Pinia · Vue Router 4 · axios · opencc-js · Web Audio API
**后端**：Java 17 · Spring Boot 3.2 · MyBatis 3.0（**纯 XML mapper，无 MyBatis-Plus**）· jjwt · jBCrypt · Lombok · Spring RestClient
**数据库**：MySQL 8.0（utf8mb4），12 张 `t_` 前缀表，1:N / N:M / 自引用 N:M 三种关系
**安全**：手写 JWT `HandlerInterceptor` + `@RequireRole` 自定义注解 RBAC（不依赖 Spring Security）
**AI**：阿里云百炼 DashScope `qwen-turbo`（OpenAI 兼容端点）

完整说明见 `docs/技术栈.md`。

---

## 🚀 快速启动

### 0. 前置依赖

| 工具 | 版本 | 用途 |
|---|---|---|
| JDK | 17+ | 后端 |
| Maven | 3.8+ | 后端构建（已配 Aliyun 镜像） |
| Node.js | 18+ | 前端 |
| MySQL | 8.0 | 数据库 |

### 1. 创建数据库

```sql
CREATE DATABASE lyrascore DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```

执行 DDL 与种子数据：

```bash
mysql -u root -p lyrascore < backend/src/main/resources/db/schema.sql
mysql -u root -p lyrascore < backend/src/main/resources/db/seed.sql
```

### 2. 配置环境变量（PowerShell）

复制 `.env.example` 后填入真实值，并写入当前 PowerShell 会话；推荐写进 `$PROFILE` 永久生效：

```powershell
$env:DB_PASSWORD       = "你的MySQL密码"
$env:JWT_SECRET        = "至少32位的随机字符串"
$env:DASHSCOPE_API_KEY = "百炼API Key（可选）"
$env:AI_ENABLED        = "true"   # 没配 key 就改 false，AI 自动降级
```

> `.env` 文件**不进 git**，请确认 `.gitignore` 已忽略。

### 3. 启动后端

```powershell
cd backend
mvn spring-boot:run
```

监听 `http://localhost:8080/api`（context-path = `/api`）。
图片上传目录：`backend/uploads/scores/` 与 `backend/uploads/avatars/`。

### 4. 启动前端

```powershell
cd frontend
npm install
npm run dev
```

监听 `http://localhost:5200`，Vite 代理把 `/api` 与 `/uploads` 都转给后端 8080。

---

## 🔑 默认账号（来自 `seed.sql`）

| 角色 | 用户名 | 密码 | 说明 |
|---|---|---|---|
| 普通用户 | `lyra` | `123456` | 主演示账号，已有种子数据 |
| 普通用户 | `lyra2` | `123456` | 用于演示好友/点歌 |
| 管理员 | `admin` | `admin123` | 登录后跳转 `/admin` |

> 管理员账号的 `role` 字段在 seed.sql 里已设为 `admin`。如果是手动注册的账号要升管理员，执行：
> ```sql
> UPDATE t_user SET role = 'admin' WHERE username = 'admin';
> ```

---

## 📁 目录结构

```
LyraScore/
├── backend/                     # Spring Boot 后端
│   ├── src/main/java/com/lyrascore/
│   │   ├── user/                # 用户 + JWT
│   │   ├── score/               # 谱仓
│   │   ├── plan/                # 练习计划
│   │   ├── log/                 # 练习日志 + AI 练习总结
│   │   ├── setlist/             # 歌单
│   │   ├── friendship/          # 好友（自引用 N:M）
│   │   ├── songrequest/         # 点歌 + AI 清洗
│   │   ├── badge/               # 徽章 + EventListener
│   │   ├── admin/               # 管理员后台 + @RequireRole
│   │   ├── common/              # JwtInterceptor / 异常处理 / R 包装
│   │   └── ai/                  # AiService（点歌清洗 + 练习总结）
│   └── src/main/resources/
│       ├── application.yml      # 公共配置
│       ├── application-dev.yml  # 开发环境（读环境变量）
│       ├── mapper/              # MyBatis XML mapper
│       └── db/                  # schema.sql + seed.sql
├── frontend/                    # Vue 3 前端
│   └── src/
│       ├── views/               # 13 个业务页面 + 3 个 admin 页面
│       ├── stores/              # Pinia（user store）
│       ├── router/              # 路由 + 守卫
│       ├── utils/               # axios 封装（401 去重）
│       └── styles/              # 琥珀主题 CSS 变量
└── docs/                        # 设计文档与课设交付物
    ├── PRD.md                   # 产品需求 v5.0
    ├── E-R 图.md                # 概念结构（Mermaid ERD）
    ├── 开发计划.md              # 6 阶段 14 任务
    ├── 技术栈.md                # 完整工具链 + 选型理由
    ├── 实验报告_草稿.md         # 课设报告草稿（6 章）
    └── 截图清单.md              # 演示视频/报告截图清单
```

---

## 📐 几个关键技术决策

> 详细背景见 `CLAUDE.md` 的 **Locked-in technical decisions** 节。

1. **图片本地存盘** — 不引对象存储；DB 存相对路径（`uploads/scores/xxx.jpg`），多图用逗号分隔。
2. **JWT + 自写拦截器** — 不引 Spring Security；`HandlerInterceptor` 解析 token → `ThreadLocal` 存上下文。
3. **`@RequireRole` 自定义注解 + 反射** — 类级或方法级 RBAC，管理员后台所有接口受保护。
4. **徽章用 Spring `@EventListener`** — 替代 PRD 早期提到的「DB 触发器」方案，业务事件解耦。
5. **AI 失败必须降级** — 任何 `RestClient` 异常都不应阻塞主流程：点歌写 `status=2` 待人工审核 / 练习总结回退本地拼接文案。
6. **外键策略分级** — 关联表（plan_items / setlist_items）→ `CASCADE`，历史日志 → `RESTRICT` 不可丢。

---

## 📚 文档索引

| 文档 | 内容 |
|---|---|
| `docs/PRD.md` | 产品需求 v5.0（与代码状态对齐） |
| `docs/E-R 图.md` | E-R 图 + 12 张表字段细节 |
| `docs/开发计划.md` | 6 阶段、14 任务、垂直切片演化路径 |
| `docs/技术栈.md` | 前后端技术栈 + 每项选型理由 |
| `docs/实验报告_草稿.md` | 实验报告 6 章草稿（学校模板填充用） |
| `docs/截图清单.md` | 18 张应用截图 + 3 张数据库截图 |
| `CLAUDE.md` | 给 AI 助手看的项目上下文与技术决策记录 |

---

## 📜 版权说明

本仓库为 2025–2026 学年《数据库分析与设计实习》课程作业，仅作教学演示用途。
