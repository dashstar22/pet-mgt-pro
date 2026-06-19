# Admin UI Improvements — 设计文档

## 概述

四项独立改进：删除审核记录、头像显示、宠物状态手动修改、品种/状态中文化。

---

## 1. 删除审核记录

### 现状
- 后端 `DELETE /api/admin/applications/{id}` 已存在，Service 层 `adminDeleteById()` 已实现（限制：已通过的不允许删除）
- 前端 `admin.js` 已有 `deleteApp(id)` 接口函数
- **缺失**：UI 层无删除入口

### 设计
| 位置 | 改动 |
|------|------|
| `ApplicationListView.vue` | 操作列加"删除"按钮，`v-if="row.status !== 'approved'"`；点击弹 `ElMessageBox.confirm` 确认，确认后调用 `deleteApp(row.id)` 并刷新列表 |
| `ApplicationDetailView.vue` | 通过/拒绝按钮旁加"删除"按钮（`v-if="application.status !== 'approved'"`），同样 confirm → delete → router.push 回列表 |
| 错误处理 | 后端已有限制（approved 不可删），前端 `catch` 后 `ElMessage.error` 提示 |

---

## 2. 头像显示

### 现状
- 上传链路完整（`POST /api/upload` → `updateProfile` → 存储 `avatarUrl`）
- `FileUpload` 组件内部可预览（`previewUrl` computed）
- **缺失**：AppHeader 导航栏不显示头像，ProfileView 没有上传后的独立预览

### 设计
| 位置 | 改动 |
|------|------|
| `AppHeader.vue` | 用户名左侧插入 `<el-avatar :src="avatarUrl" :size="32" />`，`avatarUrl` 通过 `getImageUrl(userStore.userInfo?.avatarUrl)` 计算，无头像时显示默认 el-avatar 图标 |
| `ProfileView.vue` | FileUpload 上方或同行左侧加当前头像大图预览（`<el-avatar :src="avatarUrl" :size="80" />`），上传后即时更新 |
| `imageUrl.js` | 复用已有的 `getImageUrl()` 工具函数，无需改动 |

---

## 3. 宠物状态手动修改

### 现状
- 后端 `PUT /api/admin/pets/{id}` 已支持可选 `status` 参数
- 前端 `PetFormView` 无状态下拉框
- 状态只能通过申请审批流程自动变更

### 设计
| 位置 | 改动 |
|------|------|
| `PetManageView.vue` | 操作列加"改状态"按钮或下拉，提供选项：可领养(`available`) / 已领养(`adopted`) / 待审核(`pending`)，确认后调用 `updatePet(id, { status })` 并刷新列表 |
| `PetFormView.vue` | 编辑模式下（`isEdit` 为 true）增加 `el-select` 状态下拉框，选项同上，`v-model="form.status"`，提交时在 FormData 中附带 `status` |
| 后端 | 无需改动，`update` 方法已支持 status 参数 |

---

## 4. 品种名称和状态中文化

### 现状
- 数据库种子数据品种名全为英文（British Shorthair 等）
- `PetCard.vue` / `PetDetailView.vue` 状态只映射了 `available→可领养`，其余状态显示英文原文
- `PetManageView.vue` 状态映射已是中文，作为参照

### 设计

#### 4.1 品种名映射
- **新建** `frontend/src/utils/labels.js`，导出两个映射：
  - `breedLabel`：`{ "British Shorthair": "英短", "Persian": "波斯", "Siamese": "暹罗", "Maine Coon": "缅因", "Ragdoll": "布偶", "Golden Retriever": "金毛", "Corgi": "柯基", "Husky": "哈士奇", "Labrador": "拉布拉多", "Poodle": "贵宾", "Holland Lop": "荷兰垂耳兔", "Netherland Dwarf": "荷兰侏儒兔", "Mini Rex": "迷你雷克斯兔" }`
  - `petStatusLabel`：`{ available: '可领养', adopted: '已领养', pending: '待审核' }`
  - `appStatusLabel`：`{ pending: '待审核', approved: '已通过', rejected: '已拒绝', cancelled: '已取消' }`
- **修改** `data.sql`：种子数据品种名改为中文
- **使用方**（`PetCard.vue`、`PetDetailView.vue`、`PetListView.vue`、`PetManageView.vue`、`PetFormView.vue`、`ApplicationListView.vue`、`ApplicationDetailView.vue` 等）：
  - 品种名：`breedLabel[breedName] || breedName`
  - 宠物状态：`petStatusLabel[status] || status`
  - 申请状态：`appStatusLabel[status] || status`（已有自维护映射的组件改为引用此统一映射）

#### 4.2 状态中文化
- **修改 `PetCard.vue`**：`pet.status === 'available' ? '可领养' : pet.status` → `petStatusLabel[pet.status] || pet.status`
- **修改 `PetDetailView.vue`**：同上
- **修改 `PetManageView.vue`**：本地 statusLabel → 改用 `labels.js` 统一导出
- **修改 `ApplicationListView.vue`**：本地 statusLabel → 改用 `labels.js` 统一导出
- **修改 `ApplicationDetailView.vue`**：同上

---

## 不改动范围
- 后端 API 无需改动（所有端点已完备）
- 数据库 schema 不变
- 路由不变
- PetFormView 创建模式不显示状态下拉（创建时后端强制 available）
