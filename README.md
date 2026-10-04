# League of Locals User API

## Environments and deployments

The API is deployed as the same Helm release (`league-of-locals-user-api`) into
two isolated Kubernetes namespaces:

| Environment | Namespace | Deployment path | Service port |
| --- | --- | --- | --- |
| Development | `league-of-locals-dev` | Every push to `main` | NodePort `32621` |
| Production | `league-of-locals-prod` | Manual workflow dispatch after validation | NodePort `32622` |

The deployment workflow builds an image tagged with the commit SHA. Development is
updated to that SHA automatically. To promote a tested version, run **Build and
Deploy User API** from the GitHub Actions UI, enter that same SHA as `image_tag`,
and approve the `production` environment if GitHub environment protection is
enabled. This means production receives the exact image that was tested rather
than a rebuilt image.

The reusable deployment workflow installs Doppler Kubernetes Operator chart
`1.7.1` as the cluster-wide `doppler-operator` Helm release, then installs the
User API chart in the target namespace. The operator keeps the app's Kubernetes
Secrets in sync with Doppler and restarts the Deployment when they change.
For a local render, supply a Doppler project/config slug:

```sh
helm lint ./helm/league-of-locals-user-api \
  --values ./helm/league-of-locals-user-api/values-dev.yaml \
  --set-string image.tag=example-sha \
  --set-string doppler.project=your-project \
  --set-string doppler.config=dev

helm template league-of-locals-user-api ./helm/league-of-locals-user-api \
  --namespace league-of-locals-dev \
  --values ./helm/league-of-locals-user-api/values-dev.yaml \
  --set-string image.tag=example-sha \
  --set-string doppler.project=your-project \
  --set-string doppler.config=dev
```

### One-time migration from the current raw manifests

The existing deployment was applied without a namespace, so Kubernetes placed
it in `default`. Its service owns NodePort `32621`, which would prevent the new
development service from starting. After configuring Doppler and the GitHub
Environment settings below and before the first `main` deployment, verify and
remove only those legacy resources:

```sh
kubectl -n default get deployment league-of-locals-user-api
kubectl -n default get service league-of-locals-user-api-svc
kubectl -n default delete deployment league-of-locals-user-api
kubectl -n default delete service league-of-locals-user-api-svc
```

If the original release was deployed to a namespace other than `default`, use
that namespace in the commands instead. Do not delete a resource until its name
and namespace match the legacy User API resources above.

### Required setup before the first deployment

Create a Doppler project with separate development and production configs
(or use separate projects). Put these values in **each** config with different
environment-specific values:

- `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. Use distinct databases and
  credentials so test traffic cannot reach production data.
- `AUTH0_DOMAIN`, `AUTH0_CLIENT_ID`, `AUTH0_CLIENT_SECRET`, and
  `AUTH0_ISSUER_URI`. `AUTH0_ISSUER_URI` must match the tenant that issues the
  JWTs accepted by that environment.
- `DOCKER_CONFIG_JSON` if the GHCR image is private. Its value must be a valid
  Docker config JSON object with an `auths.ghcr.io` entry authorized to read the
  package. Doppler syncs it to `ghcr-secret` as a
  `kubernetes.io/dockerconfigjson` Kubernetes Secret. If the image is public,
  set `doppler.registry.enabled=false` in the matching Helm values file; the
  chart will omit the image pull secret reference.

Create a read-only Doppler service token for each config. In the repository's
`dev` and `production` GitHub Environments, set:

| Setting | Type | Purpose |
| --- | --- | --- |
| `DOPPLER_PROJECT` | Variable | Doppler project slug for that environment |
| `DOPPLER_CONFIG` | Variable | Doppler config slug for that environment |
| `DOPPLER_SERVICE_TOKEN` | Secret | Read-only service token scoped to that config |
| `DO_API_TOKEN` | Secret | DigitalOcean token for cluster access; can instead be repository-wide |

The workflow creates only the per-environment `doppler-user-api-token`
Kubernetes Secret to bootstrap the operator. The Helm chart creates two
`DopplerSecret` resources in the application's namespace. The operator then
maintains `league-of-locals-user-api-secrets` for the environment variables used
by the application and `ghcr-secret` for image pulls, checking Doppler for
updates by default every 60 seconds. The registry credential stays separate
because Kubernetes requires it to use the `kubernetes.io/dockerconfigjson`
secret type, and it is not exposed to the app container.

Give the GitHub `production` Environment the desired approval rule. The
reusable workflow owns the environment assignment, so production approval is
requested before it can access the production Doppler token. Neither secret
values nor the Doppler token are passed through Helm values or stored in Helm
release history.

If an earlier chart version created the separate
`league-of-locals-user-api-db-creds` and `auth0-api-creds` Secrets, the operator
does not delete them when their old `DopplerSecret` resources are removed.
Confirm that the combined secret has synchronized and the Deployment is healthy,
then delete those two unused Secrets. To check status without printing values:

```sh
kubectl -n league-of-locals-dev get dopplersecrets
kubectl -n league-of-locals-dev get secrets league-of-locals-user-api-secrets ghcr-secret
kubectl -n league-of-locals-dev delete secrets league-of-locals-user-api-db-creds auth0-api-creds --ignore-not-found
```

The Doppler operator is cluster-wide. If it is already installed outside this
workflow, make sure the existing Helm release name and namespace match
`doppler-operator` in `doppler-operator-system` before enabling this workflow.
When intentionally changing the pinned operator chart version, follow
[Doppler's CRD upgrade procedure](https://docs.doppler.com/docs/doppler-k8s-operator-installation-upgrades)
because Helm does not update CRDs during a normal chart upgrade. Restrict
Kubernetes secret read permissions and enable secret encryption at rest.

The chart keeps NodePort access for compatibility with the current deployment.
NodePorts are cluster-wide, hence the distinct values above. Keep the Cloudflare
Origin Rules pointing the development and production hostnames at their
respective ports, and restrict direct access to the origin where possible.

Before production holds user data, also add automated database migrations (for
example Flyway or Liquibase), backups with a restore test, and logs/metrics/
alerts. The application currently uses `spring.jpa.hibernate.ddl-auto=update`;
that is convenient during development but is not a safe long-term production
schema migration strategy.
