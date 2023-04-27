SHELL := /bin/bash

.PHONY: clean
clean:
	docker compose --profile api down -v

.PHONY: local-api
local-api:
	@./mill cats.api.docker.build
	CATS_API_IMAGE=$$( \
			./mill cats.api.docker.fullImageName \
			| cat out/cats/api/docker/fullImageName.json \
			| jq -r '.value' \
		) \
		docker compose --profile api up

.PHONY: test-local-api
test-local-api:
	@for id in {1,2,"meep"}; do curl -sv localhost:8080/store/$$id 2>&1 | egrep '(^{"|^< HTTP/1.1)'; done

.PHONY: api-db-shell
api-db-shell:
	docker exec -it scala-module-base-postgres-1 \
		/bin/bash -c 'psql -U postgres-user -d kv-store'

