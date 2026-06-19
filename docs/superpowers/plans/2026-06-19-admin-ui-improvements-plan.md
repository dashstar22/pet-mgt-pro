# Admin UI Improvements — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Four frontend UI improvements: delete review records, avatar display, pet status modification, and Chinese labels for breeds/statuses. Plus one small backend endpoint for status-only updates.

**Architecture:** All changes are frontend Vue components with one backend addition. A new shared `labels.js` utility centralizes all Chinese display mappings. Components import from this utility instead of maintaining local mappings. Backend gets a minimal `PATCH /api/admin/pets/{id}/status` endpoint for status-only updates.

**Tech Stack:** Vue 3 (Composition API), Element Plus, Spring Boot, MyBatis-Plus, MySQL

---

### Task 1: Create shared labels utility

**Files:**
- Create: `frontend/src/utils/labels.js`

- [ ] **Step 1: Write the labels utility**

```js
// frontend/src/utils/labels.js

/**
 * Breed name mapping: English → Chinese.
 * Covers seed data breeds. New entries added here as needed.
 */
export const breedLabel = {
  'British Shorthair': '英短',
  'Persian': '波斯',
  'Siamese': '暹罗',
  'Maine Coon': '缅因',
  'Ragdoll': '布偶',
  'Golden Retriever': '金毛',
  'Corgi': '柯基',
  'Husky': '哈士奇',
  'Labrador': '拉布拉多',
  'Poodle': '贵宾',
  'Holland Lop': '荷兰垂耳兔',
  'Netherland Dwarf': '荷兰侏儒兔',
  'Mini Rex': '迷你雷克斯兔',
}

/**
 * Resolve breed display name — uses mapping when available, falls back to raw value.
 */
export function getBreedDisplay(breedName) {
  if (!breedName) return '未知品种'
  return breedLabel[breedName] || breedName
}

/**
 * Pet status mapping: value → Chinese label.
 */
export const petStatusLabel = {
  available: '可领养',
  adopted: '已领养',
  pending: '待审核',
}

/** Resolve pet status display name. */
export function getPetStatusDisplay(status) {
  if (!status) return '未知'
  return petStatusLabel[status] || status
}

/** Pet status → Element Plus tag type. */
export function getPetStatusTagType(status) {
  const m = { available: 'success', adopted: 'warning', pending: 'info' }
  return m[status] || 'info'
}

/**
 * Application status mapping.
 */
export const appStatusLabel = {
  pending: '待审核',
  approved: '已通过',
  rejected: '已拒绝',
  cancelled: '已取消',
}

/** Resolve application status display name. */
export function getAppStatusDisplay(status) {
  if (!status) return '未知'
  return appStatusLabel[status] || status
}

/** Application status → Element Plus tag type. */
export function getAppStatusTagType(status) {
  const m = { pending: 'warning', approved: 'success', rejected: 'danger', cancelled: 'info' }
  return m[status] || 'info'
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/utils/labels.js
git commit -m "feat: add shared labels utility for Chinese display mappings"
```

---

### Task 2: Update seed data — breed names to Chinese

**Files:**
- Modify: `src/main/resources/data.sql:14-27`

- [ ] **Step 1: Replace breed names in data.sql**

```sql
INSERT IGNORE INTO pet_breed (breed_name, pet_type) VALUES
('英短', '猫'),
('波斯', '猫'),
('暹罗', '猫'),
('缅因', '猫'),
('布偶', '猫'),
('金毛', '狗'),
('柯基', '狗'),
('哈士奇', '狗'),
('拉布拉多', '狗'),
('贵宾', '狗'),
('荷兰垂耳兔', '兔'),
('荷兰侏儒兔', '兔'),
('迷你雷克斯兔', '兔');
```

- [ ] **Step 2: Commit**

```bash
git add src/main/resources/data.sql
git commit -m "feat: change seed breed names to Chinese"
```

---

### Task 3: Update PetCard.vue — Chinese labels for breed name and status

**Files:**
- Modify: `frontend/src/components/PetCard.vue`

- [ ] **Step 1: Update template to use Chinese labels**

In `<template>`, line 9 and 13-14:

Replace:
```html
<p class="pet-card-breed">{{ pet.breedName || '未知品种' }}</p>
```
With:
```html
<p class="pet-card-breed">{{ getBreedDisplay(pet.breedName) }}</p>
```

