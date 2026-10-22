<!--
  P4 (sprint 2) : page publique de réponse du candidat à une proposition d'embauche.
  Route (sans connexion) : { path: '/proposition/:jeton', component: PropositionReponse, meta: { public: true } }
  API : GET /api/public/propositions/{jeton}, POST …/reponse, POST …/declin
-->
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const API = import.meta.env.VITE_API_URL ?? ''
const route = useRoute()
const jeton = route.params.jeton

const proposition = ref(null)
const reponses = ref({})          // idCondition -> { accepte: true|false|null, commentaire: '' }
const erreur = ref('')
const envoi = ref(false)
const declinOuvert = ref(false)
const motifDeclin = ref('')

const aRepondre = computed(() => (proposition.value?.conditions ?? []).filter(c => c.accepte === null))
const complet = computed(() => aRepondre.value.every(c => reponses.value[c.id]?.accepte !== null))

async function api(chemin, corps) {
  const r = await fetch(`${API}/api/public/propositions/${encodeURIComponent(jeton)}${chemin}`, {
    method: corps === undefined ? 'GET' : 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: corps === undefined ? undefined : JSON.stringify(corps),
  })
  const donnees = await r.json().catch(() => ({}))
  if (!r.ok) throw new Error(donnees.message || 'Une erreur est survenue. Réessayez plus tard.')
  return donnees
}

function afficher(p) {
  proposition.value = p
  const r = {}
  p.conditions.filter(c => c.accepte === null).forEach(c => { r[c.id] = { accepte: null, commentaire: '' } })
  reponses.value = r
}

async function charger() {
  erreur.value = ''
  try { afficher(await api('')) } catch (e) { erreur.value = e.message }
}

async function envoyer() {
  if (!complet.value) { erreur.value = 'Indiquez votre choix pour chaque condition.'; return }
  envoi.value = true; erreur.value = ''
  try {
    afficher(await api('/reponse', {
      reponses: aRepondre.value.map(c => ({
        idCondition: c.id,
        accepte: reponses.value[c.id].accepte,
        commentaire: reponses.value[c.id].accepte ? null : (reponses.value[c.id].commentaire || null),
      })),
    }))
  } catch (e) { erreur.value = e.message } finally { envoi.value = false }
}

async function decliner() {
  envoi.value = true; erreur.value = ''
  try {
    afficher(await api('/declin', { commentaire: motifDeclin.value || null }))
    declinOuvert.value = false
  } catch (e) { erreur.value = e.message } finally { envoi.value = false }
}

function dateFr(d) { return d ? new Date(d + 'T00:00:00').toLocaleDateString('fr-FR') : '' }

onMounted(charger)
</script>

