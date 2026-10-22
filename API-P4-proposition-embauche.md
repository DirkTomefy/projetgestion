# P4 (Fifaliana) – Sprint 2 : proposition d'embauche et clôture des offres

Script de base : `data/2026-10-22-18-00-p4-proposition-embauche.sql` (après les scripts P5 ; rejouable).
Erreurs : format commun `ErreurApi { status, message, champs }`.

## Parcours

1. P5 retient une candidature → elle apparaît dans `GET /api/rh/candidatures-retenues`.
2. La RH ouvre `GET /api/rh/propositions/formulaire?idCandidature=…` : sièges du besoin avec leur fourchette salariale, types de contrat, valeurs par défaut.
3. La RH crée la proposition (`POST /api/rh/propositions`) : salaire contrôlé dans la grille (catégorie × échelon du poste, grille en vigueur à la date de début). La réponse contient `lienReponse` = `{app.front.url}/proposition/{jeton}` à transmettre au candidat.
4. Le candidat ouvre le lien (page Vue publique, `meta.public`) et accepte ou refuse chaque condition.
   - Refus d'au moins une condition → `etape = ATTENTE_RH` : la RH fait une contre-proposition (`relance_condition`, l'ancienne valeur est gardée) ou retire la proposition.
   - Tout accepté → statut `ACCEPTER`, puis `EmbaucheService.embaucher(id)` (P3) dans la même transaction, puis clôture de l'offre si tous les sièges sont `POURVU`.
   - Date limite dépassée sans réponse → `EXPIREE` (tâche quotidienne, ou à l'ouverture du lien). La candidature reste RETENUE : la RH peut refaire une proposition.
5. Chaque événement est historisé dans `histo_proposition_embauche` (statut, utilisateur, date, commentaire ; utilisateur vide = candidat ou tâche automatique).

`statut` : `PROPOSER`, `ACCEPTER`, `REFUSER`, `EXPIREE`.
`etape` (pour l'affichage) : `ATTENTE_CANDIDAT`, `ATTENTE_RH`, `ACCEPTEE`, `REFUSEE`, `EXPIREE`.

## RH (rôle RH)

| Méthode | Route | Corps / paramètres |
|---|---|---|
| GET | `/api/rh/propositions?statut=` | liste |
| GET | `/api/rh/propositions/formulaire?idCandidature=` | données du formulaire |
| GET | `/api/rh/propositions/fourchette?idPoste=&date=AAAA-MM-JJ` | fourchette salariale |
| POST | `/api/rh/propositions` | `{ idCandidature, idBesoinPoste, salaire, dateDebut, horaire, lieu, conditionsTravail?, idTypeContrat?, dateLimiteReponse, commentaire? }` |
| GET | `/api/rh/propositions/{id}` | fiche : conditions, contre-propositions, historique, lien |
| POST | `/api/rh/propositions/{id}/contre-proposition` | `{ conditions: [{ idCondition, valeur }], dateLimiteReponse?, commentaire? }` – une valeur pour chaque condition refusée |
| POST | `/api/rh/propositions/{id}/retrait` | `{ commentaire }` (obligatoire) → `REFUSER` |

Règles : candidature `RETENUE` sans autre proposition en cours ; siège du besoin de l'offre, ni `POURVU` ni `ANNULE`, sans autre proposition en cours ; date limite ≥ aujourd'hui ; date de début ≥ date limite ; horaire et lieu obligatoires ; type de contrat par défaut : celui de l'offre, sinon CE.

Valeurs stockées (lues par l'embauche P3) : `SALAIRE` "1950000", `DATE_DEBUT` "2026-11-05", `TYPE_CONTRAT` id de `type_contrat` ("1" ; la contre-proposition accepte l'id ou le libellé), texte libre pour `HORAIRE`, `LIEU`, `CONDITIONS`.

## Page publique du candidat (sans connexion)

| Méthode | Route | Corps |
|---|---|---|
| GET | `/api/public/propositions/{jeton}` | — |
| POST | `/api/public/propositions/{jeton}/reponse` | `{ reponses: [{ idCondition, accepte, commentaire? }] }` – toutes les conditions en attente |
| POST | `/api/public/propositions/{jeton}/declin` | `{ commentaire? }` → `REFUSER` |

Réponse : `{ etape, message, peutRepondre, nom, prenom, poste, unite, referenceOffre, dateLimiteReponse, conditions: [{ id, code, libelle, valeurAffichee, accepte, enContreProposition, ancienneValeurAffichee, … }] }`.

## Clôture des offres (bloc 2)

- Tâche quotidienne (`app.recrutement.cron`, 00:05 par défaut, et au démarrage) : offres `PUBLIEE` dont la date limite est passée → `CLOTUREE` (`DATE_LIMITE`) ; offres dont tous les sièges sont `POURVU` → `CLOTUREE` (`POSTES_POURVUS`) ; propositions sans réponse après leur date limite → `EXPIREE`.
- Après chaque embauche : clôture immédiate si tous les sièges du besoin sont pourvus.
- Bouton « Clôturer » : `POST /api/rh/offres/{id}/cloturer` (`MANUELLE`, utilisateur enregistré).
- Une offre clôturée disparaît du site public mais reste consultable : `GET /api/rh/offres/{id}`, `GET /api/rh/offres/{id}/candidatures`. `OffreDto` expose `dateCloture`, `motifCloture`, `cloturable`.

## Interfaces avec les autres membres

- **P3 → P4** `EmbaucheService` (interface dans `service/embauche`) : implémentée par `EmbaucheServiceP3`, qui appelle `EmployeService.embaucher` (P3) et renvoie l'id du contrat créé.
- **P3, P4** `GrilleSalarialeService.fourchette(Integer idPoste, LocalDate date)` → `Optional<FourchetteSalariale>` (min, max, catégorie, échelon).
- **P4 → P3** `PdfService.generer(String modele, Map<String, Object> variables)` → `byte[]` (OffrePdfService s'appuie dessus).

---

# P4 – Sprint 3 : notifications et emails

Script : `data/2026-10-22-23-00-p4-notifications.sql` (après le script P4 du sprint 2 ; rejouable).
Dépendance ajoutée : `spring-boot-starter-mail`.

## Notifications dans l'application

Table `notification` : destinataire (`id_utilisateur`), `type`, `message`, `lien` (route du front), `lu`, `date_creation`.

Service commun `NotificationService` (dans la transaction de l'appelant) :

| Méthode | Destinataires |
|---|---|
| `notifier(idUtilisateur, type, message, lien)` | un utilisateur |
| `notifierRole("RH", type, message, lien)` | tous les utilisateurs du rôle |
| `notifierPoste(idPoste, type, message, lien)` | occupants actuels du poste (affectation en cours) + leurs délégués pendant une délégation en cours (« par délégation de … ») |
| `notifierRoleUneFois(role, type, message, lien)` | comme `notifierRole`, sans doublon pour le même type et lien (contrôles quotidiens) |

Moments clés (appel par chaque membre, exemples dans la Javadoc de `NotificationService`) :

| Moment | Membre | Appel conseillé | Type |
|---|---|---|---|
| Une demande attend votre validation | P3 | `notifierPoste(idPosteValideur, …)` | `VALIDATION_EN_ATTENTE` |
| Une nouvelle candidature est arrivée | P5 | `notifierRole("RH", …)` | `CANDIDATURE_RECUE` |
| Un candidat a répondu à une proposition | P4 | **fait** (accepte, refuse, décline) + expiration | `PROPOSITION_REPONSE`, `PROPOSITION_EXPIREE` |
| Un contrat arrive à échéance | P2 | `notifierRoleUneFois("RH", …)` | `CONTRAT_ECHEANCE` |
| Une tâche d'onboarding vous est assignée | P1 | `notifierPoste(idPosteResponsable, …)` | `ONBOARDING_TACHE` |

API de la cloche (utilisateur connecté) :

| Méthode | Route | Réponse |
|---|---|---|
| GET | `/api/notifications?nonLues=false&limite=20` | `[{ id, type, message, lien, lu, dateCreation }]` |
| GET | `/api/notifications/compteur` | `{ nonLues }` |
| POST | `/api/notifications/{id}/lue` | la notification |
| POST | `/api/notifications/tout-lire` | `{ marquees }` |

## Emails aux candidats

| Email | Déclencheur |
|---|---|
| Accusé de réception | dépôt d'une candidature (`HistoCandidatureService.depot`) |
| Proposition avec le lien de réponse | création d'une proposition, puis chaque contre-proposition |
| Résultat : refus | candidature passée à `REFUSE` (présélection ou niveau non admis), ou proposition retirée par la RH |
| Résultat : embauche | proposition acceptée et embauche (P3) réussie |

Un email qui échoue ne fait jamais échouer l'action : le contenu est produit tout de suite (erreurs journalisées), l'envoi part **après la validation** de la transaction (rien n'est envoyé si l'action est annulée), dans un fil à part (`@Async`). Modèles : `templates/emails/*.html`.

Configuration (`application.properties` ; ces valeurs sont aussi les valeurs par défaut) :

```properties
spring.mail.host=localhost
spring.mail.port=1025
app.mail.actif=true
app.mail.expediteur=recrutement@tovogestion.mg
app.mail.nom-expediteur=Service des ressources humaines
app.front.url=http://localhost:5173
```

SMTP de test : `docker run -p 8025:8025 -p 1025:1025 axllent/mailpit`, puis la boîte web sur http://localhost:8025.

## Front Vue (dossier `front-p4/`, à copier dans le dépôt du front)

- `NotificationCloche.vue` : cloche du menu, pastille des non lues (rafraîchie toutes les 30 s), liste, « tout marquer comme lu », ouverture du `lien` avec le routeur.
- `PropositionReponse.vue` : page publique du candidat. Route : `{ path: '/proposition/:jeton', component: PropositionReponse, meta: { public: true } }`.
- Liens des notifications P4 : `/rh/propositions/{id}` (adapter si la route RH diffère).
- Base de l'API : `VITE_API_URL` (vide si le front passe par le proxy Vite ; sinon autoriser `/api/**` dans la configuration CORS avec `allowCredentials(true)`).
