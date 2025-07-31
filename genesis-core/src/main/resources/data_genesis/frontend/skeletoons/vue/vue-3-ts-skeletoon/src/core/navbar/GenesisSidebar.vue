<template>
  <!-- Sidebar -->
  <nav
    id="sidebar"
    class="d-flex flex-column bg-light border-end py-4 px-2 vh-100 shadow-sm"
    style="width: 250px; min-width: 250px"
  >
    <h3 class="px-3 nav-brand">
      <span class="text-dark">Genesis</span
      ><span class="text-primary">App</span>
    </h3>
    <ul class="nav flex-column flex-grow-1">
      <li class="nav-item">
        <span class="nav-group-title nav-link text-muted text-uppercase">
          core
        </span>
      </li>
      <ul class="nav flex-column">
        <li class="nav-item">
          <router-link class="nav-link text-dark" to="/">Home</router-link>
        </li>
      </ul>
      <li class="nav-item">
        <span class="nav-group-title nav-link text-muted text-uppercase">
          entities
        </span>
      </li>
      <ul class="nav flex-column">
        <li class="nav-item">
          <router-link class="nav-link text-dark" to="/products">
            Products
          </router-link>
        </li>
      </ul>
      <li class="nav-item">
        <span class="nav-group-title nav-link text-muted text-uppercase">
          views
        </span>
      </li>
    </ul>
    <UserNavProfile name="John Doe" />
  </nav>
</template>

<script lang="ts" src="./GenesisSidebarComponent.ts"></script>

<style scoped>
.nav-group-title {
  font-size: 14px;
}
</style>
