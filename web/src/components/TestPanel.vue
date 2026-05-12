<template>
  <div class="test-panel">
    <div class="form-card">
      <div class="form-row">
        <label>推送平台</label>
        <select v-model="form.platform">
          <option value="">请选择平台</option>
          <option v-for="p in platforms" :key="p" :value="p">{{ platformLabel(p) }}</option>
        </select>
      </div>

      <div class="form-row" v-if="form.platform === 'apns'">
        <label>证书环境</label>
        <select v-model="form.env">
          <option value="development">开发环境 (Development)</option>
          <option value="distribution">生产环境 (Distribution)</option>
        </select>
      </div>

      <div class="form-row">
        <label>设备 Token</label>
        <textarea v-model="form.deviceToken" rows="3" placeholder="请输入设备推送Token"></textarea>
      </div>

      <div class="form-row">
        <label>推送内容</label>
        <input v-model="form.pushContent" placeholder="请输入推送消息内容" />
      </div>

      <div class="form-row">
        <label>{{ form.platform === 'apns' ? 'Bundle ID (应用包名)' : '应用包名' }}</label>
        <input v-model="form.packageName" :placeholder="form.platform === 'apns' ? '例如 com.example.myapp' : 'cn.wildfire.chat'" />
      </div>

      <div class="form-actions">
        <button class="btn-primary" @click="sendPush" :disabled="loading">
          {{ loading ? '发送中...' : '发送测试推送' }}
        </button>
        <span v-if="result && result.ok" class="hint">提示：推送请求已提交，实际送达结果请关注服务端日志及推送统计</span>
      </div>

      <div v-if="result" :class="['result', result.ok ? 'success' : 'error']">
        {{ result.message }}
      </div>
    </div>

    <div class="tips">
      <h4>使用说明</h4>
      <ul>
        <li>请先确保对应平台已配置正确的 AppID、密钥等参数，配置缺失时会直接提示。</li>
        <li>APNs 测试需要选择对应的证书环境（开发/生产），并确保证书文件已上传。</li>
        <li>APNs 的「应用包名」必须填写 iOS App 的 Bundle ID，且与证书/密钥所对应的 App 完全一致。</li>
        <li>设备 Token 必须是对应平台注册的有效 Token。</li>
        <li>配置错误（如密钥无效、Token 格式错误等）会在服务端日志中输出，请留意查看。</li>
        <li>推送结果可在「推送统计」页面查看。</li>
      </ul>
    </div>
  </div>
</template>

<script>
import { api } from '../api.js'

export default {
  name: 'TestPanel',
  data() {
    return {
      platforms: [],
      form: {
        platform: '',
        deviceToken: '',
        pushContent: '这是一条测试推送消息',
        packageName: 'cn.wildfire.chat',
        env: 'development'
      },
      loading: false,
      result: null
    }
  },
  mounted() {
    this.loadPlatforms()
    this.loadForm()
  },
  methods: {
    async loadPlatforms() {
      try {
        const data = await api('/api/admin/platforms')
        if (data && data.data) {
          this.platforms = data.data.filter(p => p !== 'admin')
        }
      } catch (e) {
        console.error('加载平台列表失败', e)
      }
    },
    loadForm() {
      try {
        const saved = localStorage.getItem('push_test_form')
        if (saved) {
          const parsed = JSON.parse(saved)
          Object.assign(this.form, parsed)
        }
      } catch (e) {
        // ignore parse error
      }
    },
    saveForm() {
      try {
        localStorage.setItem('push_test_form', JSON.stringify(this.form))
      } catch (e) {
        // ignore storage error
      }
    },
    platformLabel(p) {
      const map = {
        xiaomi: '小米',
        hms: '华为 (HMS)',
        honor: '荣耀',
        vivo: 'vivo',
        oppo: 'OPPO',
        fcm: 'FCM (Firebase)',
        getui: '个推',
        unipush: 'UniPush',
        apns: '苹果 APNs',
        hm: '鸿蒙 (Harmony)'
      }
      return map[p] || p
    },
    async sendPush() {
      if (!this.form.platform) {
        this.result = { ok: false, message: '请选择推送平台' }
        return
      }
      if (!this.form.deviceToken.trim()) {
        this.result = { ok: false, message: '请输入设备 Token' }
        return
      }

      this.loading = true
      this.result = null
      try {
        const payload = {
          platform: this.form.platform,
          deviceToken: this.form.deviceToken.trim(),
          pushContent: this.form.pushContent,
          packageName: this.form.packageName || 'cn.wildfire.chat'
        }
        if (this.form.platform === 'apns') {
          payload.env = this.form.env
        }
        const data = await api('/api/admin/test/push', {
          method: 'POST',
          body: payload
        })
        if (data.code === 200) {
          this.result = { ok: true, message: data.message || '推送已发送' }
          this.saveForm()
        } else {
          this.result = { ok: false, message: data.message || '推送失败' }
        }
      } catch (e) {
        this.result = { ok: false, message: '请求失败: ' + e.message }
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.test-panel {
  max-width: 600px;
}
.form-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
  margin-bottom: 20px;
}
.form-row {
  margin-bottom: 16px;
}
.form-row label {
  display: block;
  font-size: 14px;
  color: #555;
  margin-bottom: 6px;
  font-weight: 500;
}
.form-row input,
.form-row select,
.form-row textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
  font-family: inherit;
}
.form-row input:focus,
.form-row select:focus,
.form-row textarea:focus {
  outline: none;
  border-color: #409eff;
}
.form-actions {
  margin-top: 20px;
}
.btn-primary {
  background: #409eff;
  color: #fff;
  border: none;
  padding: 10px 24px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}
.btn-primary:disabled {
  background: #a0cfff;
  cursor: not-allowed;
}
.result {
  margin-top: 16px;
  padding: 12px;
  border-radius: 6px;
  font-size: 14px;
}
.result.success {
  background: #f0f9eb;
  color: #67c23a;
  border: 1px solid #e1f3d8;
}
.result.error {
  background: #fef0f0;
  color: #f56c6c;
  border: 1px solid #fde2e2;
}
.tips {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px 20px;
}
.tips h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #333;
}
.tips ul {
  margin: 0;
  padding-left: 18px;
  font-size: 13px;
  color: #666;
  line-height: 1.8;
}
</style>
