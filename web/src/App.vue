<template>
  <div id="app">
    <LoginView v-if="!isLoggedIn" @login-success="onLoginSuccess" />
    <AdminView v-else @logout="onLogout" />
  </div>
</template>

<script>
import LoginView from './components/LoginView.vue'
import AdminView from './components/AdminView.vue'

export default {
  name: 'App',
  components: { LoginView, AdminView },
  data() {
    return {
      isLoggedIn: !!localStorage.getItem('admin_token')
    }
  },
  methods: {
    onLoginSuccess() {
      this.isLoggedIn = true
    },
    onLogout() {
      localStorage.removeItem('admin_token')
      this.isLoggedIn = false
    }
  }
}
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  background: #f5f6fa;
  color: #333;
}
</style>
