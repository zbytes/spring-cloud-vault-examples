postgres:
	@echo "Enter [infra, unseal, setup, run, simulate, destroy]:"
	@read goal; cd vault-for-postgres-in-spring-boot && ./script.sh --$$goal
rabbitmq:
	@echo "Enter [infra, unseal, setup, run, simulate, destroy]:"
	@read goal; cd vault-for-rabbitmq-in-spring-boot && ./script.sh --$$goal