<template>
  <main class="prop">
    <p v-if="!proposition && !erreur" class="prop__info">Chargement…</p>
    <p v-if="erreur && !proposition" class="prop__erreur" role="alert">{{ erreur }}</p>

    <template v-if="proposition">
      <h1>Proposition d'embauche</h1>
      <p class="prop__sous-titre">
        {{ proposition.prenom }} {{ proposition.nom }} — <strong>{{ proposition.poste }}</strong>
        <span v-if="proposition.unite"> ({{ proposition.unite }})</span>
      </p>
      <p class="prop__message" :class="'prop__message--' + proposition.etape">{{ proposition.message }}</p>

      <form @submit.prevent="envoyer">
        <div v-for="c in proposition.conditions" :key="c.id" class="prop__condition">
          <div class="prop__ligne">
            <span class="prop__libelle">{{ c.libelle }}</span>
            <span class="prop__valeur">{{ c.valeurAffichee }}</span>
          </div>
          <p v-if="c.enContreProposition && c.accepte === null" class="prop__note">
            Nouvelle valeur (précédemment : {{ c.ancienneValeurAffichee }})
          </p>

          <div v-if="proposition.peutRepondre && c.accepte === null" class="prop__choix">
            <label><input type="radio" :name="'c' + c.id" :value="true" v-model="reponses[c.id].accepte" /> J'accepte</label>
            <label><input type="radio" :name="'c' + c.id" :value="false" v-model="reponses[c.id].accepte" /> Je refuse</label>
            <textarea v-if="reponses[c.id].accepte === false" v-model="reponses[c.id].commentaire" maxlength="2000"
                      rows="2" placeholder="Votre souhait ou votre motif (facultatif)"></textarea>
          </div>
          <p v-else-if="c.accepte === true" class="prop__etat prop__etat--ok">Acceptée</p>
          <p v-else-if="c.accepte === false" class="prop__etat prop__etat--ko">
            Refusée<span v-if="c.commentaireCandidat"> : « {{ c.commentaireCandidat }} »</span>
          </p>
        </div>

        <p v-if="erreur" class="prop__erreur" role="alert">{{ erreur }}</p>

        <div v-if="proposition.peutRepondre" class="prop__actions">
          <p class="prop__info">Réponse attendue avant le {{ dateFr(proposition.dateLimiteReponse) }}.</p>
          <button type="submit" class="prop__bouton" :disabled="envoi || !complet">Envoyer ma réponse</button>
          <button type="button" class="prop__bouton prop__bouton--secondaire" :disabled="envoi"
                  @click="declinOuvert = !declinOuvert">Décliner la proposition</button>
        </div>
      </form>

      <div v-if="declinOuvert" class="prop__declin">
        <label for="motif">Motif (facultatif)</label>
        <textarea id="motif" v-model="motifDeclin" rows="3" maxlength="2000"></textarea>
        <button type="button" class="prop__bouton prop__bouton--danger" :disabled="envoi" @click="decliner">
          Confirmer : je décline la proposition
        </button>
      </div>
    </template>
  </main>
</template>

<style scoped>
.prop { max-width: 640px; margin: 32px auto; padding: 0 16px; font-family: Arial, Helvetica, sans-serif; color: #1f2933; }
h1 { font-size: 24px; margin: 0 0 4px; }
.prop__sous-titre { color: #52606d; margin: 0 0 16px; }
.prop__message { padding: 12px 14px; border-radius: 6px; background: #f1f5f9; }
.prop__message--ACCEPTEE { background: #e6f4ef; color: #1f6f5c; }
.prop__message--REFUSEE, .prop__message--EXPIREE { background: #fdecec; color: #9b2c2c; }
.prop__condition { border-bottom: 1px solid #e4e7eb; padding: 14px 0; }
.prop__ligne { display: flex; justify-content: space-between; gap: 12px; flex-wrap: wrap; }
.prop__libelle { color: #52606d; }
.prop__valeur { font-weight: bold; }
.prop__note { font-size: 13px; color: #c2410c; margin: 4px 0 0; }
.prop__choix { margin-top: 8px; display: flex; flex-wrap: wrap; gap: 8px 18px; }
.prop__choix textarea { flex-basis: 100%; }
textarea { width: 100%; box-sizing: border-box; font: inherit; padding: 8px; border: 1px solid #cbd2d9; border-radius: 6px; }
.prop__etat { margin: 6px 0 0; font-size: 14px; }
.prop__etat--ok { color: #1f6f5c; }
.prop__etat--ko { color: #9b2c2c; }
.prop__actions { margin-top: 18px; display: flex; flex-wrap: wrap; gap: 10px; align-items: center; }
.prop__actions .prop__info { flex-basis: 100%; margin: 0; }
.prop__bouton { background: #1f6f5c; color: #fff; border: 0; border-radius: 6px; padding: 10px 18px; font: inherit; cursor: pointer; }
.prop__bouton:disabled { opacity: .5; cursor: not-allowed; }
.prop__bouton--secondaire { background: #fff; color: #1f2933; border: 1px solid #cbd2d9; }
.prop__bouton--danger { background: #b83232; margin-top: 8px; }
.prop__declin { margin-top: 16px; padding: 14px; border: 1px solid #f5c2c2; border-radius: 6px; }
.prop__erreur { color: #9b2c2c; }
.prop__info { color: #52606d; }
</style>
