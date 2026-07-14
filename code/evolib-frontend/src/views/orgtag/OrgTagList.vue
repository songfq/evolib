<template>
  <AppLayout>
    <div class="page-container">
      <div class="page-header">
        <h2 class="page-title">组织标签</h2>
      </div>
      <div class="search-card">
        <div class="search-form">
          <EvoInput v-model="number" placeholder="请输入编码" label="编码" />
          <EvoButton type="primary" @click="getList">查询</EvoButton>
        </div>
        <div v-if="errorMsg" class="error-text">{{ errorMsg }}</div>
      </div>
      <EvoTable
        :columns="columns"
        :data="listdata"
        rowKey="id"
        :empty-text="showEmptyText ? '暂无在借记录' : ''"
      />
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
import { api } from "@/utils/api";
import AppLayout from "@/components/layout/AppLayout.vue";
import EvoInput from "@/components/common/EvoInput.vue";
import EvoButton from "@/components/common/EvoButton.vue";
import EvoTable from "@/components/common/EvoTable.vue";

const router = useRouter();
const number = ref("");
const errorMsg = ref("");
const listdata = ref([]);

const columns = [
  { key: "number", title: "编码", width: "auto" },
  { key: "name", title: "名称", width: "auto" },
  { key: "state", title: "状态", width: "120px" },
  { key: "createTime", title: "创建时间", width: "120px" },
  { key: "modifyTime", title: "修改时间", width: "120px" },
];

async function getList() {
  try {
    const result = await api.get("/orgTags/list");
    if (result.code === 0) {
      listdata.value = result.data?.records || [];
    } else {
      errorMsg.value = result.message;
    }
  } catch {
    errorMsg.value = "网络异常，请稍后重试";
  }
}
</script>