Replace:
```html
<el-tag size="small" :type="pet.status === 'available' ? 'success' : 'warning'">
  {{ pet.status === 'available' ? '可领养' : pet.status }}
</el-tag>
```
With:
```html
<el-tag size="small" :type="getPetStatusTagType(pet.status)">
  {{ getPetStatusDisplay(pet.status) }}
</el-tag>
```

- [ ] **Step 2: Update script to import from labels.js**

In `<script setup>`, add import after the existing `getImageUrl` import:

```js
import { getImageUrl } from '@/utils/imageUrl'
import { getBreedDisplay, getPetStatusDisplay, getPetStatusTagType } from '@/utils/labels'
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/components/PetCard.vue
git commit -m "feat: use Chinese labels for breed and status in PetCard"
```

---

### Task 4: Update PetDetailView.vue — Chinese labels for breed name and status

**Files:**
- Modify: `frontend/src/views/PetDetailView.vue`

- [ ] **Step 1: Update template**

Line 31 — replace:
```html
<p class="detail-breed">{{ pet.breedName || '未知品种' }}</p>
```
With:
```html
<p class="detail-breed">{{ getBreedDisplay(pet.breedName) }}</p>
```

Lines 43-45 — replace:
```html
<el-tag :type="pet.status === 'available' ? 'success' : 'warning'">
  {{ pet.status === 'available' ? '可领养' : pet.status }}
</el-tag>
```
With:
```html
<el-tag :type="getPetStatusTagType(pet.status)">
  {{ getPetStatusDisplay(pet.status) }}
</el-tag>
```

- [ ] **Step 2: Update script imports**

Add to the import block (line 77):
```js
import { getBreedDisplay, getPetStatusDisplay, getPetStatusTagType } from '@/utils/labels'
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/views/PetDetailView.vue
git commit -m "feat: use Chinese labels for breed and status in PetDetailView"
```

---

### Task 5: Update PetManageView.vue — labels + status change button

**Files:**
- Modify: `frontend/src/views/admin/PetManageView.vue`
- Modify: `frontend/src/api/admin.js` (add status update API)
- Create: `src/main/java/com/petmgt/controller/admin/PetStatusController.java` or modify `PetManageController.java`

- [ ] **Step 1: Add backend endpoint for status-only update**

Modify `src/main/java/com/petmgt/controller/admin/PetManageController.java` — add after the `delete` method (line 184):

```java
@PatchMapping("/{id}/status")
public ApiResponse<Void> updateStatus(
        @PathVariable Long id,
        @RequestParam("status") String status) {
    if (!List.of("available", "adopted", "pending").contains(status)) {
        return ApiResponse.error(400, "无效的状态值，有效值为: available, adopted, pending");
    }
    Pet pet = petMapper.selectById(id);
    if (pet == null) {
        return ApiResponse.error(404, "宠物不存在");
    }
    pet.setStatus(status);
    petMapper.updateById(pet);
    return ApiResponse.success("状态更新成功", null);
}
```

- [ ] **Step 2: Add frontend API function**

In `frontend/src/api/admin.js`, add after the `deletePet` function (line 48):

```js
export function updatePetStatus(id, status) {
  return request.patch(`/admin/pets/${id}/status`, null, { params: { status } })
}
```

- [ ] **Step 3: Update PetManageView template — replace status display and add status change**

Replace the status column template (lines 47-51):
```html
<el-table-column prop="status" label="状态" width="90">
  <template #default="{ row }">
    <el-tag :type="statusType(row.status)" size="small">
      {{ statusLabel(row.status) }}
    </el-tag>
  </template>
</el-table-column>
```
With (using labels.js):
```html
<el-table-column prop="status" label="状态" width="90">
  <template #default="{ row }">
    <el-tag :type="getPetStatusTagType(row.status)" size="small">
      {{ getPetStatusDisplay(row.status) }}
    </el-tag>
  </template>
</el-table-column>
```

Replace the breed filter dropdown labels to use Chinese display (line 22):
```html
<el-option
  v-for="b in breeds"
  :key="b.id"
  :label="getBreedDisplay(b.breedName)"
  :value="b.id"
/>
```

Add a "改状态" dropdown in the actions column, after the edit button (line 58):
```html
<el-table-column label="操作" width="240" fixed="right">
  <template #default="{ row }">
    <el-button size="small" @click="$router.push(`/admin/pets/${row.id}/edit`)">编辑</el-button>
    <el-dropdown trigger="click" @command="(status) => handleStatusChange(row, status)" style="margin-left:8px">
      <el-button size="small" type="warning">
        改状态<el-icon class="el-icon--right"><ArrowDown /></el-icon>
      </el-button>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item
            v-for="opt in statusOptions"
            :key="opt.value"
            :command="opt.value"
            :disabled="row.status === opt.value"
          >
            {{ opt.label }}
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
    <el-button size="small" type="danger" @click="handleDelete(row)" style="margin-left:8px">删除</el-button>
  </template>
</el-table-column>
```

