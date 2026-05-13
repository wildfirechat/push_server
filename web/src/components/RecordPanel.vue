<template>
  <div class="record-panel">
    <div class="filter-bar">
      <div class="filter-item">
        <label>开始时间</label>
        <input type="datetime-local" v-model="filters.startTime" />
      </div>
      <div class="filter-item">
        <label>结束时间</label>
        <input type="datetime-local" v-model="filters.endTime" />
      </div>
      <div class="filter-item">
        <label>状态</label>
        <select v-model="filters.success">
          <option value="">全部</option>
          <option value="true">成功</option>
          <option value="false">失败</option>
        </select>
      </div>
      <div class="filter-item">
        <label>用户 ID</label>
        <input v-model="filters.userId" placeholder="请输入用户 ID" />
      </div>
      <div class="filter-item">
        <button class="btn-primary" @click="loadRecords(1)">查询</button>
        <button class="btn-secondary" @click="resetFilters">重置</button>
      </div>
    </div>

    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>时间</th>
            <th>平台</th>
            <th>用户 ID</th>
            <th>设备 Token</th>
            <th>推送内容</th>
            <th>状态</th>
            <th>失败原因</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="7" class="center">加载中...</td>
          </tr>
          <tr v-else-if="records.length === 0">
            <td colspan="7" class="center">暂无数据</td>
          </tr>
          <tr v-for="r in records" :key="r.id">
            <td>{{ formatTime(r.pushTime) }}</td>
            <td>{{ r.platform }}</td>
            <td>{{ r.userId || '-' }}</td>
            <td :title="r.deviceToken">{{ truncate(r.deviceToken, 24) }}</td>
            <td :title="r.pushContent">{{ truncate(r.pushContent, 30) }}</td>
            <td>
              <span :class="['status-badge', r.success ? 'success' : 'fail']">
                {{ r.success ? '成功' : '失败' }}
              </span>
            </td>
            <td :title="r.errorMsg">{{ truncate(r.errorMsg, 30) || '-' }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="pagination" v-if="total > 0">
      <button :disabled="page <= 1" @click="loadRecords(page - 1)">上一页</button>
      <span class="page-info">第 {{ page }} 页 / 共 {{ totalPages }} 页（{{ total }} 条）</span>
      <button :disabled="page >= totalPages" @click="loadRecords(page + 1)">下一页</button>
      <select v-model="size" @change="loadRecords(1)">
        <option :value="10">10 条/页</option>
        <option :value="20">20 条/页</option>
        <option :value="50">50 条/页</option>
      </select>
    </div>
  </div>
</template>

<script>
import { api } from '../api.js'

export default {
  name: 'RecordPanel',
  data() {
    const now = new Date()
    const pad = n => n.toString().padStart(2, '0')
    const toDatetimeLocal = d => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
    const startOfDay = new Date(now.getFullYear(), now.getMonth(), now.getDate())
    const endOfDay = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59)
    return {
      filters: {
        startTime: toDatetimeLocal(startOfDay),
        endTime: toDatetimeLocal(endOfDay),
        success: '',
        userId: ''
      },
      records: [],
      page: 1,
      size: 20,
      total: 0,
      loading: false
    }
  },
  computed: {
    totalPages() {
      return Math.ceil(this.total / this.size) || 1
    }
  },
  mounted() {
    this.loadRecords(1)
  },
  methods: {
    async loadRecords(page) {
      this.loading = true
      this.page = page
      try {
        const params = new URLSearchParams()
        params.append('page', page)
        params.append('size', this.size)
        if (this.filters.startTime) params.append('startTime', this.filters.startTime.replace('T', ' ') + ':00')
        if (this.filters.endTime) params.append('endTime', this.filters.endTime.replace('T', ' ') + ':00')
        if (this.filters.success !== '') params.append('success', this.filters.success)
        if (this.filters.userId) params.append('userId', this.filters.userId)

        const res = await api('/api/admin/records?' + params.toString())
        if (res.code === 200) {
          this.records = res.data || []
          this.total = res.total || 0
        } else {
          this.records = []
          this.total = 0
        }
      } catch (e) {
        console.error('加载推送记录失败', e)
        this.records = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },
    resetFilters() {
      const now = new Date()
      const pad = n => n.toString().padStart(2, '0')
      const toDatetimeLocal = d => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
      const startOfDay = new Date(now.getFullYear(), now.getMonth(), now.getDate())
      const endOfDay = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59)
      this.filters = { startTime: toDatetimeLocal(startOfDay), endTime: toDatetimeLocal(endOfDay), success: '', userId: '' }
      this.loadRecords(1)
    },
    formatTime(iso) {
      if (!iso) return '-'
      const d = new Date(iso)
      const pad = n => n.toString().padStart(2, '0')
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
    },
    truncate(str, len) {
      if (!str) return ''
      return str.length > len ? str.substring(0, len) + '...' : str
    }
  }
}
</script>

<style scoped>
.record-panel {
  max-width: 1200px;
}
.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
  align-items: flex-end;
}
.filter-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.filter-item label {
  font-size: 12px;
  color: #666;
}
.filter-item input,
.filter-item select {
  padding: 8px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 13px;
  min-width: 140px;
}
.btn-primary {
  background: #409eff;
  color: #fff;
  border: none;
  padding: 8px 18px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
}
.btn-secondary {
  background: #f5f7fa;
  color: #606266;
  border: 1px solid #dcdfe6;
  padding: 8px 18px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  margin-left: 8px;
}
.table-wrap {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
  overflow-x: auto;
}
.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.data-table th,
.data-table td {
  padding: 12px 14px;
  text-align: left;
  border-bottom: 1px solid #eee;
}
.data-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #333;
}
.data-table tr:hover {
  background: #f5f7fa;
}
.center {
  text-align: center;
  color: #999;
  padding: 40px;
}
.status-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}
.status-badge.success {
  background: #e1f3d8;
  color: #67c23a;
}
.status-badge.fail {
  background: #fde2e2;
  color: #f56c6c;
}
.pagination {
  margin-top: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  justify-content: center;
}
.pagination button {
  padding: 6px 14px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
}
.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.pagination select {
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 13px;
}
.page-info {
  font-size: 13px;
  color: #666;
}
</style>
