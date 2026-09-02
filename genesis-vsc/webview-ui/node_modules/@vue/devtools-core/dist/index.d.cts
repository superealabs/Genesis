import * as _vue_devtools_kit0 from "@vue/devtools-kit";
import { AppRecord, CustomCommand, CustomTab, DevToolsV6PluginAPIHookKeys, DevToolsV6PluginAPIHookPayloads, OpenInEditorOptions, Router, RouterInfo, getRpcClient, getRpcServer, getViteRpcClient } from "@vue/devtools-kit";
import * as vue from "vue";
import { App, Ref } from "vue";

//#region src/client.d.ts
declare function setDevToolsClientUrl(url: string): void;
declare function getDevToolsClientUrl(): any;
//#endregion
//#region src/rpc/global.d.ts
declare enum DevToolsMessagingEvents {
  INSPECTOR_TREE_UPDATED = "inspector-tree-updated",
  INSPECTOR_STATE_UPDATED = "inspector-state-updated",
  DEVTOOLS_STATE_UPDATED = "devtools-state-updated",
  ROUTER_INFO_UPDATED = "router-info-updated",
  TIMELINE_EVENT_UPDATED = "timeline-event-updated",
  INSPECTOR_UPDATED = "inspector-updated",
  ACTIVE_APP_UNMOUNTED = "active-app-updated",
  DESTROY_DEVTOOLS_CLIENT = "destroy-devtools-client",
  RELOAD_DEVTOOLS_CLIENT = "reload-devtools-client"
}
declare const functions: {
  on: (event: string, handler: Function) => void;
  off: (event: string, handler: Function) => void;
  once: (event: string, handler: Function) => void;
  emit: (event: string, ...args: any[]) => void;
  heartbeat: () => boolean;
  devtoolsState: () => {
    connected: boolean;
    clientConnected: boolean;
    vueVersion: string;
    tabs: _vue_devtools_kit0.CustomTab[];
    commands: _vue_devtools_kit0.CustomCommand[];
    vitePluginDetected: boolean;
    appRecords: {
      id: string;
      name: string;
      version: string | undefined;
      routerId: string | undefined;
      iframe: string | undefined;
    }[];
    activeAppRecordId: string;
    timelineLayersState: Record<string, boolean>;
  };
  getInspectorTree(payload: Pick<DevToolsV6PluginAPIHookPayloads[DevToolsV6PluginAPIHookKeys.GET_INSPECTOR_TREE], "inspectorId" | "filter">): Promise<string>;
  getInspectorState(payload: Pick<DevToolsV6PluginAPIHookPayloads[DevToolsV6PluginAPIHookKeys.GET_INSPECTOR_STATE], "inspectorId" | "nodeId">): Promise<string>;
  editInspectorState(payload: DevToolsV6PluginAPIHookPayloads[DevToolsV6PluginAPIHookKeys.EDIT_INSPECTOR_STATE]): Promise<void>;
  sendInspectorState(id: string): void;
  inspectComponentInspector(): Promise<string>;
  cancelInspectComponentInspector(): void;
  getComponentRenderCode(id: string): any;
  scrollToComponent(id: string): void;
  inspectDOM(id: string): void;
  getInspectorNodeActions(id: string): {
    icon: string;
    tooltip?: string;
    action: (nodeId: string) => void | Promise<void>;
  }[] | undefined;
  getInspectorActions(id: string): {
    icon: string;
    tooltip?: string;
    action: () => void | Promise<void>;
  }[] | undefined;
  updateTimelineLayersState(state: Record<string, boolean>): void;
  callInspectorNodeAction(inspectorId: string, actionIndex: number, nodeId: string): void;
  callInspectorAction(inspectorId: string, actionIndex: number): void;
  openInEditor(options: OpenInEditorOptions): void;
  checkVueInspectorDetected(): Promise<boolean>;
  enableVueInspector(): Promise<void>;
  toggleApp(id: string, options?: {
    inspectingComponent?: boolean;
  }): Promise<void>;
  updatePluginSettings(pluginId: string, key: string, value: string): void;
  getPluginSettings(pluginId: string): {
    options: Record<string, {
      label: string;
      description?: string;
    } & ({
      type: "boolean";
      defaultValue: boolean;
    } | {
      type: "choice";
      defaultValue: string | number;
      options: {
        value: string | number;
        label: string;
      }[];
      component?: "select" | "button-group";
    } | {
      type: "text";
      defaultValue: string;
    })> | null;
    values: any;
  };
  getRouterInfo(): RouterInfo;
  navigate(path: string): Promise<Awaited<ReturnType<Router["push"]>> | Record<string, never>> | undefined;
  getMatchedRoutes(path: string): RouterInfo["routes"];
  toggleClientConnected(state: boolean): void;
  getCustomInspector(): {
    id: string;
    label: string;
    logo: string;
    icon: string;
    packageName: string | undefined;
    homepage: string | undefined;
    pluginId: string;
  }[];
  getInspectorInfo(id: string): {
    id: string;
    label: string;
    logo: string | undefined;
    packageName: string | undefined;
    homepage: string | undefined;
    timelineLayers: {
      id: string;
      label: string;
      color: number;
    }[];
    treeFilterPlaceholder: string;
    stateFilterPlaceholder: string;
  } | undefined;
  highlighComponent(uid: string): Promise<any>;
  unhighlight(): Promise<any>;
  updateDevToolsClientDetected(params: Record<string, boolean>): void;
  initDevToolsServerListener(): void;
};
type RPCFunctions = typeof functions;
declare const rpc: {
  value: ReturnType<typeof getRpcClient<RPCFunctions>>;
  functions: ReturnType<typeof getRpcClient<RPCFunctions>>;
};
declare const rpcServer: {
  value: ReturnType<typeof getRpcServer<RPCFunctions>>;
  functions: ReturnType<typeof getRpcServer<RPCFunctions>>;
};
declare function onRpcConnected(callback: () => void): void;
declare function onRpcSeverReady(callback: () => void): void;
//#endregion
//#region src/rpc/types.d.ts
type AssetType = 'image' | 'font' | 'video' | 'audio' | 'text' | 'json' | 'wasm' | 'other';
interface AssetInfo {
  path: string;
  type: AssetType;
  publicPath: string;
  relativePath: string;
  filePath: string;
  size: number;
  mtime: number;
}
interface ImageMeta {
  width: number;
  height: number;
  orientation?: number;
  type?: string;
  mimeType?: string;
}
interface AssetImporter {
  url: string;
  id: string | null;
}
interface AssetEntry {
  path: string;
  content: string;
  encoding?: BufferEncoding;
  override?: boolean;
}
interface CodeSnippet {
  code: string;
  lang: string;
  name: string;
  docs?: string;
}
interface ModuleInfo {
  id: string;
  plugins: {
    name: string;
    transform?: number;
    resolveId?: number;
  }[];
  deps: string[];
  virtual: boolean;
}
//#endregion
//#region src/rpc/vite.d.ts
declare const viteRpcFunctions: {
  on: (event: string, handler: Function) => void;
  off: (event: string, handler: Function) => void;
  once: (event: string, handler: Function) => void;
  emit: (event: string, ...args: any[]) => void;
  heartbeat: () => boolean;
};
type ViteRPCFunctions = typeof viteRpcFunctions & {
  getStaticAssets: () => Promise<AssetInfo[]>;
  getAssetImporters: (url: string) => Promise<AssetImporter[]>;
  getImageMeta: (filepath: string) => Promise<ImageMeta>;
  getTextAssetContent: (filepath: string, limit?: number) => Promise<string>;
  getRoot: () => Promise<string>;
  getGraphModules: () => Promise<ModuleInfo[]>;
};
declare const viteRpc: {
  value: ReturnType<typeof getViteRpcClient<ViteRPCFunctions>>;
  functions: ReturnType<typeof getViteRpcClient<ViteRPCFunctions>>;
};
declare function onViteRpcConnected(callback: () => void): void;
declare function createViteClientRpc(): void;
declare function createViteServerRpc(functions: Record<string, any>): void;
//#endregion
//#region src/vue-plugin/devtools-state.d.ts
interface DevToolsState {
  connected: boolean;
  clientConnected: boolean;
  vueVersion: string;
  tabs: CustomTab[];
  commands: CustomCommand[];
  vitePluginDetected: boolean;
  appRecords: AppRecord[];
  activeAppRecordId: string;
  timelineLayersState: Record<string, boolean>;
}
type DevToolsRefState = { [P in keyof DevToolsState]: Ref<DevToolsState[P]> };
declare function VueDevToolsVuePlugin(): {
  install(app: App): void;
};
declare function createDevToolsStateContext(): {
  getDevToolsState: () => void;
  connected: Ref<boolean, boolean>;
  clientConnected: Ref<boolean, boolean>;
  vueVersion: Ref<string, string>;
  tabs: Ref<{
    name: string;
    icon?: string | undefined;
    title: string;
    view: {
      type: "iframe";
      src: string;
      persistent?: boolean | undefined;
    } | {
      type: "vnode";
      vnode: vue.VNode;
    } | {
      type: "sfc";
      sfc: string;
    };
    category?: ("app" | "pinned" | "modules" | "advanced") | undefined;
  }[], CustomTab[] | {
    name: string;
    icon?: string | undefined;
    title: string;
    view: {
      type: "iframe";
      src: string;
      persistent?: boolean | undefined;
    } | {
      type: "vnode";
      vnode: vue.VNode;
    } | {
      type: "sfc";
      sfc: string;
    };
    category?: ("app" | "pinned" | "modules" | "advanced") | undefined;
  }[]>;
  commands: Ref<{
    id: string;
    title: string;
    description?: string | undefined;
    order?: number | undefined;
    icon?: string | undefined;
    action?: {
      type: "url";
      src: string;
    } | undefined;
    children?: {
      icon?: string | undefined;
      title: string;
      id: string;
      description?: string | undefined;
      order?: number | undefined;
      action?: {
        type: "url";
        src: string;
      } | undefined;
    }[] | undefined;
  }[], CustomCommand[] | {
    id: string;
    title: string;
    description?: string | undefined;
    order?: number | undefined;
    icon?: string | undefined;
    action?: {
      type: "url";
      src: string;
    } | undefined;
    children?: {
      icon?: string | undefined;
      title: string;
      id: string;
      description?: string | undefined;
      order?: number | undefined;
      action?: {
        type: "url";
        src: string;
      } | undefined;
    }[] | undefined;
  }[]>;
  vitePluginDetected: Ref<boolean, boolean>;
  appRecords: Ref<{
    id: string;
    name: string;
    app?: _vue_devtools_kit0.App;
    version?: string | undefined;
    types?: Record<string, string | symbol> | undefined;
    instanceMap: Map<string, any> & Omit<Map<string, any>, keyof Map<any, any>>;
    perfGroupIds: Map<string, {
      groupId: number;
      time: number;
    }> & Omit<Map<string, {
      groupId: number;
      time: number;
    }>, keyof Map<any, any>>;
    rootInstance: _vue_devtools_kit0.VueAppInstance;
    routerId?: string | undefined;
    iframe?: string | undefined;
  }[], AppRecord[] | {
    id: string;
    name: string;
    app?: _vue_devtools_kit0.App;
    version?: string | undefined;
    types?: Record<string, string | symbol> | undefined;
    instanceMap: Map<string, any> & Omit<Map<string, any>, keyof Map<any, any>>;
    perfGroupIds: Map<string, {
      groupId: number;
      time: number;
    }> & Omit<Map<string, {
      groupId: number;
      time: number;
    }>, keyof Map<any, any>>;
    rootInstance: _vue_devtools_kit0.VueAppInstance;
    routerId?: string | undefined;
    iframe?: string | undefined;
  }[]>;
  activeAppRecordId: Ref<string, string>;
  timelineLayersState: Ref<Record<string, boolean>, Record<string, boolean>>;
};
declare function useDevToolsState(): DevToolsRefState;
declare function onDevToolsConnected(fn: () => void): () => void;
declare function refreshCurrentPageData(): void;
//#endregion
export { AssetEntry, AssetImporter, AssetInfo, AssetType, CodeSnippet, DevToolsMessagingEvents, ImageMeta, ModuleInfo, RPCFunctions, ViteRPCFunctions, VueDevToolsVuePlugin, createDevToolsStateContext, createViteClientRpc, createViteServerRpc, functions, getDevToolsClientUrl, onDevToolsConnected, onRpcConnected, onRpcSeverReady, onViteRpcConnected, refreshCurrentPageData, rpc, rpcServer, setDevToolsClientUrl, useDevToolsState, viteRpc, viteRpcFunctions };