<template>
  <div>
    <div class="card">
      <div class="card-title">选择推送平台</div>
      <div class="platform-list">
        <div
          class="platform-tag"
          v-for="p in platforms"
          :key="p"
          :class="{ active: currentPlatform === p }"
          @click="selectPlatform(p)"
        >
          {{ p }}
        </div>
      </div>
    </div>
    <div class="card" v-if="currentPlatform">
      <div class="card-title">{{ currentPlatform }} 配置</div>
      <div class="toast" :class="{ error: toastError }" v-if="toastMsg">{{ toastMsg }}</div>
      <div class="form-row" v-for="(group, gi) in configGroups" :key="gi">
        <div class="form-group" v-for="key in group" :key="key">
          <label>
            {{ key }}
            <span v-if="fieldTips[key]" class="tip-trigger" @mouseenter="showTip(key)" @mouseleave="hideTip">
              ℹ️
              <transition name="fade">
                <span v-if="activeTip === key" class="tip-popup">{{ fieldTips[key] }}</span>
              </transition>
            </span>
          </label>
          <div class="input-wrap">
            <textarea v-if="configData[key] && configData[key].length > 60" rows="3" v-model="configData[key]"></textarea>
            <input v-else type="text" v-model="configData[key]" />
            <button v-if="isUploadField(key)" class="btn-upload" @click="triggerUpload(key)">上传</button>
          </div>
          <input type="file" :ref="el => setFileInputRef(key, el)" style="display:none" @change="handleFileChange($event, key)" />
        </div>
      </div>
      <div class="cluster-tip" v-if="currentPlatform">
        <span class="tip-icon">ℹ️</span>
        配置保存后当前节点立即生效；如为集群部署，其他节点最长约 30 秒后自动同步。
      </div>
      <div style="margin-top:16px;">
        <button class="btn btn-primary" @click="saveConfig" :disabled="loading">
          {{ loading ? '保存中...' : '保存配置' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { api } from '../api.js'

const uploadFields = [
  'apns.auth_key_path',
  'fcm.credentialsPath'
]

export default {
  name: 'ConfigPanel',
  data() {
    return {
      platforms: [],
      currentPlatform: '',
      configData: {},
      fileInputs: {},
      loading: false,
      toastMsg: '',
      toastError: false,
      activeTip: '',
      fieldTips: {
        'apns.auth_key_path': 'APNs p8 认证密钥文件路径，上传后自动填充',
        'apns.key_id': 'APNs p8 密钥 ID，在 Apple Developer 账户的 Keys 页面获取',
        'apns.team_id': 'Apple Developer Team ID，在 Apple Developer 账户的 Membership 页面获取',
        'apns.alert': '普通推送提示音，默认 default',
        'apns.voipAlert': 'VoIP 推送提示音，默认 ring.caf',
        'apns.voipFeature': '是否开启 VoIP 推送功能，默认 false',
        'fcm.credentialsPath': 'FCM 服务账号 JSON 凭证文件路径，上传后自动填充',
        'fcm.databaseUrl': '【选填】Firebase Realtime Database URL，仅在使用 Firebase Realtime Database 时需要，格式如 https://<项目名>.firebaseio.com/',
        'getui.appId': '个推应用 ID',
        'getui.appKey': '个推应用 Key',
        'getui.masterSecret': '个推 Master Secret',
        'getui.domain': '个推 REST API 域名，默认 https://restapi.getui.com/v2/',
        'hms.appId': '华为推送应用 ID',
        'hms.appSecret': '华为推送应用 Secret',
        'honor.appId': '荣耀推送应用 ID',
        'honor.appSecret': '荣耀推送应用 Secret',
        'honor.badgeClass': '荣耀推送角标类名（可选）',
        'oppo.AppKey': 'OPPO 推送 AppKey',
        'oppo.AppSecret': 'OPPO 推送 AppSecret',
        'vivo.appId': 'vivo 推送应用 ID',
        'vivo.appKey': 'vivo 推送应用 Key',
        'vivo.appSecret': 'vivo 推送应用 Secret',
        'xiaomi.appSecret': '小米推送应用 Secret',
        'xiaomi.channelId': '小米推送渠道 ID（可选）',
        'hm.iss': '鸿蒙推送服务账号 Issuer ID',
        'hm.kid': '鸿蒙推送密钥 ID',
        'hm.privateKey': '鸿蒙推送私钥内容',
        'hm.projectId': '鸿蒙推送项目 ID',
        'hm.supportVoipPush': '是否支持鸿蒙 VoIP 推送，默认 false',
        'unipush.url': 'UniPush 云函数地址',
        'unipush.huaweiCategory': '华为分类标识（可选）',
        'unipush.harmonyCategory': '鸿蒙分类标识（可选）',
        'unipush.vivoCategory': 'vivo 分类标识（可选）'
      }
    }
  },
  computed: {
    configGroups() {
      const keys = Object.keys(this.configData)
      const groups = []
      for (let i = 0; i < keys.length; i += 2) {
        groups.push(keys.slice(i, i + 2))
      }
      return groups
    }
  },
  mounted() {
    this.loadPlatforms()
  },
  methods: {
    async loadPlatforms() {
      const res = await api('/api/admin/platforms')
      if (res.code === 200) {
        this.platforms = res.data.filter(p => p !== 'admin')
      }
    },
    async selectPlatform(platform) {
      this.currentPlatform = platform
      const res = await api('/api/admin/config/' + platform)
      if (res.code === 200) {
        this.configData = { ...res.data }
      }
    },
    async saveConfig() {
      this.loading = true
      try {
        const res = await api('/api/admin/config/' + this.currentPlatform, {
          method: 'POST',
          body: this.configData
        })
        this.showToast(res.code === 200 ? '保存成功，当前节点已生效' : (res.message || '保存失败'), res.code !== 200)
      } catch (e) {
        this.showToast('保存失败', true)
      } finally {
        this.loading = false
      }
    },
    showToast(msg, isError) {
      this.toastMsg = msg
      this.toastError = isError
      setTimeout(() => { this.toastMsg = '' }, 3000)
    },
    showTip(key) {
      this.activeTip = key
    },
    hideTip() {
      this.activeTip = ''
    },
    isUploadField(key) {
      return uploadFields.includes(key)
    },
    setFileInputRef(key, el) {
      if (el) {
        this.fileInputs[key] = el
      }
    },
    triggerUpload(key) {
      const input = this.fileInputs[key]
      if (input) {
        input.click()
      }
    },
    async handleFileChange(event, key) {
      const file = event.target.files[0]
      if (!file) return

      const formData = new FormData()
      formData.append('platform', this.currentPlatform)
      formData.append('field', key)
      formData.append('file', file)

      try {
        const res = await api('/api/admin/upload', {
          method: 'POST',
          body: formData
        })
        if (res.code === 200) {
          this.configData[key] = res.path
          this.showToast('上传成功: ' + res.path + '，当前节点已生效', false)
        } else {
          this.showToast(res.message || '上传失败', true)
        }
      } catch (e) {
        this.showToast('上传出错', true)
      }
      // 清空input，允许重复上传同一文件
      event.target.value = ''
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
.platform-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.platform-tag {
  padding: 8px 16px;
  border-radius: 4px;
  background: #ecf0f1;
  color: #555;
  cursor: pointer;
  font-size: 13px;
  border: 1px solid #ddd;
}
.platform-tag:hover, .platform-tag.active {
  background: #667eea;
  color: #fff;
  border-color: #667eea;
}
.form-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 16px;
}
.form-group {
  flex: 1 1 300px;
}
.form-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  color: #555;
}
.input-wrap {
  display: flex;
  gap: 8px;
  align-items: flex-start;
}
.form-group input, .form-group textarea {
  flex: 1;
  padding: 9px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
}
.form-group input:focus, .form-group textarea:focus {
  border-color: #667eea;
}
.btn-upload {
  padding: 9px 14px;
  background: #27ae60;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  white-space: nowrap;
}
.btn-upload:hover {
  background: #219150;
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
.cluster-tip {
  margin-top: 12px;
  padding: 10px 14px;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: 4px;
  color: #0369a1;
  font-size: 13px;
  line-height: 1.5;
}
.cluster-tip .tip-icon {
  margin-right: 4px;
}
.tip-trigger {
  position: relative;
  cursor: help;
  margin-left: 4px;
  font-size: 13px;
  user-select: none;
}
.tip-popup {
  position: absolute;
  left: 50%;
  bottom: 120%;
  transform: translateX(-50%);
  background: #333;
  color: #fff;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.5;
  white-space: normal;
  max-width: 280px;
  width: max-content;
  z-index: 10;
  pointer-events: none;
}
.tip-popup::after {
  content: '';
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  border: 5px solid transparent;
  border-top-color: #333;
}
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
