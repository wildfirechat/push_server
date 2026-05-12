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
      <div class="forgot-link">
        <a href="javascript:void(0)" @click="showForgotTip">忘记密码？</a>
      </div>
      <div class="error-msg" v-if="errorMsg">{{ errorMsg }}</div>

      <div v-if="forgotTipVisible" class="tip-overlay" @click.self="forgotTipVisible = false">
        <div class="tip-box">
          <h4>恢复默认密码</h4>
          <p>如果您忘记了管理员密码，可以通过以下步骤恢复默认密码：</p>
          <ol>
            <li>停止推送服务</li>
            <li>删除数据库中的 <code>admin_user</code> 表数据</li>
            <li>重新启动服务，系统会自动创建默认管理员账号</li>
          </ol>
          <p class="tip-note">默认账号：<b>admin</b> / 默认密码：<b>admin123</b></p>
          <button class="btn-close" @click="forgotTipVisible = false">我知道了</button>
        </div>
      </div>
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
      loading: false,
      forgotTipVisible: false
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
        } else if (res.code === 423) {
          this.errorMsg = res.message || '账户已锁定，请稍后再试'
        } else {
          this.errorMsg = res.message || '登录失败'
        }
      } catch (e) {
        this.errorMsg = '网络错误，请稍后重试'
      } finally {
        this.loading = false
      }
    },
    showForgotTip() {
      this.forgotTipVisible = true
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
.forgot-link {
  text-align: right;
  margin-top: 10px;
}
.forgot-link a {
  color: #667eea;
  font-size: 13px;
  text-decoration: none;
}
.forgot-link a:hover {
  text-decoration: underline;
}
.tip-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}
.tip-box {
  background: #fff;
  padding: 28px 32px;
  border-radius: 8px;
  width: 420px;
  max-width: 90%;
  box-shadow: 0 4px 20px rgba(0,0,0,0.2);
}
.tip-box h4 {
  margin: 0 0 14px 0;
  font-size: 16px;
  color: #333;
}
.tip-box p {
  font-size: 13px;
  color: #555;
  line-height: 1.6;
  margin: 0 0 10px 0;
}
.tip-box ol {
  margin: 0 0 14px 0;
  padding-left: 20px;
  font-size: 13px;
  color: #555;
  line-height: 1.8;
}
.tip-box code {
  background: #f0f0f0;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 12px;
}
.tip-note {
  background: #f5f7fa;
  padding: 10px 12px;
  border-radius: 4px;
  margin-bottom: 16px;
}
.btn-close {
  width: 100%;
  padding: 10px;
  background: #667eea;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}
.btn-close:hover {
  background: #5a6fd6;
}
</style>
