<template>
  <div class="layout">
    <div class="sidebar">
      <div class="logo">推送服务管理</div>
      <ul class="menu">
        <li :class="{ active: activeTab === 'stats' }" @click="activeTab = 'stats'">推送统计</li>
        <li :class="{ active: activeTab === 'config' }" @click="activeTab = 'config'">配置管理</li>
        <li :class="{ active: activeTab === 'test' }" @click="activeTab = 'test'">推送测试</li>
        <li :class="{ active: activeTab === 'records' }" @click="activeTab = 'records'">推送记录</li>
        <li :class="{ active: activeTab === 'password' }" @click="activeTab = 'password'">修改密码</li>
      </ul>
    </div>
    <div class="main">
      <div class="header">
        <h2>{{ pageTitle }}</h2>
        <span class="logout" @click="logout">退出登录</span>
      </div>
      <StatsPanel v-if="activeTab === 'stats'" />
      <ConfigPanel v-if="activeTab === 'config'" />
      <TestPanel v-if="activeTab === 'test'" />
      <RecordPanel v-if="activeTab === 'records'" />
      <PasswordPanel v-if="activeTab === 'password'" @password-changed="logout" />
    </div>
  </div>
</template>

<script>
import ConfigPanel from './ConfigPanel.vue'
import TestPanel from './TestPanel.vue'
import RecordPanel from './RecordPanel.vue'
import StatsPanel from './StatsPanel.vue'
import PasswordPanel from './PasswordPanel.vue'

export default {
  name: 'AdminView',
  components: { ConfigPanel, TestPanel, RecordPanel, StatsPanel, PasswordPanel },
  data() {
    return {
      activeTab: 'stats'
    }
  },
  computed: {
    pageTitle() {
      const titles = { stats: '推送统计', config: '配置管理', test: '推送测试', records: '推送记录', password: '修改密码' }
      return titles[this.activeTab] || ''
    }
  },
  methods: {
    logout() {
      this.$emit('logout')
    }
  }
}
</script>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
}
.sidebar {
  width: 220px;
  background: #2c3e50;
  color: #fff;
  flex-shrink: 0;
}
.logo {
  padding: 20px;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
.menu {
  list-style: none;
  padding: 10px 0;
}
.menu li {
  padding: 14px 20px;
  cursor: pointer;
  transition: background 0.2s;
  font-size: 14px;
}
.menu li:hover, .menu li.active {
  background: rgba(255,255,255,0.1);
}
.main {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.header h2 {
  font-size: 20px;
}
.logout {
  color: #e74c3c;
  cursor: pointer;
  font-size: 14px;
}
</style>
