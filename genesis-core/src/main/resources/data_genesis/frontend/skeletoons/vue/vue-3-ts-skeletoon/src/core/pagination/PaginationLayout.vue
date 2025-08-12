<template>
  <div class="row my-3">
    <nav class="col">
      <ul class="pagination gap-2">
        <!-- Previous -->
        <li class="page-item me-2">
          <button
            typ="button"
            class="page-link border-0 rounded-3 shadow-sm"
            :class="{
              disabled: page <= 1,
            }"
            aria-label="Previous"
            @click.prevent="goToPreviousPage"
          >
            <i class="bi bi-chevron-left text-dark"></i>
          </button>
        </li>

        <!-- Pages -->
        <li
          v-for="pageNum in pageNumbers"
          :key="pageNum"
          class="page-item"
          :class="{
            active: pageNum === page,
            disabled: pageNum === '...',
          }"
        >
          <a
            v-if="pageNum !== '...'"
            class="page-link border-0 rounded-3 fw-medium"
            :class="{
              'bg-light text-dark': pageNum !== page,
              'shadow-sm': pageNum === page,
            }"
            href="#"
            @click.prevent="onChangePage(pageNum)"
          >
            {{ pageNum }}
          </a>
          <span v-else class="page-link bg-light border-0 text-muted">
            ...
          </span>
        </li>

        <!-- Next -->
        <li class="page-item ms-2 rounded-3">
          <button
            class="page-link rounded-3 shadow-sm border-0"
            :class="{
              disabled: page >= end,
            }"
            aria-label="Next"
            @click.prevent="goToNextPage"
          >
            <i class="bi bi-chevron-right text-dark"></i>
          </button>
        </li>
      </ul>
    </nav>
    <div class="col-auto" v-if="quickForm">
      <div class="d-flex align-items-center gap-2">
        <label class="text-nowrap" for="select-page">Going to page:</label>
        <select
          id="select-page"
          class="border-0 bg-white shadow-sm form-select"
          v-model="selectedPage"
          @change="goToSelectedPage"
        >
          <option
            v-for="option in pageSelectOptions"
            :key="option.value"
            :value="option.value"
          >
            {{ option.label }}
          </option>
        </select>
        <GenesisButton
          class="btn-light border-0 shadow-sm"
          @click="goToSelectedPage"
          >GO</GenesisButton
        >
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, ref } from "vue";
import { SelectOption } from "../../models/SelectOption";
import GenesisButton from "../button/GenesisButton.vue";

export default defineComponent({
  name: "PaginationLayout",
  components: { GenesisButton },
  props: {
    start: { required: true, type: Number },
    end: { required: true, type: Number },
    page: { required: true, type: Number },
    quickForm: { required: false, type: Boolean },
  },
  emits: ["update:page"],
  setup(props, { emit }) {
    const selectedPage = ref<number>(1);

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
    const pageSelectOptions = computed(() => {
      const totalPages = props.end;
      const options: SelectOption[] = [];
      for (let index = 1; index <= totalPages; index++) {
        options.push({
          label: index.toString(),
          value: index,
        });
      }
      return options;
    });

    const onChangePage = (page: number | string) => {
      if (!page) {
        page = 1;
      }
      if (typeof page == "string") {
        page = Number.parseInt(page);
      }
      emit("update:page", page);
    };

    const goToSelectedPage = () => {
      onChangePage(selectedPage.value);
    };

    const goToPreviousPage = () => {
      if (props.page > 1) {
        emit("update:page", props.page - 1);
      }
    };

    const goToNextPage = () => {
      if (props.page < props.end) {
        emit("update:page", props.page + 1);
      }
    };

    return {
      pageNumbers,
      onChangePage,
      goToPreviousPage,
      goToNextPage,
      selectedPage,
      pageSelectOptions,
      goToSelectedPage,
    };
  },
});
</script>

<style scoped></style>