- [ ] **Step 4: Update PetManageView script**

Replace the local `statusType` and `statusLabel` functions with imports from `labels.js`.
Add `handleStatusChange` function. Add `statusOptions` constant.

Update the imports:
```js
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { getAdminPetList, deletePet, updatePetStatus } from '@/api/admin'
import { getBreeds } from '@/api/breed'
import { getBreedDisplay, getPetStatusDisplay, getPetStatusTagType } from '@/utils/labels'
import Pagination from '@/components/Pagination.vue'
```

Add after the existing constant declarations:
```js
const statusOptions = [
  { label: '可领养', value: 'available' },
  { label: '已领养', value: 'adopted' },
  { label: '待审核', value: 'pending' },
]
```

Add the status change handler (after `handleDelete`):
```js
async function handleStatusChange(row, newStatus) {
  try {
    const labelMap = { available: '可领养', adopted: '已领养', pending: '待审核' }
    await ElMessageBox.confirm(
      `确定将「${row.name}」的状态从「${getPetStatusDisplay(row.status)}」改为「${labelMap[newStatus]}」吗？`,
      '确认修改状态',
      { type: 'warning', confirmButtonText: '确定修改' }
    )
    await updatePetStatus(row.id, newStatus)
    ElMessage.success('状态已更新')
    loadData()
  } catch {}
}
```

Remove the old local functions `statusType` and `statusLabel` (lines 130-137).

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/petmgt/controller/admin/PetManageController.java
git add frontend/src/api/admin.js
git add frontend/src/views/admin/PetManageView.vue
git commit -m "feat: add pet status change in admin list + use labels.js"
```

---

### Task 6: Update PetFormView.vue — labels + status dropdown in edit mode

**Files:**
- Modify: `frontend/src/views/admin/PetFormView.vue`

- [ ] **Step 1: Update breed dropdown to use Chinese labels**

Line 11 — replace:
```html
<el-option v-for="b in breeds" :key="b.id" :label="b.breedName" :value="b.id" />
```
With:
```html
<el-option v-for="b in breeds" :key="b.id" :label="getBreedDisplay(b.breedName)" :value="b.id" />
```

- [ ] **Step 2: Add status dropdown for edit mode**

After the "领养要求" form item (line 40), add:
```html
<el-form-item v-if="isEdit" label="领养状态" prop="status">
  <el-select v-model="form.status" placeholder="请选择状态">
    <el-option label="可领养" value="available" />
    <el-option label="已领养" value="adopted" />
    <el-option label="待审核" value="pending" />
  </el-select>
</el-form-item>
```

- [ ] **Step 3: Add `status` to the reactive form**

In the `form` reactive declaration (line 113), add after `adoptionRequirement`:
```js
const form = reactive({
  name: '', breedId: null, gender: '公', age: 0, weight: null,
  healthStatus: '', vaccineStatus: '', sterilizationStatus: '',
  personality: '', adoptionRequirement: '', status: '',
  existingImages: [],
  newImages: [],
  deleteImageIds: [],
})
```

- [ ] **Step 4: Load status when editing**

In `loadPet` function (line 144), after `form.adoptionRequirement = pet.adoptionRequirement || ''`:
```js
form.status = pet.status || ''
```

- [ ] **Step 5: Send status in handleSave for edit mode**

In `handleSave` (line 248), after `fd.append('adoptionRequirement', form.adoptionRequirement || '')`:
```js
if (isEdit.value && form.status) {
  fd.append('status', form.status)
}
```

- [ ] **Step 6: Add import for getBreedDisplay**

In the `<script setup>` imports (line 94), add:
```js
import { getBreedDisplay } from '@/utils/labels'
```

- [ ] **Step 7: Commit**

```bash
git add frontend/src/views/admin/PetFormView.vue
git commit -m "feat: add status dropdown in edit mode + Chinese breed labels in PetFormView"
```

---

### Task 7: Update ApplicationListView.vue — labels + delete button

**Files:**
- Modify: `frontend/src/views/admin/ApplicationListView.vue`

- [ ] **Step 1: Replace local statusLabel/statusType with labels.js imports**

In `<script setup>`, replace the import line:
```js
import { getAppList } from '@/api/admin'
```
With:
```js
import { getAppList, deleteApp } from '@/api/admin'
import { getAppStatusDisplay, getAppStatusTagType } from '@/utils/labels'
```

- [ ] **Step 2: Update template status column to use label functions**

Lines 18-21 — replace:
```html
<el-table-column prop="status" label="状态" width="80">
  <template #default="{ row }">
    <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
  </template>
