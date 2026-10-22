<!--
  P4 (sprint 3) : cloche du menu, avec le nombre de notifications non lues.
  À placer dans la barre de menu des pages connectées : <NotificationCloche />
  API : GET /api/notifications/compteur, GET /api/notifications, POST /api/notifications/{id}/lue, POST /api/notifications/tout-lire
  Base de l'API : VITE_API_URL (vide si le front passe par le proxy Vite).
-->
<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'

const API = import.meta.env.VITE_API_URL ?? ''
const INTERVALLE_MS = 30000

const router = useRouter()
const nonLues = ref(0)
const notifications = ref([])
const ouvert = ref(false)
const chargement = ref(false)
const racine = ref(null)
let minuterie = null

async function api(chemin, options = {}) {
  const r = await fetch(API + chemin, { credentials: 'include', headers: { Accept: 'application/json' }, ...options })
  if (!r.ok) throw new Error('HTTP ' + r.status)
  return r.json()
}

async function rafraichirCompteur() {
  try {
    nonLues.value = (await api('/api/notifications/compteur')).nonLues
  } catch { /* session expirée ou serveur indisponible : on réessaie au prochain tour */ }
}

async function charger() {
  chargement.value = true
  try {
    notifications.value = await api('/api/notifications?limite=15')
  } finally {
    chargement.value = false
  }
}

async function basculer() {
  ouvert.value = !ouvert.value
  if (ouvert.value) await charger()
}

async function ouvrir(n) {
  if (!n.lu) {
    try {
      await api(`/api/notifications/${n.id}/lue`, { method: 'POST' })
      n.lu = true
      nonLues.value = Math.max(0, nonLues.value - 1)
    } catch { /* la navigation reste possible */ }
  }
  ouvert.value = false
  if (n.lien) router.push(n.lien)
}

async function toutLire() {
  await api('/api/notifications/tout-lire', { method: 'POST' })
  notifications.value.forEach(n => { n.lu = true })
  nonLues.value = 0
}

function date(d) {
  return new Date(d).toLocaleString('fr-FR', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' })
}

function clicDehors(e) {
  if (ouvert.value && racine.value && !racine.value.contains(e.target)) ouvert.value = false
}

onMounted(() => {
  rafraichirCompteur()
  minuterie = setInterval(rafraichirCompteur, INTERVALLE_MS)
  document.addEventListener('click', clicDehors)
})
onBeforeUnmount(() => {
  clearInterval(minuterie)
  document.removeEventListener('click', clicDehors)
})
</script>

<template>
  <div ref="racine" class="cloche">
    <button type="button" class="cloche__bouton" :aria-label="`Notifications : ${nonLues} non lue(s)`" @click="basculer">
      <svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true">
        <path fill="currentColor" d="M12 22a2.5 2.5 0 0 0 2.45-2h-4.9A2.5 2.5 0 0 0 12 22Zm7-6V11a7 7 0 0 0-5.5-6.84V3.5a1.5 1.5 0 0 0-3 0v.66A7 7 0 0 0 5 11v5l-2 2v1h18v-1l-2-2Z"/>
      </svg>
      <span v-if="nonLues > 0" class="cloche__pastille">{{ nonLues > 99 ? '99+' : nonLues }}</span>
    </button>

    <div v-if="ouvert" class="cloche__panneau" role="dialog" aria-label="Notifications">
      <div class="cloche__entete">
        <strong>Notifications</strong>
        <button v-if="nonLues > 0" type="button" class="cloche__lien" @click="toutLire">Tout marquer comme lu</button>
      </div>
      <p v-if="chargement" class="cloche__vide">Chargement…</p>
      <p v-else-if="!notifications.length" class="cloche__vide">Aucune notification</p>
      <ul v-else class="cloche__liste">
        <li v-for="n in notifications" :key="n.id">
          <button type="button" class="cloche__item" :class="{ 'cloche__item--non-lue': !n.lu }" @click="ouvrir(n)">
            <span class="cloche__message">{{ n.message }}</span>
            <span class="cloche__date">{{ date(n.dateCreation) }}</span>
          </button>
        </li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.cloche { position: relative; display: inline-block; }
.cloche__bouton { position: relative; background: none; border: 0; cursor: pointer; color: inherit; padding: 6px; }
.cloche__pastille { position: absolute; top: 0; right: 0; min-width: 18px; height: 18px; padding: 0 5px; border-radius: 9px;
  background: #d64545; color: #fff; font-size: 11px; line-height: 18px; text-align: center; font-weight: bold; }
.cloche__panneau { position: absolute; right: 0; top: calc(100% + 6px); width: min(360px, 92vw); max-height: 70vh; overflow: auto;
  background: #fff; color: #1f2933; border: 1px solid #e4e7eb; border-radius: 8px; box-shadow: 0 8px 24px rgba(0,0,0,.12); z-index: 1000; }
.cloche__entete { display: flex; justify-content: space-between; align-items: center; padding: 10px 14px; border-bottom: 1px solid #e4e7eb; }
.cloche__lien { background: none; border: 0; color: #1f6f5c; cursor: pointer; font-size: 13px; }
.cloche__vide { padding: 16px 14px; color: #7b8794; margin: 0; }
.cloche__liste { list-style: none; margin: 0; padding: 0; }
.cloche__item { display: block; width: 100%; text-align: left; background: none; border: 0; border-bottom: 1px solid #f0f2f4;
  padding: 10px 14px; cursor: pointer; font: inherit; color: inherit; }
.cloche__item:hover { background: #f7f8fa; }
.cloche__item--non-lue { background: #eef7f4; font-weight: 600; }
.cloche__message { display: block; font-size: 14px; }
.cloche__date { display: block; font-size: 12px; color: #7b8794; margin-top: 2px; font-weight: normal; }
</style>
