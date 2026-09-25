// genesis-sdk-web/genesis-web-core/src/core/services/base.service.ts

/**
 * Classe de base abstraite définissant le contrat pour les services 
 * de communication par échange de messages (ex: IPC, WebSocket, Event Bus).
 * 
 * Toute implémentation concrète doit respecter cette interface pour garantir 
 * une cohérence dans la manière dont l'application envoie et reçoit des événements.
 */
export abstract class BaseService {
  
  /**
   * Envoie un message d'un type spécifique avec une charge utile optionnelle.
   * 
   * @param type - L'identifiant unique du type de message (ex: 'USER_LOGIN', 'FETCH_DATA').
   * @param payload - Les données associées au message. L'utilisation de 'unknown' 
   *                  garantit la sécurité du typage par rapport à 'any'.
   */
  abstract sendMessage(type: string, payload?: unknown): void;

  /**
   * S'abonne aux messages d'un type spécifique pour exécuter une action en réponse.
   * 
   * @typeParam T - Le type attendu des données reçues dans le callback.
   * @param type - L'identifiant du type de message à écouter.
   * @param callback - La fonction à exécuter lorsque le message est reçu.
   */
  abstract onMessage<T>(type: string, callback: (data: T) => void): void;
}