</el-table-column>
```
With:
```html
<el-table-column prop="status" label="状态" width="80">
  <template #default="{ row }">
    <el-tag :type="getAppStatusTagType(row.status)" size="small">{{ getAppStatusDisplay(row.status) }}</el-tag>
  </template>
</el-table-column>
```

- [ ] **Step 3: Add delete button in actions column**

Replace the actions column template (lines 26-30):
```html
<el-table-column label="操作" width="140" fixed="right">
  <template #default="{ row }">
    <el-button size="small" @click.stop="goDetail(row)">审核</el-button>
    <el-button
      v-if="row.status !== 'approved'"
      size="small"
      type="danger"
      @click.stop="handleDelete(row)"
    >删除</el-button>
  </template>
</el-table-column>
```

- [ ] **Step 4: Add handleDelete function and remove old local functions**

Add `ElMessageBox` to the element-plus import:
```js
import { ElMessage, ElMessageBox } from 'element-plus'
```

Add `handleDelete` after `goDetail`:
```js
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定要删除「${row.petName}」的审核记录吗？此操作不可恢复。`,
      '确认删除',
      { type: 'warning', confirmButtonText: '确定删除' }
    )
    await deleteApp(row.id)
    ElMessage.success('已删除')
    loadData()
  } catch {}
}
```

Remove the old local `statusType` and `statusLabel` functions (lines 64-65). Keep `formatDate`.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/views/admin/ApplicationListView.vue
git commit -m "feat: add delete button in application list + use labels.js"
```

---

### Task 8: Update ApplicationDetailView.vue — labels + delete button

**Files:**
- Modify: `frontend/src/views/admin/ApplicationDetailView.vue`

- [ ] **Step 1: Update imports**

In `<script setup>`, replace:
```js
import { getAppDetail, approveApp, rejectApp } from '@/api/admin'
```
With:
```js
import { getAppDetail, approveApp, rejectApp, deleteApp } from '@/api/admin'
import { getAppStatusDisplay, getAppStatusTagType } from '@/utils/labels'
```

- [ ] **Step 2: Update status tag in template to use label functions**

Lines 19-21 — replace:
```html
<el-tag :type="statusType(detail.application?.status)" size="small">
  {{ statusLabel(detail.application?.status) }}
</el-tag>
```
With:
```html
<el-tag :type="getAppStatusTagType(detail.application?.status)" size="small">
  {{ getAppStatusDisplay(detail.application?.status) }}
</el-tag>
```

- [ ] **Step 3: Add delete button in review actions area**

After the reject button (line 73), add:
```html
<el-button
  v-if="detail.application?.status !== 'approved'"
  type="danger"
  size="large"
  plain
  @click="handleDelete"
>
  <el-icon><Delete /></el-icon> 删除记录
</el-button>
```

Update the icon import on line 109 to include `Delete`:
```js
import { Check, Close, ArrowLeft, Delete } from '@element-plus/icons-vue'
```

- [ ] **Step 4: Add handleDelete function**

Add after `handleReject`:
```js
async function handleDelete() {
  try {
    await ElMessageBox.confirm(
      '确定要删除此审核记录吗？此操作不可恢复。',
      '确认删除',
      { type: 'warning', confirmButtonText: '确定删除' }
    )
    await deleteApp(route.params.id)
    ElMessage.success('已删除')
    router.push('/admin/applications')
  } catch {}
}
```

- [ ] **Step 5: Remove old local status functions**

Remove local `statusType` and `statusLabel` functions (lines 156-161). Keep `scoreColor` and `formatDate`.

- [ ] **Step 6: Commit**

```bash
git add frontend/src/views/admin/ApplicationDetailView.vue
git commit -m "feat: add delete button in application detail + use labels.js"
```

---

### Task 9: Add avatar display to AppHeader

**Files:**
- Modify: `frontend/src/components/AppHeader.vue`

- [ ] **Step 1: Add avatar before username in logout item**

In `<template>`, line 46 — replace:
```html
<template v-if="userStore.isLoggedIn">
  <el-menu-item index="logout" class="logout-item">
    <span style="color: #909399;">{{ userStore.userInfo?.username }}</span>
    &nbsp;退出
  </el-menu-item>
