# LyraScore 云端谱仓

数字化乐谱资源管理与协作系统。详细产品需求见 `docs/PRD.md`，技术决策见 `CLAUDE.md`。

## 技术栈

- 前端：Vue 3 + Element Plus + Vite
- 后端：Java 17 + Spring Boot 3.2 + MyBatis
- 数据库：MySQL 8.0
- AI：阿里云百炼 qwen-turbo

## 首次运行

### 1. 创建数据库

```sql
CREATE DATABASE lyrascore DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

DDL 待按 `docs/E-R 图.md` 落地后写入 `backend/src/main/resources/db/schema.sql`。

### 2. 配置环境变量

```bash
cp .env.example .env
# 编辑 .env 填入数据库密码、JWT secret
```

启动前需把 `.env` 里的变量导入当前 shell。Windows PowerShell：

```powershell
Get-Content .env | ForEach-Object {
  if ($_ -match '^([^#=]+)=(.*)$') { Set-Item "env:$($matches[1])" $matches[2] }
}
```

也可以直接在 IDE 的运行配置里设置环境变量。

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

监听 `http://localhost:8080/api`。首次构建会从阿里云镜像拉取 jar 包，约 3–5 分钟。

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

监听 `http://localhost:5173`，通过 Vite 代理把 `/api` 请求转给后端。

## 阿里云百炼 API key（阶段五才需要）

- 控制台：<https://bailian.console.aliyun.com>
- 在 `.env` 填入 `DASHSCOPE_API_KEY` 并把 `AI_ENABLED` 改为 `true`。
- 未配置时，AI 点歌走 mock 兜底，不阻塞前期开发。

## 目录速览

- `backend/` — Spring Boot 后端
- `frontend/` — Vue 3 前端
- `docs/` — 产品文档（PRD、ERD、开发计划）
- `CLAUDE.md` — 给 AI 助手看的项目上下文
