# fe-component — 前端页面开发 SOP（7 步）

## 目标

用 Element Plus 组件拼出一个业务页面，完成从「0 到可交互」的完整流程。

---

## 前置条件

- ✅ 前端骨架已通过 `rules/fe-scaffold.md` 搭建
- ✅ 理解 `rules/fe-standards.md` 的 SFC 结构 / 路由 / Store / Axios 模式
- ✅ 对应的后端 API 已开发完成（`rules/be-api.md`）
- ✅ 理解 `rules/api-design.md` 的 API 契约（错误码 / 分页格式）

---

## 通用页面模板

### 模板 A：列表搜索页（P-02 图书检索 / P-07 在借清单）

```vue
<!-- XxxView.vue — 列表搜索页通用模板 -->
<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';     // Element Plus 消息提示
import { api } from '@/utils/api';            // Axios 封装

// ---- 响应式状态 ----
const keyword = ref('');                      // 搜索关键词（v-model 绑定到输入框）
const tableData = ref([]);                    // 表格数据
const loading  = ref(false);                  // 加载状态
const total    = ref(0);                      // 总记录数（用于分页）
const page     = ref(1);                      // 当前页码
const pageSize = 10;                          // 每页条数

// ---- 业务方法 ----
async function doSearch() {
  if (!keyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词');
    return;
  }
  loading.value = true;
  try {
    const resp = await api.get('/books/search', {
      params: { keyword: keyword.value, page: page.value, size: pageSize }
      // axios 会自动将对象转为 Query String: ?keyword=Java&page=1&size=10
    });
    if (resp.code === 0) {
      tableData.value = resp.data.list || [];
      total.value = resp.data.total || 0;
    } else {
      ElMessage.error(resp.message);           // 直接显示后端中文错误消息
    }
  } catch (err) {
    ElMessage.error('网络异常，请稍后重试');
  } finally {
    loading.value = false;
  }
}

// 页码变化 → 重新搜索
function onPageChange(newPage) {
  page.value = newPage;
  doSearch();
}

// 页面首次加载时自动执行一次搜索（如果需要默认数据）
// onMounted(() => { doSearch(); });
</script>

<template>
  <el-container>
    <el-header height="56px">
      <h2>图书检索</h2>
    </el-header>
    <el-main>
      <!-- 搜索条件区 — Element Plus 输入框 + 按钮 -->
      <div style="display: flex; gap: 12px; margin-bottom: 16px;">
        <el-input v-model="keyword" placeholder="搜索书名、作者或 ISBN" style="width: 400px;" />
        <el-button type="primary" @click="doSearch">搜索</el-button>
      </div>

      <!-- 结果表格 — Element Plus 表格 + loading -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="title"  label="书名" />
        <el-table-column prop="author" label="作者" width="180" />
        <el-table-column prop="isbn"   label="ISBN"  width="200" />
        <el-table-column prop="availableStock" label="可借数量" width="100" />
        <el-table-column prop="shelfLocation" label="架位号" width="120" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" link @click="goDetail(row.isbn)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 — Element Plus 分页组件 -->
      <el-pagination
        v-if="total > 0"
        :current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="onPageChange"
        style="margin-top: 16px; justify-content: center;"
      />
    </el-main>
  </el-container>
</template>

<style scoped>
/* 页面特有样式 — 全局样式走 theme.css，这里只写本页独有的 */
.el-header {
  display: flex;
  align-items: center;
  background: var(--color-bg-container);
  border-bottom: 1px solid var(--color-border);
}
</style>
```

### 模板 B：表单提交页（P-04 借书 / P-05 还书 / P-06 注册 / P-08 上架）

```vue
<!-- XxxView.vue — 表单提交页通用模板 -->
<script setup>
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { api } from '@/utils/api';

// ---- 响应式状态 ----
const loading  = ref(false);
const formRef  = ref(null);          // el-form 引用，用于触发表单校验

const form = reactive({
  readerId: '',
  isbn:     '',
});

// el-form 校验规则 — Element Plus 内置校验
const rules = {
  readerId: [{ required: true, message: '请输入读者借阅证号', trigger: 'blur' }],
  isbn:     [{ required: true, message: '请输入图书 ISBN',   trigger: 'blur' }],
};

// ---- 业务方法 ----
async function doSubmit() {
  // 触发 Element Plus 表单校验
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;                           // 校验不通过 → 停止提交

  loading.value = true;
  try {
    const resp = await api.post('/borrow-records', { ...form });
    if (resp.code === 0) {
      ElMessage.success('借书成功');
      // 重置表单
      form.readerId = '';
      form.isbn = '';
      formRef.value.resetFields();
    } else {
      ElMessage.error(resp.message);            // 后端错误码 → 中文提示
    }
  } catch (err) {
    ElMessage.error('网络异常');
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <el-container>
    <el-header><h2>借书操作台</h2></el-header>
    <el-main>
      <!-- Element Plus 表单 — 自动校验 + 提交 -->
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 500px;">
        <el-form-item label="读者ID" prop="readerId">
          <el-input v-model="form.readerId" placeholder="请输入借阅证号" />
        </el-form-item>
        <el-form-item label="图书ISBN" prop="isbn">
          <el-input v-model="form.isbn" placeholder="请输入图书 ISBN" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="doSubmit">确认借书</el-button>
        </el-form-item>
      </el-form>
    </el-main>
  </el-container>
</template>
```