</template>
```
With:
```html
<template v-if="userStore.isLoggedIn">
  <el-menu-item index="logout" class="logout-item">
    <el-avatar :src="avatarSrc" :size="24" class="header-avatar">
      <el-icon :size="14"><UserFilled /></el-icon>
    </el-avatar>
    <span style="color: #909399; margin-left: 6px;">{{ userStore.userInfo?.username }}</span>
    &nbsp;退出
  </el-menu-item>
</template>
```

- [ ] **Step 2: Add computed avatarSrc**

In `<script setup>`, add after the `useUserStore()` call:
```js
import { getImageUrl } from '@/utils/imageUrl'
import { UserFilled } from '@element-plus/icons-vue'

const avatarSrc = computed(() => getImageUrl(userStore.userInfo?.avatarUrl))
```

- [ ] **Step 3: Add style for header avatar**

In `<style scoped>`, add:
```css
.header-avatar {
  flex-shrink: 0;
}
```

- [ ] **Step 4: Commit**

```bash
git add frontend/src/components/AppHeader.vue
git commit -m "feat: add avatar display in AppHeader"
```

---

### Task 10: Add avatar preview to ProfileView

**Files:**
- Modify: `frontend/src/views/user/ProfileView.vue`

- [ ] **Step 1: Add avatar preview above FileUpload**

In `<template>`, replace lines 6-9:
```html
<div class="profile-section">
  <h3>头像</h3>
  <FileUpload v-model="avatarUrl" />
</div>
```
With:
```html
<div class="profile-section">
  <h3>头像</h3>
  <div class="avatar-area">
    <el-avatar :src="avatarPreviewUrl" :size="80" class="profile-avatar">
      <el-icon :size="40"><UserFilled /></el-icon>
    </el-avatar>
    <div class="avatar-upload">
      <FileUpload v-model="avatarUrl" />
    </div>
  </div>
</div>
```

- [ ] **Step 2: Add computed for avatar preview URL**

In `<script setup>`, add import:
```js
import { getImageUrl } from '@/utils/imageUrl'
import { UserFilled } from '@element-plus/icons-vue'
```

Add computed after `avatarUrl` declaration (line 58):
```js
import { computed } from 'vue'  // add to existing import

const avatarPreviewUrl = computed(() => getImageUrl(avatarUrl.value))
```

Note: `ref` and `reactive` are already imported. Add `computed` to the existing import from `'vue'`:
```js
import { reactive, ref, computed } from 'vue'
```

- [ ] **Step 3: Add styles for avatar layout**

In `<style scoped>`, add after `.profile-section h3` block:
```css
.avatar-area {
  display: flex;
  align-items: center;
  gap: 24px;
}

.profile-avatar {
  flex-shrink: 0;
}

.avatar-upload {
  flex: 1;
}
```

- [ ] **Step 4: Commit**

```bash
git add frontend/src/views/user/ProfileView.vue
git commit -m "feat: add avatar preview in ProfileView"
```

---

### Task 11: Final integration check

- [ ] **Step 1: Verify all imports are consistent**

Run a quick grep to confirm no component still uses the old local `statusType`/`statusLabel` inline mappings (excluding the newly created `labels.js` and components intentionally using the shared utility):
```bash
cd frontend && grep -rn "function statusType\|function statusLabel" src/
```
Expected: no output (all local status functions removed and replaced with labels.js imports).

- [ ] **Step 2: Verify the app builds and compiles**

```bash
cd frontend && npm run build
```
Expected: build succeeds with no errors.

- [ ] **Step 3: Commit any remaining changes**

```bash
git status
```

---

## Completion Checklist

After implementation, verify:

1. **Delete review records**: Login as admin → navigate to 审核管理 → both list delete and detail delete work. Approved records show no delete button. Deleting a pending record reverts pet status.
2. **Avatar display**: Upload avatar in 个人中心 → avatar shows in header next to username, and as large preview in ProfileView. Logout and login again → avatar persists.
3. **Pet status change**: Login as admin → navigate to 宠物管理 → use "改状态" dropdown to change status. Edit pet → see status dropdown in edit mode → save changes status.
4. **Chinese labels**: Visit 宠物列表, 宠物详情, 管理后台 → all breed names and statuses display in Chinese. No English raw strings visible.
