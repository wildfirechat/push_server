<template>
  <div class="card" style="max-width:500px;">
    <div class="card-title">修改管理员密码</div>
    <div class="form-group">
      <label>原密码</label>
      <input type="password" v-model="oldPassword" placeholder="请输入原密码" />
    </div>
    <div class="form-group">
      <label>新密码</label>
      <input type="password" v-model="newPassword" placeholder="请输入新密码" />
    </div>
    <div class="form-group">
      <label>确认新密码</label>
      <input type="password" v-model="confirmPassword" placeholder="请再次输入新密码" />
    </div>
    <button class="btn btn-primary" @click="changePassword">确认修改</button>
  </div>
</template>

<script>
import { api } from '../api.js'

export default {
  name: 'PasswordPanel',
  data() {
    return {
      oldPassword: '',
      newPassword: '',
      confirmPassword: '',
      loading: false,
      toastMsg: '',
      toastError: false
    }
  },
  methods: {
    async changePassword() {
      this.toastMsg = ''
      if (!this.oldPassword || !this.newPassword || !this.confirmPassword) {
        this.showToast('请填写完整信息', true)
        return
      }
      if (this.newPassword !== this.confirmPassword) {
        this.showToast('两次输入的新密码不一致', true)
        return
      }
      this.loading = true
      try {
        const res = await api('/api/admin/password', {
          method: 'POST',
          body: { oldPassword: this.oldPassword, newPassword: this.newPassword }
        })
        if (res.code === 200) {
          this.showToast('密码修改成功，请重新登录', false)
          setTimeout(() => this.$emit('password-changed'), 1500)
        } else {
          this.showToast(res.message || '修改失败', true)
        }
      } catch (e) {
        this.showToast('修改失败', true)
      } finally {
        this.loading = false
      }
    },
    showToast(msg, isError) {
      this.toastMsg = msg
      this.toastError = isError
      setTimeout(() => { this.toastMsg = '' }, 3000)
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
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #2c3e50;
}
.form-group {
  margin-bottom: 16px;
}
.form-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  color: #555;
}
.form-group input {
  width: 100%;
  padding: 9px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
}
.btn {
  padding: 9px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}
.btn-primary {
  background: #667eea;
  color: #fff;
}
.btn-primary:disabled {
  background: #a0a8d6;
  cursor: not-allowed;
}
.toast {
  padding: 10px 16px;
  margin-bottom: 12px;
  border-radius: 4px;
  background: #2ecc71;
  color: #fff;
  font-size: 14px;
}
.toast.error {
  background: #e74c3c;
}
</style>
