<template>
  <div class="login-wrap">
    <div class="login-box">
      <h2>推送服务管理后台</h2>
      <div class="form-group">
        <label>用户名</label>
        <input type="text" v-model="username" placeholder="请输入用户名" @keydown.enter="login" />
      </div>
      <div class="form-group">
        <label>密码</label>
        <input type="password" v-model="password" placeholder="请输入密码" @keydown.enter="login" />
      </div>
      <button class="btn-login" @click="login" :disabled="loading">
        {{ loading ? '登录中...' : '登录' }}
      </button>
      <div class="error-msg" v-if="errorMsg">{{ errorMsg }}</div>
    </div>
  </div>
</template>

<script>
import { api } from '../api.js'

export default {
  name: 'LoginView',
  data() {
    return {
      username: 'admin',
      password: '',
      errorMsg: '',
      loading: false
    }
  },
  methods: {
    async login() {
      this.errorMsg = ''
      if (!this.username || !this.password) {
        this.errorMsg = '请输入用户名和密码'
        return
      }
      this.loading = true
      try {
        const res = await api('/api/admin/login', {
          method: 'POST',
          body: { username: this.username, password: this.password }
        })
        if (res.code === 200) {
          localStorage.setItem('admin_token', res.token)
          this.$emit('login-success')
        } else {
          this.errorMsg = res.message || '登录失败'
        }
      } catch (e) {
        this.errorMsg = '网络错误，请稍后重试'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.login-wrap {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-box {
  background: #fff;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
  width: 360px;
}
.login-box h2 {
  text-align: center;
  margin-bottom: 24px;
  color: #333;
}
.form-group {
  margin-bottom: 16px;
}
.form-group label {
  display: block;
  margin-bottom: 6px;
  color: #555;
  font-size: 14px;
}
.form-group input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
}
.form-group input:focus {
  border-color: #667eea;
}
.btn-login {
  width: 100%;
  padding: 12px;
  background: #667eea;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 15px;
  cursor: pointer;
}
.btn-login:hover:not(:disabled) {
  background: #5a6fd6;
}
.btn-login:disabled {
  background: #a0a8d6;
  cursor: not-allowed;
}
.error-msg {
  color: #e74c3c;
  font-size: 13px;
  margin-top: 12px;
  text-align: center;
}
</style>
