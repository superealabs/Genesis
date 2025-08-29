// src/types/navigation.ts
export interface NavItem {
    label: string;
    path?: string;              // leaf
    icon?: React.ReactNode;     // optional icon
    children?: NavItem[];       // recursion
}