### 模板 C：详情展示页（P-03 图书详情）

```vue
<!-- XxxView.vue — 详情展示页通用模板 -->
<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { api } from '@/utils/api';

const route = useRoute();                     // 获取 URL 参数 {isbn}
const book   = ref(null);
const loading = ref(true);

onMounted(async () => {
  const isbn = route.params.isbn;              // 从 URL /reader/detail/978-xxx 中取 isbn
  try {
    const resp = await api.get(`/books/${isbn}`);
    book.value = resp.data;
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <el-container v-loading="loading">
    <el-main>
      <el-descriptions v-if="book" :column="2" border>
        <el-descriptions-item label="书名">{{ book.title }}</el-descriptions-item>
        <el-descriptions-item label="作者">{{ book.author }}</el-descriptions-item>
        <el-descriptions-item label="ISBN">{{ book.isbn }}</el-descriptions-item>
        <el-descriptions-item label="架位号">{{ book.shelfLocation }}</el-descriptions-item>
        <el-descriptions-item label="总库存">{{ book.totalStock }}</el-descriptions-item>
        <el-descriptions-item label="可借数量">{{ book.availableStock }}</el-descriptions-item>
        <el-descriptions-item label="简介" :span="2">{{ book.description }}</el-descriptions-item>
      </el-descriptions>
      <el-button @click="$router.back()">返回检索页</el-button>
    </el-main>
  </el-container>
</template>
```

---

## 标准 7 步流程

### 第 1 步：确定页面编号 + 选择模板

根据 `docs/SRS_v1.0.md` §8 确定页面编号（P-01~P-10），然后选择模板：

| 页面类型 | 用哪个模板 | 哪些页面 |
| --- | --- | --- |
| 列表搜索 | 模板 A | P-02 图书检索、P-07 在借清单 |
| 表单提交 | 模板 B | P-01 登录、P-04 借书、P-05 还书、P-06 注册、P-08 上架、P-09 下架、P-10 重置密码 |
| 详情展示 | 模板 C | P-03 图书详情 |

### 第 2 步：创建 Vue 文件 + 编写 <script setup>

在 `src/views/{角色}/` 下创建 `XxxView.vue`，从模板粘贴骨架，修改 API 调用路径和字段名。

> **为什么 <script setup> 要放在最前面？** 阅读代码时先看到数据流（状态 → 方法 → 生命周期），然后看模板（UI 拼装），最后看样式。这是 Vue 3 社区的推荐顺序。

### 第 3 步：编写 <template> 用 Element Plus 拼 UI

- 布局用 `<el-container>` + `<el-header>` + `<el-main>`
- 表格用 `<el-table>` + `<el-table-column>`
- 表单用 `<el-form>` + `<el-form-item>` + `<el-input>`
- 弹窗用 `<el-dialog>`
- 所有文字内容使用 Element Plus 默认样式（继承 `theme.css` 的 CSS 变量）

### 第 4 步：编写 <style scoped>

仅写页面特有的微调样式。所有颜色通过 `var(--color-xxx)` 引用 `theme.css`。

**禁止**：写死颜色值（如 `color: #333`）、写死尺寸（如 `width: 200px`，应该用百分比或响应式）

### 第 5 步：对接后端 API

确认 API 调用与 `rules/api-design.md` 的契约一致：
- [ ] URL 路径正确
- [ ] 请求方法正确（GET/POST/PUT/DELETE）
- [ ] 错误时显示 `resp.message`（后端返回的中文）

### 第 6 步：注册路由

在 `src/router/index.js` 中新增路由条目，标注 `meta.role`：

```javascript
// 示例：新增借书页面路由
{
  path: '/circulation/borrow',
  name: 'Borrow',
  component: () => import('@/views/circulation/BorrowView.vue'),  // 懒加载
  meta: { role: 'ROLE_CIRCULATION' }                                // 权限标注
}
```

### 第 7 步：生成学习笔记

本次页面开发完成后，生成学习笔记 `docs/learning/fe/{模块}/{日期}-{功能}.md`。

格式参照 `rules/learning.md`，四个段落：
1. 做了什么（页面编号 + 功能描述 + 涉及文件）
2. 代码逻辑（Element Plus 组件组合方式 + 中文注释解释）
3. 前后端如何联动（用户操作 → Axios 请求 → 后端 → 响应 → 页面更新）
4. 关键技术点（Vue 3 响应式原理 / Element Plus 组件配置要点）
