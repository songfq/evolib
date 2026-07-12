# 2026-07-10 — npm install 作用详解

---

## 一、做了什么

- 功能描述：学习 npm install 的作用和原理，理解为什么前端项目搭建后必须执行这个命令
- 涉及文件：
  - `evolib-frontend/package.json`（依赖清单）
  - `evolib-frontend/node_modules/`（依赖安装目录）
- 对应需求：理解前端包管理机制，能够正确安装和管理项目依赖

---

## 二、核心概念

### package.json — 依赖清单

```json
{
  "name": "evolib-frontend",
  "version": "1.0.0",
  "scripts": {
    "dev": "vite",
    "build": "vite build"
  },
  "dependencies": {
    "vue": "^3.4.21",
    "vue-router": "^4.3.0",
    "pinia": "^2.1.7",
    "axios": "^1.6.7"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.4",
    "vite": "^5.1.6"
  }
}
```

> **为什么需要 package.json？**
> 它就像一份购物清单，告诉 npm 需要买哪些"材料"（依赖包）以及买什么版本。没有它，npm 不知道该下载什么。

### npm install — 下载依赖

执行 `npm install` 后，npm 会：

1. 读取 `package.json` 中的依赖声明
2. 从 npm 仓库（registry.npmmirror.com）下载所有依赖包
3. 自动解析传递性依赖（依赖的依赖）
4. 将所有包解压到 `node_modules/` 目录
5. 生成 `package-lock.json`（锁定精确版本）

### node_modules — 依赖仓库

```
node_modules/
├── vue/              ← Vue 框架核心代码（约 1MB）
├── vue-router/       ← 路由库（约 200KB）
├── pinia/            ← 状态管理库（约 100KB）
├── axios/            ← HTTP 客户端（约 50KB）
├── vite/             ← 构建工具（约 5MB）
├── @vitejs/plugin-vue/ ← Vite Vue 插件
├── ...
└── （所有子依赖，如 rollup、esbuild 等）
```

> **为什么需要 node_modules？**
> 项目代码中使用 `import vue from 'vue'` 时，JavaScript 引擎会到 `node_modules/` 目录查找对应的包。没有它，运行时会报"模块找不到"错误。

---

## 三、工作流程

```
项目创建完成（仅 package.json + src/）
    │
    │  npm install
    ▼
npm 读取 package.json
    │
    │  查询 npm 仓库
    ▼
下载所有依赖包（包括子依赖）
    │
    │  解压到 node_modules/
    ▼
生成 package-lock.json（锁定版本）
    │
    ▼
项目可以运行了！
```

---

## 四、关键技术点

| 技术点 | 是什么 | 为什么用 |
| --- | --- | --- |
| **dependencies** | 生产依赖 | 应用运行时必需的包（Vue、Vue Router 等） |
| **devDependencies** | 开发依赖 | 开发和构建时需要的工具（Vite、插件等） |
| **package-lock.json** | 版本锁定文件 | 记录精确版本号，确保团队成员安装的版本一致 |
| **^ 版本前缀** | 兼容更新 | `^3.4.21` 表示允许安装 3.x.x 的最新版本 |
| **node_modules** | 依赖目录 | 存放所有下载的包，供应用运行时引用 |

### 版本号规则

```bash
^3.4.21  # 允许 3.x.x（不升级到 4.x）
~3.4.21  # 允许 3.4.x（不升级到 3.5）
3.4.21   # 固定版本
latest   # 最新版本
```

---

## 五、类比理解

| 前端 | 后端（Java Maven） |
| --- | --- |
| `package.json` | `pom.xml` |
| `npm install` | `mvn install` |
| `node_modules/` | `~/.m2/repository/` |
| `package-lock.json` | `pom.xml` 中的固定版本号 |
| `scripts.dev` | `mvn spring-boot:run` |

> **Maven 用户理解**：
> - `pom.xml` 只声明了依赖坐标（groupdId、artifactId、version）
> - `mvn install` 才会从 Maven 仓库下载 jar 包到本地仓库
> - 前端的 `npm install` 做的是同样的事情

---

## 六、常见问题

| 问题 | 原因 | 解决方案 |
| --- | --- | --- |
| **EPERM 权限错误** | npm 缓存目录没有写入权限 | 修改缓存目录到用户目录：`npm config set cache "C:\Users\songf\.npm-cache"` |
| **网络超时** | 连接 npm 仓库超时 | 使用国内镜像：`npm config set registry https://registry.npmmirror.com` |
| **node_modules 不存在** | 没有执行 npm install | 执行 `npm install` |
| **依赖冲突** | 不同依赖要求同一包的不同版本 | 删除 `node_modules` 和 `package-lock.json`，重新执行 `npm install` |

---

## 七、最佳实践

1. **首次克隆项目后，先执行 npm install**
2. **不要手动修改 node_modules**，通过 npm 命令管理
3. **不要提交 node_modules 到 Git**，在 `.gitignore` 中排除
4. **定期更新依赖**：`npm update`
5. **使用 npm 镜像加速**：`npm config set registry https://registry.npmmirror.com`