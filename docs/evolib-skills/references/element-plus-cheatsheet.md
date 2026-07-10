# Element Plus 常用组件速查

> 完整文档：[element-plus.org](https://element-plus.org/zh-CN/component/button.html)

## 按钮

```vue
<el-button type="primary" @click="fn">主按钮</el-button>
<el-button type="default">次按钮</el-button>
<el-button type="danger">危险按钮</el-button>
<el-button :loading="true">Loading</el-button>
<el-button :disabled="true">禁用</el-button>
```

## 输入框

```vue
<el-input v-model="val" placeholder="请输入" clearable />
<el-input v-model="val" type="password" show-password />
<el-input v-model="val" @keyup.enter="fn" />
```

## 下拉选择

```vue
<el-select v-model="val" placeholder="请选择">
  <el-option label="书名" value="title" />
  <el-option label="作者" value="author" />
  <el-option label="ISBN" value="isbn" />
</el-select>
```

## 表格

```vue
<el-table :data="list" border stripe v-loading="loading">
  <el-table-column prop="title" label="书名" />
  <el-table-column prop="author" label="作者" width="180" />
  <el-table-column label="操作" width="100">
    <template #default="{ row }">
      <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
    </template>
  </el-table-column>
</el-table>
```

## 分页

```vue
<el-pagination
  :current-page="page"
  :page-size="10"
  :total="total"
  layout="total, prev, pager, next"
  @current-change="fn"
/>
```

## 弹窗

```vue
<el-dialog v-model="visible" title="提示" width="400px">
  弹窗内容
  <template #footer>
    <el-button @click="visible = false">取消</el-button>
    <el-button type="primary" @click="handleConfirm">确认</el-button>
  </template>
</el-dialog>
```

## 表单

```vue
<el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
  <el-form-item label="名称" prop="name">
    <el-input v-model="form.name" />
  </el-form-item>
  <el-form-item>
    <el-button type="primary" @click="doSubmit">提交</el-button>
  </el-form-item>
</el-form>
```

## 布局

```vue
<el-container>
  <el-header height="56px">Header</el-header>
  <el-container>
    <el-aside width="220px">Sidebar</el-aside>
    <el-main>Content</el-main>
  </el-container>
</el-container>
```

## 消息提示

```vue
<script setup>
import { ElMessage } from 'element-plus';
ElMessage.success('操作成功');
ElMessage.error('操作失败');
ElMessage.warning('警告');
ElMessage.info('提示');
</script>
```

## 描述列表（详情页专用）

```vue
<el-descriptions :column="2" border>
  <el-descriptions-item label="书名">Java 编程思想</el-descriptions-item>
  <el-descriptions-item label="作者">Bruce Eckel</el-descriptions-item>
</el-descriptions>
```
