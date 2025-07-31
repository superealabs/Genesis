<template>
  <div class="d-flex align-items-center justify-content-between gap-2">
    <div class="row align-items-center g-2">
      <div class="col">
        <div
          class="avatar d-flex align-items-center justify-content-center rounded-circle bg-secondary text-white fw-bold"
          style="width: 40px; height: 40px"
        >
          <i class="fa-regular fa-user"></i>
        </div>
      </div>
      <div class="col-auto">
        <p class="mb-0 fw-medium text-dark">{{ name }}</p>
      </div>
    </div>
    <div>
      <GenesisButton icon="fa fa-sign-out" class="btn"></GenesisButton>
    </div>
  </div>
</template>
<script src="./UserNavProfileComponent.ts" lang="ts"></script>
