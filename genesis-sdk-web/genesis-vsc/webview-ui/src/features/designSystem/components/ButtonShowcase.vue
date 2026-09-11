<template>
    <ShowcaseLayout title="Buttons">
        <div class="w-full space-y-8">
            <!-- ═══════════════════════════════════════════════════════════ -->
            <!-- RECTANGLE -->
            <!-- ═══════════════════════════════════════════════════════════ -->
            
            <!-- Section 1 : Rectangle - Primary -->
            <section>
                <h3 class="text-xs font-semibold text-text-muted uppercase mb-3">Rectangle - Primary</h3>
                <div class="grid grid-cols-8 gap-4">
                    <div v-for="size in sizes" :key="size" class="aspect-square flex items-center justify-center bg-bg-light rounded p-2">
                        <GenesisButton variant="primary" :size="size">
                            <template #leftIcon>
                                <IconHome />
                            </template>
                            Home
                        </GenesisButton>
                    </div>
                </div>
                <div class="grid grid-cols-8 gap-4 mt-2">
                    <div v-for="size in sizes" :key="size" class="text-center text-[10px] text-text-muted">
                        {{ size }}
                    </div>
                </div>
            </section>

            <!-- Section 2 : Rectangle - Secondary -->
            <section>
                <h3 class="text-xs font-semibold text-text-muted uppercase mb-3">Rectangle - Secondary</h3>
                <div class="grid grid-cols-8 gap-4">
                    <div v-for="size in sizes" :key="size" class="aspect-square flex items-center justify-center bg-bg-light rounded p-2">
                        <GenesisButton variant="secondary" :size="size">
                            <template #leftIcon>
                                <IconHome />
                            </template>
                            Home
                        </GenesisButton>
                    </div>
                </div>
            </section>

            <!-- Section 3 : Rectangle - Text Only -->
            <section>
                <h3 class="text-xs font-semibold text-text-muted uppercase mb-3">Rectangle - Text Only</h3>
                <div class="grid grid-cols-8 gap-4">
                    <div v-for="size in sizes" :key="size" class="aspect-square flex items-center justify-center bg-bg-light rounded p-2">
                        <GenesisButton variant="secondary" :size="size">
                            Home
                        </GenesisButton>
                    </div>
                </div>
            </section>

            <!-- Section 4 : Rectangle + RightIcon (CaretRight) -->
            <section>
                <h3 class="text-xs font-semibold text-text-muted uppercase mb-3">Rectangle + RightIcon (CaretRight)</h3>
                <div class="grid grid-cols-8 gap-4">
                    <div v-for="size in sizes" :key="size" class="aspect-square flex items-center justify-center bg-bg-light rounded p-2">
                        <GenesisButton variant="secondary" :size="size">
                            <template #leftIcon>
                                <IconHome />
                            </template>
                            Home
                            <template #rightIcon>
                                <IconCaretRight />
                            </template>
                        </GenesisButton>
                    </div>
                </div>
            </section>

            <!-- Section 5 : Rectangle + RightIcon (ChevronDown) -->
            <section>
                <h3 class="text-xs font-semibold text-text-muted uppercase mb-3">Rectangle + RightIcon (ChevronDown)</h3>
                <div class="grid grid-cols-8 gap-4">
                    <div v-for="size in sizes" :key="size" class="aspect-square flex items-center justify-center bg-bg-light rounded p-2">
                        <GenesisButton variant="secondary" :size="size">
                            <template #leftIcon>
                                <IconHome />
                            </template>
                            Home
                            <template #rightIcon>
                                <IconChevronDown />
                            </template>
                        </GenesisButton>
                    </div>
                </div>
            </section>

            <!-- Section 6 : Ghost (visibleBackground=false) -->
            <section>
                <h3 class="text-xs font-semibold text-text-muted uppercase mb-3">Ghost (visibleBackground=false)</h3>
                <div class="grid grid-cols-8 gap-4">
                    <div v-for="size in sizes" :key="size" class="aspect-square flex items-center justify-center bg-bg-light rounded p-2">
                        <GenesisButton variant="secondary" :size="size" :visibleBackground="false">
                            <template #leftIcon>
                                <IconHome />
                            </template>
                            Home
                        </GenesisButton>
                    </div>
                </div>
            </section>

            <!-- ═══════════════════════════════════════════════════════════ -->
            <!-- SQUARE (GenesisButtonIcon) -->
            <!-- ═══════════════════════════════════════════════════════════ -->

            <!-- Section 7 : Square - Primary -->
            <section>
                <h3 class="text-xs font-semibold text-text-muted uppercase mb-3">Square - Primary</h3>
                <div class="grid grid-cols-8 gap-4">
                    <div v-for="size in sizes" :key="size" class="aspect-square flex items-center justify-center bg-bg-light rounded p-2">
                        <GenesisButtonIcon variant="primary" :size="size">
                            <IconHome />
                        </GenesisButtonIcon>
                    </div>
                </div>
            </section>

            <!-- Section 8 : Square - Secondary -->
            <section>
                <h3 class="text-xs font-semibold text-text-muted uppercase mb-3">Square - Secondary</h3>
                <div class="grid grid-cols-8 gap-4">
                    <div v-for="size in sizes" :key="size" class="aspect-square flex items-center justify-center bg-bg-light rounded p-2">
                        <GenesisButtonIcon variant="secondary" :size="size">
                            <IconHome />
                        </GenesisButtonIcon>
                    </div>
                </div>
            </section>

            <!-- Section 9 : Square - Ghost -->
            <section>
                <h3 class="text-xs font-semibold text-text-muted uppercase mb-3">Square - Ghost</h3>
                <div class="grid grid-cols-8 gap-4">
                    <div v-for="size in sizes" :key="size" class="aspect-square flex items-center justify-center bg-bg-light rounded p-2">
                        <GenesisButtonIcon variant="secondary" :size="size" :visibleBackground="false">
                            <IconHome />
                        </GenesisButtonIcon>
                    </div>
                </div>
            </section>

            <!-- ═══════════════════════════════════════════════════════════ -->
            <!-- CIRCLE (GenesisButtonIcon) -->
            <!-- ═══════════════════════════════════════════════════════════ -->

            <!-- Section 10 : Circle - Primary -->
            <section>
                <h3 class="text-xs font-semibold text-text-muted uppercase mb-3">Circle - Primary</h3>
                <div class="grid grid-cols-8 gap-4">
                    <div v-for="size in sizes" :key="size" class="aspect-square flex items-center justify-center bg-bg-light rounded p-2">
                        <GenesisButtonIcon variant="primary" shape="circle" :size="size">
                            <IconHome />
                        </GenesisButtonIcon>
                    </div>
                </div>
            </section>

            <!-- Section 11 : Circle - Secondary -->
            <section>
                <h3 class="text-xs font-semibold text-text-muted uppercase mb-3">Circle - Secondary</h3>
                <div class="grid grid-cols-8 gap-4">
                    <div v-for="size in sizes" :key="size" class="aspect-square flex items-center justify-center bg-bg-light rounded p-2">
                        <GenesisButtonIcon variant="secondary" shape="circle" :size="size">
                            <IconHome />
                        </GenesisButtonIcon>
                    </div>
                </div>
            </section>

            <!-- Section 12 : Circle - Ghost -->
            <section>
                <h3 class="text-xs font-semibold text-text-muted uppercase mb-3">Circle - Ghost</h3>
                <div class="grid grid-cols-8 gap-4">
                    <div v-for="size in sizes" :key="size" class="aspect-square flex items-center justify-center bg-bg-light rounded p-2">
                        <GenesisButtonIcon variant="secondary" shape="circle" :size="size" :visibleBackground="false">
                            <IconHome />
                        </GenesisButtonIcon>
                    </div>
                </div>
            </section>

            <!-- ═══════════════════════════════════════════════════════════ -->
            <!-- ÉTATS SPÉCIAUX -->
            <!-- ═══════════════════════════════════════════════════════════ -->

            <!-- Section 13 : Disabled -->
            <section>
                <h3 class="text-xs font-semibold text-text-muted uppercase mb-3">Disabled</h3>
                <div class="grid grid-cols-8 gap-4">
                    <div v-for="size in sizes" :key="size" class="aspect-square flex items-center justify-center bg-bg-light rounded p-2">
                        <GenesisButton variant="primary" :size="size" disabled>
                            <template #leftIcon>
                                <IconHome />
                            </template>
                            Home
                        </GenesisButton>
                    </div>
                </div>
            </section>

            <!-- Section 14 : FillWidth -->
            <section>
                <h3 class="text-xs font-semibold text-text-muted uppercase mb-3">FillWidth</h3>
                <div class="space-y-4">
                    <div class="bg-bg-light rounded p-4">
                        <GenesisButton variant="primary" :fillWidth="true">
                            <template #leftIcon>
                                <IconHome />
                            </template>
                            Home (fillWidth)
                        </GenesisButton>
                    </div>
                    <div class="bg-bg-light rounded p-4">
                        <GenesisButton variant="secondary" :fillWidth="true">
                            <template #leftIcon>
                                <IconHome />
                            </template>
                            Home
                            <template #rightIcon>
                                <IconCaretRight />
                            </template>
                        </GenesisButton>
                    </div>
                    <div class="bg-bg-light rounded p-4">
                        <GenesisButton variant="secondary" :fillWidth="true" :visibleBackground="false">
                            <template #leftIcon>
                                <IconHome />
                            </template>
                            Home (ghost + fillWidth)
                            <template #rightIcon>
                                <IconChevronDown />
                            </template>
                        </GenesisButton>
                    </div>
                </div>
            </section>
        </div>
    </ShowcaseLayout>
</template>

<script setup lang="ts">
import ShowcaseLayout from './layouts/ShowcaseLayout.vue';
import GenesisButton from '@/core/components/ui/actions/GenesisButton.vue';
import GenesisButtonIcon from '@/core/components/ui/actions/GenesisButtonIcon.vue';
import IconHome from '@/core/components/ui/icons/IconHome.vue';
import IconCaretRight from '@/core/components/ui/icons/IconCaretRight.vue';
import IconChevronDown from '@/core/components/ui/icons/IconChevronDown.vue';

const sizes = ['xs', 'sm', 'md', 'lg', 'xl', '2xl'] as const;
</script>