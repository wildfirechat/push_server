<template>
  <div>
    <div class="card">
      <div class="card-title" style="display:flex; justify-content:space-between; align-items:center;">
        <span>实时推送统计</span>
        <button class="btn btn-danger" @click="resetStats">重置统计</button>
      </div>
      <table>
        <thead>
          <tr>
            <th>平台</th>
            <th>总推送数</th>
            <th>成功数</th>
            <th>失败数</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in stats" :key="item.platform">
            <td>{{ item.platform }}</td>
            <td>{{ item.totalCount }}</td>
            <td>{{ item.successCount }}</td>
            <td>{{ item.failCount }}</td>
          </tr>
        </tbody>
      </table>
      <div class="empty-tip" v-if="stats.length === 0">暂无统计数据</div>
    </div>

    <div class="card">
      <div class="card-title" style="display:flex; justify-content:space-between; align-items:center;">
        <span>按天统计</span>
        <div class="date-filter">
          <select v-model="selectedPlatform">
            <option value="">全部平台</option>
            <option v-for="p in platforms" :key="p" :value="p">{{ p }}</option>
          </select>
          <input type="date" v-model="startDate" />
          <span>至</span>
          <input type="date" v-model="endDate" />
          <button class="btn btn-primary" @click="loadDailyStats" :disabled="loading">查询</button>
        </div>
      </div>
      <table>
        <thead>
          <tr>
            <th>日期</th>
            <th>平台</th>
            <th>总推送数</th>
            <th>成功数</th>
            <th>失败数</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in dailyStats" :key="item.statDate + '-' + item.platform">
            <td>{{ item.statDate }}</td>
            <td>{{ item.platform }}</td>
            <td>{{ item.totalCount }}</td>
            <td>{{ item.successCount }}</td>
            <td>{{ item.failCount }}</td>
          </tr>
        </tbody>
      </table>
      <div class="empty-tip" v-if="dailyStats.length === 0">暂无数据</div>
    </div>
  </div>
</template>

<script>
import { api } from '../api.js'

function formatDate(d) {
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return year + '-' + month + '-' + day
}

export default {
  name: 'StatsPanel',
  data() {
    return {
      stats: [],
      dailyStats: [],
      platforms: [],
      selectedPlatform: '',
      startDate: '',
      endDate: '',
      loading: false,
      timer: null
    }
  },
  mounted() {
    this.loadStats()
    this.loadPlatforms()
    this.initDateRange()
    this.loadDailyStats()
    this.timer = setInterval(this.loadStats, 5000)
  },
  beforeUnmount() {
    if (this.timer) clearInterval(this.timer)
  },
  methods: {
    initDateRange() {
      const end = new Date()
      const start = new Date()
      start.setDate(start.getDate() - 6)
      this.endDate = formatDate(end)
      this.startDate = formatDate(start)
    },
    async loadStats() {
      try {
        const res = await api('/api/admin/stats')
        if (res.code === 200) {
          this.stats = res.data
        }
      } catch (e) {
        console.error('Load stats failed', e)
      }
    },
    async loadPlatforms() {
      try {
        const res = await api('/api/admin/platforms')
        if (res.code === 200) {
          this.platforms = res.data.filter(p => p !== 'admin')
        }
      } catch (e) {
        console.error('Load platforms failed', e)
      }
    },
    async loadDailyStats() {
      this.loading = true
      try {
        const params = new URLSearchParams()
        if (this.startDate) params.append('startDate', this.startDate)
        if (this.endDate) params.append('endDate', this.endDate)
        if (this.selectedPlatform) params.append('platform', this.selectedPlatform)
        const res = await api('/api/admin/stats/daily?' + params.toString())
        if (res.code === 200) {
          this.dailyStats = res.data
        }
      } catch (e) {
        console.error('Load daily stats failed', e)
      } finally {
        this.loading = false
      }
    },
    async resetStats() {
      if (!confirm('确定要重置所有统计数据吗？')) return
      try {
        const res = await api('/api/admin/stats/reset', { method: 'POST' })
        if (res.code === 200) {
          this.loadStats()
          this.loadDailyStats()
        }
      } catch (e) {
        console.error('Reset stats failed', e)
      }
    }
  }
}
</script>

<style scoped>
.card {
  background: #fff;
  border-radius: 6px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  margin-bottom: 20px;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #2c3e50;
}
table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}
th, td {
  text-align: left;
  padding: 12px;
  border-bottom: 1px solid #eee;
}
th {
  background: #f8f9fa;
  color: #666;
}
.empty-tip {
  text-align: center;
  color: #999;
  padding: 40px;
}
.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
}
.btn-danger {
  background: #e74c3c;
  color: #fff;
}
.btn-primary {
  background: #667eea;
  color: #fff;
}
.btn-primary:disabled {
  background: #a0a8d6;
  cursor: not-allowed;
}
.date-filter {
  display: flex;
  align-items: center;
  gap: 8px;
}
.date-filter input, .date-filter select {
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 13px;
}
.date-filter span {
  font-size: 13px;
  color: #666;
}
</style>
