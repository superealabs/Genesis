import { ref } from "vue";

export function useLoading() {
  const loading = ref(false);
  const startLoading = () => {
    loading.value = true;
  };
  const stopLoading = () => {
    loading.value = false;
  };

  //   const loadingAction = (action: any) => {
  //     startLoading();
  //     action.call();
  //     stopLoading();
  //   };

  return {
    loading,
    startLoading,
    stopLoading,
  };
}
