import ProductListView from "@/views/product/list/ProductListView.vue";
import { RouteRecordRaw } from "vue-router";

const productRoutes: Array<RouteRecordRaw> = [
  {
    path: "/products",
    name: "ProductListView",
    component: ProductListView,
  },
];

export default productRoutes;
