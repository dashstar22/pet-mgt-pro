<template>
  <div class="pagination-wrapper" v-if="total > 0">
    <el-pagination
      :current-page="props.page"
      :page-size="props.size"
      :page-sizes="[12, 24, 48]"
      :total="props.total"
      layout="total, sizes, prev, pager, next, jumper"
      @current-change="(val) => emit('update:page', val)"
      @size-change="(val) => emit('update:size', val)"
    />
  </div>
</template>

<script setup>
const props = defineProps({
  total: { type: Number, default: 0 },
  page: { type: Number, default: 1 },
  size: { type: Number, default: 12 },
})

const emit = defineEmits(['update:page', 'update:size'])
</script>

<style scoped>
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
