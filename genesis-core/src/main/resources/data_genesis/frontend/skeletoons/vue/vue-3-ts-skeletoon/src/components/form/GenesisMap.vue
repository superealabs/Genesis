<template>
  <div class="genesis-map">
    <!-- Label -->
    <div v-if="label" class="map-label">
      <label :for="inputId">
        {{ label }}
        <span v-if="mandatory" class="mandatory">*</span>
      </label>
    </div>

    <!-- Coordonnées -->
    <div class="coordinates">
      <GenesisInput
        v-model="latitudeInput"
        label="Latitude"
        :input-id="`${inputId}-latitude`"
        type="number"
        inputmode="decimal"
        step="any"
        autocomplete="off"
        :violation="latitudeError"
        @blur="applyCoordinates"
      />

      <GenesisInput
        v-model="longitudeInput"
        label="Longitude"
        :input-id="`${inputId}-longitude`"
        type="number"
        inputmode="decimal"
        step="any"
        autocomplete="off"
        :violation="longitudeError"
        @blur="applyCoordinates"
      />
    </div>

    <!-- Leaflet -->
    <div
      ref="mapContainer"
      class="map"
    />

    <!-- Informations -->
    <div class="map-info">
      <span v-if="hasLocation">
        {{ latitude!.toFixed(6) }},
        {{ longitude!.toFixed(6) }}
      </span>

      <span v-else>
        Cliquez sur la carte pour sélectionner une position
      </span>
    </div>

    <!-- Erreur globale -->
    <div
      v-if="violation"
      class="error"
    >
      {{ violation }}
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  computed,
  nextTick,
  onBeforeUnmount,
  onMounted,
  ref,
  watch,
} from 'vue'

import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

import GenesisInput from './GenesisInput.vue'

defineOptions({
  inheritAttrs: false,
})

/**
 * Props
 *
 * Le composant utilise deux v-model :
 *
 * v-model:latitude
 * v-model:longitude
 */
const props = withDefaults(
  defineProps<{
    latitude?: number | string | null
    longitude?: number | string | null

    label?: string
    inputId?: string
    violation?: string
    mandatory?: boolean

    /**
     * Centre initial de la carte.
     */
    center?: [number, number]

    /**
     * Zoom initial.
     */
    zoom?: number

    /**
     * Autorise le clic sur la carte.
     */
    clickable?: boolean

    /**
     * Autorise le déplacement du marqueur.
     */
    draggable?: boolean
  }>(),
  {
    latitude: null,
    longitude: null,

    label: '',
    inputId: 'genesis-map',

    violation: '',
    mandatory: false,

    center: () => [-18.8792, 47.5079],
    zoom: 13,

    clickable: true,
    draggable: true,
  },
)

/**
 * Events
 */
const emit = defineEmits<{
  (
    event: 'update:latitude',
    value: number | null,
  ): void

  (
    event: 'update:longitude',
    value: number | null,
  ): void

  (
    event: 'change',
    value: {
      latitude: number | null
      longitude: number | null
    },
  ): void
}>()

/**
 * DOM de la carte.
 */
const mapContainer = ref<HTMLElement | null>(null)

/**
 * Valeurs internes.
 */
const latitude = ref<number | null>(null)
const longitude = ref<number | null>(null)

/**
 * Valeurs des inputs.
 */
const latitudeInput = ref('')
const longitudeInput = ref('')

/**
 * Instance Leaflet.
 */
let map: L.Map | null = null

/**
 * Marqueur Leaflet.
 */
let marker: L.Marker | null = null

/**
 * Une position complète existe.
 */
const hasLocation = computed(() => {
  return (
    latitude.value !== null &&
    longitude.value !== null
  )
})

/**
 * Validation latitude.
 */
const latitudeError = computed(() => {
  if (!latitudeInput.value) {
    return ''
  }

  const value = Number(
    latitudeInput.value.replace(',', '.'),
  )

  if (!Number.isFinite(value)) {
    return 'Latitude invalide'
  }

  if (value < -90 || value > 90) {
    return 'La latitude doit être comprise entre -90 et 90'
  }

  return ''
})

/**
 * Validation longitude.
 */
const longitudeError = computed(() => {
  if (!longitudeInput.value) {
    return ''
  }

  const value = Number(
    longitudeInput.value.replace(',', '.'),
  )

  if (!Number.isFinite(value)) {
    return 'Longitude invalide'
  }

  if (value < -180 || value > 180) {
    return 'La longitude doit être comprise entre -180 et 180'
  }

  return ''
})

