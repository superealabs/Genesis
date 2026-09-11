// ═══ TYPES SPÉCIFIQUES À L'UI ═══
// Réexporte tout le shared pour que les composants Vue existants continuent de fonctionner
// sans avoir à changer tous leurs imports d'un coup.
export * from '../../../../../genesis-web-types-shared/src/generator.shared';

// Tu pourras ajouter ici uniquement les types qui ont ABSOLUMENT besoin 
// d'imports avec '@/...' (ex: types liés à des composants Pinia/Vue spécifiques)