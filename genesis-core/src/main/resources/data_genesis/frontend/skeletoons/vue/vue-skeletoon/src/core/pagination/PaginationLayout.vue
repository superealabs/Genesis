<template>
  <nav>
    <ul class="pagination color-dark gap-2">
      <!-- <li class="page-item">
        <a class="page-link" href="#" aria-label="Previous">
          <span aria-hidden="true">&laquo;</span>
        </a>
      </li> -->
      <li
        v-for="(page, index) in pageNumbers"
        :key="index"
        class="page-item color-dark"
        :class="{
          active: page == currentPage,
          disabled: page === '...',
          'rounded-0': true,
        }"
      >
        <a
          v-if="page !== '...'"
          :class="{
            'page-link': true,
            'bg-light': page != currentPage,
            'bg-secondary': page == currentPage,
            'border-0': true,
            'px-4': true,
            'py-1': true,
            'rounded-3': true,
            'text-dark': true,
          }"
          href="#"
          @click.prevent="$emit('update:current', page)"
        >
          {{ page }}
        </a>
        <span v-else class="page-link bg-light border-0">...</span>
      </li>
      <!-- <li class="page-item">
        <a class="page-link" href="#" aria-label="Next">
          <span aria-hidden="true">&raquo;</span>
        </a>
      </li> -->
    </ul>
  </nav>
</template>

<script lang="ts">
import { computed, defineComponent, ref, unref } from "vue";

export default defineComponent({
  name: "PaginationLayout",
  props: {
    start: { required: true, type: Number },
    end: { required: true, type: Number },
    current: { required: false, type: Number },
  },
  emits: ["update:current"],
  setup(props) {
    const currentPage = ref(props.current);
    if (!currentPage.value) {
      currentPage.value = unref(props.start);
    }
    const pageNumbers = computed(() => {
      const pages: (number | string)[] = [];

      const maxVisible = 5;
      const totalPages = props.end;

      // Add first 5 pages
      for (let i = props.start; i <= Math.min(maxVisible, totalPages); i++) {
        pages.push(i);
      }

      // Add ellipsis if there are more pages
      if (totalPages > maxVisible) {
        pages.push("...");
        pages.push(totalPages);
      }

      return pages;
    });

    return {
      pageNumbers,
      currentPage,
    };
  },
});
</script>

<style scoped>
.bg-secondary {
  --bs-bg-opacity: 1;
  background-color: rgb(223, 230, 241) !important;
}
</style>