/**
 * Conversion vers un nombre.
 */
const parseCoordinate = (
  value: number | string | null | undefined,
): number | null => {
  if (
    value === null ||
    value === undefined ||
    value === ''
  ) {
    return null
  }

  const parsed =
    typeof value === 'number'
      ? value
      : Number(
        String(value).replace(',', '.'),
      )

  return Number.isFinite(parsed)
    ? parsed
    : null
}

/**
 * Vérifie la latitude.
 */
const isValidLatitude = (
  value: number,
): boolean => {
  return (
    Number.isFinite(value) &&
    value >= -90 &&
    value <= 90
  )
}

/**
 * Vérifie la longitude.
 */
const isValidLongitude = (
  value: number,
): boolean => {
  return (
    Number.isFinite(value) &&
    value >= -180 &&
    value <= 180
  )
}

/**
 * Met à jour le modèle et le marqueur.
 */
const setLocation = (
  newLatitude: number | null,
  newLongitude: number | null,
  center = true,
): void => {
  latitude.value = newLatitude
  longitude.value = newLongitude

  latitudeInput.value =
    newLatitude === null
      ? ''
      : String(newLatitude)

  longitudeInput.value =
    newLongitude === null
      ? ''
      : String(newLongitude)

  /**
   * Mise à jour des deux v-model.
   */
  emit(
    'update:latitude',
    newLatitude,
  )

  emit(
    'update:longitude',
    newLongitude,
  )

  /**
   * Événement global optionnel.
   */
  emit('change', {
    latitude: newLatitude,
    longitude: newLongitude,
  })

  /**
   * Si une des deux coordonnées
   * manque, on supprime le marqueur.
   */
  if (
    newLatitude === null ||
    newLongitude === null
  ) {
    removeMarker()
    return
  }

  updateMarker(center)
}

/**
 * Création / mise à jour du marqueur.
 */
const updateMarker = (
  center = false,
): void => {
  if (
    !map ||
    latitude.value === null ||
    longitude.value === null
  ) {
    return
  }

  const position: L.LatLngExpression = [
    latitude.value,
    longitude.value,
  ]

  /**
   * Création du marqueur.
   */
  if (!marker) {
    marker = L.marker(position, {
      draggable: props.draggable,
    }).addTo(map)

    if (props.draggable) {
      marker.on(
        'dragend',
        handleMarkerDrag,
      )
    }
  } else {
    /**
     * Déplacement du marqueur existant.
     */
    marker.setLatLng(position)
  }

  /**
   * Recentrage.
   */
  if (center) {
    map.setView(
      position,
      map.getZoom(),
    )
  }
}

/**
 * Suppression du marqueur.
 */
const removeMarker = (): void => {
  if (!marker) {
    return
  }

  marker.off(
    'dragend',
    handleMarkerDrag,
  )

  marker.remove()

  marker = null
}

/**
 * Clic sur la carte.
 */
const handleMapClick = (
  event: L.LeafletMouseEvent,
): void => {
  if (!props.clickable) {
    return
  }

  const lat = Number(
    event.latlng.lat.toFixed(6),
  )

  const lng = Number(
    event.latlng.lng.toFixed(6),
  )

  if (!isValidLatitude(lat)) {
    return
  }

  if (!isValidLongitude(lng)) {
    return
  }

  setLocation(
    lat,
    lng,
    true,
  )
}

/**
 * Déplacement du marqueur.
 */
const handleMarkerDrag = (
  event: L.DragEndEvent,
): void => {
  const draggedMarker =
    event.target as L.Marker

  const position =
    draggedMarker.getLatLng()

  const lat = Number(
    position.lat.toFixed(6),
  )

  const lng = Number(
    position.lng.toFixed(6),
  )

  if (!isValidLatitude(lat)) {
    return
  }

  if (!isValidLongitude(lng)) {
    return
  }

  setLocation(
    lat,
    lng,
    false,
  )
}

/**
 * Applique les coordonnées
 * saisies manuellement.
 */
