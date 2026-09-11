import { ref, computed, type Ref, type ComputedRef } from 'vue';

export interface UseResizableOptions {
    minWidth?: number;
    maxWidth?: number | (() => number);
    minHeight?: number;
    maxHeight?: number | (() => number);
    resizableX?: Ref<boolean>;
    resizableY?: Ref<boolean>;
}

export interface UseResizableReturn {
    resizeStyle: ComputedRef<Record<string, string>>;
    isResizing: Ref<boolean>;
    startResizeBottom: (e: MouseEvent) => void;
    startResizeLeft: (e: MouseEvent) => void;
    startResizeRight: (e: MouseEvent) => void;
}

export function useResizable(options: UseResizableOptions = {}): UseResizableReturn {
    const {
        minWidth = 250,
        maxWidth = () => window.innerWidth * 0.9,
        minHeight = 150,
        maxHeight = () => window.innerHeight * 0.9,
        resizableX = ref(true),
        resizableY = ref(true),
    } = options;

    const resizeWidth = ref<number | null>(null);
    const resizeHeight = ref<number | null>(null);
    const isResizing = ref(false);

    const resizeStyle = computed(() => {
        const styles: Record<string, string> = {};
        if (resizeWidth.value) styles.width = `${resizeWidth.value}px`;
        if (resizeHeight.value) styles.height = `${resizeHeight.value}px`;
        return styles;
    });

    const getMaxWidth = () => typeof maxWidth === 'function' ? maxWidth() : maxWidth;
    const getMaxHeight = () => typeof maxHeight === 'function' ? maxHeight() : maxHeight;

    // ═══ Resize BOTTOM ═══
    function startResizeBottom(e: MouseEvent) {
        if (!resizableY.value) return;
        isResizing.value = true;
        document.body.classList.add('is-resizing');

        const el = (e.currentTarget as HTMLElement).parentElement!;
        const startHeight = el.offsetHeight;
        const startY = e.clientY;

        const onMove = (e: MouseEvent) => {
            const delta = e.clientY - startY;
            resizeHeight.value = Math.max(minHeight, Math.min(getMaxHeight(), startHeight + delta));
        };
        const onUp = () => {
            document.removeEventListener('mousemove', onMove);
            document.removeEventListener('mouseup', onUp);
            document.body.classList.remove('is-resizing');
            setTimeout(() => { isResizing.value = false; }, 0);
        };
        document.addEventListener('mousemove', onMove);
        document.addEventListener('mouseup', onUp);
    }

    // ═══ Resize LEFT ═══
    function startResizeLeft(e: MouseEvent) {
        if (!resizableX.value) return;
        isResizing.value = true;
        document.body.classList.add('is-resizing');

        const el = (e.currentTarget as HTMLElement).parentElement!;
        const startWidth = el.offsetWidth;
        const startX = e.clientX;

        const onMove = (e: MouseEvent) => {
            const delta = startX - e.clientX;
            resizeWidth.value = Math.max(minWidth, Math.min(getMaxWidth(), startWidth + delta));
        };
        const onUp = () => {
            document.removeEventListener('mousemove', onMove);
            document.removeEventListener('mouseup', onUp);
            document.body.classList.remove('is-resizing');
            setTimeout(() => { isResizing.value = false; }, 0);
        };
        document.addEventListener('mousemove', onMove);
        document.addEventListener('mouseup', onUp);
    }

    // ═══ Resize RIGHT ═══
    function startResizeRight(e: MouseEvent) {
        if (!resizableX.value) return;
        isResizing.value = true;
        document.body.classList.add('is-resizing');

        const el = (e.currentTarget as HTMLElement).parentElement!;
        const startWidth = el.offsetWidth;
        const startX = e.clientX;

        const onMove = (e: MouseEvent) => {
            const delta = e.clientX - startX;
            resizeWidth.value = Math.max(minWidth, Math.min(getMaxWidth(), startWidth + delta));
        };
        const onUp = () => {
            document.removeEventListener('mousemove', onMove);
            document.removeEventListener('mouseup', onUp);
            document.body.classList.remove('is-resizing');
            setTimeout(() => { isResizing.value = false; }, 0);
        };
        document.addEventListener('mousemove', onMove);
        document.addEventListener('mouseup', onUp);
    }

    return {
        resizeStyle,
        isResizing,
        startResizeBottom,
        startResizeLeft,
        startResizeRight
    };
}