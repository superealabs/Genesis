// genesis-web-core/src/core/services/base.service.ts
export abstract class BaseService {
    abstract sendMessage(type: string, payload?: any): void;
    abstract onMessage<T>(type: string, callback: (data: T) => void): void;
}