const applyCoordinates = (): void => {
  const lat =
    latitudeInput.value.trim() === ''
      ? null
      : Number(
        latitudeInput.value.replace(
          ',',
          '.',
        ),
      )

  const lng =
    longitudeInput.value.trim() === ''
      ? null
      : Number(
        longitudeInput.value.replace(
          ',',
          '.',
        ),
      )

  /**
   * Latitude invalide.
   */
  if (
    lat !== null &&
    !isValidLatitude(lat)
  ) {
    return
  }

  /**
   * Longitude invalide.
   */
  if (
    lng !== null &&
    !isValidLongitude(lng)
  ) {
    return
  }

  setLocation(
    lat === null
      ? null
      : Number(lat.toFixed(6)),
    lng === null
      ? null
      : Number(lng.toFixed(6)),
    true,
  )
}

/**
 * Synchronise les props vers
 * l'état interne.
 *
 * Exemple :
 *
 * formModel.totalSpent
 *        ↓
 * latitude
 *
 * formModel.averageSessionMinutes
 *        ↓
 * longitude
 */
const syncModel = (): void => {
  const newLatitude =
    parseCoordinate(
      props.latitude,
    )

  const newLongitude =
    parseCoordinate(
      props.longitude,
    )

  latitude.value = newLatitude
  longitude.value = newLongitude

  latitudeInput.value =
    newLatitude === null
      ? ''
      : String(newLatitude)

  longitudeInput.value =
    newLongitude === null
      ? ''
      : String(newLongitude)

  /**
   * Aucun point complet.
   */
  if (
    newLatitude === null ||
    newLongitude === null
  ) {
    removeMarker()
    return
  }

  /**
   * Met à jour le marqueur
   * sans modifier les v-model.
   */
  updateMarker(false)
}

/**
 * Initialisation Leaflet.
 */
onMounted(async () => {
  await nextTick()

  if (!mapContainer.value) {
    return
  }

  /**
   * Création de la carte.
   */
  map = L.map(
    mapContainer.value,
    {
      center: props.center,
      zoom: props.zoom,
    },
  )

  /**
   * OpenStreetMap.
   */
  L.tileLayer(
    'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
    {
      maxZoom: 19,
      attribution:
        '&copy; OpenStreetMap contributors',
    },
  ).addTo(map)

  /**
   * Clic sur la carte.
   */
  if (props.clickable) {
    map.on(
      'click',
      handleMapClick,
    )
  }

  /**
   * Synchronisation initiale.
   */
  syncModel()

  /**
   * Correction du rendu lorsque
   * Leaflet est initialisé dans
   * un container qui vient d'être affiché.
   */
  setTimeout(() => {
    map?.invalidateSize()
  }, 100)
})

/**
 * Synchronisation lorsque latitude
 * ou longitude change depuis le parent.
 */
watch(
  [
    () => props.latitude,
    () => props.longitude,
  ],
  () => {
    syncModel()
  },
)

/**
 * Gestion du draggable.
 */
watch(
  () => props.draggable,
  (draggable) => {
    if (!marker) {
      return
    }

    if (draggable) {
      marker.dragging?.enable()

      marker.off(
        'dragend',
        handleMarkerDrag,
      )

      marker.on(
        'dragend',
        handleMarkerDrag,
      )
    } else {
      marker.dragging?.disable()

      marker.off(
        'dragend',
        handleMarkerDrag,
      )
    }
  },
)

/**
 * Nettoyage.
 */
onBeforeUnmount(() => {
  if (!map) {
    return
  }

  map.off(
    'click',
    handleMapClick,
  )

  if (marker) {
    marker.off(
      'dragend',
      handleMarkerDrag,
    )
  }

  map.remove()

  map = null
  marker = null
})
</script>

<style scoped>
.genesis-map {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  width: 100%;
}

.map-label {
  display: flex;
  align-items: center;
}

.map-label label {
  font-size: 0.875rem;
  font-weight: 500;
}

.mandatory {
  margin-left: 0.25rem;
}

.coordinates {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.75rem;
}

.map {
  width: 100%;
  height: 400px;
  overflow: hidden;

  border: 1px solid
  var(--color-base-300, #e5e7eb);

  border-radius: 0.75rem;

  background: #f3f4f6;
}

.map-info {
  font-size: 0.75rem;
  color: rgba(0, 0, 0, 0.55);
  font-family: monospace;
}

.error {
  font-size: 0.75rem;
  color: var(--color-error, #ef4444);
}

@media (max-width: 640px) {
  .coordinates {
    grid-template-columns: 1fr;
  }

  .map {
    height: 300px;
  }
}
</style>
