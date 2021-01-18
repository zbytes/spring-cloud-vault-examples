postgres:
	@echo "Enter [infra, unseal, setup, simulate, destroy]:"
	@read goal; cd vault-for-postgres-in-spring-boot && ./script.sh --$$goal
