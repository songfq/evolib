<template>
  <table class="evo-table">
    <thead>
      <tr>
        <th v-for="col in columns" :key="col.key" :style="{ width: col.width }">
          {{ col.title }}
        </th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="(row, idx) in data" :key="rowKey ? row[rowKey] : idx" :class="{ 'row-overdue': row._overdue }">
        <td v-for="col in columns" :key="col.key">
          <slot :name="`cell-${col.key}`" :row="row" :value="row[col.key]">
            {{ row[col.key] }}
          </slot>
        </td>
      </tr>
    </tbody>
  </table>
</template>

<script setup>
defineProps({
  columns: { type: Array, required: true },
  data: { type: Array, required: true },
  rowKey: { type: String, default: '' },
});
</script>

<style scoped>
.evo-table { width: 100%; border-collapse: collapse; background: var(--color-bg-page); font-size: var(--font-size-base); }
.evo-table th { height: var(--table-row-height); padding: 0 var(--spacing-md); background: var(--color-bg-container); color: var(--color-text); border-bottom: 2px solid var(--color-border); text-align: left; font-weight: 600; }
.evo-table td { height: var(--table-row-height); padding: 0 var(--spacing-md); border-bottom: 1px solid var(--color-border-light); color: var(--color-text); }
.evo-table tbody tr:hover { background: var(--color-bg-hover); }
.evo-table .row-overdue { background: var(--color-overdue); }
</style>